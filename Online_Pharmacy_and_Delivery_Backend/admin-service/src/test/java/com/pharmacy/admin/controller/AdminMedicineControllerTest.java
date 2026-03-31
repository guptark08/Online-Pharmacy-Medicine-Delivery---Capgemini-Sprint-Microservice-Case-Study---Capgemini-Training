package com.pharmacy.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.admin.config.SecurityConfig;
import com.pharmacy.admin.dto.request.MedicineRequestDto;
import com.pharmacy.admin.dto.response.MedicineResponseDto;
import com.pharmacy.admin.exception.GlobalExceptionHandler;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.security.JwtUtil;
import com.pharmacy.admin.service.AdminCategoryService;
import com.pharmacy.admin.service.AdminMedicineService;

@WebMvcTest(AdminMedicineController.class)
@Import({ GlobalExceptionHandler.class, SecurityConfig.class })
@DisplayName("AdminMedicineController Tests")
class AdminMedicineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminMedicineService medicineService;

    @MockBean
    private AdminCategoryService categoryService;

    @MockBean
    private JwtUtil jwtUtil;

    private MedicineResponseDto buildResponse(Long id, String name) {
        return MedicineResponseDto.builder()
                .id(id)
                .name(name)
                .price(25.0)
                .stock(100)
                .categoryId(1L)
                .categoryName("Pain Relief")
                .isActive(true)
                .expiryDate("2027-01-01")
                .build();
    }

    private MedicineRequestDto buildRequest() {
        MedicineRequestDto dto = new MedicineRequestDto();
        dto.setName("Paracetamol");
        dto.setPrice(25.0);
        dto.setStock(100);
        dto.setCategoryId(1L);
        dto.setExpiryDate("2027-01-01");
        return dto;
    }

    @Test
    @DisplayName("GET /api/admin/medicines should return 200 with list")
    @WithMockUser(roles = "ADMIN")
    void getAllMedicines_returns200WithList() throws Exception {
        when(medicineService.getAllMedicines())
                .thenReturn(List.of(buildResponse(1L, "Paracetamol")));

        mockMvc.perform(get("/api/admin/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Paracetamol"));
    }

    @Test
    @DisplayName("GET /api/admin/medicines/{id} should return 200 when found")
    @WithMockUser(roles = "ADMIN")
    void getMedicineById_found_returns200() throws Exception {
        when(medicineService.getMedicineById(1L)).thenReturn(buildResponse(1L, "Paracetamol"));

        mockMvc.perform(get("/api/admin/medicines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Paracetamol"));
    }

    @Test
    @DisplayName("GET /api/admin/medicines/{id} should return 404 when not found")
    @WithMockUser(roles = "ADMIN")
    void getMedicineById_notFound_returns404() throws Exception {
        when(medicineService.getMedicineById(99L))
                .thenThrow(new ResourceNotFoundException("Medicine", 99L));

        mockMvc.perform(get("/api/admin/medicines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/admin/medicines should return 201 when valid")
    @WithMockUser(roles = "ADMIN")
    void addMedicine_validInput_returns201() throws Exception {
        when(medicineService.addMedicine(any())).thenReturn(buildResponse(1L, "Paracetamol"));

        mockMvc.perform(post("/api/admin/medicines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Paracetamol"));
    }

    @Test
    @DisplayName("POST /api/admin/medicines should return 400 when name is blank")
    @WithMockUser(roles = "ADMIN")
    void addMedicine_blankName_returns400() throws Exception {
        MedicineRequestDto dto = buildRequest();
        dto.setName("");

        mockMvc.perform(post("/api/admin/medicines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/admin/medicines should return 400 when price is zero")
    @WithMockUser(roles = "ADMIN")
    void addMedicine_zeroPrice_returns400() throws Exception {
        MedicineRequestDto dto = buildRequest();
        dto.setPrice(0.0);

        mockMvc.perform(post("/api/admin/medicines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/admin/medicines/{id} should return 200 when valid update")
    @WithMockUser(roles = "ADMIN")
    void updateMedicine_validInput_returns200() throws Exception {
        when(medicineService.updateMedicine(eq(1L), any()))
                .thenReturn(buildResponse(1L, "Paracetamol 500mg"));

        mockMvc.perform(put("/api/admin/medicines/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Paracetamol 500mg"));
    }

    @Test
    @DisplayName("DELETE /api/admin/medicines/{id} should return 200 after soft delete")
    @WithMockUser(roles = "ADMIN")
    void deleteMedicine_returns200() throws Exception {
        doNothing().when(medicineService).deleteMedicine(1L);

        mockMvc.perform(delete("/api/admin/medicines/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/admin/medicines/{id}/stock should return 200 with updated stock")
    @WithMockUser(roles = "ADMIN")
    void updateStock_returns200() throws Exception {
        MedicineResponseDto updated = buildResponse(1L, "Paracetamol");
        updated.setStock(50);
        when(medicineService.updateStock(1L, 50)).thenReturn(updated);

        mockMvc.perform(patch("/api/admin/medicines/1/stock")
                        .with(csrf())
                        .param("stock", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(50));
    }

    @Test
    @DisplayName("GET /api/admin/medicines/alerts/low-stock should return 200 with alerts")
    @WithMockUser(roles = "ADMIN")
    void getLowStockMedicines_returns200() throws Exception {
        when(medicineService.getLowStockMedicines())
                .thenReturn(List.of(buildResponse(2L, "Aspirin")));

        mockMvc.perform(get("/api/admin/medicines/alerts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Aspirin"));
    }

    @Test
    @DisplayName("Should return 403 when user does not have ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void getAllMedicines_nonAdminUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/medicines"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when no authentication provided")
    void getAllMedicines_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/medicines"))
                .andExpect(status().isUnauthorized());
    }
}
