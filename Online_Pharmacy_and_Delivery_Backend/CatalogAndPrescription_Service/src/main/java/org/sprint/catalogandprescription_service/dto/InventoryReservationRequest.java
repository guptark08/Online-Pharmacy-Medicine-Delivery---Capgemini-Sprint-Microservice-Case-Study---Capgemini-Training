package org.sprint.catalogandprescription_service.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationRequest {

    private Long orderId;

    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<LineItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItem {

        @NotNull(message = "Medicine ID is required")
        private Long medicineId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
