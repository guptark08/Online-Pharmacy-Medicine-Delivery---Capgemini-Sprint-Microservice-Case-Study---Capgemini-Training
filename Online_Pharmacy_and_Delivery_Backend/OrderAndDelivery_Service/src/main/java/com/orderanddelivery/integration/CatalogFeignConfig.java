package com.orderanddelivery.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orderanddelivery.exception.ExternalServiceException;
import com.orderanddelivery.exception.InvalidOrderStateException;
import com.orderanddelivery.exception.ResourceNotFoundException;

import feign.Response;
import feign.codec.ErrorDecoder;

@Configuration
public class CatalogFeignConfig {

    @Bean
    public ErrorDecoder catalogErrorDecoder() {
        return (String methodKey, Response response) -> {
            int status = response.status();
            String endpoint = methodKey == null ? "Catalog API" : methodKey;

            if (status == 404) {
                return new ResourceNotFoundException("Resource not found in catalog service: " + endpoint);
            }
            if (status == 400) {
                return new IllegalArgumentException("Bad request to catalog service: " + endpoint);
            }
            if (status == 409) {
                return new InvalidOrderStateException("Catalog operation conflict: " + endpoint);
            }
            if (status == 401 || status == 403) {
                return new ExternalServiceException("Unauthorized access while calling catalog service: " + endpoint);
            }
            if (status >= 500) {
                return new ExternalServiceException("Catalog service is currently unavailable");
            }

            return new ExternalServiceException("Unexpected response from catalog service (HTTP " + status + ")");
        };
    }
}
