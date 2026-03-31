package org.sprint.catalogandprescription_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineDTO {

	private Long id; // Not required on request, returned in response

	@NotBlank(message = "Medicine name is required")
	@Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
	private String name;

	@NotBlank(message = "Manufacturer is required")
	private String manufacturer;

	private String description;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.01", message = "Price must be greater than 0")
	private BigDecimal price;

	private BigDecimal discountedPrice;

	@NotNull(message = "Stock is required")
	@Min(value = 0, message = "Stock cannot be negative")
	private Integer stock;

	private Boolean requiresPrescription;

	private String dosageInfo;

	private String sideEffects;

	private String imageUrl;

	private LocalDate expiryDate;

	private String status; // "AVAILABLE", "OUT_OF_STOCK"

	@NotNull(message = "Category ID is required")
	private Long categoryId;

	private String categoryName; // For response — flattened for simplicity
}