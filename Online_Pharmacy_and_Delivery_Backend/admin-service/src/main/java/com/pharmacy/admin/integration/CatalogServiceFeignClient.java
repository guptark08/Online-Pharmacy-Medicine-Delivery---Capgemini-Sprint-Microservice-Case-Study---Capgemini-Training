package com.pharmacy.admin.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "catalog-service",
        url = "${catalog.service.url:http://catalog-service:8082}",
        configuration = AdminFeignConfig.class,
        fallbackFactory = CatalogServiceFeignFallbackFactory.class)
public interface CatalogServiceFeignClient {

    @GetMapping("/api/catalog/admin/stats")
    RemoteApiResponse<CatalogAdminStatsRemoteDTO> getCatalogStats(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
}
