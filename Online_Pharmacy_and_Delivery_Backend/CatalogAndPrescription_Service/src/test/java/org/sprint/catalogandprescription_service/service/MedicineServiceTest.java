package org.sprint.catalogandprescription_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.sprint.catalogandprescription_service.dto.MedicineDTO;
import org.sprint.catalogandprescription_service.entities.Category;
import org.sprint.catalogandprescription_service.entities.Medicine;
import org.sprint.catalogandprescription_service.globalexception.ResourceNotFoundException;
import org.sprint.catalogandprescription_service.repository.CategoryRepository;
import org.sprint.catalogandprescription_service.repository.MedicineRepository;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private MedicineService medicineService;

    @Test
    void getAllMedicines_invalidSortBy_throwsBadRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> medicineService.getAllMedicines(null, null, null, 0, 10, "invalidField"));
    }

    @Test
    void getMedicineById_inactiveOrMissing_throwsNotFound() {
        when(medicineRepository.findByIdAndIsActiveTrue(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicineService.getMedicineById(100L));
    }

    @Test
    void createMedicine_discountedPriceGreaterThanPrice_throwsBadRequest() {
        MedicineDTO dto = MedicineDTO.builder()
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .price(new BigDecimal("10.00"))
                .discountedPrice(new BigDecimal("12.00"))
                .stock(10)
                .categoryId(1L)
                .build();

        when(medicineRepository.existsByNameIgnoreCaseAndIsActiveTrue("Paracetamol")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> medicineService.createMedicine(dto));
    }

    @Test
    void updateMedicine_discontinuedStatus_appliesDiscontinued() {
        Category category = Category.builder().id(2L).name("Pain Relief").build();
        Medicine existing = Medicine.builder()
                .id(10L)
                .name("Old")
                .manufacturer("OldCo")
                .price(new BigDecimal("20.00"))
                .stock(5)
                .category(category)
                .isActive(true)
                .status(Medicine.MedicineStatus.AVAILABLE)
                .build();

        MedicineDTO dto = MedicineDTO.builder()
                .name("Updated")
                .manufacturer("NewCo")
                .price(new BigDecimal("18.00"))
                .discountedPrice(new BigDecimal("15.00"))
                .stock(8)
                .requiresPrescription(false)
                .categoryId(2L)
                .status("DISCONTINUED")
                .build();

        when(medicineRepository.findByIdAndIsActiveTrue(10L)).thenReturn(Optional.of(existing));
        when(medicineRepository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot("Updated", 10L)).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(medicineRepository.save(existing)).thenReturn(existing);

        medicineService.updateMedicine(10L, dto);

        assertEquals(Medicine.MedicineStatus.DISCONTINUED, existing.getStatus());
    }

    @Test
    void createMedicine_validInput_savesAndReturnsDTO() {
        MedicineDTO dto = MedicineDTO.builder()
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .description("Pain relief")
                .price(new BigDecimal("25.00"))
                .stock(100)
                .requiresPrescription(false)
                .expiryDate(LocalDate.now().plusYears(1))
                .categoryId(1L)
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Pain Relief")
                .build();

        Medicine saved = Medicine.builder()
                .id(11L)
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .description("Pain relief")
                .price(new BigDecimal("25.00"))
                .stock(100)
                .requiresPrescription(false)
                .expiryDate(dto.getExpiryDate())
                .category(category)
                .isActive(true)
                .status(Medicine.MedicineStatus.AVAILABLE)
                .build();

        when(medicineRepository.existsByNameIgnoreCaseAndIsActiveTrue("Paracetamol")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(saved);

        MedicineDTO response = medicineService.createMedicine(dto);

        assertEquals("Paracetamol", response.getName());
        assertEquals(new BigDecimal("25.00"), response.getPrice());
        assertEquals(1L, response.getCategoryId());
    }

    @Test
    void createMedicine_duplicateName_throwsIllegalArgument() {
        MedicineDTO dto = MedicineDTO.builder()
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .price(new BigDecimal("25.00"))
                .stock(100)
                .categoryId(1L)
                .build();

        when(medicineRepository.existsByNameIgnoreCaseAndIsActiveTrue("Paracetamol")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> medicineService.createMedicine(dto));
    }

    @Test
    void createMedicine_categoryNotFound_throwsResourceNotFound() {
        MedicineDTO dto = MedicineDTO.builder()
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .price(new BigDecimal("25.00"))
                .stock(100)
                .categoryId(999L)
                .build();

        when(medicineRepository.existsByNameIgnoreCaseAndIsActiveTrue("Paracetamol")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> medicineService.createMedicine(dto));
    }

    @Test
    void deleteMedicine_success_setsIsActiveFalse() {
        Category category = Category.builder().id(1L).name("Pain Relief").build();
        Medicine medicine = Medicine.builder()
                .id(7L)
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .price(new BigDecimal("25.00"))
                .stock(20)
                .isActive(true)
                .status(Medicine.MedicineStatus.AVAILABLE)
                .category(category)
                .build();

        when(medicineRepository.findByIdAndIsActiveTrue(7L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        medicineService.deleteMedicine(7L);

        verify(medicineRepository).save(any(Medicine.class));
        assertEquals(Boolean.FALSE, medicine.getIsActive());
    }

    @Test
    void getAllMedicines_withKeyword_callsSearchMedicines() {
        Category category = Category.builder().id(1L).name("Pain Relief").build();
        Medicine medicine = Medicine.builder()
                .id(1L)
                .name("Paracetamol")
                .manufacturer("ABC Pharma")
                .price(new BigDecimal("25.00"))
                .stock(10)
                .category(category)
                .isActive(true)
                .status(Medicine.MedicineStatus.AVAILABLE)
                .build();

        Page<Medicine> page = new PageImpl<>(List.of(medicine));
        when(medicineRepository.searchMedicines(eq("para"), any(Pageable.class))).thenReturn(page);

        medicineService.getAllMedicines("para", null, null, 0, 10, "name");

        verify(medicineRepository).searchMedicines(eq("para"), any(Pageable.class));
    }

    @Test
    void getAllMedicines_withCategoryId_callsFindByCategory() {
        Category category = Category.builder().id(2L).name("Antibiotic").build();
        Medicine medicine = Medicine.builder()
                .id(2L)
                .name("Azithromycin")
                .manufacturer("XYZ Pharma")
                .price(new BigDecimal("99.00"))
                .stock(15)
                .category(category)
                .isActive(true)
                .status(Medicine.MedicineStatus.AVAILABLE)
                .build();

        Page<Medicine> page = new PageImpl<>(List.of(medicine));
        when(medicineRepository.findByCategoryIdAndIsActiveTrue(eq(2L), any(Pageable.class))).thenReturn(page);

        medicineService.getAllMedicines(null, 2L, null, 0, 10, "name");

        verify(medicineRepository).findByCategoryIdAndIsActiveTrue(eq(2L), any(Pageable.class));
    }
}

