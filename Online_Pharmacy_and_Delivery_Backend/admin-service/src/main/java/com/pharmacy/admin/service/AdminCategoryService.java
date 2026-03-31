package com.pharmacy.admin.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.request.CategoryRequestDto;
import com.pharmacy.admin.dto.response.CategoryResponseDto;
import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.exception.DuplicateResourceException;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private static final Logger log = LoggerFactory.getLogger(AdminCategoryService.class);

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        String normalizedName = normalizeRequired(dto.getName(), "Category name is required");

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Category already exists with name: " + normalizedName);
        }

        Category category = Category.builder()
                .name(normalizedName)
                .description(normalizeOptional(dto.getDescription()))
                .imageUrl(normalizeOptional(dto.getImageUrl()))
                .isActive(true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category id={} name={}", saved.getId(), saved.getName());
        return mapToDto(saved);
    }

    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::mapToDto).toList();
    }

    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream().map(this::mapToDto).toList();
    }

    public CategoryResponseDto getCategoryById(Long id) {
        return mapToDto(findOrThrow(id));
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        Category category = findOrThrow(id);

        String normalizedName = normalizeRequired(dto.getName(), "Category name is required");

        if (!category.getName().equalsIgnoreCase(normalizedName)
                && categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Category already exists with name: " + normalizedName);
        }

        category.setName(normalizedName);
        category.setDescription(normalizeOptional(dto.getDescription()));
        category.setImageUrl(normalizeOptional(dto.getImageUrl()));

        Category saved = categoryRepository.save(category);
        log.info("Updated category id={}", saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findOrThrow(id);
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Soft deleted category id={}", id);
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }

    private CategoryResponseDto mapToDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt() == null ? null : category.getCreatedAt().toString())
                .build();
    }
}
