package org.sprint.catalogandprescription_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.sprint.catalogandprescription_service.dto.PrescriptionResponseDTO;
import org.sprint.catalogandprescription_service.entities.Prescription;
import org.sprint.catalogandprescription_service.globalexception.ResourceNotFoundException;
import org.sprint.catalogandprescription_service.messaging.DomainEventPublisher;
import org.sprint.catalogandprescription_service.messaging.PharmacyEventRoutingKeys;
import org.sprint.catalogandprescription_service.messaging.events.PrescriptionReviewedEvent;
import org.sprint.catalogandprescription_service.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Autowired(required = false)
    private DomainEventPublisher domainEventPublisher;

    @Value("${app.upload.dir:uploads/prescriptions}")
    private String uploadDir;

    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Map<String, String> FILE_TYPE_BY_EXTENSION = Map.of(
            "pdf", "PDF",
            "jpg", "JPEG",
            "jpeg", "JPEG",
            "png", "PNG");

    public PrescriptionResponseDTO uploadPrescription(MultipartFile file, Long customerId) throws IOException {
        validateFile(file);

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String uniqueFileName = UUID.randomUUID() + "." + extension;
        Path filePath = uploadPath.resolve(uniqueFileName).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        Prescription prescription = Prescription.builder()
                .customerId(customerId)
                .fileName(originalFileName)
                .filePath(filePath.toString())
                .fileType(FILE_TYPE_BY_EXTENSION.get(extension))
                .fileSize(file.getSize())
                .status(Prescription.PrescriptionStatus.PENDING)
                .build();

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Prescription uploaded: customerId={}, prescriptionId={}", customerId, saved.getId());

        return convertToDTO(saved);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        String contentType = normalizeContentType(file.getContentType());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: pdf, jpg, jpeg, png");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, JPG, and PNG are allowed");
        }

        if (!isExtensionMatchingContentType(extension, contentType)) {
            throw new IllegalArgumentException("File extension does not match content type");
        }
    }

    public PrescriptionResponseDTO reviewPrescription(Long prescriptionId, String status, String notes, Long adminId) {

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));

        if (prescription.getStatus() != Prescription.PrescriptionStatus.PENDING) {
            throw new IllegalStateException("Prescription is not in PENDING status");
        }

        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);

        Prescription.PrescriptionStatus newStatus;
        if ("APPROVED".equals(normalizedStatus)) {
            newStatus = Prescription.PrescriptionStatus.APPROVED;
        } else if ("REJECTED".equals(normalizedStatus)) {
            newStatus = Prescription.PrescriptionStatus.REJECTED;
            if (notes == null || notes.isBlank()) {
                throw new IllegalArgumentException("Review notes are required when rejecting a prescription");
            }
        } else {
            throw new IllegalArgumentException("Invalid status. Use APPROVED or REJECTED");
        }

        Prescription.PrescriptionStatus previousStatus = prescription.getStatus();

        prescription.setStatus(newStatus);
        prescription.setReviewNotes(notes);
        prescription.setReviewedBy(adminId);
        prescription.setReviewedAt(java.time.LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(prescription);
        publishPrescriptionReviewedEvent(saved, previousStatus);

        return convertToDTO(saved);
    }


    public void linkPrescriptionToOrder(Long prescriptionId, Long customerId, Long orderId) {
        if (prescriptionId == null || customerId == null || orderId == null) {
            throw new IllegalArgumentException("prescriptionId, customerId and orderId are required");
        }

        Prescription prescription = prescriptionRepository.findByIdAndCustomerId(prescriptionId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found for current user"));

        if (prescription.getStatus() == Prescription.PrescriptionStatus.REJECTED
                || prescription.getStatus() == Prescription.PrescriptionStatus.EXPIRED) {
            throw new IllegalStateException("Cannot link rejected or expired prescription to order");
        }

        if (prescription.getOrderId() != null && !prescription.getOrderId().equals(orderId)) {
            throw new IllegalStateException("Prescription is already linked to a different order");
        }

        prescription.setOrderId(orderId);
        prescriptionRepository.save(prescription);
    }
    @Transactional(readOnly = true)
    public String getPrescriptionStatusForCustomer(Long prescriptionId, Long customerId) {
        Prescription prescription = prescriptionRepository.findByIdAndCustomerId(prescriptionId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found for current user"));
        return prescription.getStatus().name();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getByCustomer(Long customerId) {
        return prescriptionRepository.findByCustomerIdOrderByUploadedAtDesc(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getPendingPrescriptions() {
        return prescriptionRepository.findByStatusOrderByUploadedAtAsc(Prescription.PrescriptionStatus.PENDING).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void publishPrescriptionReviewedEvent(
            Prescription prescription,
            Prescription.PrescriptionStatus previousStatus) {

        if (domainEventPublisher == null) {
            return;
        }

        PrescriptionReviewedEvent event = PrescriptionReviewedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now().toString())
                .prescriptionId(prescription.getId())
                .customerId(prescription.getCustomerId())
                .previousStatus(previousStatus.name())
                .newStatus(prescription.getStatus().name())
                .reviewedBy(prescription.getReviewedBy())
                .reviewedAt(prescription.getReviewedAt() == null ? null : prescription.getReviewedAt().toString())
                .reviewNotes(prescription.getReviewNotes())
                .build();

        domainEventPublisher.publishAfterCommit(PharmacyEventRoutingKeys.PRESCRIPTION_REVIEWED, event);
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }
        return Paths.get(originalFileName).getFileName().toString().trim();
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index <= 0 || index == fileName.length() - 1) {
            throw new IllegalArgumentException("File extension is required");
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        return "image/jpg".equals(normalized) ? "image/jpeg" : normalized;
    }

    private boolean isExtensionMatchingContentType(String extension, String contentType) {
        return switch (extension) {
        case "pdf" -> "application/pdf".equals(contentType);
        case "jpg", "jpeg" -> "image/jpeg".equals(contentType);
        case "png" -> "image/png".equals(contentType);
        default -> false;
        };
    }

    private PrescriptionResponseDTO convertToDTO(Prescription p) {
        return PrescriptionResponseDTO.builder()
                .id(p.getId())
                .customerId(p.getCustomerId())
                .fileName(p.getFileName())
                .fileType(p.getFileType())
                .fileSize(p.getFileSize())
                .status(p.getStatus().name())
                .reviewNotes(p.getReviewNotes())
                .uploadedAt(p.getUploadedAt())
                .reviewedAt(p.getReviewedAt())
                .build();
    }
}

