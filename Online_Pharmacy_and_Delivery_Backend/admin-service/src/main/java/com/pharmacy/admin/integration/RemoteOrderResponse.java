package com.pharmacy.admin.integration;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class RemoteOrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private java.time.LocalDateTime createdAt;
    private List<RemoteOrderItemResponse> items;

    @Data
    public static class RemoteOrderItemResponse {
        private Long medicineId;
        private String medicineName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
