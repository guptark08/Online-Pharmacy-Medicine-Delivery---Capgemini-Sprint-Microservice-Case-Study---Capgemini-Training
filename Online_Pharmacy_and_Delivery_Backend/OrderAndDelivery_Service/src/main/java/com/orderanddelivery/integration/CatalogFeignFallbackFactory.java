package com.orderanddelivery.integration;

import java.util.Map;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.orderanddelivery.exception.ExternalServiceException;

@Component
public class CatalogFeignFallbackFactory implements FallbackFactory<CatalogFeignClient> {

    @Override
    public CatalogFeignClient create(Throwable cause) {
        return new CatalogFeignClient() {
            @Override
            public Map<String, Object> getMedicineById(Long medicineId) {
                throw new ExternalServiceException(
                        "Catalog service fallback triggered while fetching medicine " + medicineId,
                        cause);
            }

            @Override
            public Map<String, Object> getPrescriptionStatusForCurrentUser(Long prescriptionId, String authorizationHeader) {
                throw new ExternalServiceException(
                        "Catalog service fallback triggered while validating prescription status",
                        cause);
            }

            @Override
            public Map<String, Object> linkPrescriptionToOrder(
                    Long prescriptionId,
                    Long orderId,
                    String authorizationHeader) {
                throw new ExternalServiceException(
                        "Catalog service fallback triggered while linking prescription to order",
                        cause);
            }

            @Override
            public Map<String, Object> reserveInventory(String authorizationHeader, InventoryReservationRequest request) {
                throw new ExternalServiceException(
                        "Catalog service fallback triggered while reserving inventory",
                        cause);
            }
        };
    }
}