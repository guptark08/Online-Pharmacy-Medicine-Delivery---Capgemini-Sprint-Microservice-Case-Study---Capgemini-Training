package com.pharmacy.admin.integration;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CatalogAdminStatsRemoteDTO {

    private long totalMedicines;
    private long activeMedicines;
    private long lowStockCount;
    private long expiringIn30DaysCount;
    private long alreadyExpiredCount;
    private double totalInventoryValue;

    private long pendingPrescriptions;
    private long approvedPrescriptions;
    private long rejectedPrescriptions;

    private List<MedicineInventoryItem> lowStockItems;
    private List<MedicineInventoryItem> expiringItems;

    @Data
    public static class MedicineInventoryItem {
        private Long id;
        private String name;
        private Integer stock;
        private BigDecimal price;
        private String expiryDate;
        private String categoryName;
    }
}
