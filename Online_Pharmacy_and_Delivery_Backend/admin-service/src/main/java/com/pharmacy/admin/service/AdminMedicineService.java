package com.pharmacy.admin.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.request.MedicineRequestDto;
import com.pharmacy.admin.dto.response.MedicineResponseDto;
import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.exception.DuplicateResourceException;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.repository.CategoryRepository;
import com.pharmacy.admin.repository.MedicineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMedicineService {

    private static final Logger log = LoggerFactory.getLogger(AdminMedicineService.class);
    private static final int LOW_STOCK_THRESHOLD = 10;

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public MedicineResponseDto addMedicine(MedicineRequestDto dto) {
        validateSkuUniqueness(dto.getSku(), null);

        Category category = findCategoryOrThrow(dto.getCategoryId());
        LocalDate expiryDate = parseAndValidateExpiryDate(dto.getExpiryDate());

        Medicine medicine = Medicine.builder()
                .name(normalizeRequired(dto.getName(), "Medicine name is required"))
                .genericName(normalizeOptional(dto.getGenericName()))
                .manufacturer(normalizeOptional(dto.getManufacturer()))
                .dosageForm(normalizeOptional(dto.getDosageForm()))
                .strength(normalizeOptional(dto.getStrength()))
                .price(dto.getPrice())
                .mrp(dto.getMrp() != null ? dto.getMrp() : dto.getPrice())
                .stock(dto.getStock())
                .requiresPrescription(dto.isRequiresPrescription())
                .category(category)
                .imageUrl(normalizeOptional(dto.getImageUrl()))
                .description(normalizeOptional(dto.getDescription()))
                .sku(normalizeOptional(dto.getSku()))
                .expiryDate(expiryDate)
                .isActive(true)
                .build();

        Medicine saved = medicineRepository.save(medicine);
        log.info("Added medicine id={} name={}", saved.getId(), saved.getName());
        return mapToDto(saved);
    }

    public List<MedicineResponseDto> getAllMedicines() {
        return medicineRepository.findAll(Sort.by("name")).stream().map(this::mapToDto).toList();
    }

    public List<MedicineResponseDto> getActiveMedicines() {
        return medicineRepository.findByIsActiveTrueOrderByNameAsc().stream().map(this::mapToDto).toList();
    }

    public MedicineResponseDto getMedicineById(Long id) {
        return mapToDto(findMedicineOrThrow(id));
    }

    public List<MedicineResponseDto> searchMedicines(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Search query cannot be empty");
        }

        Page<Medicine> result = medicineRepository.searchMedicines(
                query.trim(),
                PageRequest.of(page, size, Sort.by("name")));

        return result.getContent().stream().map(this::mapToDto).toList();
    }

    public List<MedicineResponseDto> getMedicinesByCategory(Long categoryId) {
        findCategoryOrThrow(categoryId);
        return medicineRepository.findByCategoryIdAndIsActiveTrue(categoryId).stream().map(this::mapToDto).toList();
    }

    @Transactional
    public MedicineResponseDto updateMedicine(Long id, MedicineRequestDto dto) {
        Medicine medicine = findMedicineOrThrow(id);

        validateSkuUniqueness(dto.getSku(), medicine.getSku());

        Category category = findCategoryOrThrow(dto.getCategoryId());
        LocalDate expiryDate = parseAndValidateExpiryDate(dto.getExpiryDate());

        medicine.setName(normalizeRequired(dto.getName(), "Medicine name is required"));
        medicine.setGenericName(normalizeOptional(dto.getGenericName()));
        medicine.setManufacturer(normalizeOptional(dto.getManufacturer()));
        medicine.setDosageForm(normalizeOptional(dto.getDosageForm()));
        medicine.setStrength(normalizeOptional(dto.getStrength()));
        medicine.setPrice(dto.getPrice());
        medicine.setMrp(dto.getMrp() != null ? dto.getMrp() : dto.getPrice());
        medicine.setStock(dto.getStock());
        medicine.setRequiresPrescription(dto.isRequiresPrescription());
        medicine.setCategory(category);
        medicine.setImageUrl(normalizeOptional(dto.getImageUrl()));
        medicine.setDescription(normalizeOptional(dto.getDescription()));
        medicine.setSku(normalizeOptional(dto.getSku()));
        medicine.setExpiryDate(expiryDate);

        Medicine saved = medicineRepository.save(medicine);
        log.info("Updated medicine id={}", id);
        return mapToDto(saved);
    }

    @Transactional
    public MedicineResponseDto updateStock(Long id, int newStock) {
        if (newStock < 0) {
            throw new BadRequestException("Stock cannot be negative");
        }

        Medicine medicine = findMedicineOrThrow(id);
        medicine.setStock(newStock);

        Medicine saved = medicineRepository.save(medicine);
        log.info("Updated stock for medicine id={} to {}", id, newStock);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = findMedicineOrThrow(id);
        medicine.setActive(false);
        medicineRepository.save(medicine);
        log.info("Soft deleted medicine id={}", id);
    }

    public List<MedicineResponseDto> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines(LOW_STOCK_THRESHOLD).stream().map(this::mapToDto).toList();
    }

    public List<MedicineResponseDto> getExpiringMedicines() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAhead = today.plusDays(30);

        return medicineRepository.findMedicinesExpiringBetween(today, thirtyDaysAhead)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<MedicineResponseDto> getExpiredMedicines() {
        return medicineRepository.findExpiringMedicines(LocalDate.now()).stream().map(this::mapToDto).toList();
    }

    private void validateSkuUniqueness(String incomingSku, String currentSku) {
        if (incomingSku == null || incomingSku.isBlank()) {
            return;
        }

        String normalizedSku = incomingSku.trim();
        boolean skuChanged = currentSku == null || !normalizedSku.equals(currentSku);

        if (skuChanged && medicineRepository.existsBySku(normalizedSku)) {
            throw new DuplicateResourceException("Medicine with SKU '" + normalizedSku + "' already exists");
        }
    }

    private Medicine findMedicineOrThrow(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", id));
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private LocalDate parseAndValidateExpiryDate(String dateStr) {
        LocalDate parsedDate;

        try {
            parsedDate = LocalDate.parse(dateStr);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid expiry date format. Use yyyy-MM-dd");
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Expiry date cannot be in the past");
        }

        return parsedDate;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }

    public MedicineResponseDto mapToDto(Medicine medicine) {
        return MedicineResponseDto.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .genericName(medicine.getGenericName())
                .manufacturer(medicine.getManufacturer())
                .dosageForm(medicine.getDosageForm())
                .strength(medicine.getStrength())
                .price(medicine.getPrice())
                .mrp(medicine.getMrp())
                .stock(medicine.getStock())
                .requiresPrescription(medicine.isRequiresPrescription())
                .isActive(medicine.isActive())
                .expiryDate(medicine.getExpiryDate() == null ? null : medicine.getExpiryDate().toString())
                .categoryId(medicine.getCategory() == null ? null : medicine.getCategory().getId())
                .categoryName(medicine.getCategory() == null ? null : medicine.getCategory().getName())
                .imageUrl(medicine.getImageUrl())
                .description(medicine.getDescription())
                .sku(medicine.getSku())
                .createdAt(medicine.getCreatedAt() == null ? null : medicine.getCreatedAt().toString())
                .updatedAt(medicine.getUpdatedAt() == null ? null : medicine.getUpdatedAt().toString())
                .build();
    }
}
