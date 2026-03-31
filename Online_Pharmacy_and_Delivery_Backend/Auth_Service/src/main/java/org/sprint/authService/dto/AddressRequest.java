package org.sprint.authService.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Street address cannot be empty")
    @Size(max = 255, message = "Street address is too long")
    private String street_address;

    @NotBlank(message = "City is required")
    @Size(max = 80, message = "City is too long")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 80, message = "State is too long")
    private String state;

    @NotNull(message = "Pincode is required")
    @Min(value = 100000, message = "Pincode must be at least 6 digits")
    @Max(value = 999999, message = "Pincode cannot exceed 6 digits")
    private Integer pincode;

    private boolean isDefault;
}
