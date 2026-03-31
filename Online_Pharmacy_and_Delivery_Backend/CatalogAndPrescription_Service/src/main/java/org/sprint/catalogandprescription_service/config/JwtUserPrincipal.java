package org.sprint.catalogandprescription_service.config;

public record JwtUserPrincipal(Long userId, String username, String role, String email) {
}