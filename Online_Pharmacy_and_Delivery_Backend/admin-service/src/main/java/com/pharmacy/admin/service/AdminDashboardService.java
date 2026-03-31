package com.pharmacy.admin.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.response.DashboardResponseDto;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.integration.CatalogAdminStatsRemoteDTO;
import com.pharmacy.admin.integration.CrossServiceAnalyticsClient;
import com.pharmacy.admin.integration.RemoteOrderResponse;
import com.pharmacy.admin.repository.MedicineRepository;
import com.pharmacy.admin.repository.OrderRepository;
import com.pharmacy.admin.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardService.class);

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int RECENT_ORDERS_LIMIT = 10;

    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final CrossServiceAnalyticsClient crossServiceAnalyticsClient;

    @Transactional(readOnly = true)
    public DashboardResponseDto getDashboardData() {
        log.debug("Building dashboard data");

        Optional<List<RemoteOrderResponse>> remoteOrders = crossServiceAnalyticsClient.fetchAdminOrders();
        Optional<CatalogAdminStatsRemoteDTO> remoteCatalogStats = crossServiceAnalyticsClient.fetchCatalogStats();

        if (remoteOrders.isPresent() && remoteCatalogStats.isPresent()) {
            return buildFromRemote(remoteOrders.get(), remoteCatalogStats.get());
        }

        return buildFromLocal();
    }

    private DashboardResponseDto buildFromRemote(List<RemoteOrderResponse> orders, CatalogAdminStatsRemoteDTO catalogStats) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIDNIGHT);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);
        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIDNIGHT);

        long totalOrders = orders.size();
        long pendingPayment = countStatus(orders, "PAYMENT_PENDING");
        long packed = countStatus(orders, "PACKED");
        long outForDelivery = countStatus(orders, "OUT_FOR_DELIVERY");
        long delivered = countStatus(orders, "DELIVERED");
        long cancelled = countStatus(orders, "ADMIN_CANCELLED") + countStatus(orders, "CUSTOMER_CANCELLED");

        BigDecimal totalRevenue = sumDeliveredRevenue(orders);
        BigDecimal revenueToday = sumDeliveredRevenueBetween(orders, todayStart, todayEnd);
        BigDecimal revenueThisMonth = sumDeliveredRevenueBetween(orders, monthStart, todayEnd);

        List<DashboardResponseDto.LowStockAlert> lowStockAlerts = safeList(catalogStats.getLowStockItems())
                .stream()
                .map(item -> DashboardResponseDto.LowStockAlert.builder()
                        .medicineId(item.getId())
                        .medicineName(item.getName())
                        .currentStock(item.getStock() == null ? 0 : item.getStock())
                        .build())
                .toList();

        List<DashboardResponseDto.ExpiryAlert> expiryAlerts = safeList(catalogStats.getExpiringItems())
                .stream()
                .map(item -> DashboardResponseDto.ExpiryAlert.builder()
                        .medicineId(item.getId())
                        .medicineName(item.getName())
                        .expiryDate(item.getExpiryDate())
                        .stock(item.getStock() == null ? 0 : item.getStock())
                        .build())
                .toList();

        List<DashboardResponseDto.RecentOrder> recentOrderDtos = orders.stream()
                .sorted(Comparator.comparing(RemoteOrderResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECENT_ORDERS_LIMIT)
                .map(order -> DashboardResponseDto.RecentOrder.builder()
                        .orderId(order.getId())
                        .userEmail("N/A")
                        .status(order.getStatus())
                        .totalAmount(toDouble(order.getTotalAmount()))
                        .createdAt(order.getCreatedAt() == null ? null : order.getCreatedAt().toString())
                        .build())
                .toList();

        return DashboardResponseDto.builder()
                .totalOrders(totalOrders)
                .pendingPaymentOrders(pendingPayment)
                .packedOrders(packed)
                .outForDeliveryOrders(outForDelivery)
                .deliveredOrders(delivered)
                .cancelledOrders(cancelled)
                .pendingPrescriptions(catalogStats.getPendingPrescriptions())
                .approvedPrescriptions(catalogStats.getApprovedPrescriptions())
                .rejectedPrescriptions(catalogStats.getRejectedPrescriptions())
                .totalMedicines(catalogStats.getTotalMedicines())
                .activeMedicines(catalogStats.getActiveMedicines())
                .lowStockCount(catalogStats.getLowStockCount())
                .expiringThisMonthCount(catalogStats.getExpiringIn30DaysCount())
                .totalRevenue(toDouble(totalRevenue))
                .revenueThisMonth(toDouble(revenueThisMonth))
                .revenueToday(toDouble(revenueToday))
                .lowStockAlerts(lowStockAlerts)
                .expiryAlerts(expiryAlerts)
                .recentOrders(recentOrderDtos)
                .build();
    }

    private DashboardResponseDto buildFromLocal() {
        long totalOrders = orderRepository.count();
        long pendingPayment = orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING);
        long packed = orderRepository.countByStatus(OrderStatus.PACKED);
        long outForDelivery = orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY);
        long delivered = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long adminCancelled = orderRepository.countByStatus(OrderStatus.ADMIN_CANCELLED);
        long customerCancelled = orderRepository.countByStatus(OrderStatus.CUSTOMER_CANCELLED);

        long pendingPrescriptions = prescriptionRepository.countByStatus(PrescriptionStatus.PENDING);
        long approvedPrescriptions = prescriptionRepository.countByStatus(PrescriptionStatus.APPROVED);
        long rejectedPrescriptions = prescriptionRepository.countByStatus(PrescriptionStatus.REJECTED);

        long totalMedicines = medicineRepository.count();
        long activeMedicines = medicineRepository.countByIsActiveTrue();
        long lowStockCount = medicineRepository.countLowStockMedicines(LOW_STOCK_THRESHOLD);

        LocalDate today = LocalDate.now();
        LocalDate oneMonthAhead = today.plusDays(30);

        List<Medicine> expiringMedicines = medicineRepository.findMedicinesExpiringBetween(today, oneMonthAhead);
        long expiringCount = expiringMedicines.size();

        BigDecimal totalRevenue = BigDecimal.valueOf(orderRepository.getTotalRevenue());

        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIDNIGHT);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);
        BigDecimal revenueToday = BigDecimal.valueOf(orderRepository.getRevenueForPeriod(todayStart, todayEnd));

        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIDNIGHT);
        BigDecimal revenueThisMonth = BigDecimal.valueOf(orderRepository.getRevenueForPeriod(monthStart, todayEnd));

        List<Medicine> lowStockMedicines = medicineRepository.findLowStockMedicines(LOW_STOCK_THRESHOLD);

        List<DashboardResponseDto.LowStockAlert> lowStockAlerts = lowStockMedicines.stream()
                .map(medicine -> DashboardResponseDto.LowStockAlert.builder()
                        .medicineId(medicine.getId())
                        .medicineName(medicine.getName())
                        .currentStock(medicine.getStock())
                        .build())
                .toList();

        List<DashboardResponseDto.ExpiryAlert> expiryAlerts = expiringMedicines.stream()
                .map(medicine -> DashboardResponseDto.ExpiryAlert.builder()
                        .medicineId(medicine.getId())
                        .medicineName(medicine.getName())
                        .expiryDate(medicine.getExpiryDate() == null ? null : medicine.getExpiryDate().toString())
                        .stock(medicine.getStock())
                        .build())
                .toList();

        List<Order> recentOrders = orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_ORDERS_LIMIT))
                .getContent();

        List<DashboardResponseDto.RecentOrder> recentOrderDtos = recentOrders.stream()
                .map(order -> DashboardResponseDto.RecentOrder.builder()
                        .orderId(order.getId())
                        .userEmail(order.getUserEmail())
                        .status(order.getStatus().name())
                        .totalAmount(order.getTotalAmount())
                        .createdAt(order.getCreatedAt() == null ? null : order.getCreatedAt().toString())
                        .build())
                .toList();

        return DashboardResponseDto.builder()
                .totalOrders(totalOrders)
                .pendingPaymentOrders(pendingPayment)
                .packedOrders(packed)
                .outForDeliveryOrders(outForDelivery)
                .deliveredOrders(delivered)
                .cancelledOrders(adminCancelled + customerCancelled)
                .pendingPrescriptions(pendingPrescriptions)
                .approvedPrescriptions(approvedPrescriptions)
                .rejectedPrescriptions(rejectedPrescriptions)
                .totalMedicines(totalMedicines)
                .activeMedicines(activeMedicines)
                .lowStockCount(lowStockCount)
                .expiringThisMonthCount(expiringCount)
                .totalRevenue(toDouble(totalRevenue))
                .revenueThisMonth(toDouble(revenueThisMonth))
                .revenueToday(toDouble(revenueToday))
                .lowStockAlerts(lowStockAlerts)
                .expiryAlerts(expiryAlerts)
                .recentOrders(recentOrderDtos)
                .build();
    }

    private long countStatus(List<RemoteOrderResponse> orders, String expectedStatus) {
        return orders.stream()
                .map(RemoteOrderResponse::getStatus)
                .filter(status -> status != null && status.toUpperCase(Locale.ROOT).equals(expectedStatus))
                .count();
    }

    private BigDecimal sumDeliveredRevenue(List<RemoteOrderResponse> orders) {
        return orders.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(order.getStatus()))
                .map(RemoteOrderResponse::getTotalAmount)
                .map(this::toBigDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDeliveredRevenueBetween(List<RemoteOrderResponse> orders, LocalDateTime start, LocalDateTime end) {
        return orders.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(order.getStatus()))
                .filter(order -> order.getCreatedAt() != null
                        && !order.getCreatedAt().isBefore(start)
                        && !order.getCreatedAt().isAfter(end))
                .map(RemoteOrderResponse::getTotalAmount)
                .map(this::toBigDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CatalogAdminStatsRemoteDTO.MedicineInventoryItem> safeList(
            List<CatalogAdminStatsRemoteDTO.MedicineInventoryItem> items) {
        return items == null ? List.of() : items;
    }

    private BigDecimal toBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}
