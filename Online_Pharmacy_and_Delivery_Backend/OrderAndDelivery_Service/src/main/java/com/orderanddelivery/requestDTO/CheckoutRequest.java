package com.orderanddelivery.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CheckoutRequest {

    @Positive(message = "Address ID must be positive")
    private Long addressId;

    @Valid
    private UserAddressRequest newAddress;

    @NotBlank(message = "Delivery slot is required")
    private String deliverySlot;

    @Positive(message = "Prescription ID must be positive")
    private Long prescriptionId;
}
