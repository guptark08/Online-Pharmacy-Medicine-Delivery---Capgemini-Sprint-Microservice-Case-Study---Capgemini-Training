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
public class OrderCreatedEvent {

    private String eventId;
    private String occurredAt;

    private Long orderId;
    private Long userId;
    private String orderStatus;

    private BigDecimal totalAmount;
    private BigDecimal finalAmount;

    private Integer itemCount;
    private Long prescriptionId;
    private String deliveryPincode;
}
