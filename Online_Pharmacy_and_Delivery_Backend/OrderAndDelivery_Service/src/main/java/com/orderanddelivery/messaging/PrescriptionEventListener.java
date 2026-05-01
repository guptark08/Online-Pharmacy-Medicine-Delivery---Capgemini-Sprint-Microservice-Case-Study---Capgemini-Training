package com.orderanddelivery.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderanddelivery.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class PrescriptionEventListener {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @RabbitListener(queues = "${pharmacy.events.queues.prescription-reviewed:pharmacy.order.prescription-reviewed}")
    public void onPrescriptionReviewed(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode rxIdNode = root.get("prescriptionId");
            JsonNode newStatusNode = root.get("newStatus");

            if (rxIdNode == null || rxIdNode.isNull() || newStatusNode == null || newStatusNode.isNull()) {
                log.warn("PrescriptionReviewed event missing required fields: {}", payload);
                return;
            }

            Long prescriptionId = rxIdNode.asLong();
            String newStatus = newStatusNode.asText();

            orderService.handlePrescriptionReviewed(prescriptionId, newStatus);
            log.info("Synced order status from prescription review: prescriptionId={} newStatus={}",
                    prescriptionId, newStatus);
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse PrescriptionReviewed event: {}", ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Failed to handle PrescriptionReviewed event: {}", ex.getMessage(), ex);
        }
    }
}
