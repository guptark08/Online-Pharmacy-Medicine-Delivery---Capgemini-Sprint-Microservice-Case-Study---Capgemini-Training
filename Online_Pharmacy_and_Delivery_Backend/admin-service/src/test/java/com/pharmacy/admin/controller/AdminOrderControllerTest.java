package com.pharmacy.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.pharmacy.admin.config.SecurityConfig;
import com.pharmacy.admin.dto.request.OrderStatusUpdateDto;
import com.pharmacy.admin.dto.response.OrderResponseDto;
import com.pharmacy.admin.exception.GlobalExceptionHandler;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.security.JwtUtil;
import com.pharmacy.admin.service.AdminOrderService;
import com.pharmacy.admin.service.AdminPrescriptionService;

@WebMvcTest(AdminOrderController.class)
@Import({ GlobalExceptionHandler.class, SecurityConfig.class })
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminOrderService orderService;

    @MockBean
    private AdminPrescriptionService prescriptionService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_adminRole_returns200() throws Exception {
        when(orderService.updateOrderStatus(eq(1L), any(OrderStatusUpdateDto.class)))
                .thenReturn(orderResponse(1L, "PACKED"));

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"PACKED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateOrderStatus_customerRole_returns403() throws Exception {
        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"PACKED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_orderNotFound_returns404() throws Exception {
        when(orderService.updateOrderStatus(eq(1L), any(OrderStatusUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Order", 1L));

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"PACKED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_adminRole_returns200WithList() throws Exception {
        when(orderService.getAllOrders(0, 20)).thenReturn(List.of(orderResponse(1L, "PAID")));

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelOrder_adminRole_returns200() throws Exception {
        when(orderService.cancelOrder(1L, "OutOfStock")).thenReturn(orderResponse(1L, "ADMIN_CANCELLED"));

        mockMvc.perform(put("/api/admin/orders/1/cancel")
                        .param("reason", "OutOfStock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private OrderResponseDto orderResponse(Long id, String status) {
        return OrderResponseDto.builder()
                .id(id)
                .status(status)
                .deliveryAddress("FC Road, Pune")
                .deliverySlot("9AM-12PM")
                .totalAmount(250.0)
                .build();
    }
}
