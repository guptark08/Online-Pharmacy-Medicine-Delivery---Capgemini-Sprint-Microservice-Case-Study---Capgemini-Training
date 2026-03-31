package org.sprint.catalogandprescription_service.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.catalogandprescription_service.entities.Category;
import org.sprint.catalogandprescription_service.entities.Inventory;
import org.sprint.catalogandprescription_service.entities.Medicine;
import org.sprint.catalogandprescription_service.entities.Prescription;
import org.sprint.catalogandprescription_service.repository.CategoryRepository;
import org.sprint.catalogandprescription_service.repository.InventoryRepository;
import org.sprint.catalogandprescription_service.repository.MedicineRepository;
import org.sprint.catalogandprescription_service.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class TestDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Category painRelief = ensureCategory("Pain Relief", "Pain management and fever medicines", "/images/categories/pain-relief.png");
        Category antibiotics = ensureCategory("Antibiotics", "Prescription antibiotics for infection treatment", "/images/categories/antibiotics.png");
        Category diabetes = ensureCategory("Diabetes Care", "Blood sugar management medicines", "/images/categories/diabetes.png");
        Category vitamins = ensureCategory("Vitamins", "Daily wellness and immunity support", "/images/categories/vitamins.png");
        Category coldFlu = ensureCategory("Cold And Flu", "Cold, cough, and allergy relief medicines", "/images/categories/cold-flu.png");

        Medicine paracetamol = ensureMedicine(
                "Paracetamol 500mg",
                painRelief,
                "HealWell Pharma",
                "Everyday fever and pain relief tablet",
                "40.00",
                "35.00",
                150,
                false,
                "1 tablet after food",
                "Rare nausea",
                "/images/medicines/paracetamol.png",
                LocalDate.now().plusMonths(18));

        Medicine azithromycin = ensureMedicine(
                "Azithromycin 250mg",
                antibiotics,
                "MediCure Labs",
                "Common antibiotic used for bacterial infections",
                "120.00",
                "110.00",
                60,
                true,
                "As prescribed by physician",
                "Stomach upset",
                "/images/medicines/azithromycin.png",
                LocalDate.now().plusMonths(10));

        Medicine metformin = ensureMedicine(
                "Metformin 500mg",
                diabetes,
                "GlucoLife",
                "Oral diabetes medicine for blood sugar control",
                "85.00",
                "78.00",
                90,
                false,
                "1 tablet twice daily",
                "Mild stomach discomfort",
                "/images/medicines/metformin.png",
                LocalDate.now().plusMonths(16));

        Medicine vitaminC = ensureMedicine(
                "Vitamin C Tablets",
                vitamins,
                "NutriLeaf",
                "Vitamin supplement for immunity support",
                "30.00",
                "28.00",
                200,
                false,
                "1 tablet daily",
                "Usually well tolerated",
                "/images/medicines/vitamin-c.png",
                LocalDate.now().plusMonths(24));

        Medicine insulin = ensureMedicine(
                "Insulin Glargine",
                diabetes,
                "LifeDose Biotech",
                "Long-acting insulin injection",
                "780.00",
                "760.00",
                8,
                true,
                "Use only as prescribed",
                "Low blood sugar",
                "/images/medicines/insulin-glargine.png",
                LocalDate.now().plusDays(20));

        Medicine coughSyrup = ensureMedicine(
                "Cough Syrup DX",
                coldFlu,
                "BreatheEasy",
                "Relief for dry cough and throat irritation",
                "95.00",
                "90.00",
                12,
                false,
                "10ml twice a day",
                "Drowsiness",
                "/images/medicines/cough-syrup-dx.png",
                LocalDate.now().minusDays(5));

        ensureInventory(paracetamol, "PARA-500-A", 90, LocalDate.now().plusMonths(12), "18.00");
        ensureInventory(paracetamol, "PARA-500-B", 60, LocalDate.now().plusMonths(18), "19.00");
        ensureInventory(azithromycin, "AZI-250-A", 60, LocalDate.now().plusMonths(10), "72.00");
        ensureInventory(metformin, "MET-500-A", 50, LocalDate.now().plusMonths(8), "42.00");
        ensureInventory(metformin, "MET-500-B", 40, LocalDate.now().plusMonths(16), "40.00");
        ensureInventory(vitaminC, "VITC-1000-A", 200, LocalDate.now().plusMonths(24), "12.00");
        ensureInventory(insulin, "INS-GLA-A", 8, LocalDate.now().plusDays(20), "510.00");
        ensureInventory(coughSyrup, "COUGH-DX-A", 12, LocalDate.now().minusDays(5), "45.00");

        ensurePrescription(
                2L,
                "alice-approved-prescription.pdf",
                "/seed/prescriptions/alice-approved-prescription.pdf",
                Prescription.PrescriptionStatus.APPROVED,
                "Verified for long-term medication",
                1L);

        ensurePrescription(
                3L,
                "bob-pending-prescription.pdf",
                "/seed/prescriptions/bob-pending-prescription.pdf",
                Prescription.PrescriptionStatus.PENDING,
                null,
                null);

        ensurePrescription(
                3L,
                "bob-rejected-prescription.pdf",
                "/seed/prescriptions/bob-rejected-prescription.pdf",
                Prescription.PrescriptionStatus.REJECTED,
                "Prescription image was unclear",
                1L);
    }

    private Category ensureCategory(String name, String description, String iconUrl) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .iconUrl(iconUrl)
                        .isActive(true)
                        .build()));
    }

    private Medicine ensureMedicine(
            String name,
            Category category,
            String manufacturer,
            String description,
            String price,
            String discountedPrice,
            int stock,
            boolean requiresPrescription,
            String dosageInfo,
            String sideEffects,
            String imageUrl,
            LocalDate expiryDate) {

        return medicineRepository.findAll().stream()
                .filter(medicine -> name.equalsIgnoreCase(medicine.getName()))
                .findFirst()
                .orElseGet(() -> medicineRepository.save(Medicine.builder()
                        .name(name)
                        .category(category)
                        .manufacturer(manufacturer)
                        .description(description)
                        .price(new BigDecimal(price))
                        .discountedPrice(new BigDecimal(discountedPrice))
                        .stock(stock)
                        .requiresPrescription(requiresPrescription)
                        .dosageInfo(dosageInfo)
                        .sideEffects(sideEffects)
                        .imageUrl(imageUrl)
                        .expiryDate(expiryDate)
                        .status(stock > 0 ? Medicine.MedicineStatus.AVAILABLE : Medicine.MedicineStatus.OUT_OF_STOCK)
                        .isActive(true)
                        .build()));
    }

    private void ensureInventory(
            Medicine medicine,
            String batchNumber,
            int quantity,
            LocalDate expiryDate,
            String costPrice) {

        boolean batchExists = inventoryRepository.findAll().stream()
                .anyMatch(inventory -> batchNumber.equalsIgnoreCase(inventory.getBatchNumber()));

        if (batchExists) {
            return;
        }

        inventoryRepository.save(Inventory.builder()
                .medicine(medicine)
                .batchNumber(batchNumber)
                .quantity(quantity)
                .expiryDate(expiryDate)
                .costPrice(new BigDecimal(costPrice))
                .receivedDate(LocalDate.now().minusDays(15))
                .batchStatus(expiryDate.isBefore(LocalDate.now()) ? Inventory.BatchStatus.EXPIRED : Inventory.BatchStatus.ACTIVE)
                .build());
    }

    private void ensurePrescription(
            Long customerId,
            String fileName,
            String filePath,
            Prescription.PrescriptionStatus status,
            String reviewNotes,
            Long reviewedBy) {

        List<Prescription> existingPrescriptions = prescriptionRepository.findByCustomerIdOrderByUploadedAtDesc(customerId);
        boolean alreadyPresent = existingPrescriptions.stream()
                .anyMatch(prescription -> fileName.equalsIgnoreCase(prescription.getFileName()));

        if (alreadyPresent) {
            return;
        }

        prescriptionRepository.save(Prescription.builder()
                .customerId(customerId)
                .fileName(fileName)
                .filePath(filePath)
                .fileType("application/pdf")
                .fileSize(125_000L)
                .status(status)
                .reviewNotes(reviewNotes)
                .reviewedBy(reviewedBy)
                .build());
    }
}
