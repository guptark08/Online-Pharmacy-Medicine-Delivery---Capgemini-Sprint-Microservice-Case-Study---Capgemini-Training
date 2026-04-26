package org.sprint.catalogandprescription_service.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.sprint.catalogandprescription_service.config.JwtUserPrincipal;
import org.sprint.catalogandprescription_service.dto.ApiResponse;
import org.sprint.catalogandprescription_service.dto.PrescriptionResponseDTO;
import org.sprint.catalogandprescription_service.service.PrescriptionService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Upload a prescription file")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> uploadPrescription(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtUserPrincipal currentUser) throws IOException {

        PrescriptionResponseDTO result = prescriptionService.uploadPrescription(file, extractCurrentUserId(currentUser));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Prescription uploaded successfully", result));
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Download/view prescription file")
    public ResponseEntity<Resource> getPrescriptionFile(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal currentUser) {
        Long userId = extractCurrentUserId(currentUser);
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(
                currentUser.role() != null ? currentUser.role().replace("ROLE_", "") : "");
        Resource resource = prescriptionService.getPrescriptionFile(id, isAdmin ? null : userId);
        String contentType = resource != null && resource.getFilename() != null
                ? getContentType(resource.getFilename())
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getMyPrescriptions(
            @AuthenticationPrincipal JwtUserPrincipal currentUser) {

        List<PrescriptionResponseDTO> list = prescriptionService.getByCustomer(extractCurrentUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Prescriptions fetched", list));
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> getPrescriptionStatusForCurrentUser(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal currentUser) {

        String status = prescriptionService.getPrescriptionStatusForCustomer(id, extractCurrentUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Prescription status fetched", status));
    }

    @PutMapping("/{id}/link-order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> linkPrescriptionToOrder(
            @PathVariable Long id,
            @PathVariable Long orderId,
            @AuthenticationPrincipal JwtUserPrincipal currentUser) {

        prescriptionService.linkPrescriptionToOrder(id, extractCurrentUserId(currentUser), orderId);
        return ResponseEntity.ok(ApiResponse.success("Prescription linked to order", null));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDTO>>> getPending() {
        List<PrescriptionResponseDTO> pending = prescriptionService.getPendingPrescriptions();
        return ResponseEntity.ok(ApiResponse.success("Pending prescriptions", pending));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve or reject a prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> reviewPrescription(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal JwtUserPrincipal currentUser) {

        PrescriptionResponseDTO result = prescriptionService.reviewPrescription(
                id,
                status,
                notes,
                extractCurrentUserId(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Prescription reviewed", result));
    }

    private Long extractCurrentUserId(JwtUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new IllegalStateException("Unable to resolve authenticated user");
        }
        return currentUser.userId();
    }

    private String getContentType(String filename) {
        if (filename == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}