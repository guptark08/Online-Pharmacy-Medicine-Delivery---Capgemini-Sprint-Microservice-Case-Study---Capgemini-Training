package org.sprint.authService.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.sprint.authService.config.JwtFilter;
import org.sprint.authService.config.SecurityConfig;
import org.sprint.authService.dto.UserRequest;
import org.sprint.authService.dto.UserResponse;
import org.sprint.authService.entities.User;
import org.sprint.authService.exception.GlobalExceptionHandler;
import org.sprint.authService.services.AuthService;
import org.sprint.authService.services.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@WebMvcTest(UserController.class)
@Import({ GlobalExceptionHandler.class, SecurityConfig.class })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtFilter jwtFilter;

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
    void signup_validInput_returns201() throws Exception {
        UserResponse created = UserResponse.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .username("alice")
                .mobile("9999999999")
                .role("CUSTOMER")
                .status(true)
                .build();

        when(userService.addUser(any(UserRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void signup_blankName_returns400() throws Exception {
        Map<String, Object> request = validSignupRequest();
        request.put("name", "");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.name", containsString("Name")));
    }

    @Test
    void signup_invalidEmail_returns400() throws Exception {
        Map<String, Object> request = validSignupRequest();
        request.put("email", "notanemail");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shortPassword_returns400() throws Exception {
        Map<String, Object> request = validSignupRequest();
        request.put("password", "abc");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_passwordMissingSpecialCharacter_returns400() throws Exception {
        Map<String, Object> request = validSignupRequest();
        request.put("password", "Password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.password", containsString("special character")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_adminRole_returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                UserResponse.builder()
                        .id(1L)
                        .name("Alice")
                        .email("alice@example.com")
                        .username("alice")
                        .mobile("9999999999")
                        .role("CUSTOMER")
                        .status(true)
                        .build()));

        mockMvc.perform(get("/api/auth/all"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllUsers_customerRole_returns403() throws Exception {
        mockMvc.perform(get("/api/auth/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void getMe_authenticated_returns200WithCurrentUser() throws Exception {
        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .username("alice")
                .mobile("9999999999")
                .role("ROLE_CUSTOMER")
                .status(true)
                .build();

        when(userService.getActiveByUsername("alice")).thenReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    private Map<String, Object> validSignupRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Alice");
        request.put("email", "alice@example.com");
        request.put("username", "alice");
        request.put("mobile", "9999999999");
        request.put("password", "Password@123");
        return request;
    }

}
