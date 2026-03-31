package com.pharmacy.apigateway.swagger;

import java.util.Locale;

public record SwaggerUserContext(boolean authenticated, String role) {

    public static SwaggerUserContext anonymous() {
        return new SwaggerUserContext(false, null);
    }

    public static SwaggerUserContext authenticated(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return new SwaggerUserContext(true, "CUSTOMER");
        }
        String normalized = rawRole.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return new SwaggerUserContext(true, normalized);
    }

    public boolean admin() {
        return authenticated && "ADMIN".equals(role);
    }

    public boolean customer() {
        return authenticated && "CUSTOMER".equals(role);
    }
}
