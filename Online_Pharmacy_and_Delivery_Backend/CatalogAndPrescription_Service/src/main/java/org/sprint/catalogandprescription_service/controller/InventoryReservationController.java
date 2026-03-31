package org.sprint.catalogandprescription_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sprint.catalogandprescription_service.dto.ApiResponse;
import org.sprint.catalogandprescription_service.dto.InventoryReservationRequest;
import org.sprint.catalogandprescription_service.dto.InventoryReservationResponse;
import org.sprint.catalogandprescription_service.service.InventoryReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog/inventory")
@RequiredArgsConstructor
public class InventoryReservationController {

    private final InventoryReservationService inventoryReservationService;

    @PostMapping("/reserve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InventoryReservationResponse>> reserveInventory(
            @Valid @RequestBody InventoryReservationRequest request) {

        InventoryReservationResponse response = inventoryReservationService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success("Inventory reserved successfully", response));
    }
}
