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
public class SalesReportDto {

    private String startDate;
    private String endDate;
    private String generatedAt;

    // ── Summary ───────────────────────────────────────────
    private long totalOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long failedPaymentOrders;

    private double totalRevenue;
    private double averageOrderValue;

    // ── Top medicines ─────────────────────────────────────
    private List<TopMedicine> topMedicines;

    // ── Daily breakdown ───────────────────────────────────
    private List<DailyRevenue> dailyRevenue;

    // ── Nested classes ────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopMedicine {
        private String name;
        private long quantitySold;
        private double revenue;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DailyRevenue {
        private String date;
        private double revenue;
        private long orderCount;
    }
}
