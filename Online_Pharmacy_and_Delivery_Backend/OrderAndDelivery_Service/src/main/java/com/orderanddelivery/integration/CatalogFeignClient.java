package com.orderanddelivery.integration;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "catalog-service",
        url = "${catalog.service.url:http://catalog-service:8082}",
        configuration = CatalogFeignConfig.class,
        fallbackFactory = CatalogFeignFallbackFactory.class)
public interface CatalogFeignClient {

    @GetMapping("/api/catalog/medicines/{id}")
    Map<String, Object> getMedicineById(@PathVariable("id") Long medicineId);

    @GetMapping("/api/catalog/prescriptions/{id}/status")
    Map<String, Object> getPrescriptionStatusForCurrentUser(
            @PathVariable("id") Long prescriptionId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

    @PutMapping("/api/catalog/prescriptions/{id}/link-order/{orderId}")
    Map<String, Object> linkPrescriptionToOrder(
            @PathVariable("id") Long prescriptionId,
            @PathVariable("orderId") Long orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

    @PutMapping("/api/catalog/prescriptions/{id}/cancel")
    Map<String, Object> cancelPrescription(
            @PathVariable("id") Long prescriptionId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

    @PostMapping("/api/catalog/inventory/reserve")
    Map<String, Object> reserveInventory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody InventoryReservationRequest request);
}
