package com.pharmacy.admin.security;

public record JwtUserPrincipal(Long userId, String username, String role) {
}
