package org.sprint.catalogandprescription_service.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.catalogandprescription_service.dto.CategoryDTO;
import org.sprint.catalogandprescription_service.entities.Category;
import org.sprint.catalogandprescription_service.globalexception.ResourceNotFoundException;
import org.sprint.catalogandprescription_service.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDto)
                .toList();
    }

    public CategoryDTO getActiveCategoryById(Long id) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return toDto(category);
    }

    private CategoryDTO toDto(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .isActive(category.getIsActive())
                .build();
    }
}
