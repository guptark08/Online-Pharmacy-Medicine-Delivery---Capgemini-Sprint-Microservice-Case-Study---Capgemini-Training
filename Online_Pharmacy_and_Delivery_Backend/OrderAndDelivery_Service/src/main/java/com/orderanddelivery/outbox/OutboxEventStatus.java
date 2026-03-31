package com.orderanddelivery.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}