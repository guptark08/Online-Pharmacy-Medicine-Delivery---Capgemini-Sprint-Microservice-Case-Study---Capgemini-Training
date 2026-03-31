package com.pharmacy.admin.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MedicineRequestDto {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @Size(max = 200)
    private String genericName;

    @Size(max = 200)
    private String manufacturer;

    @Size(max = 50)
    private String dosageForm;       // tablet, syrup, injection, capsule

    @Size(max = 50)
    private String strength;         // 500mg, 10ml

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @DecimalMin(value = "0.0")
    private Double mrp;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private boolean requiresPrescription;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 500)
    private String imageUrl;

    private String description;

    // Format: yyyy-MM-dd  e.g. "2026-12-31"
    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Expiry date must be in format yyyy-MM-dd")
    private String expiryDate;

    @Size(max = 50)
    private String sku;
}
