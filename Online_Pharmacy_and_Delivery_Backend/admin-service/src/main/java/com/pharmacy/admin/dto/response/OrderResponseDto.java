package com.pharmacy.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String status;
    private double totalAmount;
    private double discountAmount;
    private double taxAmount;
    private double deliveryCharge;
    private List<OrderItemResponseDto> items;
    private String deliveryAddress;
    private String pincode;
    private String deliverySlot;
    private Long prescriptionId;
    private String adminNote;
    private String paymentMethod;
    private String paymentId;
    private String createdAt;
    private String updatedAt;
    private String deliveredAt;
}
