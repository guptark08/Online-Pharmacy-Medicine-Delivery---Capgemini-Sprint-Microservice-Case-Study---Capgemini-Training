package com.pharmacy.admin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pharmacy.admin.dto.response.InventoryReportDto;
import com.pharmacy.admin.dto.response.SalesReportDto;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.integration.CatalogAdminStatsRemoteDTO;
import com.pharmacy.admin.integration.CrossServiceAnalyticsClient;
import com.pharmacy.admin.integration.RemoteOrderResponse;
import com.pharmacy.admin.repository.MedicineRepository;
import com.pharmacy.admin.repository.OrderRepository;
import com.pharmacy.admin.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private static final Logger log = LoggerFactory.getLogger(AdminReportService.class);

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final CrossServiceAnalyticsClient crossServiceAnalyticsClient;

    @Transactional(readOnly = true)
    public SalesReportDto getSalesReport(String startDateStr, String endDateStr) {
        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Optional<List<RemoteOrderResponse>> remoteOrders = crossServiceAnalyticsClient.fetchAdminOrders();
        if (remoteOrders.isPresent()) {
            return buildSalesReportFromRemote(startDateStr, endDateStr, start, end, remoteOrders.get());
        }

        return buildSalesReportFromLocal(startDateStr, endDateStr, startDate, endDate, start, end);
    }

    @Transactional(readOnly = true)
    public InventoryReportDto getInventoryReport() {
        Optional<CatalogAdminStatsRemoteDTO> remoteCatalogStats = crossServiceAnalyticsClient.fetchCatalogStats();
        if (remoteCatalogStats.isPresent()) {
            return buildInventoryReportFromRemote(remoteCatalogStats.get());
        }

        return buildInventoryReportFromLocal();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getPrescriptionVolumeReport(String startDateStr, String endDateStr) {
        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        long totalUploaded = prescriptionRepository.countUploadedBetween(start, end);
        long pending = prescriptionRepository.countByStatusAndUploadedAtBetween(PrescriptionStatus.PENDING, start, end);
        long approved = prescriptionRepository.countByStatusAndUploadedAtBetween(PrescriptionStatus.APPROVED, start, end);
        long rejected = prescriptionRepository.countByStatusAndUploadedAtBetween(PrescriptionStatus.REJECTED, start, end);

        return Map.of(
                "totalUploaded", totalUploaded,
                "pending", pending,
                "approved", approved,
                "rejected", rejected);
    }

    private SalesReportDto buildSalesReportFromRemote(
            String startDateStr,
            String endDateStr,
            LocalDateTime start,
            LocalDateTime end,
            List<RemoteOrderResponse> remoteOrders) {

        List<RemoteOrderResponse> ordersInRange = remoteOrders.stream()
                .filter(order -> order.getCreatedAt() != null
                        && !order.getCreatedAt().isBefore(start)
                        && !order.getCreatedAt().isAfter(end))
                .toList();

        List<RemoteOrderResponse> deliveredOrders = ordersInRange.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(order.getStatus()))
                .toList();

        long deliveredCount = deliveredOrders.size();

        BigDecimal totalRevenue = deliveredOrders.stream()
                .map(RemoteOrderResponse::getTotalAmount)
                .map(this::toBigDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = deliveredCount == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(deliveredCount), 2, RoundingMode.HALF_UP);

        long cancelledCount = ordersInRange.stream()
                .filter(order -> "ADMIN_CANCELLED".equalsIgnoreCase(order.getStatus())
                        || "CUSTOMER_CANCELLED".equalsIgnoreCase(order.getStatus()))
                .count();

        long failedPaymentCount = ordersInRange.stream()
                .filter(order -> "PAYMENT_FAILED".equalsIgnoreCase(order.getStatus()))
                .count();

        Map<String, long[]> topMedicineAgg = deliveredOrders.stream()
                .flatMap(order -> safeOrderItems(order).stream())
                .collect(Collectors.toMap(
                        item -> item.getMedicineName() == null ? "Unknown" : item.getMedicineName(),
                        item -> new long[] {
                                item.getQuantity(),
                                resolveSubtotal(item).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue()
                        },
                        (left, right) -> new long[] { left[0] + right[0], left[1] + right[1] }));

        List<SalesReportDto.TopMedicine> topMedicines = topMedicineAgg.entrySet().stream()
                .map(entry -> SalesReportDto.TopMedicine.builder()
                        .name(entry.getKey())
                        .quantitySold(entry.getValue()[0])
                        .revenue(entry.getValue()[1] / 100.0)
                        .build())
                .sorted(Comparator.comparingLong(SalesReportDto.TopMedicine::getQuantitySold).reversed())
                .limit(10)
                .toList();

        Map<String, List<RemoteOrderResponse>> deliveredByDay = deliveredOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate().toString()));

        List<SalesReportDto.DailyRevenue> dailyRevenue = deliveredByDay.entrySet().stream()
                .map(entry -> {
                    BigDecimal dayRevenue = entry.getValue().stream()
                            .map(RemoteOrderResponse::getTotalAmount)
                            .map(this::toBigDecimal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return SalesReportDto.DailyRevenue.builder()
                            .date(entry.getKey())
                            .revenue(toDouble(dayRevenue))
                            .orderCount(entry.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(SalesReportDto.DailyRevenue::getDate))
                .toList();

        log.info("Sales report generated from remote order-service for {} to {}", startDateStr, endDateStr);

        return SalesReportDto.builder()
                .startDate(startDateStr)
                .endDate(endDateStr)
                .generatedAt(LocalDateTime.now().toString())
                .totalOrders(ordersInRange.size())
                .deliveredOrders(deliveredCount)
                .cancelledOrders(cancelledCount)
                .failedPaymentOrders(failedPaymentCount)
                .totalRevenue(toDouble(totalRevenue))
                .averageOrderValue(toDouble(averageOrderValue))
                .topMedicines(topMedicines)
                .dailyRevenue(dailyRevenue)
                .build();
    }

    private SalesReportDto buildSalesReportFromLocal(
            String startDateStr,
            String endDateStr,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime start,
            LocalDateTime end) {

        List<Order> orders = orderRepository.findOrdersBetween(start, end);

        List<Order> deliveredOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .toList();

        long deliveredCount = deliveredOrders.size();

        BigDecimal totalRevenue = deliveredOrders.stream()
                .map(order -> BigDecimal.valueOf(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = deliveredCount == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(deliveredCount), 2, RoundingMode.HALF_UP);

        long cancelledCount = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.ADMIN_CANCELLED
                        || order.getStatus() == OrderStatus.CUSTOMER_CANCELLED)
                .count();

        long failedPaymentCount = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAYMENT_FAILED)
                .count();

        List<Object[]> topRows = orderRepository.findTopSellingMedicinesForPeriod(start, end, PageRequest.of(0, 10));
        List<SalesReportDto.TopMedicine> topMedicines = topRows.stream()
                .map(row -> SalesReportDto.TopMedicine.builder()
                        .name(row[0] == null ? null : row[0].toString())
                        .quantitySold(row[1] == null ? 0L : ((Number) row[1]).longValue())
                        .revenue(toDouble(toBigDecimal(row[2])))
                        .build())
                .toList();

        Map<String, Long> deliveredOrdersByDate = deliveredOrders.stream()
                .filter(order -> order.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()));

        List<Object[]> dailyRows = orderRepository.getDailyRevenue(start, end);
        List<SalesReportDto.DailyRevenue> dailyRevenue = dailyRows.stream()
                .map(row -> {
                    String day = row[0] == null ? "" : row[0].toString();
                    BigDecimal revenue = toBigDecimal(row[1]);
                    long orderCount = deliveredOrdersByDate.getOrDefault(day, 0L);

                    return SalesReportDto.DailyRevenue.builder()
                            .date(day)
                            .revenue(toDouble(revenue))
                            .orderCount(orderCount)
                            .build();
                })
                .toList();

        log.info("Sales report generated from local DB for {} to {}", startDate, endDate);

        return SalesReportDto.builder()
                .startDate(startDateStr)
                .endDate(endDateStr)
                .generatedAt(LocalDateTime.now().toString())
                .totalOrders(orders.size())
                .deliveredOrders(deliveredCount)
                .cancelledOrders(cancelledCount)
                .failedPaymentOrders(failedPaymentCount)
                .totalRevenue(toDouble(totalRevenue))
                .averageOrderValue(toDouble(averageOrderValue))
                .topMedicines(topMedicines)
                .dailyRevenue(dailyRevenue)
                .build();
    }

    private InventoryReportDto buildInventoryReportFromRemote(CatalogAdminStatsRemoteDTO stats) {
        List<InventoryReportDto.MedicineInventoryItem> lowStockItems = safeMedicineList(stats.getLowStockItems()).stream()
                .map(this::toInventoryItem)
                .toList();

        List<InventoryReportDto.MedicineInventoryItem> expiringItems = safeMedicineList(stats.getExpiringItems()).stream()
                .map(this::toInventoryItem)
                .toList();

        BigDecimal totalInventoryValue = BigDecimal.valueOf(stats.getTotalInventoryValue());

        return InventoryReportDto.builder()
                .generatedAt(LocalDateTime.now().toString())
                .totalMedicines(stats.getTotalMedicines())
                .outOfStockCount(0)
                .lowStockCount(stats.getLowStockCount())
                .expiringIn30DaysCount(stats.getExpiringIn30DaysCount())
                .alreadyExpiredCount(stats.getAlreadyExpiredCount())
                .totalInventoryValue(toDouble(totalInventoryValue))
                .lowStockItems(lowStockItems)
                .expiringItems(expiringItems)
                .build();
    }

    private InventoryReportDto buildInventoryReportFromLocal() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAhead = today.plusDays(30);

        List<Medicine> activeMedicines = medicineRepository.findByIsActiveTrueOrderByNameAsc();

        long outOfStockCount = activeMedicines.stream().filter(medicine -> medicine.getStock() <= 0).count();
        long lowStockCount = activeMedicines.stream()
                .filter(medicine -> medicine.getStock() > 0 && medicine.getStock() < LOW_STOCK_THRESHOLD)
                .count();

        List<Medicine> expiringMedicines = medicineRepository.findMedicinesExpiringBetween(today, thirtyDaysAhead);
        long expiringIn30DaysCount = expiringMedicines.size();

        long alreadyExpiredCount = activeMedicines.stream()
                .map(Medicine::getExpiryDate)
                .filter(expiryDate -> expiryDate != null && expiryDate.isBefore(today))
                .count();

        BigDecimal totalInventoryValue = activeMedicines.stream()
                .map(medicine -> BigDecimal.valueOf(medicine.getPrice())
                        .multiply(BigDecimal.valueOf(Math.max(medicine.getStock(), 0))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InventoryReportDto.MedicineInventoryItem> lowStockItems = activeMedicines.stream()
                .filter(medicine -> medicine.getStock() < LOW_STOCK_THRESHOLD)
                .sorted(Comparator.comparingInt(Medicine::getStock))
                .map(this::mapToInventoryItem)
                .toList();

        List<InventoryReportDto.MedicineInventoryItem> expiringItems = expiringMedicines.stream()
                .sorted(Comparator.comparing(Medicine::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::mapToInventoryItem)
                .toList();

        log.info("Inventory report generated from local DB");

        return InventoryReportDto.builder()
                .generatedAt(LocalDateTime.now().toString())
                .totalMedicines(activeMedicines.size())
                .outOfStockCount(outOfStockCount)
                .lowStockCount(lowStockCount)
                .expiringIn30DaysCount(expiringIn30DaysCount)
                .alreadyExpiredCount(alreadyExpiredCount)
                .totalInventoryValue(toDouble(totalInventoryValue))
                .lowStockItems(lowStockItems)
                .expiringItems(expiringItems)
                .build();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid date format: '" + dateStr + "'. Use yyyy-MM-dd");
        }
    }

    private InventoryReportDto.MedicineInventoryItem mapToInventoryItem(Medicine medicine) {
        return InventoryReportDto.MedicineInventoryItem.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .sku(medicine.getSku())
                .stock(medicine.getStock())
                .price(medicine.getPrice())
                .expiryDate(medicine.getExpiryDate() == null ? null : medicine.getExpiryDate().toString())
                .categoryName(medicine.getCategory() == null ? null : medicine.getCategory().getName())
                .build();
    }

    private InventoryReportDto.MedicineInventoryItem toInventoryItem(CatalogAdminStatsRemoteDTO.MedicineInventoryItem medicine) {
        return InventoryReportDto.MedicineInventoryItem.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .sku(null)
                .stock(medicine.getStock() == null ? 0 : medicine.getStock())
                .price(toDouble(medicine.getPrice()))
                .expiryDate(medicine.getExpiryDate())
                .categoryName(medicine.getCategoryName())
                .build();
    }

    private List<CatalogAdminStatsRemoteDTO.MedicineInventoryItem> safeMedicineList(
            List<CatalogAdminStatsRemoteDTO.MedicineInventoryItem> medicines) {
        return medicines == null ? List.of() : medicines;
    }

    private List<RemoteOrderResponse.RemoteOrderItemResponse> safeOrderItems(RemoteOrderResponse order) {
        return order.getItems() == null ? List.of() : order.getItems();
    }

    private BigDecimal resolveSubtotal(RemoteOrderResponse.RemoteOrderItemResponse item) {
        if (item.getTotalPrice() != null) {
            return item.getTotalPrice();
        }

        BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
        return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private BigDecimal toBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimalValue) {
            return decimalValue;
        }
        if (value instanceof Number numberValue) {
            return BigDecimal.valueOf(numberValue.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}
