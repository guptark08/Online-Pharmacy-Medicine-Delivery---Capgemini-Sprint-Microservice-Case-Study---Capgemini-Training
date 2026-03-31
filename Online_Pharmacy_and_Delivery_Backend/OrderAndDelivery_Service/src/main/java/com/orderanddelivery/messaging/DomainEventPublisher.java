package com.orderanddelivery.messaging;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderanddelivery.outbox.OutboxEvent;
import com.orderanddelivery.outbox.OutboxEventRepository;
import com.orderanddelivery.outbox.OutboxEventStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    @Value("${pharmacy.events.exchange:" + PharmacyEventRoutingKeys.EXCHANGE + "}")
    private String exchangeName;

    @Value("${pharmacy.events.rabbit.enabled:true}")
    private boolean rabbitEnabled;

    @Transactional
    public void publishAfterCommit(String routingKey, Object payload) {
        if (!rabbitEnabled) {
            return;
        }

        String jsonPayload = serializePayload(payload);
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(extractEventId(jsonPayload))
                .exchangeName(exchangeName)
                .routingKey(routingKey)
                .payload(jsonPayload)
                .status(OutboxEventStatus.PENDING)
                .attempts(0)
                .nextAttemptAt(LocalDateTime.now())
                .build();

        outboxEventRepository.save(outboxEvent);
        log.debug("Queued domain event in outbox id={} routingKey={}", outboxEvent.getId(), routingKey);
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize domain event payload", ex);
        }
    }

    private String extractEventId(String jsonPayload) {
        try {
            JsonNode root = objectMapper.readTree(jsonPayload);
            JsonNode eventIdNode = root.get("eventId");
            if (eventIdNode == null || eventIdNode.isNull()) {
                return null;
            }
            String eventId = eventIdNode.asText();
            return eventId == null || eventId.isBlank() ? null : eventId.trim();
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}