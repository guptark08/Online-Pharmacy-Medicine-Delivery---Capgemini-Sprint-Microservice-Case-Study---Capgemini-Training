package com.pharmacy.admin.integration;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CrossServiceAnalyticsClient {

    private static final Logger log = LoggerFactory.getLogger(CrossServiceAnalyticsClient.class);

    private final OrderServiceFeignClient orderServiceFeignClient;
    private final CatalogServiceFeignClient catalogServiceFeignClient;
    private final AuthServiceFeignClient authServiceFeignClient;
    private final TokenRelay tokenRelay;

    public Optional<List<RemoteOrderResponse>> fetchAdminOrders() {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            List<RemoteOrderResponse> orders = orderServiceFeignClient.getAdminOrders(authHeader, 0, 1000);
            return Optional.ofNullable(orders);
        } catch (RuntimeException ex) {
            log.warn("Falling back to local order metrics. Remote order fetch failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RemoteOrderResponse> fetchAdminOrderById(Long orderId) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(orderServiceFeignClient.getAdminOrderById(authHeader, orderId));
        } catch (RuntimeException ex) {
            log.warn("Failed to fetch order {}: {}", orderId, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RemoteOrderResponse> updateOrderStatus(Long orderId, String status) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(orderServiceFeignClient.updateOrderStatus(authHeader, orderId, status));
        } catch (RuntimeException ex) {
            log.warn("Failed to update order status {}: {}", orderId, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RemoteOrderResponse> cancelOrder(Long orderId, String reason) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(orderServiceFeignClient.cancelOrder(authHeader, orderId, reason));
        } catch (RuntimeException ex) {
            log.warn("Failed to cancel order {}: {}", orderId, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<CatalogAdminStatsRemoteDTO> fetchCatalogStats() {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            RemoteApiResponse<CatalogAdminStatsRemoteDTO> response = catalogServiceFeignClient.getCatalogStats(authHeader);
            if (response == null || response.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getData());
        } catch (RuntimeException ex) {
            log.warn("Falling back to local catalog metrics. Remote catalog fetch failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RemoteUserResponse> fetchUserById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            RemoteApiResponse<RemoteUserResponse> response = authServiceFeignClient.getUserById(authHeader, userId);
            if (response == null || response.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getData());
        } catch (RuntimeException ex) {
            log.warn("Failed to fetch user {}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<RemotePrescriptionResponse>> fetchPendingPrescriptions() {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            log.warn("No authorization header found for pending prescriptions request");
            return Optional.empty();
        }

        try {
            log.info("Fetching pending prescriptions from catalog-service");
            RemoteApiResponse<List<RemotePrescriptionResponse>> response = catalogServiceFeignClient.getPendingPrescriptions(authHeader);
            if (response == null) {
                log.warn("Received null response from catalog-service for pending prescriptions");
                return Optional.empty();
            }
            log.info("Received {} pending prescriptions from catalog-service", 
                     response.getData() != null ? response.getData().size() : "null");
            if (response.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getData());
        } catch (RuntimeException ex) {
            log.error("Failed to fetch pending prescriptions from catalog-service: {}", ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public Optional<List<RemotePrescriptionResponse>> fetchAllPrescriptions(int page, int size) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            RemoteApiResponse<List<RemotePrescriptionResponse>> response = catalogServiceFeignClient.getAllPrescriptions(authHeader, page, size);
            if (response == null || response.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getData());
        } catch (RuntimeException ex) {
            log.warn("Failed to fetch all prescriptions: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RemotePrescriptionResponse> fetchPrescriptionById(Long prescriptionId) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            RemoteApiResponse<RemotePrescriptionResponse> response = catalogServiceFeignClient.getPrescriptionById(authHeader, prescriptionId);
            if (response == null || response.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getData());
        } catch (RuntimeException ex) {
            log.warn("Failed to fetch prescription {}: {}", prescriptionId, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RemotePrescriptionResponse> reviewPrescription(Long prescriptionId, String decision, String notes) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            RemoteApiResponse<RemotePrescriptionResponse> response = catalogServiceFeignClient.reviewPrescription(authHeader, prescriptionId, decision, notes);
            if (response == null || response.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(response.getData());
        } catch (RuntimeException ex) {
            log.warn("Failed to review prescription {}: {}", prescriptionId, ex.getMessage());
            return Optional.empty();
        }
    }
}
