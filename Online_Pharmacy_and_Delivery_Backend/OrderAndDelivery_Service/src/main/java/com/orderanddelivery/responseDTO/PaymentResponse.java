package com.orderanddelivery.responseDTO;

import java.math.BigDecimal;

import com.orderanddelivery.enums.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentStatus status;
    private String method;
    private String transactionId;
    private BigDecimal amount;
    private String message;
}
