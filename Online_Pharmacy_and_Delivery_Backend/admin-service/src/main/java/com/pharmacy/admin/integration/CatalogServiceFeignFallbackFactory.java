package com.pharmacy.admin.integration;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CatalogServiceFeignFallbackFactory implements FallbackFactory<CatalogServiceFeignClient> {

    @Override
    public CatalogServiceFeignClient create(Throwable cause) {
        return authorizationHeader -> null;
    }
}
