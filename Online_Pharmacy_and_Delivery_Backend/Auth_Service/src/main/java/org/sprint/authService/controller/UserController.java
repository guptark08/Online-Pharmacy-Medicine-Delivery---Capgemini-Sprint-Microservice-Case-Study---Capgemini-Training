package org.sprint.authService.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sprint.authService.dto.AddressResponse;
import org.sprint.authService.dto.ApiResponse;
import org.sprint.authService.dto.AuthResponse;
import org.sprint.authService.dto.RefreshTokenRequest;
import org.sprint.authService.dto.UserRequest;
import org.sprint.authService.dto.UserResponse;
import org.sprint.authService.entities.User;
import org.sprint.authService.services.AuthService;
import org.sprint.authService.services.UserService;
import org.sprint.authService.util.RoleNormalizer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", userService.getAllUsers()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        User current = userService.getActiveByUsername(authentication.getName());
        UserResponse response = UserResponse.builder()
                .id(current.getId())
                .name(current.getName())
                .email(current.getEmail())
                .username(current.getUsername())
                .mobile(current.getMobile())
                .role(RoleNormalizer.normalizeOrDefault(current.getRole()))
                .status(current.isStatus())
                .emailVerified(current.getEmailVerified())
                .addresses(current.getAddresses() != null ? 
                    current.getAddresses().stream()
                        .map(addr -> AddressResponse.builder()
                            .id(addr.getId())
                            .street_address(addr.getStreet_address())
                            .city(addr.getCity())
                            .state(addr.getState())
                            .pincode(addr.getPincode())
                            .isDefault(addr.isDefault())
                            .build())
                        .toList() : null)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Current user fetched", response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> addUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse created = userService.addUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", created));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = extractBearerToken(authHeader);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        return token;
    }
}
