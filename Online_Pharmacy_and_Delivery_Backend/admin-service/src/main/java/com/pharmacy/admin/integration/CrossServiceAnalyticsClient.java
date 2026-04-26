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

    public Optional<List<RemotePrescriptionResponse>> fetchPendingPrescriptions() {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(catalogServiceFeignClient.getPendingPrescriptions(authHeader));
        } catch (RuntimeException ex) {
            log.warn("Failed to fetch pending prescriptions: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<RemotePrescriptionResponse>> fetchAllPrescriptions(int page, int size) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(catalogServiceFeignClient.getAllPrescriptions(authHeader, page, size));
        } catch (RuntimeException ex) {
            log.warn("Failed to fetch all prescriptions: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<RemotePrescriptionResponse> reviewPrescription(Long prescriptionId, String decision, String notes) {
        String authHeader = tokenRelay.currentAuthorizationHeader();
        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(catalogServiceFeignClient.reviewPrescription(authHeader, prescriptionId, decision, notes));
        } catch (RuntimeException ex) {
            log.warn("Failed to review prescription {}: {}", prescriptionId, ex.getMessage());
            return Optional.empty();
        }
    }
}
