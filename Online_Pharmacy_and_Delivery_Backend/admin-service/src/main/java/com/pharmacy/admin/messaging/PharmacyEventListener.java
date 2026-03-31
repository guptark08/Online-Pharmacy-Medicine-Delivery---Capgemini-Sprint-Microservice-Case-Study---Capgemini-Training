package com.pharmacy.admin.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class PharmacyEventListener {

    private final ObjectMapper objectMapper;
    private final EventIdempotencyService eventIdempotencyService;

    @RabbitListener(queues = "${pharmacy.events.queues.order-created}")
    public void onOrderCreated(String payload) {
        handleEvent("ORDER_CREATED", payload);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.payment-succeeded}")
    public void onPaymentSucceeded(String payload) {
        handleEvent("PAYMENT_SUCCEEDED", payload);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.payment-failed}")
    public void onPaymentFailed(String payload) {
        handleEvent("PAYMENT_FAILED", payload);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.prescription-reviewed}")
    public void onPrescriptionReviewed(String payload) {
        handleEvent("PRESCRIPTION_REVIEWED", payload);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.order-status-changed}")
    public void onOrderStatusChanged(String payload) {
        handleEvent("ORDER_STATUS_CHANGED", payload);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.inventory-adjusted}")
    public void onInventoryAdjusted(String payload) {
        handleEvent("INVENTORY_ADJUSTED", payload);
    }

    private void handleEvent(String eventType, String payload) {
        JsonNode root = parsePayload(payload);
        String eventId = extractEventId(root);

        if (eventId == null) {
            log.warn("Received {} event without eventId. Processing without deduplication.", eventType);
        } else if (!eventIdempotencyService.markIfFirstProcessing(eventId, eventType)) {
            return;
        }

        log.info("Processed {} event eventId={} payload={}", eventType, eventId, payload);
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid event payload JSON", ex);
        }
    }

    private String extractEventId(JsonNode root) {
        JsonNode eventIdNode = root.get("eventId");
        if (eventIdNode == null || eventIdNode.isNull()) {
            return null;
        }

        String eventId = eventIdNode.asText();
        if (eventId == null || eventId.isBlank()) {
            return null;
        }

        return eventId.trim();
    }
}
