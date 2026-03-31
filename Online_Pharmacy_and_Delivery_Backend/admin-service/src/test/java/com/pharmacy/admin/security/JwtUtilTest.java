package com.pharmacy.admin.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // 256-bit test secret (base64 encoded)
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private static final long EXPIRATION_MS = 86_400_000L; // 24h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
    }

    // ── Build a token for testing ─────────────────────────────────
    private String buildToken(String username, String role, long expiryMs) {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(TEST_SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", 42L);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Should extract username correctly from valid token")
    void extractUsername_validToken_returnsUsername() {
        String token = buildToken("admin@pharmacy.com", "ADMIN", EXPIRATION_MS);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("admin@pharmacy.com");
    }

    @Test
    @DisplayName("Should extract role correctly from valid token")
    void extractRole_validToken_returnsRole() {
        String token = buildToken("admin@pharmacy.com", "ADMIN", EXPIRATION_MS);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should return true for a valid, non-expired token")
    void validateToken_validToken_returnsTrue() {
        String token = buildToken("admin@pharmacy.com", "ADMIN", EXPIRATION_MS);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should return false for an expired token")
    void validateToken_expiredToken_returnsFalse() {
        // token expired 1 second ago
        String token = buildToken("admin@pharmacy.com", "ADMIN", -1000L);
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Should return false for a malformed token string")
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("this.is.not.a.token")).isFalse();
    }

    @Test
    @DisplayName("Should return false for an empty string")
    void validateToken_emptyString_returnsFalse() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify an expired token via isTokenExpired")
    void isTokenExpired_expiredToken_returnsTrue() {
        String token = buildToken("user@test.com", "CUSTOMER", -5000L);
        assertThat(jwtUtil.isTokenExpired(token)).isTrue();
    }

    @Test
    @DisplayName("Should correctly identify a valid token as not expired")
    void isTokenExpired_validToken_returnsFalse() {
        String token = buildToken("user@test.com", "CUSTOMER", EXPIRATION_MS);
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }
}
