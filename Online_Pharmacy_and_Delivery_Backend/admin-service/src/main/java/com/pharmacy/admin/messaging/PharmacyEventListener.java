package com.pharmacy.admin.messaging;

import java.util.function.Consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.repository.MedicineRepository;
import com.pharmacy.admin.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class PharmacyEventListener {

    private final ObjectMapper objectMapper;
    private final EventIdempotencyService eventIdempotencyService;
    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;

    @RabbitListener(queues = "${pharmacy.events.queues.order-created}")
    public void onOrderCreated(String payload) {
        processEvent("ORDER_CREATED", payload, this::handleOrderCreated);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.payment-succeeded}")
    public void onPaymentSucceeded(String payload) {
        processEvent("PAYMENT_SUCCEEDED", payload, this::handlePaymentSucceeded);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.payment-failed}")
    public void onPaymentFailed(String payload) {
        processEvent("PAYMENT_FAILED", payload, this::handlePaymentFailed);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.prescription-reviewed}")
    public void onPrescriptionReviewed(String payload) {
        processEvent("PRESCRIPTION_REVIEWED", payload, root -> {});
    }

    @RabbitListener(queues = "${pharmacy.events.queues.order-status-changed}")
    public void onOrderStatusChanged(String payload) {
        processEvent("ORDER_STATUS_CHANGED", payload, this::handleOrderStatusChanged);
    }

    @RabbitListener(queues = "${pharmacy.events.queues.inventory-adjusted}")
    public void onInventoryAdjusted(String payload) {
        processEvent("INVENTORY_ADJUSTED", payload, this::handleInventoryAdjusted);
    }

    // ── Event handlers ────────────────────────────────────────────────────

    private void handleOrderCreated(JsonNode root) {
        long orderId = root.path("orderId").asLong(0);
        if (orderId == 0) return;

        OrderStatus status = parseStatus(root.path("orderStatus").asText("PAYMENT_PENDING"));
        double total = root.path("finalAmount").asDouble(root.path("totalAmount").asDouble(0));
        long userId = root.path("userId").asLong(0);
        JsonNode rxNode = root.path("prescriptionId");
        Long prescriptionId = rxNode.isNull() || rxNode.isMissingNode() ? null : rxNode.asLong();
        String statusStr = (status != null ? status : OrderStatus.PAYMENT_PENDING).name();

        // INSERT IGNORE preserves the order-service ID and skips duplicates
        int rows = orderRepository.insertOrderFromEvent(orderId, userId, statusStr, total, prescriptionId);
        log.info("Admin DB: insertOrderFromEvent id={} status={} rows={}", orderId, statusStr, rows);
    }

    private void handleOrderStatusChanged(JsonNode root) {
        long orderId = root.path("orderId").asLong(0);
        if (orderId == 0) return;

        OrderStatus newStatus = parseStatus(root.path("newStatus").asText(""));
        if (newStatus == null) return;

        long userId = root.path("userId").asLong(0);
        // ON DUPLICATE KEY UPDATE — atomic upsert, safe against race with ORDER_CREATED
        orderRepository.upsertStatus(orderId, userId, newStatus.name());
        log.info("Admin DB: order id={} upserted → {}", orderId, newStatus);
    }

    private void handlePaymentSucceeded(JsonNode root) {
        long orderId = root.path("orderId").asLong(0);
        if (orderId == 0) return;

        long userId  = root.path("userId").asLong(0);
        String method = root.path("paymentMethod").asText("UNKNOWN");
        String txId   = root.path("transactionId").asText("");

        orderRepository.upsertPayment(orderId, userId, OrderStatus.PAID.name(), method, txId);
        log.info("Admin DB: payment succeeded for order id={} method={}", orderId, method);
    }

    private void handlePaymentFailed(JsonNode root) {
        long orderId = root.path("orderId").asLong(0);
        if (orderId == 0) return;

        long userId = root.path("userId").asLong(0);
        orderRepository.upsertStatus(orderId, userId, OrderStatus.PAYMENT_FAILED.name());
    }

    private void handleInventoryAdjusted(JsonNode root) {
        long medicineId = root.path("medicineId").asLong(0);
        if (medicineId == 0) return;

        int currentStock = root.path("currentStock").asInt(-1);
        if (currentStock < 0) return;

        medicineRepository.findById(medicineId).ifPresent(medicine -> {
            medicine.setStock(currentStock);
            medicine.setActive(root.path("active").asBoolean(true));
            medicineRepository.save(medicine);
            log.debug("Admin DB: stock for medicine id={} updated to {}", medicineId, currentStock);
        });
    }

    // ── Infra helpers ─────────────────────────────────────────────────────

    private void processEvent(String eventType, String payload, Consumer<JsonNode> handler) {
        JsonNode root = parsePayload(payload);
        String eventId = extractEventId(root);

        if (eventId != null && !eventIdempotencyService.markIfFirstProcessing(eventId, eventType)) {
            log.debug("Skipping duplicate {} event eventId={}", eventType, eventId);
            return;
        }

        try {
            handler.accept(root);
        } catch (Exception ex) {
            log.error("Failed to process {} event eventId={}: {}", eventType, eventId, ex.getMessage(), ex);
        }

        log.info("Processed {} event eventId={}", eventType, eventId);
    }

    private OrderStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown order status value: {}", value);
            return null;
        }
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid event payload JSON", ex);
        }
    }

    private String extractEventId(JsonNode root) {
        JsonNode node = root.get("eventId");
        if (node == null || node.isNull()) return null;
        String id = node.asText();
        return (id == null || id.isBlank()) ? null : id.trim();
    }
}
