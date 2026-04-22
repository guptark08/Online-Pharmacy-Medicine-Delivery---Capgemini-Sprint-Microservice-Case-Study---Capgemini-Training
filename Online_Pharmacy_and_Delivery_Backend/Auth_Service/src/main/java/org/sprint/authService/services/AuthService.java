package org.sprint.authService.services;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.authService.config.JwtService;
import org.sprint.authService.dto.AuthRequest;
import org.sprint.authService.dto.AuthResponse;
import org.sprint.authService.entities.RefreshToken;
import org.sprint.authService.entities.User;
import org.sprint.authService.security.JwtTokenRevocationService;
import org.sprint.authService.util.RoleNormalizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenRevocationService jwtTokenRevocationService;

    @Transactional
    public AuthResponse authenticate(AuthRequest authRequest) {
        String requestedUsername = authRequest.getUsername() == null ? "" : authRequest.getUsername().trim();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestedUsername, authRequest.getPassword()));

        String authenticatedUsername = authentication.getName();
        User user = userService.getActiveByUsername(authenticatedUsername);

        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            log.warn("Login rejected: Email not verified for user {}", user.getUsername());
            throw new BadCredentialsException("Please verify your email address before logging in. Check your inbox for the verification email.");
        }

        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.issueToken(user);
        log.info("Authenticated user username={} userId={} role={}",
                user.getUsername(), user.getId(), RoleNormalizer.normalizeOrDefault(user.getRole()));

        return buildAuthResponse(user, token, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refreshAccessToken(String presentedRefreshToken) {
        RefreshToken rotatedRefreshToken = refreshTokenService.rotateToken(presentedRefreshToken);
        User user = rotatedRefreshToken.getUser();

        if (!user.isStatus()) {
            throw new IllegalArgumentException("User is inactive");
        }

        String accessToken = jwtService.generateToken(user);
        log.info("Refreshed access token for user username={} userId={}", user.getUsername(), user.getId());
        return buildAuthResponse(user, accessToken, rotatedRefreshToken.getToken());
    }

    public void logout(String rawAccessToken) {
        if (rawAccessToken == null || rawAccessToken.isBlank()) {
            throw new IllegalArgumentException("Access token is required for logout");
        }

        Long userId = jwtService.extractUserId(rawAccessToken);
        String tokenId = jwtService.extractTokenId(rawAccessToken);
        Date expiration = jwtService.getExpiration(rawAccessToken);

        if (userId != null) {
            refreshTokenService.revokeAllActiveTokensByUserId(userId);
        }

        jwtTokenRevocationService.revokeToken(
                tokenId,
                rawAccessToken,
                expiration == null ? null : expiration.toInstant());
        log.info("Logged out tokenId={}", tokenId);
    }

    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(RoleNormalizer.normalizeOrDefault(user.getRole()))
                .active(user.isStatus())
                .build();
    }
}
