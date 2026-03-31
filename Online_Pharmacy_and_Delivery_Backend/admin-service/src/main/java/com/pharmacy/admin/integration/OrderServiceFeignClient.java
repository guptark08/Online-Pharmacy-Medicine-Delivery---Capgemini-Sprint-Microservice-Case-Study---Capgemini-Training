package com.pharmacy.admin.integration;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "order-service",
        url = "${order.service.url:http://order-service:8083}",
        configuration = AdminFeignConfig.class,
        fallbackFactory = OrderServiceFeignFallbackFactory.class)
public interface OrderServiceFeignClient {

    @GetMapping("/api/orders/admin")
    List<RemoteOrderResponse> getAdminOrders(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "500") int size);
}
