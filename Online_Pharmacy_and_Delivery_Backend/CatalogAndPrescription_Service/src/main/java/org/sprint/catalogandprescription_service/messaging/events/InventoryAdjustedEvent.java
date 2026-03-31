package org.sprint.catalogandprescription_service.messaging.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustedEvent {

    private String eventId;
    private String occurredAt;

    private Long medicineId;
    private String medicineName;
    private Long categoryId;

    private Integer previousStock;
    private Integer currentStock;

    private String adjustmentType;
    private String reason;

    private Boolean active;
}
