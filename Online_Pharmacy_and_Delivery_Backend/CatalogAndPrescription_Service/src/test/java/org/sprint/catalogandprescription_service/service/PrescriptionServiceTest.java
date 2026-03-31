package org.sprint.catalogandprescription_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.sprint.catalogandprescription_service.dto.PrescriptionResponseDTO;
import org.sprint.catalogandprescription_service.entities.Prescription;
import org.sprint.catalogandprescription_service.repository.PrescriptionRepository;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(prescriptionService, "uploadDir", "target/test-uploads");
    }

    @Test
    void uploadPrescription_validPdfFile_savesAndReturnsDTO() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prescription.pdf",
                "application/pdf",
                "pdf-content".getBytes(StandardCharsets.UTF_8));

        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription toSave = invocation.getArgument(0);
            toSave.setId(1L);
            return toSave;
        });

        PrescriptionResponseDTO response = prescriptionService.uploadPrescription(file, 10L);

        assertEquals("PENDING", response.getStatus());
        assertEquals("prescription.pdf", response.getFileName());
        assertNotNull(response.getId());
    }

    @Test
    void uploadPrescription_emptyFile_throwsIllegalArgument() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "prescription.pdf",
                "application/pdf",
                new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.uploadPrescription(emptyFile, 10L));

        assertTrue(ex.getMessage().toLowerCase().contains("empty"));
    }

    @Test
    void uploadPrescription_wrongContentType_throwsIllegalArgument() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prescription.pdf",
                "text/plain",
                "plain-text".getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.uploadPrescription(file, 10L));

        assertTrue(ex.getMessage().contains("Invalid file type"));
    }

    @Test
    void reviewPrescription_approved_success() {
        Prescription pending = Prescription.builder()
                .id(1L)
                .customerId(10L)
                .status(Prescription.PrescriptionStatus.PENDING)
                .build();

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrescriptionResponseDTO response = prescriptionService.reviewPrescription(1L, "APPROVED", "OK", 1L);

        assertEquals("APPROVED", response.getStatus());
        assertEquals(1L, pending.getReviewedBy());
    }

    @Test
    void reviewPrescription_alreadyApproved_throwsBadRequest() {
        Prescription approved = Prescription.builder()
                .id(2L)
                .customerId(10L)
                .status(Prescription.PrescriptionStatus.APPROVED)
                .build();

        when(prescriptionRepository.findById(2L)).thenReturn(Optional.of(approved));

        assertThrows(IllegalStateException.class,
                () -> prescriptionService.reviewPrescription(2L, "APPROVED", "Already approved", 1L));
    }

    @Test
    void reviewPrescription_invalidStatus_throwsBadRequest() {
        Prescription pending = Prescription.builder()
                .id(3L)
                .customerId(10L)
                .status(Prescription.PrescriptionStatus.PENDING)
                .build();

        when(prescriptionRepository.findById(3L)).thenReturn(Optional.of(pending));

        assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.reviewPrescription(3L, "EXPIRED", "x", 99L));
    }

    @Test
    void reviewPrescription_rejectedWithoutNotes_throwsBadRequest() {
        Prescription pending = Prescription.builder()
                .id(4L)
                .customerId(10L)
                .status(Prescription.PrescriptionStatus.PENDING)
                .build();

        when(prescriptionRepository.findById(4L)).thenReturn(Optional.of(pending));

        assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.reviewPrescription(4L, "REJECTED", " ", 99L));
    }

    @Test
    void uploadPrescription_filenameWithoutExtension_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prescription",
                "application/pdf",
                "abc".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalArgumentException.class, () -> prescriptionService.uploadPrescription(file, 10L));
    }
}
