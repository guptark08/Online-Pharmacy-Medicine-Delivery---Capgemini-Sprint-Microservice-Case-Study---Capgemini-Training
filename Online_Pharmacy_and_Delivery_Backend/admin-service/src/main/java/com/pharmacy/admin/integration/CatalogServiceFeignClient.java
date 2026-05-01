package com.pharmacy.admin.integration;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "catalog-service",
        url = "${catalog.service.url:http://catalog-service:8082}",
        configuration = AdminFeignConfig.class,
        fallbackFactory = CatalogServiceFeignFallbackFactory.class)
public interface CatalogServiceFeignClient {

    @GetMapping("/api/catalog/admin/stats")
    RemoteApiResponse<CatalogAdminStatsRemoteDTO> getCatalogStats(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

    @GetMapping("/api/catalog/prescriptions/pending")
    RemoteApiResponse<List<RemotePrescriptionResponse>> getPendingPrescriptions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

    @GetMapping("/api/catalog/prescriptions")
    RemoteApiResponse<List<RemotePrescriptionResponse>> getAllPrescriptions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size);

    @GetMapping("/api/catalog/prescriptions/{id}")
    RemoteApiResponse<RemotePrescriptionResponse> getPrescriptionById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("id") Long id);

    @PutMapping("/api/catalog/prescriptions/{id}/review")
    RemoteApiResponse<RemotePrescriptionResponse> reviewPrescription(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(name = "notes", required = false) String notes);
}
