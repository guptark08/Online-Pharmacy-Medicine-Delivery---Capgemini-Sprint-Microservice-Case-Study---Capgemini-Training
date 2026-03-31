package com.orderanddelivery.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderanddelivery.config.JwtUserPrincipal;
import com.orderanddelivery.integration.AddressClient;
import com.orderanddelivery.requestDTO.UserAddressRequest;
import com.orderanddelivery.responseDTO.UserAddressResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressClient addressClient;

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getMyAddresses(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        String bearerToken = extractBearerToken(authorizationHeader);
        ensureUser(principal);
        return ResponseEntity.ok(addressClient.getCurrentUserAddresses(bearerToken));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> getAddressById(
            @PathVariable Long addressId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        String bearerToken = extractBearerToken(authorizationHeader);
        ensureUser(principal);
        return ResponseEntity.ok(addressClient.getAddressByIdForCurrentUser(addressId, bearerToken));
    }

    @PostMapping
    public ResponseEntity<UserAddressResponse> addAddress(
            @Valid @RequestBody UserAddressRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        String bearerToken = extractBearerToken(authorizationHeader);
        ensureUser(principal);
        UserAddressResponse created = addressClient.addAddressForCurrentUser(request, bearerToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private void ensureUser(JwtUserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new IllegalArgumentException("Authenticated user information is missing");
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        return authorizationHeader.substring(7).trim();
    }
}
