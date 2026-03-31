package com.orderanddelivery.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.orderanddelivery.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private String deliveryAddress;
    private String deliverySlot;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    @Data
    public static class OrderItemResponse {
        private Long medicineId;
        private String medicineName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
