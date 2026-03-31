package com.orderanddelivery.responseDTO;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CartResponse {
    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal subtotal;
    private boolean hasRxItems;

    @Data
    public static class CartItemResponse {
        private Long id;
        private Long medicineId;
        private String medicineName;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal lineTotal;
        private boolean requiresPrescription;
        private boolean substituteAllowed;
    }
}