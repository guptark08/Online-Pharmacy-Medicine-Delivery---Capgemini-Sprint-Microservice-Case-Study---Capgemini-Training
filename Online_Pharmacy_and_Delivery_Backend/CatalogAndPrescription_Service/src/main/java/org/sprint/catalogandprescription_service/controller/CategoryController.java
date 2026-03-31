package org.sprint.catalogandprescription_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sprint.catalogandprescription_service.dto.ApiResponse;
import org.sprint.catalogandprescription_service.dto.CategoryDTO;
import org.sprint.catalogandprescription_service.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "Browse medicine categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all active categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getActiveCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category fetched successfully", category));
    }
}
