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
public class InventoryReportDto {

    private String generatedAt;

    private long totalMedicines;
    private long outOfStockCount;
    private long lowStockCount;
    private long expiringIn30DaysCount;
    private long alreadyExpiredCount;
    private double totalInventoryValue;

    private List<MedicineInventoryItem> lowStockItems;
    private List<MedicineInventoryItem> expiringItems;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MedicineInventoryItem {
        private Long id;
        private String name;
        private String sku;
        private int stock;
        private double price;
        private String expiryDate;
        private String categoryName;
    }
}
