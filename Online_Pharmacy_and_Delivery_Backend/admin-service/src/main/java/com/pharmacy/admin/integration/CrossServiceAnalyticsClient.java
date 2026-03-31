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
}
