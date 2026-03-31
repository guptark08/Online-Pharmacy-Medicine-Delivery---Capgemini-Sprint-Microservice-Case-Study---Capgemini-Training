package com.pharmacy.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pharmacy.admin.dto.request.CategoryRequestDto;
import com.pharmacy.admin.dto.response.CategoryResponseDto;
import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.exception.DuplicateResourceException;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AdminCategoryService adminCategoryService;

    @Test
    void createCategory_validInput_savesAndReturnsDto() {
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName("Pain Relief");
        request.setDescription("Pain medicines");

        Category saved = Category.builder()
                .id(1L)
                .name("Pain Relief")
                .description("Pain medicines")
                .isActive(true)
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Pain Relief")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponseDto response = adminCategoryService.createCategory(request);

        assertEquals("Pain Relief", response.getName());
        assertTrue(response.isActive());
    }

    @Test
    void createCategory_duplicateName_throwsDuplicateResource() {
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName("Pain Relief");

        when(categoryRepository.existsByNameIgnoreCase("Pain Relief")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> adminCategoryService.createCategory(request));
    }

    @Test
    void getCategoryById_notFound_throwsResourceNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminCategoryService.getCategoryById(99L));
    }

    @Test
    void updateCategory_sameNameNoConflict_updatesSuccessfully() {
        Category existing = Category.builder()
                .id(1L)
                .name("Pain Relief")
                .description("Old desc")
                .isActive(true)
                .build();

        CategoryRequestDto request = new CategoryRequestDto();
        request.setName("Pain Relief");
        request.setDescription("Updated desc");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponseDto response = adminCategoryService.updateCategory(1L, request);

        assertEquals("Pain Relief", response.getName());
        assertEquals("Updated desc", response.getDescription());
    }

    @Test
    void deleteCategory_exists_setsInactive() {
        Category existing = Category.builder()
                .id(1L)
                .name("Pain Relief")
                .isActive(true)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminCategoryService.deleteCategory(1L);

        assertFalse(existing.isActive());
        verify(categoryRepository).save(existing);
    }
}
