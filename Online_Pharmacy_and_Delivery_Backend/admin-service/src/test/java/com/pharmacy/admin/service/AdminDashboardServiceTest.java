package com.pharmacy.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.pharmacy.admin.dto.response.DashboardResponseDto;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.integration.CrossServiceAnalyticsClient;
import com.pharmacy.admin.repository.MedicineRepository;
import com.pharmacy.admin.repository.OrderRepository;
import com.pharmacy.admin.repository.PrescriptionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDashboardService Tests")
class AdminDashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private CrossServiceAnalyticsClient crossServiceAnalyticsClient;

    @InjectMocks
    private AdminDashboardService dashboardService;

    @BeforeEach
    void setUp() {
        when(crossServiceAnalyticsClient.fetchAdminOrders()).thenReturn(Optional.empty());
        when(crossServiceAnalyticsClient.fetchCatalogStats()).thenReturn(Optional.empty());

        when(orderRepository.count()).thenReturn(120L);
        when(orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING)).thenReturn(5L);
        when(orderRepository.countByStatus(OrderStatus.PACKED)).thenReturn(8L);
        when(orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY)).thenReturn(12L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(80L);
        when(orderRepository.countByStatus(OrderStatus.ADMIN_CANCELLED)).thenReturn(3L);
        when(orderRepository.countByStatus(OrderStatus.CUSTOMER_CANCELLED)).thenReturn(7L);

        when(prescriptionRepository.countByStatus(PrescriptionStatus.PENDING)).thenReturn(4L);
        when(prescriptionRepository.countByStatus(PrescriptionStatus.APPROVED)).thenReturn(60L);
        when(prescriptionRepository.countByStatus(PrescriptionStatus.REJECTED)).thenReturn(6L);

        when(medicineRepository.count()).thenReturn(200L);
        when(medicineRepository.countByIsActiveTrue()).thenReturn(190L);
        when(medicineRepository.countLowStockMedicines(10)).thenReturn(7L);

        when(orderRepository.getTotalRevenue()).thenReturn(150000.0);
        when(orderRepository.getRevenueForPeriod(any(), any())).thenReturn(12000.0);

        when(medicineRepository.findLowStockMedicines(10)).thenReturn(buildLowStockList());
        when(medicineRepository.findMedicinesExpiringBetween(any(), any())).thenReturn(buildExpiringList());

        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(buildRecentOrders()));
    }

    @Test
    @DisplayName("Should return non-null dashboard with all fields populated")
    void getDashboardData_returnsFullDashboard() {
        DashboardResponseDto result = dashboardService.getDashboardData();
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should aggregate order KPIs correctly")
    void getDashboardData_orderKpis_correct() {
        DashboardResponseDto result = dashboardService.getDashboardData();

        assertThat(result.getTotalOrders()).isEqualTo(120L);
        assertThat(result.getPendingPaymentOrders()).isEqualTo(5L);
        assertThat(result.getPackedOrders()).isEqualTo(8L);
        assertThat(result.getOutForDeliveryOrders()).isEqualTo(12L);
        assertThat(result.getDeliveredOrders()).isEqualTo(80L);
        assertThat(result.getCancelledOrders()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should aggregate prescription KPIs correctly")
    void getDashboardData_prescriptionKpis_correct() {
        DashboardResponseDto result = dashboardService.getDashboardData();

        assertThat(result.getPendingPrescriptions()).isEqualTo(4L);
        assertThat(result.getApprovedPrescriptions()).isEqualTo(60L);
        assertThat(result.getRejectedPrescriptions()).isEqualTo(6L);
    }

    @Test
    @DisplayName("Should aggregate inventory KPIs correctly")
    void getDashboardData_inventoryKpis_correct() {
        DashboardResponseDto result = dashboardService.getDashboardData();

        assertThat(result.getTotalMedicines()).isEqualTo(200L);
        assertThat(result.getActiveMedicines()).isEqualTo(190L);
        assertThat(result.getLowStockCount()).isEqualTo(7L);
        assertThat(result.getExpiringThisMonthCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should include revenue KPIs")
    void getDashboardData_revenueKpis_correct() {
        DashboardResponseDto result = dashboardService.getDashboardData();

        assertThat(result.getTotalRevenue()).isEqualTo(150000.0);
    }

    @Test
    @DisplayName("Should include low stock alert list")
    void getDashboardData_lowStockAlerts_populated() {
        DashboardResponseDto result = dashboardService.getDashboardData();

        assertThat(result.getLowStockAlerts()).isNotNull();
        assertThat(result.getLowStockAlerts()).hasSize(2);
        assertThat(result.getLowStockAlerts().get(0).getMedicineName()).isEqualTo("Aspirin");
    }

    @Test
    @DisplayName("Should include expiry alert list")
    void getDashboardData_expiryAlerts_populated() {
        DashboardResponseDto result = dashboardService.getDashboardData();

        assertThat(result.getExpiryAlerts()).isNotNull();
        assertThat(result.getExpiryAlerts()).hasSize(1);
    }

    @Test
    @DisplayName("Should include recent orders list")
    void getDashboardData_recentOrders_populated() {
        DashboardResponseDto result = dashboardService.getDashboardData();

        assertThat(result.getRecentOrders()).isNotNull();
        assertThat(result.getRecentOrders()).hasSize(2);
    }

    private List<Medicine> buildLowStockList() {
        Medicine first = Medicine.builder()
                .id(1L)
                .name("Aspirin")
                .stock(3)
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        Medicine second = Medicine.builder()
                .id(2L)
                .name("Ibuprofen")
                .stock(7)
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        return List.of(first, second);
    }

    private List<Medicine> buildExpiringList() {
        Medicine medicine = Medicine.builder()
                .id(3L)
                .name("Vitamin D")
                .stock(50)
                .expiryDate(LocalDate.now().plusDays(20))
                .build();

        return List.of(medicine);
    }

    private List<Order> buildRecentOrders() {
        Order first = Order.builder()
                .id(1L)
                .userEmail("a@a.com")
                .status(OrderStatus.PAID)
                .totalAmount(400.0)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        Order second = Order.builder()
                .id(2L)
                .userEmail("b@b.com")
                .status(OrderStatus.DELIVERED)
                .totalAmount(200.0)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        return List.of(first, second);
    }
}
