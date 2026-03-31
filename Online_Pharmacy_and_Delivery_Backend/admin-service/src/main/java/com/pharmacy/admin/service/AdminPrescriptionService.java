package com.pharmacy.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.request.PrescriptionReviewDto;
import com.pharmacy.admin.dto.response.PrescriptionResponseDto;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(AdminPrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;

    public List<PrescriptionResponseDto> getPendingPrescriptions() {
        return prescriptionRepository.findByStatusOrderByUploadedAtAsc(PrescriptionStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<PrescriptionResponseDto> getAllPrescriptions(int page, int size) {
        return prescriptionRepository.findAllByOrderByUploadedAtDesc(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public PrescriptionResponseDto getPrescriptionById(Long id) {
        return mapToDto(findOrThrow(id));
    }

    @Transactional
    public PrescriptionResponseDto reviewPrescription(Long id, PrescriptionReviewDto dto, Long adminId) {
        Prescription prescription = findOrThrow(id);

        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new BadRequestException("Prescription is already reviewed. Current status: " + prescription.getStatus());
        }

        String decision = normalizeRequired(dto.getDecision(), "Decision is required").toUpperCase(Locale.ROOT);

        if ("APPROVED".equals(decision)) {
            prescription.setStatus(PrescriptionStatus.APPROVED);
            prescription.setRejectionReason(null);
        } else if ("REJECTED".equals(decision)) {
            String rejectionReason = normalizeRequired(dto.getRejectionReason(), "Rejection reason is required");
            prescription.setStatus(PrescriptionStatus.REJECTED);
            prescription.setRejectionReason(rejectionReason);
        } else {
            throw new BadRequestException("Decision must be APPROVED or REJECTED");
        }

        prescription.setReviewedByAdminId(adminId);
        prescription.setReviewedAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Prescription id={} reviewed by admin={} with decision={}", id, adminId, decision);
        return mapToDto(saved);
    }

    private Prescription findOrThrow(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    public PrescriptionResponseDto mapToDto(Prescription prescription) {
        return PrescriptionResponseDto.builder()
                .id(prescription.getId())
                .userId(prescription.getUserId())
                .userEmail(prescription.getUserEmail())
                .fileUrl(prescription.getFileUrl())
                .fileType(prescription.getFileType())
                .status(prescription.getStatus() == null ? null : prescription.getStatus().name())
                .doctorName(prescription.getDoctorName())
                .doctorRegNumber(prescription.getDoctorRegNumber())
                .reviewedByAdminId(prescription.getReviewedByAdminId())
                .rejectionReason(prescription.getRejectionReason())
                .orderId(prescription.getOrderId())
                .uploadedAt(prescription.getUploadedAt() == null ? null : prescription.getUploadedAt().toString())
                .reviewedAt(prescription.getReviewedAt() == null ? null : prescription.getReviewedAt().toString())
                .build();
    }
}
