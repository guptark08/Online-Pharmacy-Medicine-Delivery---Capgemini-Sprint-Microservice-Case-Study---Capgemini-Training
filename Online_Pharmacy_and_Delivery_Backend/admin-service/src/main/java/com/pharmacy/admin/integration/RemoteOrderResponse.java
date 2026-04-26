package com.pharmacy.admin.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class RemoteOrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal deliveryCharge;
    private String deliveryAddress;
    private String deliverySlot;
    private String pincode;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long prescriptionId;
    private String paymentMethod;
    private String paymentId;
    private String adminNote;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;
    private List<RemoteOrderItemResponse> items;

    @Data
    public static class RemoteOrderItemResponse {
        private Long id;
        private Long medicineId;
        private String medicineName;
        private String medicineStrength;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}