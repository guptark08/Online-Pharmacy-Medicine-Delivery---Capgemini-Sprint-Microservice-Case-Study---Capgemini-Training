package org.sprint.catalogandprescription_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.sprint.catalogandprescription_service.config.JwtFilter;
import org.sprint.catalogandprescription_service.config.SecurityConfig;
import org.sprint.catalogandprescription_service.dto.MedicineDTO;
import org.sprint.catalogandprescription_service.globalexception.GlobalExceptionHandler;
import org.sprint.catalogandprescription_service.globalexception.ResourceNotFoundException;
import org.sprint.catalogandprescription_service.service.MedicineService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@WebMvcTest(MedicineController.class)
@Import({ GlobalExceptionHandler.class, SecurityConfig.class })
class MedicineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicineService medicineService;

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
    void getMedicines_publicEndpoint_returns200WithoutAuth() throws Exception {
        when(medicineService.getAllMedicines(anyString(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(new PageImpl<>(List.of(buildMedicineDto())));

        mockMvc.perform(get("/api/catalog/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMedicineById_notFound_returns404() throws Exception {
        when(medicineService.getMedicineById(99L))
                .thenThrow(new ResourceNotFoundException("Medicine not found with id: 99"));

        mockMvc.perform(get("/api/catalog/medicines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createMedicine_adminRole_returns201() throws Exception {
        when(medicineService.createMedicine(any(MedicineDTO.class))).thenReturn(buildMedicineDto());

        mockMvc.perform(post("/api/catalog/medicines")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateMedicineJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void createMedicine_customerRole_returns403() throws Exception {
        mockMvc.perform(post("/api/catalog/medicines")
                        .with(user("alice").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateMedicineJson()))
                .andExpect(status().isForbidden());
    }

    private MedicineDTO buildMedicineDto() {
        return MedicineDTO.builder()
                .id(1L)
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .description("Pain relief")
                .price(new BigDecimal("25.00"))
                .stock(100)
                .requiresPrescription(false)
                .expiryDate(LocalDate.now().plusYears(1))
                .status("AVAILABLE")
                .categoryId(1L)
                .categoryName("Pain Relief")
                .build();
    }

    private String validCreateMedicineJson() {
        return "{"
                + "\"name\":\"Paracetamol\","
                + "\"manufacturer\":\"ABC Pharma\","
                + "\"description\":\"Pain relief\","
                + "\"price\":25.00,"
                + "\"stock\":100,"
                + "\"requiresPrescription\":false,"
                + "\"expiryDate\":\"2030-01-01\","
                + "\"status\":\"AVAILABLE\","
                + "\"categoryId\":1"
                + "}";
    }
}

