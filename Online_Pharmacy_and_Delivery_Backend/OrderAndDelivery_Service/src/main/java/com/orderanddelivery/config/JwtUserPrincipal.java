package com.orderanddelivery.config;

public record JwtUserPrincipal(Long userId, String username, String role, String email) {
}