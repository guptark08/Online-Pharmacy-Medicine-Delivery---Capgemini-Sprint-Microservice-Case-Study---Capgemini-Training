package org.sprint.authService.util;

import java.util.Locale;

public final class RoleNormalizer {

    private RoleNormalizer() {
    }

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }

    public static String normalizeOrDefault(String role) {
        String normalized = normalize(role);
        return normalized == null ? "CUSTOMER" : normalized;
    }

    public static String toAuthority(String role) {
        return "ROLE_" + normalizeOrDefault(role);
    }
}
