package com.orderanddelivery.messaging.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {

    private String eventId;
    private String occurredAt;

    private Long orderId;
    private Long userId;

    private String previousStatus;
    private String newStatus;

    private String changedBy;
    private String reason;
}
