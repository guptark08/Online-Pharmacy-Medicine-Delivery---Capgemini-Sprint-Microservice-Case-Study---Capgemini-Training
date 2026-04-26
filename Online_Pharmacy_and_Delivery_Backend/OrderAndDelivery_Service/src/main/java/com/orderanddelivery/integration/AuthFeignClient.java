package com.orderanddelivery.integration;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url:http://auth-service:8081}",
        configuration = AuthFeignConfig.class,
        fallbackFactory = AuthFeignFallbackFactory.class)
public interface AuthFeignClient {

    @GetMapping("/api/auth/users/{id}")
    Map<String, Object> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("id") Long userId);

    @GetMapping("/api/address")
    Map<String, Object> getCurrentUserAddresses(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

    @PostMapping("/api/address")
    Map<String, Object> addAddress(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody Map<String, Object> requestBody);
}
