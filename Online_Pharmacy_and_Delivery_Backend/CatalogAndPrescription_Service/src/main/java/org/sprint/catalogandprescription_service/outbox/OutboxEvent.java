package org.sprint.catalogandprescription_service.outbox;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status_next_attempt", columnList = "status,nextAttemptAt"),
        @Index(name = "idx_outbox_event_id", columnList = "eventId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String eventId;

    @Column(nullable = false, length = 180)
    private String exchangeName;

    @Column(nullable = false, length = 180)
    private String routingKey;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(length = 1000)
    private String lastError;

    private LocalDateTime publishedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) {
            status = OutboxEventStatus.PENDING;
        }
        if (attempts == null) {
            attempts = 0;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (nextAttemptAt == null) {
            nextAttemptAt = now;
        }
    }
}
