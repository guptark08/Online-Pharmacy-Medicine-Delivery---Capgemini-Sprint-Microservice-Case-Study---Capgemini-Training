package org.sprint.catalogandprescription_service.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sprint.catalogandprescription_service.entities.Inventory;

import jakarta.persistence.LockModeType;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByMedicineId(Long medicineId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.medicine.id = :medicineId "
            + "AND i.batchStatus = 'ACTIVE'")
    Integer getTotalStockByMedicineId(Long medicineId);

    @Query("SELECT i FROM Inventory i WHERE i.expiryDate BETWEEN :today AND :futureDate "
            + "AND i.batchStatus = 'ACTIVE'")
    List<Inventory> findExpiringBatches(LocalDate today, LocalDate futureDate);

    List<Inventory> findByBatchStatus(Inventory.BatchStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.medicine.id = :medicineId AND i.batchStatus = 'ACTIVE' "
            + "ORDER BY i.expiryDate ASC, i.id ASC")
    List<Inventory> findActiveBatchesForMedicineForUpdate(@Param("medicineId") Long medicineId);
}
