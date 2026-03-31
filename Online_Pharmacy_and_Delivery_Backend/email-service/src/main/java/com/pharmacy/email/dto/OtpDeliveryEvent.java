package com.pharmacy.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpDeliveryEvent {
    private String eventId;
    private String occurredAt;
    private Long userId;
    private String email;
    private String userName;
    private String otpCode;
    private String purpose;
    private Integer expirationMinutes;
}
