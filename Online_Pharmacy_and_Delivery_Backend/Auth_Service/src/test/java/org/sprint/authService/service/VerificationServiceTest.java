package org.sprint.authService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.sprint.authService.dao.UserRepository;
import org.sprint.authService.dao.VerificationTokenRepository;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationService verificationService;

    @Test
    void getVerificationUrl_usesFrontendBaseUrl() {
        ReflectionTestUtils.setField(verificationService, "frontendBaseUrl", "http://localhost:5173");

        String url = verificationService.getVerificationUrl("verification-token");

        assertEquals("http://localhost:5173/verify-email?token=verification-token", url);
    }
}
