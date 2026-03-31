package com.pharmacy.admin.integration;

import java.util.List;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceFeignFallbackFactory implements FallbackFactory<OrderServiceFeignClient> {

    @Override
    public OrderServiceFeignClient create(Throwable cause) {
        return (authorizationHeader, page, size) -> List.of();
    }
}
