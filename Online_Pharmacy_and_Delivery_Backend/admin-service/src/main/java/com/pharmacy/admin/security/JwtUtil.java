package com.pharmacy.admin.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256 signing");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object value = extractAllClaims(token).get("role");
        if (value == null) {
            return null;
        }

        String normalized = value.toString().trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }

    public Long extractUserId(String token) {
        Object value = extractAllClaims(token).get("userId");

        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration == null || expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return claims.getSubject() != null && (expiration == null || expiration.after(new Date()));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
