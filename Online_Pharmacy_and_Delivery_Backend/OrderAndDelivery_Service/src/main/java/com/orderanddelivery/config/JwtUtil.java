package com.orderanddelivery.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
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

    public Long extractUserId(String token) {
        Object value = extractAllClaims(token).get("userId");
        if (value instanceof Integer intValue) {
            return intValue.longValue();
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

    public String extractRole(String token) {
        Object value = extractAllClaims(token).get("role");
        if (value == null) {
            return null;
        }
        String role = value.toString().trim().toUpperCase(Locale.ROOT);
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return claims.getSubject() != null && (expiration == null || expiration.after(new Date()));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}