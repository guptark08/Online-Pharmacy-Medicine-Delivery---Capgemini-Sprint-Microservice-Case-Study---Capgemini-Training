package com.pharmacy.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    @Column(nullable = false, updatable = false, length = 120)
    private String eventId;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}