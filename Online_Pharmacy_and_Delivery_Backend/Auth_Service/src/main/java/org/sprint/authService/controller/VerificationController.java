package org.sprint.authService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.sprint.authService.config.JwtService;
import org.sprint.authService.dao.PasswordResetTokenRepository;
import org.sprint.authService.dao.UserRepository;
import org.sprint.authService.dto.*;
import org.sprint.authService.entities.PasswordResetToken;
import org.sprint.authService.entities.RefreshToken;
import org.sprint.authService.entities.User;
import org.sprint.authService.service.*;
import org.sprint.authService.services.RefreshTokenService;
import org.sprint.authService.util.RoleNormalizer;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final OtpService otpService;
    private final VerificationService verificationService;
    private final EmailEventPublisher emailEventPublisher;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Value("${password.reset.expiration-minutes:60}")
    private int passwordResetExpirationMinutes;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @PostMapping("/verify-password-then-send-otp")
    public ResponseEntity<ApiResponse<String>> verifyPasswordAndSendOtp(@Valid @RequestBody PasswordLoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>error("Invalid username or password"));
        }

        User user = userRepository.findByUsernameOrEmailIgnoreCaseAndStatus(username, true).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>error("User not found or inactive"));
        }

        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>error("Please verify your email address before logging in"));
        }

        String otp = otpService.generateOtp(user.getEmail());

        OtpDeliveryEvent event = OtpDeliveryEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getName())
                .otpCode(otp)
                .purpose("LOGIN_WITH_PASSWORD")
                .expirationMinutes(otpExpirationMinutes)
                .build();

        emailEventPublisher.publishOtpDeliveryEvent(event);

        log.info("Password verified, OTP sent for user: {}", user.getUsername());

        return ResponseEntity.ok(ApiResponse.<String>success("OTP sent to " + maskIdentifier(user.getEmail())));
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(@Valid @RequestBody OtpVerifyRequest request) {
        User user = userRepository.findByUsernameOrEmailIgnoreCaseAndStatus(request.getIdentifier().trim(), true).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<AuthResponse>error("User not found"));
        }

        if (!otpService.validateOtp(user.getEmail(), request.getOtpCode())) {
            return ResponseEntity.badRequest().body(ApiResponse.<AuthResponse>error("Invalid or expired OTP"));
        }

        if (!user.isStatus()) {
            return ResponseEntity.badRequest().body(ApiResponse.<AuthResponse>error("User account is inactive"));
        }

        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            return ResponseEntity.badRequest().body(ApiResponse.<AuthResponse>error("Please verify your email address before logging in. Check your inbox for the verification email."));
        }

        otpService.invalidateOtp(user.getEmail());

        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.issueToken(user);

        log.info("OTP login successful for user: {} ({})", user.getUsername(), user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(RoleNormalizer.normalizeOrDefault(user.getRole()))
                .active(user.isStatus())
                .build();

        publishLoginAlert(user);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>success(response));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        boolean verified = verificationService.verifyEmailToken(token);
        if (verified) {
            return ResponseEntity.ok(ApiResponse.<String>success("Email verified successfully"));
        }
        return ResponseEntity.badRequest().body(ApiResponse.<String>error("Invalid or expired token"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@RequestParam String email) {
        User user = userRepository.findByEmailIgnoreCaseAndStatus(email, true).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>error("User not found"));
        }

        String token = verificationService.createEmailVerificationToken(user.getId());
        String verificationUrl = verificationService.getVerificationUrl(token);

        EmailVerificationEvent event = EmailVerificationEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getName())
                .verificationToken(token)
                .verificationUrl(verificationUrl)
                .build();

        emailEventPublisher.publishEmailVerificationEvent(event);

        return ResponseEntity.ok(ApiResponse.<String>success("Verification email sent"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        
        userRepository.findByEmailIgnoreCaseAndStatus(email, true).ifPresent(user -> {
            String token = generateSecureToken();
            
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .userId(user.getId())
                    .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes))
                    .createdAt(LocalDateTime.now())
                    .used(false)
                    .build();
            
            passwordResetTokenRepository.save(resetToken);
            
            String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
            
            PasswordResetEvent event = PasswordResetEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .userName(user.getName())
                    .resetToken(token)
                    .resetUrl(resetUrl)
                    .build();
            
            emailEventPublisher.publishPasswordResetEvent(event);
            
            log.info("Password reset email sent for user: {} ({})", user.getUsername(), user.getEmail());
        });
        
        return ResponseEntity.ok(ApiResponse.<String>success("If an account exists with this email, a password reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken()).orElse(null);
        
        if (resetToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>error("Invalid or expired token"));
        }
        
        if (!resetToken.isValid()) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>error("Invalid or expired token"));
        }
        
        User user = userRepository.findById(resetToken.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>error("User not found"));
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Password reset successful for user: {} ({})", user.getUsername(), user.getEmail());
        
        return ResponseEntity.ok(ApiResponse.<String>success("Password reset successfully"));
    }

    private void publishLoginAlert(User user) {
        try {
            LoginAlertEvent event = LoginAlertEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .userName(user.getName())
                    .loginTime(java.time.LocalDateTime.now().toString())
                    .deviceInfo("Web Login")
                    .build();
            emailEventPublisher.publishLoginAlertEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish login alert for user {}: {}", user.getId(), e.getMessage());
        }
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) return "***";
        if (identifier.contains("@")) {
            String[] parts = identifier.split("@");
            if (parts[0].length() > 2) return parts[0].substring(0, 2) + "***@" + parts[1];
        }
        return identifier.substring(0, 2) + "***";
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
