package com.orderanddelivery.messaging.events;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEvent {

    private String eventId;
    private String occurredAt;

    private Long orderId;
    private Long userId;
    private Long paymentId;

    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal amount;

    private String transactionId;
    private String failureReason;
}
