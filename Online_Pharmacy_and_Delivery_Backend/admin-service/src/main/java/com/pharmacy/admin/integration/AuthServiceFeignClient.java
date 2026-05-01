package com.pharmacy.admin.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url:http://auth-service:8081}",
        configuration = AdminFeignConfig.class,
        fallbackFactory = AuthServiceFeignFallbackFactory.class)
public interface AuthServiceFeignClient {

    @GetMapping("/api/auth/users/{id}")
    RemoteApiResponse<RemoteUserResponse> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("id") Long id);
}
