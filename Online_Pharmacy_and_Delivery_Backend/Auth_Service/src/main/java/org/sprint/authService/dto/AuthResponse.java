package org.sprint.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private long expiresInMs;
    private String refreshToken;

    private Long userId;
    private String username;
    private String email;
    private String role;
    private boolean active;
}
