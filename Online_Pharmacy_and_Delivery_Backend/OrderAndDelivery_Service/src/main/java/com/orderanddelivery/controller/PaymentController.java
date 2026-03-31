package com.orderanddelivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderanddelivery.config.JwtUserPrincipal;
import com.orderanddelivery.requestDTO.PaymentRequest;
import com.orderanddelivery.responseDTO.PaymentResponse;
import com.orderanddelivery.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(paymentService.initiatePayment(getUserId(principal), request));
    }

    private Long getUserId(JwtUserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new IllegalArgumentException("Authenticated user information is missing");
        }
        return principal.userId();
    }
}
