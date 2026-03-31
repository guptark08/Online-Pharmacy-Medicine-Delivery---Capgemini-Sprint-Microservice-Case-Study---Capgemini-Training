package org.sprint.catalogandprescription_service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.catalogandprescription_service.dto.CatalogAdminStatsDTO;
import org.sprint.catalogandprescription_service.entities.Medicine;
import org.sprint.catalogandprescription_service.entities.Prescription;
import org.sprint.catalogandprescription_service.repository.MedicineRepository;
import org.sprint.catalogandprescription_service.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogAdminAnalyticsService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Transactional(readOnly = true)
    public CatalogAdminStatsDTO getAdminStats() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAhead = today.plusDays(30);

        long totalMedicines = medicineRepository.count();
        long activeMedicines = medicineRepository.countByIsActiveTrue();

        List<Medicine> activeMedicinesList = medicineRepository.findByIsActiveTrueOrderByNameAsc();
        double totalInventoryValue = activeMedicinesList.stream()
                .map(medicine -> {
                    BigDecimal price = medicine.getPrice() == null ? BigDecimal.ZERO : medicine.getPrice();
                    int stock = medicine.getStock() == null ? 0 : Math.max(medicine.getStock(), 0);
                    return price.multiply(BigDecimal.valueOf(stock));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();

        List<CatalogAdminStatsDTO.MedicineInventoryItem> lowStockItems = medicineRepository
                .findByIsActiveTrueAndStockLessThanOrderByStockAsc(LOW_STOCK_THRESHOLD)
                .stream()
                .map(this::toInventoryItem)
                .toList();

        List<CatalogAdminStatsDTO.MedicineInventoryItem> expiringItems = medicineRepository
                .findByIsActiveTrueAndExpiryDateBetweenOrderByExpiryDateAsc(today, thirtyDaysAhead)
                .stream()
                .sorted(Comparator.comparing(Medicine::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toInventoryItem)
                .toList();

        long lowStockCount = lowStockItems.size();
        long expiringIn30DaysCount = medicineRepository.countActiveMedicinesExpiringBetween(today, thirtyDaysAhead);
        long alreadyExpiredCount = medicineRepository.countActiveMedicinesExpiredBefore(today);

        long pendingPrescriptions = prescriptionRepository.countByStatus(Prescription.PrescriptionStatus.PENDING);
        long approvedPrescriptions = prescriptionRepository.countByStatus(Prescription.PrescriptionStatus.APPROVED);
        long rejectedPrescriptions = prescriptionRepository.countByStatus(Prescription.PrescriptionStatus.REJECTED);

        return CatalogAdminStatsDTO.builder()
                .totalMedicines(totalMedicines)
                .activeMedicines(activeMedicines)
                .lowStockCount(lowStockCount)
                .expiringIn30DaysCount(expiringIn30DaysCount)
                .alreadyExpiredCount(alreadyExpiredCount)
                .totalInventoryValue(totalInventoryValue)
                .pendingPrescriptions(pendingPrescriptions)
                .approvedPrescriptions(approvedPrescriptions)
                .rejectedPrescriptions(rejectedPrescriptions)
                .lowStockItems(lowStockItems)
                .expiringItems(expiringItems)
                .build();
    }

    private CatalogAdminStatsDTO.MedicineInventoryItem toInventoryItem(Medicine medicine) {
        return CatalogAdminStatsDTO.MedicineInventoryItem.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .stock(medicine.getStock())
                .price(medicine.getPrice())
                .expiryDate(medicine.getExpiryDate() == null ? null : medicine.getExpiryDate().toString())
                .categoryName(medicine.getCategory() == null ? null : medicine.getCategory().getName())
                .build();
    }
}
