package com.orderanddelivery.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orderanddelivery.config.JwtUserPrincipal;
import com.orderanddelivery.enums.OrderStatus;
import com.orderanddelivery.responseDTO.CartResponse;
import com.orderanddelivery.responseDTO.OrderResponse;
import com.orderanddelivery.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrdersByUser(getUserId(principal)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<OrderResponse>> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin(page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<OrderResponse> getOrderByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdForAdmin(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrderById(getUserId(principal), id));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<CartResponse> reorder(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(orderService.reorder(getUserId(principal), id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        String bearerToken = extractBearerToken(authorizationHeader);
        return ResponseEntity.ok(orderService.cancelOrder(getUserId(principal), id, reason, bearerToken));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String bearerToken = extractBearerToken(authorizationHeader);
        return ResponseEntity.ok(orderService.updateStatus(id, status, bearerToken));
    }

    private Long getUserId(JwtUserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new IllegalArgumentException("Authenticated user information is missing");
        }
        return principal.userId();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        return authorizationHeader.substring(7).trim();
    }
}