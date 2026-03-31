package org.sprint.catalogandprescription_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sprint.catalogandprescription_service.dto.ApiResponse;
import org.sprint.catalogandprescription_service.dto.MedicineDTO;
import org.sprint.catalogandprescription_service.service.MedicineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine API", description = "Browse and manage medicines")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Get all medicines with optional search and filter")
    public ResponseEntity<ApiResponse<Page<MedicineDTO>>> getAllMedicines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        Page<MedicineDTO> medicines = medicineService.getAllMedicines(
                keyword,
                categoryId,
                requiresPrescription,
                page,
                size,
                sortBy);

        return ResponseEntity.ok(ApiResponse.success("Medicines fetched successfully", medicines));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine details by ID")
    public ResponseEntity<ApiResponse<MedicineDTO>> getMedicineById(@PathVariable Long id) {
        MedicineDTO medicine = medicineService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.success("Medicine found", medicine));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new medicine (Admin only)")
    public ResponseEntity<ApiResponse<MedicineDTO>> createMedicine(@Valid @RequestBody MedicineDTO dto) {
        MedicineDTO created = medicineService.createMedicine(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a medicine (Admin only)")
    public ResponseEntity<ApiResponse<MedicineDTO>> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineDTO dto) {

        MedicineDTO updated = medicineService.updateMedicine(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Medicine updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a medicine (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success("Medicine deleted", null));
    }
}
