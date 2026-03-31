package org.sprint.catalogandprescription_service.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sprint.catalogandprescription_service.entities.Medicine;

import jakarta.persistence.LockModeType;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    Page<Medicine> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<Medicine> findByNameContainingIgnoreCaseAndCategoryIdAndIsActiveTrue(
            String name,
            Long categoryId,
            Pageable pageable);

    Page<Medicine> findByIsActiveTrue(Pageable pageable);

    Optional<Medicine> findByIdAndIsActiveTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medicine m WHERE m.id = :id AND m.isActive = true")
    Optional<Medicine> findByIdAndIsActiveTrueForUpdate(@Param("id") Long id);

    List<Medicine> findByRequiresPrescriptionAndIsActiveTrue(Boolean requiresPrescription);

    Page<Medicine> findByRequiresPrescriptionAndIsActiveTrue(Boolean requiresPrescription, Pageable pageable);

    Page<Medicine> findByCategoryIdAndRequiresPrescriptionAndIsActiveTrue(
            Long categoryId,
            Boolean requiresPrescription,
            Pageable pageable);

    Page<Medicine> findByNameContainingIgnoreCaseAndRequiresPrescriptionAndIsActiveTrue(
            String name,
            Boolean requiresPrescription,
            Pageable pageable);

    Page<Medicine> findByNameContainingIgnoreCaseAndCategoryIdAndRequiresPrescriptionAndIsActiveTrue(
            String name,
            Long categoryId,
            Boolean requiresPrescription,
            Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE "
            + "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND m.isActive = true")
    Page<Medicine> searchMedicines(String keyword, Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.stock <= :threshold AND m.isActive = true")
    List<Medicine> findLowStockMedicines(int threshold);

    boolean existsByNameIgnoreCaseAndIsActiveTrue(String name);

    boolean existsByNameIgnoreCaseAndIsActiveTrueAndIdNot(String name, Long id);

    long countByIsActiveTrue();

    List<Medicine> findByIsActiveTrueOrderByNameAsc();

    List<Medicine> findByIsActiveTrueAndStockLessThanOrderByStockAsc(Integer threshold);

    List<Medicine> findByIsActiveTrueAndExpiryDateBetweenOrderByExpiryDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.isActive = true AND m.expiryDate BETWEEN :startDate AND :endDate")
    long countActiveMedicinesExpiringBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.isActive = true AND m.expiryDate < :beforeDate")
    long countActiveMedicinesExpiredBefore(@Param("beforeDate") LocalDate beforeDate);
}
