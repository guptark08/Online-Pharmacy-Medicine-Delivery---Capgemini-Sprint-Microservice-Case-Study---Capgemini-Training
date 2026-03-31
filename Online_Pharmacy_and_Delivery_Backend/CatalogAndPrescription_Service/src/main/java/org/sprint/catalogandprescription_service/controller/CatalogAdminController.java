package org.sprint.catalogandprescription_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sprint.catalogandprescription_service.dto.ApiResponse;
import org.sprint.catalogandprescription_service.dto.CatalogAdminStatsDTO;
import org.sprint.catalogandprescription_service.service.CatalogAdminAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CatalogAdminController {

    private final CatalogAdminAnalyticsService analyticsService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CatalogAdminStatsDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Catalog analytics fetched", analyticsService.getAdminStats()));
    }
}
