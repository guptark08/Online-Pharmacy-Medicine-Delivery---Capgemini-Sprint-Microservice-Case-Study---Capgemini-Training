package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByMedicineId(Long medicineId);

    List<Inventory> findByExpiryDateBefore(LocalDate date);

    @Query("SELECT i FROM Inventory i WHERE i.expiryDate BETWEEN :from AND :to ORDER BY i.expiryDate ASC")
    List<Inventory> findExpiringBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    boolean existsByBatchNumber(String batchNumber);
}
