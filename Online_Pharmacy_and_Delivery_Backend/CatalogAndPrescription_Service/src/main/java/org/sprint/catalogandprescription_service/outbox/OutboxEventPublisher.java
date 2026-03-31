package org.sprint.catalogandprescription_service.outbox;

import java.time.LocalDateTime;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${pharmacy.events.outbox.max-attempts:10}")
    private int maxAttempts;

    @Value("${pharmacy.events.outbox.retry-delay-seconds:15}")
    private long retryDelaySeconds;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishOutboxEvent(Long outboxEventId) {
        OutboxEvent event = outboxEventRepository.findByIdForUpdate(outboxEventId)
                .orElse(null);

        if (event == null || event.getStatus() != OutboxEventStatus.PENDING) {
            return;
        }

        if (event.getNextAttemptAt() != null && event.getNextAttemptAt().isAfter(LocalDateTime.now())) {
            return;
        }

        int attemptNumber = (event.getAttempts() == null ? 0 : event.getAttempts()) + 1;

        try {
            rabbitTemplate.convertAndSend(event.getExchangeName(), event.getRoutingKey(), event.getPayload(), message -> {
                MessageProperties properties = message.getMessageProperties();
                properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                if (event.getEventId() != null && !event.getEventId().isBlank()) {
                    properties.setMessageId(event.getEventId());
                }
                return message;
            });

            event.setAttempts(attemptNumber);
            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setNextAttemptAt(LocalDateTime.now());
            event.setLastError(null);

            outboxEventRepository.save(event);
            log.debug("Published outbox event id={} routingKey={} attempts={}",
                    event.getId(), event.getRoutingKey(), attemptNumber);

        } catch (Exception ex) {
            event.setAttempts(attemptNumber);
            event.setLastError(trimError(ex.getMessage()));

            int safeMaxAttempts = Math.max(maxAttempts, 1);
            if (attemptNumber >= safeMaxAttempts) {
                event.setStatus(OutboxEventStatus.FAILED);
                event.setNextAttemptAt(LocalDateTime.now());
                log.error("Outbox event id={} permanently failed after {} attempts", event.getId(), attemptNumber, ex);
            } else {
                long safeDelay = Math.max(retryDelaySeconds, 1L);
                event.setStatus(OutboxEventStatus.PENDING);
                event.setNextAttemptAt(LocalDateTime.now().plusSeconds(safeDelay * attemptNumber));
                log.warn("Outbox publish failed for id={} attempt={} (will retry)",
                        event.getId(), attemptNumber, ex);
            }

            outboxEventRepository.save(event);
        }
    }

    private String trimError(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown publish failure";
        }
        String normalized = message.trim();
        return normalized.length() <= 1000 ? normalized : normalized.substring(0, 1000);
    }
}