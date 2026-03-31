package com.orderanddelivery.integration;

import java.util.List;

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
    private List<LineItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItem {
        private Long medicineId;
        private Integer quantity;
    }
}
