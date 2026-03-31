package com.pharmacy.apigateway.swagger;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class GatewayOpenApiAccessPolicy {

    private static final Set<String> PUBLIC_MEDICINE_OR_CATEGORY_PREFIXES = Set.of(
            "/api/catalog/medicines",
            "/api/catalog/categories");

    private static final Pattern PRESCRIPTION_REVIEW_PATH = Pattern.compile("^/api/catalog/prescriptions/\\d+/review$");

    public ApiAccessLevel resolveAccess(String serviceId, String path, String method) {
        return switch (serviceId) {
            case "auth-service" -> resolveAuthAccess(path);
            case "catalog-service" -> resolveCatalogAccess(path, method);
            case "order-service" -> resolveOrderAccess(path);
            case "admin-service" -> resolveAdminAccess(path);
            default -> ApiAccessLevel.AUTHENTICATED;
        };
    }

    private ApiAccessLevel resolveAuthAccess(String path) {
        if (path.startsWith("/api/auth/signup")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/signin")
                || path.startsWith("/api/auth/refresh")) {
            return ApiAccessLevel.PUBLIC;
        }
        if (path.startsWith("/api/auth/all") || path.startsWith("/api/auth/admin")) {
            return ApiAccessLevel.ADMIN;
        }
        if (path.startsWith("/api/auth") || path.startsWith("/api/address")) {
            return ApiAccessLevel.AUTHENTICATED;
        }
        return ApiAccessLevel.PUBLIC;
    }

    private ApiAccessLevel resolveCatalogAccess(String path, String method) {
        if (isPublicCatalogBrowse(path, method)) {
            return ApiAccessLevel.PUBLIC;
        }
        if (path.startsWith("/api/catalog/admin")
                || path.startsWith("/api/catalog/prescriptions/pending")
                || PRESCRIPTION_REVIEW_PATH.matcher(path).matches()) {
            return ApiAccessLevel.ADMIN;
        }
        if (path.startsWith("/api/catalog/prescriptions/upload")
                || path.startsWith("/api/catalog/prescriptions/my")) {
            return ApiAccessLevel.CUSTOMER;
        }
        if (path.startsWith("/api/catalog/medicines") || path.startsWith("/api/catalog/categories")) {
            return ApiAccessLevel.ADMIN;
        }
        if (path.startsWith("/api/catalog")) {
            return ApiAccessLevel.AUTHENTICATED;
        }
        return ApiAccessLevel.PUBLIC;
    }

    private ApiAccessLevel resolveOrderAccess(String path) {
        if (path.startsWith("/api/orders/admin")) {
            return ApiAccessLevel.ADMIN;
        }
        if (path.startsWith("/api/orders")) {
            return ApiAccessLevel.AUTHENTICATED;
        }
        return ApiAccessLevel.PUBLIC;
    }

    private ApiAccessLevel resolveAdminAccess(String path) {
        if (path.startsWith("/api/admin")) {
            return ApiAccessLevel.ADMIN;
        }
        return ApiAccessLevel.PUBLIC;
    }

    private boolean isPublicCatalogBrowse(String path, String method) {
        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }
        return PUBLIC_MEDICINE_OR_CATEGORY_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
