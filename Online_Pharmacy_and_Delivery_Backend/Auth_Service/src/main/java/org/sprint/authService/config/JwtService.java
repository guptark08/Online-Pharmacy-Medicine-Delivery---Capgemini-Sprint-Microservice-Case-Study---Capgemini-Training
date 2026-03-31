package org.sprint.authService.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.sprint.authService.entities.User;
import org.sprint.authService.util.RoleNormalizer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", RoleNormalizer.normalizeOrDefault(user.getRole()));
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
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
        return null;
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    public Date getExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public String extractTokenId(String token) {
        String tokenId = extractAllClaims(token).getId();
        return tokenId == null || tokenId.isBlank() ? null : tokenId;
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject() != null && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
