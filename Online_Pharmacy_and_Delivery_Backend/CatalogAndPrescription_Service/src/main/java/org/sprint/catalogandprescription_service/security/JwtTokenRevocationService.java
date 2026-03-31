package org.sprint.catalogandprescription_service.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenRevocationService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenRevocationService.class);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    @Value("${jwt.revocation.enabled:false}")
    private boolean revocationEnabled;

    @Value("${jwt.revocation.jti-prefix:jwt:revoked:jti:}")
    private String jtiPrefix;

    @Value("${jwt.revocation.hash-prefix:jwt:revoked:hash:}")
    private String hashPrefix;

    public JwtTokenRevocationService(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    public boolean isRevoked(String tokenId, String rawToken) {
        if (!revocationEnabled) {
            return false;
        }

        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return false;
        }

        try {
            if (tokenId != null && !tokenId.isBlank()
                    && Boolean.TRUE.equals(redisTemplate.hasKey(jtiPrefix + tokenId.trim()))) {
                return true;
            }

            String fingerprint = fingerprint(rawToken);
            return fingerprint != null && Boolean.TRUE.equals(redisTemplate.hasKey(hashPrefix + fingerprint));
        } catch (Exception ex) {
            log.warn("Failed to check token revocation marker: {}", ex.getMessage());
            return false;
        }
    }

    private String fingerprint(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            return null;
        }
    }
}
