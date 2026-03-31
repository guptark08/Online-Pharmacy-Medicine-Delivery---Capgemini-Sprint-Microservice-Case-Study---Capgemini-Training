package com.orderanddelivery.integration;

import java.math.BigDecimal;

public record CatalogMedicineSnapshot(Long medicineId, String medicineName, BigDecimal unitPrice,
        boolean requiresPrescription) {
}