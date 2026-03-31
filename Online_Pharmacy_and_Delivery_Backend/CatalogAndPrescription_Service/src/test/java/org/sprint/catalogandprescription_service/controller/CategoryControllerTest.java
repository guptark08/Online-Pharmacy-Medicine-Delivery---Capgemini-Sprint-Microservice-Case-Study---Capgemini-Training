package org.sprint.catalogandprescription_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.sprint.catalogandprescription_service.config.JwtFilter;
import org.sprint.catalogandprescription_service.config.SecurityConfig;
import org.sprint.catalogandprescription_service.dto.CategoryDTO;
import org.sprint.catalogandprescription_service.globalexception.GlobalExceptionHandler;
import org.sprint.catalogandprescription_service.globalexception.ResourceNotFoundException;
import org.sprint.catalogandprescription_service.service.CategoryService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@WebMvcTest(CategoryController.class)
@Import({ GlobalExceptionHandler.class, SecurityConfig.class })
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void getCategories_publicEndpoint_returns200WithoutAuth() throws Exception {
        when(categoryService.getAllActiveCategories()).thenReturn(List.of(buildCategoryDto()));

        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Pain Relief"));
    }

    @Test
    void getCategoryById_notFound_returns404() throws Exception {
        when(categoryService.getActiveCategoryById(99L))
                .thenThrow(new ResourceNotFoundException("Category not found with id: 99"));

        mockMvc.perform(get("/api/catalog/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private CategoryDTO buildCategoryDto() {
        return CategoryDTO.builder()
                .id(1L)
                .name("Pain Relief")
                .description("Pain and fever medicines")
                .iconUrl("https://cdn.example.com/icons/pain-relief.png")
                .isActive(true)
                .build();
    }
}
