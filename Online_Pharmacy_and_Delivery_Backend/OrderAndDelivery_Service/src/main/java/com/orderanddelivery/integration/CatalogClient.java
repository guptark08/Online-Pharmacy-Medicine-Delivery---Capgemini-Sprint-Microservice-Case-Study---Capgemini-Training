package com.orderanddelivery.integration;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.orderanddelivery.entities.CartItem;
import com.orderanddelivery.exception.ExternalServiceException;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.exception.ResourceNotFoundException;

@Component
public class CatalogClient {

    private final CatalogFeignClient catalogFeignClient;

    public CatalogClient(CatalogFeignClient catalogFeignClient) {
        this.catalogFeignClient = catalogFeignClient;
    }

    public CatalogMedicineSnapshot fetchMedicineSnapshot(Long medicineId) {
        if (medicineId == null) {
            throw new IllegalArgumentException("Medicine ID is required");
        }

        try {
            Map<String, Object> root = catalogFeignClient.getMedicineById(medicineId);
            Map<String, Object> data = asMap(root == null ? null : root.get("data"), "medicine");

            String name = Objects.toString(data.get("name"), null);
            if (name == null || name.isBlank()) {
                throw new ResourceNotFoundException("Medicine details unavailable for id: " + medicineId);
            }

            BigDecimal basePrice = toBigDecimal(data.get("price"));
            BigDecimal discountedPrice = toBigDecimal(data.get("discountedPrice"));
            BigDecimal finalPrice = (discountedPrice != null && discountedPrice.compareTo(BigDecimal.ZERO) > 0)
                    ? discountedPrice
                    : basePrice;

            if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ExternalServiceException("Catalog returned invalid price for medicine id: " + medicineId);
            }

            boolean requiresPrescription = toBoolean(data.get("requiresPrescription"));
            String status = Objects.toString(data.get("status"), "AVAILABLE").toUpperCase(Locale.ROOT);
            Integer stock = toInteger(data.get("stock"));

            if ("DISCONTINUED".equals(status) || (stock != null && stock <= 0)) {
                throw new InvalidOrderStateException("Medicine is currently unavailable");
            }

            return new CatalogMedicineSnapshot(medicineId, name, finalPrice, requiresPrescription);

        } catch (ResourceNotFoundException | InvalidOrderStateException | ExternalServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("Catalog service is unavailable. Please retry.", ex);
        }
    }

    public String fetchPrescriptionStatusForUser(Long prescriptionId, String bearerToken) {
        if (prescriptionId == null) {
            throw new IllegalArgumentException("Prescription ID is required");
        }
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("Authorization token is required for prescription validation");
        }

        try {
            Map<String, Object> root = catalogFeignClient.getPrescriptionStatusForCurrentUser(
                    prescriptionId,
                    "Bearer " + bearerToken);

            Object data = root == null ? null : root.get("data");
            if (!(data instanceof String statusValue) || statusValue.isBlank()) {
                throw new ExternalServiceException("Unexpected prescription status response from catalog service");
            }

            return statusValue.toUpperCase(Locale.ROOT);
        } catch (ResourceNotFoundException | ExternalServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("Unable to validate prescription with catalog service", ex);
        }
    }

    public void linkPrescriptionToOrder(Long prescriptionId, Long orderId, String bearerToken) {
        if (prescriptionId == null) {
            throw new IllegalArgumentException("Prescription ID is required for order linkage");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required for prescription linkage");
        }
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("Authorization token is required for prescription linkage");
        }

        try {
            Map<String, Object> response = catalogFeignClient.linkPrescriptionToOrder(
                    prescriptionId,
                    orderId,
                    "Bearer " + bearerToken);

            Object success = response == null ? null : response.get("success");
            if (success instanceof Boolean successValue && !successValue) {
                String message = Objects.toString(response.get("message"), "Prescription linkage failed");
                throw new InvalidOrderStateException(message);
            }
        } catch (ResourceNotFoundException | InvalidOrderStateException | ExternalServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("Unable to link prescription to order", ex);
        }
    }

    public void reserveInventoryForCheckout(List<CartItem> cartItems, String bearerToken) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart items are required for inventory reservation");
        }
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("Authorization token is required for inventory reservation");
        }

        Map<Long, Integer> quantityByMedicine = new LinkedHashMap<>();
        for (CartItem item : cartItems) {
            if (item.getMedicineId() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Cart contains invalid medicine or quantity");
            }
            quantityByMedicine.merge(item.getMedicineId(), item.getQuantity(), Integer::sum);
        }

        List<InventoryReservationRequest.LineItem> lineItems = quantityByMedicine.entrySet().stream()
                .map(entry -> InventoryReservationRequest.LineItem.builder()
                        .medicineId(entry.getKey())
                        .quantity(entry.getValue())
                        .build())
                .toList();

        InventoryReservationRequest request = InventoryReservationRequest.builder()
                .orderId(null)
                .items(lineItems)
                .build();

        try {
            Map<String, Object> response = catalogFeignClient.reserveInventory("Bearer " + bearerToken, request);

            Object success = response == null ? null : response.get("success");
            if (success instanceof Boolean successValue && !successValue) {
                String message = Objects.toString(response.get("message"), "Inventory reservation failed");
                throw new InvalidOrderStateException(message);
            }
        } catch (ResourceNotFoundException | InvalidOrderStateException | ExternalServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("Unable to reserve inventory with catalog service", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value, String resourceName) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new ExternalServiceException("Unexpected " + resourceName + " response payload from catalog service");
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Integer intValue) {
                return BigDecimal.valueOf(intValue);
            }
            if (value instanceof Long longValue) {
                return BigDecimal.valueOf(longValue);
            }
            if (value instanceof Double doubleValue) {
                return BigDecimal.valueOf(doubleValue);
            }
            if (value instanceof BigDecimal decimalValue) {
                return decimalValue;
            }
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            throw new ExternalServiceException("Invalid numeric value received from catalog service", ex);
        }
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer intValue) {
            return intValue;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        return Integer.parseInt(value.toString());
    }
}