package org.sprint.authService.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.sprint.authService.config.JwtFilter;
import org.sprint.authService.config.JwtService;
import org.sprint.authService.config.SecurityConfig;
import org.sprint.authService.dao.PasswordResetTokenRepository;
import org.sprint.authService.dao.UserRepository;
import org.sprint.authService.entities.RefreshToken;
import org.sprint.authService.entities.User;
import org.sprint.authService.exception.GlobalExceptionHandler;
import org.sprint.authService.service.EmailEventPublisher;
import org.sprint.authService.service.OtpService;
import org.sprint.authService.service.VerificationService;
import org.sprint.authService.services.RefreshTokenService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@WebMvcTest(VerificationController.class)
@Import({ GlobalExceptionHandler.class, SecurityConfig.class })
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OtpService otpService;

    @MockBean
    private VerificationService verificationService;

    @MockBean
    private EmailEventPublisher emailEventPublisher;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void verifyPasswordAndSendOtp_withEmailIdentifier_returns200() throws Exception {
        User user = activeVerifiedUser();

        when(userRepository.findByUsernameOrEmailIgnoreCaseAndStatus("alice@example.com", true))
                .thenReturn(Optional.of(user));
        when(otpService.generateOtp("alice@example.com")).thenReturn("123456");

        mockMvc.perform(post("/api/auth/verify-password-then-send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordLoginRequest("alice@example.com", "Password@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", containsString("OTP sent to")));

        verify(otpService).generateOtp("alice@example.com");
    }

    @Test
    void verifyLoginOtp_withUsernameIdentifier_usesEmailOtpKey() throws Exception {
        User user = activeVerifiedUser();
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-123")
                .user(user)
                .revoked(false)
                .build();

        when(userRepository.findByUsernameOrEmailIgnoreCaseAndStatus("alice", true)).thenReturn(Optional.of(user));
        when(otpService.validateOtp("alice@example.com", "123456")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token-123");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);
        when(refreshTokenService.issueToken(user)).thenReturn(refreshToken);

        mockMvc.perform(post("/api/auth/verify-login-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpVerifyRequest("alice", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token-123"))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));

        verify(otpService).validateOtp("alice@example.com", "123456");
        verify(otpService).invalidateOtp("alice@example.com");
    }

    @Test
    void verifyLoginOtp_blankIdentifier_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/verify-login-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpVerifyRequest("", "123456"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.identifier").value("Username or email is required"));
    }

    private User activeVerifiedUser() {
        return User.builder()
                .id(1L)
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("encoded")
                .role("CUSTOMER")
                .status(true)
                .emailVerified(true)
                .build();
    }

    private Map<String, Object> passwordLoginRequest(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        return request;
    }

    private Map<String, Object> otpVerifyRequest(String identifier, String otpCode) {
        Map<String, Object> request = new HashMap<>();
        request.put("identifier", identifier);
        request.put("otpCode", otpCode);
        return request;
    }
}
