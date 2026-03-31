package org.sprint.catalogandprescription_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationResponse {

    private Long orderId;
    private int reservedLines;
    private int totalUnitsReserved;
}
