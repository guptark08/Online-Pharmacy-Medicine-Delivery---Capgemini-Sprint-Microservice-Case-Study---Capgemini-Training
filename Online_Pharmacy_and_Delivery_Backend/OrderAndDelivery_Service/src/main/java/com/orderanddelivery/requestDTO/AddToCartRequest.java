package com.orderanddelivery.requestDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Max 10 units per item")
    private int quantity;

    private boolean substituteAllowed;
}