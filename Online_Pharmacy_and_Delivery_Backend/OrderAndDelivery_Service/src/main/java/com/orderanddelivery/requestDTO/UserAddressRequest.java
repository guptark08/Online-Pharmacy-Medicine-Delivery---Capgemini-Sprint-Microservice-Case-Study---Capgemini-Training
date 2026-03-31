package com.orderanddelivery.requestDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAddressRequest {

    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address is too long")
    private String streetAddress;

    @NotBlank(message = "City is required")
    @Size(max = 80, message = "City is too long")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 80, message = "State is too long")
    private String state;

    @Min(value = 100000, message = "Pincode must be 6 digits")
    @Max(value = 999999, message = "Pincode must be 6 digits")
    private Integer pincode;

    private Boolean isDefault = Boolean.FALSE;
}
