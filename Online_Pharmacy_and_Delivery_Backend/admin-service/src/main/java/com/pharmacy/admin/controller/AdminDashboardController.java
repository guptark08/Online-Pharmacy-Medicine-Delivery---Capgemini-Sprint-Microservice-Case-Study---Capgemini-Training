package com.pharmacy.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.admin.dto.response.ApiResponse;
import com.pharmacy.admin.dto.response.DashboardResponseDto;
import com.pharmacy.admin.service.AdminDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Dashboard", description = "Admin dashboard KPIs, alerts, and recent activity")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get full dashboard data")
    public ResponseEntity<ApiResponse<DashboardResponseDto>> getDashboard() {
        DashboardResponseDto data = dashboardService.getDashboardData();
        return ResponseEntity.ok(ApiResponse.success("Dashboard data loaded", data));
    }
}
