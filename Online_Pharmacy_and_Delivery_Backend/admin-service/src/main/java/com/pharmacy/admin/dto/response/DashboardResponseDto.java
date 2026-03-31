package com.pharmacy.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {

    // ── Order KPIs ────────────────────────────────────────
    private long totalOrders;
    private long pendingPaymentOrders;
    private long packedOrders;
    private long outForDeliveryOrders;
    private long deliveredOrders;
    private long cancelledOrders;

    // ── Prescription KPIs ─────────────────────────────────
    private long pendingPrescriptions;
    private long approvedPrescriptions;
    private long rejectedPrescriptions;

    // ── Inventory KPIs ────────────────────────────────────
    private long totalMedicines;
    private long activeMedicines;
    private long lowStockCount;           // stock < 10
    private long expiringThisMonthCount;

    // ── Revenue KPIs ──────────────────────────────────────
    private double totalRevenue;
    private double revenueThisMonth;
    private double revenueToday;

    // ── Alert Lists (for sidebar alerts) ─────────────────
    private List<LowStockAlert> lowStockAlerts;
    private List<ExpiryAlert> expiryAlerts;

    // ── Recent orders table ───────────────────────────────
    private List<RecentOrder> recentOrders;

    // ── Nested: Low stock alert item ──────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LowStockAlert {
        private Long medicineId;
        private String medicineName;
        private int currentStock;
    }

    // ── Nested: Expiry alert item ─────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExpiryAlert {
        private Long medicineId;
        private String medicineName;
        private String expiryDate;
        private int stock;
    }

    // ── Nested: Recent order row ──────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RecentOrder {
        private Long orderId;
        private String userEmail;
        private String status;
        private double totalAmount;
        private String createdAt;
    }
}
