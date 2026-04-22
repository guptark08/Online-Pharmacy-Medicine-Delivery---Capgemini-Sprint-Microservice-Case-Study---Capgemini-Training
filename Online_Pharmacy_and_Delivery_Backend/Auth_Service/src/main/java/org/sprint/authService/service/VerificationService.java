package org.sprint.authService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.authService.dao.UserRepository;
import org.sprint.authService.dao.VerificationTokenRepository;
import org.sprint.authService.entity.VerificationToken;
import org.sprint.authService.entities.User;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${verification.token.expiration-hours:24}")
    private int tokenExpirationHours;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public String createEmailVerificationToken(Long userId) {
        String token = generateSecureToken();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .userId(userId)
                .type(VerificationToken.VerificationType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpirationHours))
                .createdAt(LocalDateTime.now())
                .build();
        tokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public boolean verifyEmailToken(String token) {
        Optional<VerificationToken> optToken = tokenRepository.findByToken(token);
        if (optToken.isEmpty()) {
            log.warn("Email verification failed: Token not found");
            return false;
        }
        VerificationToken verificationToken = optToken.get();
        if (!verificationToken.isValid()) {
            log.warn("Email verification failed: Token invalid or expired");
            return false;
        }

        Optional<User> userOpt = userRepository.findById(verificationToken.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("Email verification failed: User not found for token");
            return false;
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {} ({})", user.getUsername(), user.getEmail());
        return true;
    }

    public String getVerificationUrl(String token) {
        return frontendBaseUrl + "/verify-email?token=" + token;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
