package com.pharmacy.admin.integration;

import java.util.List;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceFeignFallbackFactory implements FallbackFactory<OrderServiceFeignClient> {

    @Override
    public OrderServiceFeignClient create(Throwable cause) {
        return new OrderServiceFeignClient() {
            @Override
            public List<RemoteOrderResponse> getAdminOrders(String authorizationHeader, int page, int size) {
                return List.of();
            }

            @Override
            public RemoteOrderResponse getAdminOrderById(String authorizationHeader, Long id) {
                return null;
            }

            @Override
            public RemoteOrderResponse updateOrderStatus(String authorizationHeader, Long id, String status) {
                return null;
            }

            @Override
            public RemoteOrderResponse cancelOrder(String authorizationHeader, Long id, String reason) {
                return null;
            }
        };
    }
}