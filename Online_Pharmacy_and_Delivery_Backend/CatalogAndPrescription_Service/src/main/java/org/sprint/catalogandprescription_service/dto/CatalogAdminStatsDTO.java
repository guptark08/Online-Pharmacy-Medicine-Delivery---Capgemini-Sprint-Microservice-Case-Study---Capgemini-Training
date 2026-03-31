package org.sprint.catalogandprescription_service.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogAdminStatsDTO {

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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicineInventoryItem {
        private Long id;
        private String name;
        private Integer stock;
        private BigDecimal price;
        private String expiryDate;
        private String categoryName;
    }
}
