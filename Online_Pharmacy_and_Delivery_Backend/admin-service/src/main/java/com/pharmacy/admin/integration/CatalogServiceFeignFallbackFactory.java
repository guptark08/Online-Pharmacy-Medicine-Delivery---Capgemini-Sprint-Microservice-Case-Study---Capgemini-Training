package com.pharmacy.admin.integration;

import java.util.List;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CatalogServiceFeignFallbackFactory implements FallbackFactory<CatalogServiceFeignClient> {

    @Override
    public CatalogServiceFeignClient create(Throwable cause) {
        return new CatalogServiceFeignClient() {
            @Override
            public RemoteApiResponse<CatalogAdminStatsRemoteDTO> getCatalogStats(String authorizationHeader) {
                return null;
            }

            @Override
            public RemoteApiResponse<List<RemotePrescriptionResponse>> getPendingPrescriptions(String authorizationHeader) {
                return null;
            }

            @Override
            public RemoteApiResponse<List<RemotePrescriptionResponse>> getAllPrescriptions(String authorizationHeader, int page, int size) {
                return null;
            }

            @Override
            public RemoteApiResponse<RemotePrescriptionResponse> getPrescriptionById(String authorizationHeader, Long id) {
                return null;
            }

            @Override
            public RemoteApiResponse<RemotePrescriptionResponse> reviewPrescription(String authorizationHeader, Long id, String status, String notes) {
                return null;
            }
        };
    }
}
