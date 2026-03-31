package org.sprint.authService.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.sprint.authService.entities.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String TEST_SECRET_KEY = "UnitTestJwtSecretKeyForHs256SigningAtLeast32BytesLong123";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    @Test
    void generateAndValidateToken_success() {
        User user = User.builder()
                .id(101L)
                .username("alice")
                .email("alice@example.com")
                .role("CUSTOMER")
                .status(true)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("alice", jwtService.extractUsername(token));
        assertEquals(101L, jwtService.extractUserId(token));
        assertEquals("CUSTOMER", jwtService.extractRole(token));
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        User user = User.builder()
                .id(50L)
                .username("bob")
                .email("bob@example.com")
                .role("ADMIN")
                .status(true)
                .build();

        String token = jwtService.generateToken(user);
        String tampered = token + "tampered";

        assertFalse(jwtService.validateToken(tampered));
    }

    @Test
    void extractUserId_whenClaimIsInteger_returnsLong() {
        String token = buildCustomToken("charlie", Map.of("userId", 7, "role", "CUSTOMER"), 3600000L, true);

        assertEquals(7L, jwtService.extractUserId(token));
    }

    @Test
    void extractUserId_whenClaimIsUnsupported_returnsNull() {
        String token = buildCustomToken("dave", Map.of("userId", "not-a-number", "role", "ADMIN"), 3600000L, true);

        assertNull(jwtService.extractUserId(token));
    }

    @Test
    void extractRole_whenClaimMissing_returnsNull() {
        String token = buildCustomToken("eve", Map.of("userId", 42L), 3600000L, true);

        assertNull(jwtService.extractRole(token));
    }

    @Test
    void extractTokenId_whenMissing_returnsNull() {
        String token = buildCustomToken("frank", Map.of("userId", 99L, "role", "CUSTOMER"), 3600000L, false);

        assertNull(jwtService.extractTokenId(token));
    }

    @Test
    void validateToken_whenSubjectMissing_returnsFalse() {
        String token = buildCustomToken(null, Map.of("userId", 1L, "role", "CUSTOMER"), 3600000L, true);

        assertFalse(jwtService.validateToken(token));
    }

    private String buildCustomToken(String username, Map<String, Object> claims, long expirationMs, boolean includeId) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();

        var builder = Jwts.builder()
                .claims(new HashMap<>(claims))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs));

        if (username != null) {
            builder = builder.subject(username);
        }
        if (includeId) {
            builder = builder.id(UUID.randomUUID().toString());
        }

        return builder.signWith(key).compact();
    }
}
