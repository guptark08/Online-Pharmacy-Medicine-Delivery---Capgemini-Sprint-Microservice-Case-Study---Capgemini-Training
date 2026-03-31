package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.request.MedicineRequestDto;
import com.pharmacy.admin.dto.response.MedicineResponseDto;
import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.exception.DuplicateResourceException;
import com.pharmacy.admin.exception.ResourceNotFoundException;
import com.pharmacy.admin.repository.CategoryRepository;
import com.pharmacy.admin.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminMedicineService Tests")
class AdminMedicineServiceTest {

    @Mock private MedicineRepository medicineRepository;
    @Mock private CategoryRepository  categoryRepository;

    @InjectMocks
    private AdminMedicineService medicineService;

    // ── Test fixtures ─────────────────────────────────────────────
    private Category sampleCategory;
    private Medicine sampleMedicine;
    private MedicineRequestDto validDto;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id(1L).name("Pain Relief").isActive(true).build();

        sampleMedicine = Medicine.builder()
                .id(1L)
                .name("Paracetamol")
                .genericName("Acetaminophen")
                .price(25.0)
                .mrp(30.0)
                .stock(100)
                .requiresPrescription(false)
                .isActive(true)
                .expiryDate(LocalDate.now().plusYears(2))
                .category(sampleCategory)
                .build();

        validDto = new MedicineRequestDto();
        validDto.setName("Paracetamol");
        validDto.setGenericName("Acetaminophen");
        validDto.setPrice(25.0);
        validDto.setMrp(30.0);
        validDto.setStock(100);
        validDto.setCategoryId(1L);
        validDto.setExpiryDate(LocalDate.now().plusYears(2).toString());
        validDto.setRequiresPrescription(false);
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("addMedicine()")
    class AddMedicineTests {

        @Test
        @DisplayName("Should add medicine and return DTO when all inputs are valid")
        void addMedicine_validInput_returnsDto() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
            when(medicineRepository.save(any(Medicine.class))).thenReturn(sampleMedicine);

            MedicineResponseDto result = medicineService.addMedicine(validDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Paracetamol");
            assertThat(result.getPrice()).isEqualTo(25.0);
            assertThat(result.getStock()).isEqualTo(100);
            verify(medicineRepository, times(1)).save(any(Medicine.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when category does not exist")
        void addMedicine_categoryNotFound_throwsException() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicineService.addMedicine(validDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category");
        }

        @Test
        @DisplayName("Should throw BadRequestException when expiry date is in the past")
        void addMedicine_pastExpiryDate_throwsException() {
            validDto.setExpiryDate("2020-01-01");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

            assertThatThrownBy(() -> medicineService.addMedicine(validDto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("past");
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when SKU already exists")
        void addMedicine_duplicateSku_throwsException() {
            validDto.setSku("SKU-001");
            when(medicineRepository.existsBySku("SKU-001")).thenReturn(true);

            assertThatThrownBy(() -> medicineService.addMedicine(validDto))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("SKU-001");
        }

        @Test
        @DisplayName("Should throw BadRequestException for invalid date format")
        void addMedicine_invalidDateFormat_throwsException() {
            validDto.setExpiryDate("31-12-2026"); // wrong format
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

            assertThatThrownBy(() -> medicineService.addMedicine(validDto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("date format");
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getMedicineById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return DTO when medicine exists")
        void getMedicineById_exists_returnsDto() {
            when(medicineRepository.findById(1L)).thenReturn(Optional.of(sampleMedicine));

            MedicineResponseDto result = medicineService.getMedicineById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Paracetamol");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when medicine does not exist")
        void getMedicineById_notFound_throwsException() {
            when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicineService.getMedicineById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateMedicine()")
    class UpdateMedicineTests {

        @Test
        @DisplayName("Should update and return updated DTO")
        void updateMedicine_validInput_returnsUpdatedDto() {
            validDto.setPrice(30.0);
            validDto.setStock(200);

            Medicine updated = Medicine.builder()
                    .id(1L).name("Paracetamol").price(30.0).stock(200)
                    .expiryDate(LocalDate.now().plusYears(2))
                    .isActive(true).category(sampleCategory).build();

            when(medicineRepository.findById(1L)).thenReturn(Optional.of(sampleMedicine));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
            when(medicineRepository.save(any(Medicine.class))).thenReturn(updated);

            MedicineResponseDto result = medicineService.updateMedicine(1L, validDto);

            assertThat(result.getPrice()).isEqualTo(30.0);
            assertThat(result.getStock()).isEqualTo(200);
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateStock()")
    class UpdateStockTests {

        @Test
        @DisplayName("Should update stock to new value")
        void updateStock_validValue_updatesStock() {
            when(medicineRepository.findById(1L)).thenReturn(Optional.of(sampleMedicine));
            when(medicineRepository.save(any(Medicine.class))).thenReturn(sampleMedicine);

            medicineService.updateStock(1L, 50);

            verify(medicineRepository).save(argThat(m -> m.getStock() == 50));
        }

        @Test
        @DisplayName("Should throw BadRequestException when stock is negative")
        void updateStock_negativeValue_throwsException() {
            assertThatThrownBy(() -> medicineService.updateStock(1L, -5))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("negative");
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteMedicine()")
    class DeleteTests {

        @Test
        @DisplayName("Should soft-delete medicine by setting isActive=false")
        void deleteMedicine_exists_marksInactive() {
            when(medicineRepository.findById(1L)).thenReturn(Optional.of(sampleMedicine));
            when(medicineRepository.save(any(Medicine.class))).thenReturn(sampleMedicine);

            medicineService.deleteMedicine(1L);

            verify(medicineRepository).save(argThat(m -> !m.isActive()));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when medicine not found")
        void deleteMedicine_notFound_throwsException() {
            when(medicineRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicineService.deleteMedicine(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Inventory alert methods")
    class AlertTests {

        @Test
        @DisplayName("getLowStockMedicines should return medicines with stock below threshold")
        void getLowStockMedicines_returnsLowStockList() {
            Medicine lowStock = Medicine.builder()
                    .id(2L).name("Aspirin").stock(3).isActive(true)
                    .category(sampleCategory)
                    .expiryDate(LocalDate.now().plusYears(1))
                    .build();

            when(medicineRepository.findLowStockMedicines(10))
                    .thenReturn(List.of(lowStock));

            List<MedicineResponseDto> result = medicineService.getLowStockMedicines();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStock()).isEqualTo(3);
        }

        @Test
        @DisplayName("getExpiringMedicines should return medicines expiring within 30 days")
        void getExpiringMedicines_returnsList() {
            Medicine expiring = Medicine.builder()
                    .id(3L).name("Vitamin C").stock(20).isActive(true)
                    .category(sampleCategory)
                    .expiryDate(LocalDate.now().plusDays(15))
                    .build();

            when(medicineRepository.findMedicinesExpiringBetween(any(), any()))
                    .thenReturn(List.of(expiring));

            List<MedicineResponseDto> result = medicineService.getExpiringMedicines();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Vitamin C");
        }
    }
}
