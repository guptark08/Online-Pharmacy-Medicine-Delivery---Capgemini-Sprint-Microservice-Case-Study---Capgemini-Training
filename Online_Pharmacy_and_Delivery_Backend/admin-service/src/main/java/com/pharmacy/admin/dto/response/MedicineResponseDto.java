package com.pharmacy.admin.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponseDto {

    private Long id;
    private String name;
    private String genericName;
    private String manufacturer;
    private String dosageForm;
    private String strength;
    private double price;
    private double mrp;
    private int stock;
    private boolean requiresPrescription;
    private boolean isActive;
    private String expiryDate;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private String description;
    private String sku;
    private String createdAt;
    private String updatedAt;
}
