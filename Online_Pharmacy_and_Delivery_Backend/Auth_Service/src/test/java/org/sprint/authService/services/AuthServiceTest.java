package org.sprint.authService.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.sprint.authService.config.JwtService;
import org.sprint.authService.dto.AuthRequest;
import org.sprint.authService.dto.AuthResponse;
import org.sprint.authService.entities.RefreshToken;
import org.sprint.authService.entities.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_success_returnsJwtResponse() {
        AuthRequest request = new AuthRequest("demo", "secret");

        Authentication authentication = new UsernamePasswordAuthenticationToken("demo", null);

        User user = User.builder()
                .id(10L)
                .username("demo")
                .email("demo@example.com")
                .role("CUSTOMER")
                .status(true)
                .emailVerified(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-123")
                .user(user)
                .revoked(false)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getActiveByUsername("demo")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("token-123");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);
        when(refreshTokenService.issueToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("token-123", response.getToken());
        assertEquals("refresh-123", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(10L, response.getUserId());
        assertEquals("CUSTOMER", response.getRole());
    }
}
