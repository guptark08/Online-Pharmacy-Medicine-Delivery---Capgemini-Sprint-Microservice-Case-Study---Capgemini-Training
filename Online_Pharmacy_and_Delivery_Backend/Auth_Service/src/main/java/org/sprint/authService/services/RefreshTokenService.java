package org.sprint.authService.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.authService.dao.RefreshTokenRepository;
import org.sprint.authService.entities.RefreshToken;
import org.sprint.authService.entities.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken issueToken(User user) {
        refreshTokenRepository.revokeAllActiveTokensByUserId(user.getId());
        return createToken(user);
    }

    @Transactional
    public RefreshToken rotateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        RefreshToken existing = refreshTokenRepository.findByTokenAndRevokedFalse(rawToken.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new IllegalArgumentException("Refresh token has expired. Please login again");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createToken(existing.getUser());
    }

    @Transactional
    public void revokeAllActiveTokensByUserId(Long userId) {
        if (userId == null) {
            return;
        }

        refreshTokenRepository.revokeAllActiveTokensByUserId(userId);
    }

    private RefreshToken createToken(User user) {
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L);

        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }
}
