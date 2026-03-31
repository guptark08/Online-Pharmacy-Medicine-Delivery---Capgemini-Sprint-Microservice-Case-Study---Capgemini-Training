package com.pharmacy.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacy.admin.dto.request.PrescriptionReviewDto;
import com.pharmacy.admin.dto.response.PrescriptionResponseDto;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.repository.PrescriptionRepository;

@ExtendWith(MockitoExtension.class)
class AdminPrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private AdminPrescriptionService adminPrescriptionService;

    @Test
    void reviewPrescription_approved_success() {
        Prescription pending = prescriptionWithStatus(1L, PrescriptionStatus.PENDING);
        PrescriptionReviewDto dto = new PrescriptionReviewDto();
        dto.setDecision("APPROVED");

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrescriptionResponseDto response = adminPrescriptionService.reviewPrescription(1L, dto, 100L);

        assertEquals("APPROVED", response.getStatus());
        assertEquals(100L, pending.getReviewedByAdminId());
        assertNotNull(pending.getReviewedAt());
    }

    @Test
    void reviewPrescription_rejected_requiresReason() {
        Prescription pending = prescriptionWithStatus(2L, PrescriptionStatus.PENDING);
        PrescriptionReviewDto dto = new PrescriptionReviewDto();
        dto.setDecision("REJECTED");
        dto.setRejectionReason("Prescription image is unclear");

        when(prescriptionRepository.findById(2L)).thenReturn(Optional.of(pending));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrescriptionResponseDto response = adminPrescriptionService.reviewPrescription(2L, dto, 101L);

        assertEquals("REJECTED", response.getStatus());
        assertEquals("Prescription image is unclear", response.getRejectionReason());
    }

    @Test
    void reviewPrescription_rejected_blankReason_throwsBadRequest() {
        Prescription pending = prescriptionWithStatus(3L, PrescriptionStatus.PENDING);
        PrescriptionReviewDto dto = new PrescriptionReviewDto();
        dto.setDecision("REJECTED");
        dto.setRejectionReason(" ");

        when(prescriptionRepository.findById(3L)).thenReturn(Optional.of(pending));

        assertThrows(BadRequestException.class,
                () -> adminPrescriptionService.reviewPrescription(3L, dto, 101L));
    }

    @Test
    void reviewPrescription_alreadyApproved_throwsBadRequest() {
        Prescription approved = prescriptionWithStatus(4L, PrescriptionStatus.APPROVED);
        PrescriptionReviewDto dto = new PrescriptionReviewDto();
        dto.setDecision("APPROVED");

        when(prescriptionRepository.findById(4L)).thenReturn(Optional.of(approved));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> adminPrescriptionService.reviewPrescription(4L, dto, 101L));

        assertTrue(ex.getMessage().contains("already reviewed"));
    }

    @Test
    void reviewPrescription_invalidDecision_throwsBadRequest() {
        Prescription pending = prescriptionWithStatus(5L, PrescriptionStatus.PENDING);
        PrescriptionReviewDto dto = new PrescriptionReviewDto();
        dto.setDecision("MAYBE");

        when(prescriptionRepository.findById(5L)).thenReturn(Optional.of(pending));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> adminPrescriptionService.reviewPrescription(5L, dto, 101L));

        assertTrue(ex.getMessage().contains("APPROVED") || ex.getMessage().contains("REJECTED"));
    }

    @Test
    void getPendingPrescriptions_returnsOnlyPendingStatus() {
        Prescription first = prescriptionWithStatus(6L, PrescriptionStatus.PENDING);
        Prescription second = prescriptionWithStatus(7L, PrescriptionStatus.PENDING);

        when(prescriptionRepository.findByStatusOrderByUploadedAtAsc(PrescriptionStatus.PENDING))
                .thenReturn(List.of(first, second));

        List<PrescriptionResponseDto> responses = adminPrescriptionService.getPendingPrescriptions();

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(response -> "PENDING".equals(response.getStatus())));
    }

    private Prescription prescriptionWithStatus(Long id, PrescriptionStatus status) {
        return Prescription.builder()
                .id(id)
                .userId(10L)
                .userEmail("alice@example.com")
                .fileUrl("/files/prescription.pdf")
                .fileType("PDF")
                .status(status)
                .build();
    }
}
