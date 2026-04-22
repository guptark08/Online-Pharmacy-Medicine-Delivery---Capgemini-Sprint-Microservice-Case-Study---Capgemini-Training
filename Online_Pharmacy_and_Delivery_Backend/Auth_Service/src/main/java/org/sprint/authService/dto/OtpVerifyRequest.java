package org.sprint.authService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {
    @NotBlank(message = "Username or email is required")
    private String identifier;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
