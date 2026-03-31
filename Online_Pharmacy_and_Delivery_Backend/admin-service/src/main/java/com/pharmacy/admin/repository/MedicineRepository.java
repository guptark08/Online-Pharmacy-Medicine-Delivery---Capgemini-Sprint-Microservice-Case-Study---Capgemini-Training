package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // ── Basic Lookups ─────────────────────────────────────────────
    List<Medicine> findByIsActiveTrueOrderByNameAsc();

    Optional<Medicine> findBySku(String sku);

    boolean existsBySku(String sku);

    // ── Search ────────────────────────────────────────────────────
    @Query("SELECT m FROM Medicine m WHERE m.isActive = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(m.genericName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Medicine> searchMedicines(@Param("q") String query, Pageable pageable);

    // ── Category filter ───────────────────────────────────────────
    List<Medicine> findByCategoryIdAndIsActiveTrue(Long categoryId);

    // ── Inventory alerts ──────────────────────────────────────────
    @Query("SELECT m FROM Medicine m WHERE m.isActive = true AND m.stock < :threshold")
    List<Medicine> findLowStockMedicines(@Param("threshold") int threshold);

    @Query("SELECT m FROM Medicine m WHERE m.isActive = true AND m.expiryDate < :date")
    List<Medicine> findExpiringMedicines(@Param("date") LocalDate date);

    @Query("SELECT m FROM Medicine m WHERE m.isActive = true AND m.expiryDate BETWEEN :from AND :to")
    List<Medicine> findMedicinesExpiringBetween(
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    // ── Dashboard counts ──────────────────────────────────────────
    long countByIsActiveTrue();

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.isActive = true AND m.stock < :threshold")
    long countLowStockMedicines(@Param("threshold") int threshold);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.isActive = true AND m.expiryDate < :date")
    long countExpiringBefore(@Param("date") LocalDate date);

    // ── Requires prescription filter ──────────────────────────────
    List<Medicine> findByRequiresPrescriptionTrueAndIsActiveTrue();
}
