package com.pharmacy.apigateway.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return serviceUnavailable("auth-service");
    }

    @RequestMapping("/catalog")
    public ResponseEntity<Map<String, Object>> catalogFallback() {
        return serviceUnavailable("catalog-service");
    }

    @RequestMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        return serviceUnavailable("order-service");
    }

    @RequestMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminFallback() {
        return serviceUnavailable("admin-service");
    }

    private ResponseEntity<Map<String, Object>> serviceUnavailable(String serviceName) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success", false,
                "message", serviceName + " is temporarily unavailable. Please retry.",
                "timestamp", Instant.now().toString()));
    }
}