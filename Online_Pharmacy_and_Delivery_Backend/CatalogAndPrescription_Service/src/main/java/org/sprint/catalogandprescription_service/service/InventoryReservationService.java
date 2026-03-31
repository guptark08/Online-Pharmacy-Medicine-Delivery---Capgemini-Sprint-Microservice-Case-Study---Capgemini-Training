package org.sprint.catalogandprescription_service.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sprint.catalogandprescription_service.dto.InventoryReservationRequest;
import org.sprint.catalogandprescription_service.dto.InventoryReservationResponse;
import org.sprint.catalogandprescription_service.entities.Inventory;
import org.sprint.catalogandprescription_service.entities.Medicine;
import org.sprint.catalogandprescription_service.globalexception.ResourceNotFoundException;
import org.sprint.catalogandprescription_service.messaging.DomainEventPublisher;
import org.sprint.catalogandprescription_service.messaging.PharmacyEventRoutingKeys;
import org.sprint.catalogandprescription_service.messaging.events.InventoryAdjustedEvent;
import org.sprint.catalogandprescription_service.repository.InventoryRepository;
import org.sprint.catalogandprescription_service.repository.MedicineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryReservationService {

    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;

    @Autowired(required = false)
    private DomainEventPublisher domainEventPublisher;

    public InventoryReservationResponse reserveStock(InventoryReservationRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Reservation request must include at least one item");
        }

        Map<Long, Integer> aggregatedQuantities = aggregateQuantities(request.getItems());

        int totalUnitsReserved = 0;
        for (Map.Entry<Long, Integer> entry : aggregatedQuantities.entrySet()) {
            reserveStockForMedicine(entry.getKey(), entry.getValue(), request.getOrderId());
            totalUnitsReserved += entry.getValue();
        }

        return InventoryReservationResponse.builder()
                .orderId(request.getOrderId())
                .reservedLines(aggregatedQuantities.size())
                .totalUnitsReserved(totalUnitsReserved)
                .build();
    }

    private Map<Long, Integer> aggregateQuantities(List<InventoryReservationRequest.LineItem> items) {
        Map<Long, Integer> quantitiesByMedicine = new LinkedHashMap<>();

        for (InventoryReservationRequest.LineItem item : items) {
            if (item.getMedicineId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each reservation item must have valid medicineId and quantity");
            }

            quantitiesByMedicine.merge(item.getMedicineId(), item.getQuantity(), Integer::sum);
        }

        return quantitiesByMedicine;
    }

    private void reserveStockForMedicine(Long medicineId, int quantity, Long orderId) {
        Medicine medicine = medicineRepository.findByIdAndIsActiveTrueForUpdate(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + medicineId));

        int previousStock = medicine.getStock() == null ? 0 : medicine.getStock();
        int remainingToReserve = quantity;

        List<Inventory> lockedBatches = inventoryRepository.findActiveBatchesForMedicineForUpdate(medicineId);

        if (!lockedBatches.isEmpty()) {
            for (Inventory batch : lockedBatches) {
                if (remainingToReserve == 0) {
                    break;
                }

                int available = Math.max(batch.getQuantity() == null ? 0 : batch.getQuantity(), 0);
                if (available == 0) {
                    continue;
                }

                int consumed = Math.min(available, remainingToReserve);
                int remainingInBatch = available - consumed;

                batch.setQuantity(remainingInBatch);
                if (remainingInBatch == 0) {
                    batch.setBatchStatus(Inventory.BatchStatus.DEPLETED);
                }

                remainingToReserve -= consumed;
            }

            if (remainingToReserve > 0) {
                throw new IllegalStateException("Insufficient stock for medicine id: " + medicineId);
            }

            inventoryRepository.saveAll(lockedBatches);

            Integer recalculated = inventoryRepository.getTotalStockByMedicineId(medicineId);
            medicine.setStock(recalculated == null ? 0 : Math.max(recalculated, 0));
        } else {
            if (previousStock < quantity) {
                throw new IllegalStateException("Insufficient stock for medicine id: " + medicineId);
            }
            medicine.setStock(previousStock - quantity);
        }

        medicine.setStatus(resolveStockStatus(medicine));

        Medicine savedMedicine = medicineRepository.save(medicine);
        publishInventoryAdjustedEvent(savedMedicine, previousStock, quantity, orderId);
    }

    private Medicine.MedicineStatus resolveStockStatus(Medicine medicine) {
        if (medicine.getStatus() == Medicine.MedicineStatus.DISCONTINUED) {
            return Medicine.MedicineStatus.DISCONTINUED;
        }

        Integer stock = medicine.getStock();
        return stock == null || stock <= 0
                ? Medicine.MedicineStatus.OUT_OF_STOCK
                : Medicine.MedicineStatus.AVAILABLE;
    }

    private void publishInventoryAdjustedEvent(Medicine medicine, Integer previousStock, Integer reservedQuantity, Long orderId) {
        if (domainEventPublisher == null) {
            return;
        }

        Integer currentStock = medicine.getStock() == null ? 0 : medicine.getStock();
        Integer before = Objects.requireNonNullElse(previousStock, 0);
        String reason = orderId == null
                ? "Stock reserved during checkout"
                : "Stock reserved for order " + orderId;

        InventoryAdjustedEvent event = InventoryAdjustedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(OffsetDateTime.now().toString())
                .medicineId(medicine.getId())
                .medicineName(medicine.getName())
                .categoryId(medicine.getCategory() == null ? null : medicine.getCategory().getId())
                .previousStock(before)
                .currentStock(currentStock)
                .adjustmentType("RESERVED")
                .reason(reason + " (reserved=" + reservedQuantity + ")")
                .active(medicine.getIsActive())
                .build();

        domainEventPublisher.publishAfterCommit(PharmacyEventRoutingKeys.INVENTORY_ADJUSTED, event);
    }
}
