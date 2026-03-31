package org.sprint.catalogandprescription_service.messaging.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionReviewedEvent {

    private String eventId;
    private String occurredAt;

    private Long prescriptionId;
    private Long customerId;

    private String previousStatus;
    private String newStatus;

    private Long reviewedBy;
    private String reviewedAt;
    private String reviewNotes;
}
