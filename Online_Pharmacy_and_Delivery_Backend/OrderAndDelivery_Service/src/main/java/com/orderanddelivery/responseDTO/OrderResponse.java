package com.orderanddelivery.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.orderanddelivery.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private OrderStatus status;
    private String deliveryAddress;
    private String deliverySlot;
    private String pincode;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private String paymentId;
    private LocalDateTime createdAt;
    private Long prescriptionId;
    private List<OrderItemResponse> items;

    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long medicineId;
        private String medicineName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
