package org.sprint.catalogandprescription_service.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherWorker {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Value("${pharmacy.events.rabbit.enabled:true}")
    private boolean rabbitEnabled;

    @Value("${pharmacy.events.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${pharmacy.events.outbox.fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        if (!rabbitEnabled) {
            return;
        }

        int safeBatchSize = Math.max(batchSize, 1);
        List<Long> eventIds = outboxEventRepository.findIdsReadyForDispatch(
                OutboxEventStatus.PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, safeBatchSize));

        if (eventIds.isEmpty()) {
            return;
        }

        log.debug("Outbox worker picked {} event(s) for publishing", eventIds.size());
        for (Long eventId : eventIds) {
            outboxEventPublisher.publishOutboxEvent(eventId);
        }
    }
}