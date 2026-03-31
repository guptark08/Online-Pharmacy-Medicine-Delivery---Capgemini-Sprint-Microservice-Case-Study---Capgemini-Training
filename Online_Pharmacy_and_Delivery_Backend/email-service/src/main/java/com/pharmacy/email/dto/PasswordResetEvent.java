package com.pharmacy.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetEvent {

    private Long userId;
    private String email;
    private String userName;
    private String resetToken;
    private String resetUrl;
}
