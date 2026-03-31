package com.orderanddelivery.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderanddelivery.config.JwtUserPrincipal;
import com.orderanddelivery.requestDTO.CheckoutRequest;
import com.orderanddelivery.responseDTO.OrderResponse;
import com.orderanddelivery.service.CheckoutService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/start")
    public ResponseEntity<OrderResponse> startCheckout(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        String bearerToken = extractBearerToken(authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkoutService.startCheckout(getUserId(principal), bearerToken, request));
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
