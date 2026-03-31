package com.pharmacy.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 500)
    private String imageUrl;
}
