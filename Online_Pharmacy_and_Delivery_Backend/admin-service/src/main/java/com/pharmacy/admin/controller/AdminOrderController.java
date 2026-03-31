package com.pharmacy.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.admin.dto.request.OrderStatusUpdateDto;
import com.pharmacy.admin.dto.request.PrescriptionReviewDto;
import com.pharmacy.admin.dto.response.ApiResponse;
import com.pharmacy.admin.dto.response.OrderResponseDto;
import com.pharmacy.admin.dto.response.PrescriptionResponseDto;
import com.pharmacy.admin.security.JwtUserPrincipal;
import com.pharmacy.admin.service.AdminOrderService;
import com.pharmacy.admin.service.AdminPrescriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Order & Prescription Management", description = "Admin order lifecycle control and prescription review queue")
public class AdminOrderController {

    private final AdminOrderService orderService;
    private final AdminPrescriptionService prescriptionService;

    @GetMapping("/orders")
    @Operation(summary = "Get all orders (paginated, newest first)")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(page, size)));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get a single order with all items")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    @GetMapping("/orders/status/{status}")
    @Operation(summary = "Get orders filtered by status")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByStatus(status)));
    }

    @GetMapping("/orders/user/{userId}")
    @Operation(summary = "Get all orders for a specific user")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByUser(userId)));
    }

    @GetMapping("/orders/active")
    @Operation(summary = "Get active orders that need admin attention")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getActiveOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getActiveOrders()));
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated", orderService.updateOrderStatus(id, dto)));
    }

    @PutMapping("/orders/{id}/cancel")
    @Operation(summary = "Admin cancel an order")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", orderService.cancelOrder(id, reason)));
    }

    @GetMapping("/prescriptions")
    @Operation(summary = "Get all prescriptions (paginated)")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDto>>> getAllPrescriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getAllPrescriptions(page, size)));
    }

    @GetMapping("/prescriptions/pending")
    @Operation(summary = "Get pending prescription review queue")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDto>>> getPendingPrescriptions() {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPendingPrescriptions()));
    }

    @GetMapping("/prescriptions/{id}")
    @Operation(summary = "Get a single prescription by ID")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionById(id)));
    }

    @PutMapping("/prescriptions/{id}/review")
    @Operation(summary = "Approve or reject a prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> reviewPrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionReviewDto dto,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        Long adminId = principal == null ? null : principal.userId();
        if (adminId == null) {
            throw new IllegalArgumentException("Authenticated admin id is missing in token claims");
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Prescription review saved",
                prescriptionService.reviewPrescription(id, dto, adminId)));
    }

    @GetMapping("/prescriptions/count")
    @Operation(summary = "Get pending prescription count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPrescriptionCount() {
        long pending = prescriptionService.getPendingPrescriptions().size();
        return ResponseEntity.ok(ApiResponse.success(Map.of("pendingCount", pending)));
    }
}
