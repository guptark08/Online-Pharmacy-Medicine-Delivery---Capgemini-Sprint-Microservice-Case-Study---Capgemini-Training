package org.sprint.catalogandprescription_service.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.sprint.catalogandprescription_service.dto.MedicineDTO;
import org.sprint.catalogandprescription_service.entities.Category;
import org.sprint.catalogandprescription_service.entities.Medicine;
import org.sprint.catalogandprescription_service.globalexception.ResourceNotFoundException;
import org.sprint.catalogandprescription_service.messaging.DomainEventPublisher;
import org.sprint.catalogandprescription_service.messaging.PharmacyEventRoutingKeys;
import org.sprint.catalogandprescription_service.messaging.events.InventoryAdjustedEvent;
import org.sprint.catalogandprescription_service.repository.CategoryRepository;
import org.sprint.catalogandprescription_service.repository.MedicineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicineService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "price",
            "stock",
            "createdAt",
            "updatedAt",
            "expiryDate");

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;

    @Autowired(required = false)
    private DomainEventPublisher domainEventPublisher;

    @Value("${app.upload.medicine-images.dir:uploads/medicine-images}")
    private String medicineImageUploadDir;

    @Transactional(readOnly = true)
    public Page<MedicineDTO> getAllMedicines(String keyword, Long categoryId, Boolean requiresPrescription, int page, int size, String sortBy) {
        Pageable pageable = buildPageable(page, size, sortBy);

        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        boolean hasKeyword = !normalizedKeyword.isEmpty();

        Page<Medicine> medicines;
        if (hasKeyword && categoryId != null && requiresPrescription != null) {
            medicines = medicineRepository.findByNameContainingIgnoreCaseAndCategoryIdAndRequiresPrescriptionAndIsActiveTrue(
                    normalizedKeyword,
                    categoryId,
                    requiresPrescription,
                    pageable);
        } else if (hasKeyword && categoryId != null) {
            medicines = medicineRepository.findByNameContainingIgnoreCaseAndCategoryIdAndIsActiveTrue(
                    normalizedKeyword,
                    categoryId,
                    pageable);
        } else if (hasKeyword && requiresPrescription != null) {
            medicines = medicineRepository.findByNameContainingIgnoreCaseAndRequiresPrescriptionAndIsActiveTrue(
                    normalizedKeyword,
                    requiresPrescription,
                    pageable);
        } else if (hasKeyword) {
            medicines = medicineRepository.searchMedicines(normalizedKeyword, pageable);
        } else if (categoryId != null && requiresPrescription != null) {
            medicines = medicineRepository.findByCategoryIdAndRequiresPrescriptionAndIsActiveTrue(
                    categoryId,
                    requiresPrescription,
                    pageable);
        } else if (categoryId != null) {
            medicines = medicineRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        } else if (requiresPrescription != null) {
            medicines = medicineRepository.findByRequiresPrescriptionAndIsActiveTrue(requiresPrescription, pageable);
        } else {
            medicines = medicineRepository.findByIsActiveTrue(pageable);
        }

        return medicines.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public MedicineDTO getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        return convertToDTO(medicine);
    }

    public MedicineDTO createMedicine(MedicineDTO dto) {
        String normalizedName = dto.getName().trim();

        if (medicineRepository.existsByNameIgnoreCaseAndIsActiveTrue(normalizedName)) {
            throw new IllegalArgumentException("Medicine with name '" + normalizedName + "' already exists");
        }

        validatePriceRules(dto.getPrice(), dto.getDiscountedPrice());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));

        Medicine medicine = Medicine.builder()
                .name(normalizedName)
                .manufacturer(dto.getManufacturer().trim())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .discountedPrice(dto.getDiscountedPrice())
                .stock(dto.getStock())
                .requiresPrescription(Boolean.TRUE.equals(dto.getRequiresPrescription()))
                .dosageInfo(dto.getDosageInfo())
                .sideEffects(dto.getSideEffects())
                .imageUrl(dto.getImageUrl())
                .expiryDate(dto.getExpiryDate())
                .status(resolveStatus(dto.getStock(), null))
                .isActive(true)
                .category(category)
                .build();

        Medicine saved = medicineRepository.save(medicine);
        publishInventoryAdjustedEvent(saved, null, "CREATED", "New medicine created");

        log.info("Medicine created: {} (id: {})", saved.getName(), saved.getId());
        return convertToDTO(saved);
    }

    public MedicineDTO updateMedicine(Long id, MedicineDTO dto) {
        Medicine existing = medicineRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + id));

        String normalizedName = dto.getName().trim();
        if (medicineRepository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot(normalizedName, id)) {
            throw new IllegalArgumentException("Medicine with name '" + normalizedName + "' already exists");
        }

        validatePriceRules(dto.getPrice(), dto.getDiscountedPrice());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));

        Integer previousStock = existing.getStock();

        existing.setName(normalizedName);
        existing.setManufacturer(dto.getManufacturer().trim());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setDiscountedPrice(dto.getDiscountedPrice());
        existing.setStock(dto.getStock());
        existing.setRequiresPrescription(Boolean.TRUE.equals(dto.getRequiresPrescription()));
        existing.setDosageInfo(dto.getDosageInfo());
        existing.setSideEffects(dto.getSideEffects());
        existing.setImageUrl(dto.getImageUrl());
        existing.setExpiryDate(dto.getExpiryDate());
        existing.setCategory(category);
        existing.setStatus(resolveStatus(dto.getStock(), dto.getStatus()));

        Medicine saved = medicineRepository.save(existing);

        String adjustmentType = Objects.equals(previousStock, saved.getStock()) ? "UPDATED" : "STOCK_UPDATED";
        String reason = Objects.equals(previousStock, saved.getStock())
                ? "Medicine details updated"
                : "Stock updated via medicine update";

        publishInventoryAdjustedEvent(saved, previousStock, adjustmentType, reason);

        return convertToDTO(saved);
    }

    public MedicineDTO uploadMedicineImage(Long id, MultipartFile file) throws IOException {
        Medicine medicine = medicineRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + id));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Image size exceeds 5MB limit");
        }

        String originalFileName = file.getOriginalFilename() == null ? "" : Paths.get(file.getOriginalFilename()).getFileName().toString().trim();
        int dotIdx = originalFileName.lastIndexOf('.');
        if (dotIdx <= 0 || dotIdx == originalFileName.length() - 1) {
            throw new IllegalArgumentException("Image file must have a valid extension");
        }
        String extension = originalFileName.substring(dotIdx + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid image type. Allowed: jpg, jpeg, png, webp");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid image content type");
        }

        Path uploadPath = Paths.get(medicineImageUploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String uniqueFileName = UUID.randomUUID() + "." + extension;
        Path filePath = uploadPath.resolve(uniqueFileName).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        medicine.setImageUrl("/api/catalog/medicines/" + id + "/image-file?file=" + uniqueFileName);
        Medicine saved = medicineRepository.save(medicine);
        log.info("Medicine image uploaded for id={}: {}", id, uniqueFileName);
        return convertToDTO(saved);
    }

    public Resource getMedicineImageFile(Long id, String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..") || fileName.contains("/")) {
            throw new IllegalArgumentException("Invalid file name");
        }
        try {
            Path uploadPath = Paths.get(medicineImageUploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Invalid file path");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (Exception e) {
            log.warn("Cannot read medicine image file: {}", fileName);
        }
        throw new ResourceNotFoundException("Medicine image file not found");
    }

    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + id));

        Integer previousStock = medicine.getStock();
        medicine.setIsActive(false);

        Medicine saved = medicineRepository.save(medicine);
        publishInventoryAdjustedEvent(saved, previousStock, "DEACTIVATED", "Medicine deactivated by admin");

        log.info("Medicine soft-deleted: {}", id);
    }

    private void publishInventoryAdjustedEvent(
            Medicine medicine,
            Integer previousStock,
            String adjustmentType,
            String reason) {

        if (domainEventPublisher == null) {
            return;
        }

        InventoryAdjustedEvent event = InventoryAdjustedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now().toString())
                .medicineId(medicine.getId())
                .medicineName(medicine.getName())
                .categoryId(medicine.getCategory() == null ? null : medicine.getCategory().getId())
                .previousStock(previousStock)
                .currentStock(medicine.getStock())
                .adjustmentType(adjustmentType)
                .reason(reason)
                .active(medicine.getIsActive())
                .build();

        domainEventPublisher.publishAfterCommit(PharmacyEventRoutingKeys.INVENTORY_ADJUSTED, event);
    }

    private Pageable buildPageable(int page, int size, String sortBy) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "name" : sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(safeSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy field: " + safeSortBy);
        }

        return PageRequest.of(page, size, Sort.by(safeSortBy).ascending());
    }

    private void validatePriceRules(BigDecimal price, BigDecimal discountedPrice) {
        if (discountedPrice != null && discountedPrice.compareTo(price) > 0) {
            throw new IllegalArgumentException("Discounted price cannot be greater than price");
        }
    }

    private Medicine.MedicineStatus resolveStatus(Integer stock, String requestedStatus) {
        if (requestedStatus != null && !requestedStatus.isBlank()) {
            try {
                Medicine.MedicineStatus parsed = Medicine.MedicineStatus
                        .valueOf(requestedStatus.trim().toUpperCase());
                if (parsed == Medicine.MedicineStatus.DISCONTINUED) {
                    return parsed;
                }
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Invalid medicine status. Allowed values: AVAILABLE, OUT_OF_STOCK, DISCONTINUED");
            }
        }

        return stock != null && stock == 0
                ? Medicine.MedicineStatus.OUT_OF_STOCK
                : Medicine.MedicineStatus.AVAILABLE;
    }

    private MedicineDTO convertToDTO(Medicine medicine) {
        return MedicineDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .manufacturer(medicine.getManufacturer())
                .description(medicine.getDescription())
                .price(medicine.getPrice())
                .discountedPrice(medicine.getDiscountedPrice())
                .stock(medicine.getStock())
                .requiresPrescription(medicine.getRequiresPrescription())
                .dosageInfo(medicine.getDosageInfo())
                .sideEffects(medicine.getSideEffects())
                .imageUrl(medicine.getImageUrl())
                .expiryDate(medicine.getExpiryDate())
                .status(medicine.getStatus().name())
                .categoryId(medicine.getCategory().getId())
                .categoryName(medicine.getCategory().getName())
                .build();
    }
}

