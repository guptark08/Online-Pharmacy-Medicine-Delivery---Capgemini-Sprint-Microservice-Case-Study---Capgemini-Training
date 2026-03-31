package org.sprint.catalogandprescription_service.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}