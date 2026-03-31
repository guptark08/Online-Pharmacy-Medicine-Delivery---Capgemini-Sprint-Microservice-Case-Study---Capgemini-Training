package com.pharmacy.admin.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InventoryRequestDto {

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotBlank(message = "Batch number is required")
    @Size(max = 100)
    private String batchNumber;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Double costPrice;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Format: yyyy-MM-dd")
    private String expiryDate;

    @NotBlank(message = "Manufacture date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Format: yyyy-MM-dd")
    private String manufactureDate;

    @Size(max = 200)
    private String supplier;
}
