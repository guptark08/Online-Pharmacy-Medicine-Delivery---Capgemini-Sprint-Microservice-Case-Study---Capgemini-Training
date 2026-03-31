package com.pharmacy.admin.messaging;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.entity.ProcessedEvent;
import com.pharmacy.admin.repository.ProcessedEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public boolean markIfFirstProcessing(String eventId, String eventType) {
        if (eventId == null || eventId.isBlank()) {
            return true;
        }

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(eventId.trim())
                .eventType(eventType)
                .processedAt(LocalDateTime.now())
                .build();

        try {
            processedEventRepository.saveAndFlush(processedEvent);
            return true;
        } catch (DataIntegrityViolationException ex) {
            log.info("Duplicate event ignored eventId={} eventType={}", eventId, eventType);
            return false;
        }
    }
}