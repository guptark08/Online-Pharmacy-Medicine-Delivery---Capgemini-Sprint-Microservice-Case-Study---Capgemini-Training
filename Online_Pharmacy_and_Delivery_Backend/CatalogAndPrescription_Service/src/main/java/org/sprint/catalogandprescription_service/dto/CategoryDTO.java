package org.sprint.catalogandprescription_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

	private Long id;

	@NotBlank(message = "Category name is required")
	@Size(min = 2, max = 100)
	private String name;

	private String description;

	private String iconUrl;

	private Boolean isActive;
}