package com.pharmacy.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.pharmacy.apigateway.security.JwtTokenRevocationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_USERNAME = "X-Username";

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/signup",
            "/api/auth/verify-password-then-send-otp",
            "/api/auth/verify-login-otp",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/refresh",
            "/api/catalog/medicines",
            "/api/catalog/categories",
            "/fallback",
            "/api-docs",
            "/gateway-docs",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",
            "/actuator/health",
            "/actuator/prometheus",
            "/actuator/info",
            "/error"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final JwtTokenRevocationService jwtTokenRevocationService;

    private SecretKey signingKey;

    public JwtValidationFilter(JwtTokenRevocationService jwtTokenRevocationService) {
        this.jwtTokenRevocationService = jwtTokenRevocationService;
    }

    @PostConstruct
    public void initialize() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        Claims claims = parseClaims(token);
        if (claims == null) {
            return unauthorized(exchange, "Invalid or expired JWT token");
        }

        if (jwtTokenRevocationService.isRevoked(claims.getId(), token)) {
            return unauthorized(exchange, "Token has been revoked");
        }

        String username = claims.getSubject();
        String role = normalizeRole(claims.get("role"));
        Long userId = extractUserId(claims.get("userId"));

        if (username == null || username.isBlank() || role == null || userId == null) {
            return unauthorized(exchange, "JWT token is missing required identity claims");
        }

        if (isAdminOnlyPath(path) && !"ADMIN".equals(role)) {
            return forbidden(exchange, "Insufficient privileges");
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(HEADER_USER_ID, String.valueOf(userId))
                .header(HEADER_USER_ROLE, role)
                .header(HEADER_USERNAME, username)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminOnlyPath(String path) {
        if (path.startsWith("/api/admin/")) {
            return true;
        }
        return path.startsWith("/actuator/")
                && !path.startsWith("/actuator/health")
                && !path.startsWith("/actuator/prometheus")
                && !path.startsWith("/actuator/info");
    }

    private Claims parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration() == null || !claims.getExpiration().toInstant().isAfter(Instant.now())) {
                return null;
            }
            return claims;
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalizeRole(Object roleClaim) {
        if (roleClaim == null) {
            return null;
        }

        String normalized = roleClaim.toString().trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return null;
        }

        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }

    private Long extractUserId(Object userIdClaim) {
        if (userIdClaim instanceof Long longValue) {
            return longValue;
        }
        if (userIdClaim instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (userIdClaim instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeError(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return writeError(exchange, HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String payload = "{\"success\":false,\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(payload.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
