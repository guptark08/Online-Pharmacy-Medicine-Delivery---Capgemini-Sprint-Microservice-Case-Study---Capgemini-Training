package com.pharmacy.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String medicineStrength;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
