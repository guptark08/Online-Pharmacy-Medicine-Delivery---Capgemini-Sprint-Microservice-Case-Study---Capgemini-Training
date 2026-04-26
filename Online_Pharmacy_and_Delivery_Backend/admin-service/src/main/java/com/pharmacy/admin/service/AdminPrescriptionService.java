package com.pharmacy.admin.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.request.PrescriptionReviewDto;
import com.pharmacy.admin.dto.response.PrescriptionResponseDto;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.integration.CrossServiceAnalyticsClient;
import com.pharmacy.admin.integration.RemotePrescriptionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(AdminPrescriptionService.class);

    private final CrossServiceAnalyticsClient crossServiceClient;

    public List<PrescriptionResponseDto> getPendingPrescriptions() {
        Optional<List<RemotePrescriptionResponse>> remoteRx = crossServiceClient.fetchPendingPrescriptions();
        if (remoteRx.isPresent()) {
            return remoteRx.get().stream()
                    .map(this::mapRemoteToDto)
                    .toList();
        }
        log.warn("Falling back to local prescriptions due to remote service unavailable");
        return List.of();
    }

    public List<PrescriptionResponseDto> getAllPrescriptions(int page, int size) {
        Optional<List<RemotePrescriptionResponse>> remoteRx = crossServiceClient.fetchAllPrescriptions(page, size);
        if (remoteRx.isPresent()) {
            return remoteRx.get().stream()
                    .map(this::mapRemoteToDto)
                    .toList();
        }
        return List.of();
    }

    public PrescriptionResponseDto getPrescriptionById(Long id) {
        Optional<List<RemotePrescriptionResponse>> allRx = crossServiceClient.fetchAllPrescriptions(0, 1000);
        if (allRx.isPresent()) {
            return allRx.get().stream()
                    .filter(rx -> id.equals(rx.getId()))
                    .map(this::mapRemoteToDto)
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Prescription not found: " + id));
        }
        throw new BadRequestException("Prescription not found: " + id);
    }

    @Transactional
    public PrescriptionResponseDto reviewPrescription(Long id, PrescriptionReviewDto dto, Long adminId) {
        String decision = dto.getDecision();
        String notes = dto.getRejectionReason();

        Optional<RemotePrescriptionResponse> updated = crossServiceClient.reviewPrescription(id, decision, notes);
        if (updated.isPresent()) {
            log.info("Prescription id={} reviewed with decision={}", id, decision);
            return mapRemoteToDto(updated.get());
        }

        log.error("Failed to review prescription via remote service, id={}", id);
        throw new BadRequestException("Failed to review prescription. Remote service may be unavailable.");
    }

    private PrescriptionResponseDto mapRemoteToDto(RemotePrescriptionResponse remote) {
        return PrescriptionResponseDto.builder()
                .id(remote.getId())
                .userId(remote.getUserId())
                .userEmail(remote.getUserEmail())
                .fileUrl(remote.getFileUrl())
                .fileType(remote.getFileType())
                .status(remote.getStatus())
                .doctorName(remote.getDoctorName())
                .doctorRegNumber(remote.getDoctorRegNumber())
                .reviewedByAdminId(remote.getReviewedByAdminId())
                .rejectionReason(remote.getRejectionReason())
                .orderId(remote.getOrderId())
                .uploadedAt(remote.getUploadedAt() == null ? null : remote.getUploadedAt().toString())
                .reviewedAt(remote.getReviewedAt() == null ? null : remote.getReviewedAt().toString())
                .build();
    }
}