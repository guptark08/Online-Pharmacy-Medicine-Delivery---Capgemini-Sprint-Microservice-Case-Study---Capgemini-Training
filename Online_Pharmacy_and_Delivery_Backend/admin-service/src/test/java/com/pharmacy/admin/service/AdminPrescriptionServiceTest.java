package com.pharmacy.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacy.admin.dto.request.PrescriptionReviewDto;
import com.pharmacy.admin.dto.response.PrescriptionResponseDto;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.integration.CrossServiceAnalyticsClient;
import com.pharmacy.admin.integration.RemotePrescriptionResponse;

@ExtendWith(MockitoExtension.class)
class AdminPrescriptionServiceTest {

    @Mock
    private CrossServiceAnalyticsClient crossServiceClient;

    @InjectMocks
    private AdminPrescriptionService adminPrescriptionService;

    @Test
    void getPendingPrescriptions_mapsCatalogResponse() {
        RemotePrescriptionResponse remote = remotePrescription(6L, "PENDING");
        remote.setCustomerId(10L);
        remote.setReviewNotes("Needs clearer dosage");

        when(crossServiceClient.fetchPendingPrescriptions()).thenReturn(Optional.of(List.of(remote)));

        List<PrescriptionResponseDto> responses = adminPrescriptionService.getPendingPrescriptions();

        assertEquals(1, responses.size());
        assertEquals("PENDING", responses.get(0).getStatus());
        assertEquals(10L, responses.get(0).getUserId());
        assertEquals("Needs clearer dosage", responses.get(0).getRejectionReason());
    }

    @Test
    void getPendingPrescriptions_returnsEmptyWhenCatalogUnavailable() {
        when(crossServiceClient.fetchPendingPrescriptions()).thenReturn(Optional.empty());

        assertEquals(List.of(), adminPrescriptionService.getPendingPrescriptions());
    }

    @Test
    void getPrescriptionById_returnsCatalogPrescription() {
        RemotePrescriptionResponse remote = remotePrescription(1L, "APPROVED");
        when(crossServiceClient.fetchPrescriptionById(1L)).thenReturn(Optional.of(remote));

        PrescriptionResponseDto response = adminPrescriptionService.getPrescriptionById(1L);

        assertEquals(1L, response.getId());
        assertEquals("APPROVED", response.getStatus());
    }

    @Test
    void getPrescriptionById_missingRemoteData_throwsBadRequest() {
        when(crossServiceClient.fetchPrescriptionById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> adminPrescriptionService.getPrescriptionById(1L));
    }

    @Test
    void reviewPrescription_success_mapsCatalogResponse() {
        PrescriptionReviewDto dto = new PrescriptionReviewDto();
        dto.setDecision("APPROVED");

        RemotePrescriptionResponse remote = remotePrescription(1L, "APPROVED");
        when(crossServiceClient.reviewPrescription(1L, "APPROVED", null)).thenReturn(Optional.of(remote));

        PrescriptionResponseDto response = adminPrescriptionService.reviewPrescription(1L, dto, 100L);

        assertEquals("APPROVED", response.getStatus());
        assertEquals(1L, response.getId());
    }

    @Test
    void reviewPrescription_remoteUnavailable_throwsBadRequest() {
        PrescriptionReviewDto dto = new PrescriptionReviewDto();
        dto.setDecision("REJECTED");
        dto.setRejectionReason("Prescription image is unclear");

        when(crossServiceClient.reviewPrescription(2L, "REJECTED", "Prescription image is unclear"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> adminPrescriptionService.reviewPrescription(2L, dto, 101L));
    }

    private RemotePrescriptionResponse remotePrescription(Long id, String status) {
        RemotePrescriptionResponse response = new RemotePrescriptionResponse();
        response.setId(id);
        response.setUserId(10L);
        response.setUserEmail("alice@example.com");
        response.setFileName("prescription.pdf");
        response.setFileUrl("/api/catalog/prescriptions/" + id + "/file");
        response.setFileType("PDF");
        response.setStatus(status);
        response.setUploadedAt(LocalDateTime.parse("2026-03-08T09:15:00"));
        return response;
    }
}
