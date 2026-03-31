package com.orderanddelivery.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orderanddelivery.exception.ExternalServiceException;
import com.orderanddelivery.exception.ResourceNotFoundException;

import feign.Response;
import feign.codec.ErrorDecoder;

@Configuration
public class AuthFeignConfig {

    @Bean
    public ErrorDecoder authErrorDecoder() {
        return (String methodKey, Response response) -> {
            int status = response.status();
            String endpoint = methodKey == null ? "Auth API" : methodKey;

            if (status == 404) {
                return new ResourceNotFoundException("Resource not found in auth service: " + endpoint);
            }
            if (status == 400) {
                return new IllegalArgumentException("Bad request to auth service: " + endpoint);
            }
            if (status == 401 || status == 403) {
                return new ExternalServiceException("Unauthorized access while calling auth service: " + endpoint);
            }
            if (status >= 500) {
                return new ExternalServiceException("Auth service is currently unavailable");
            }

            return new ExternalServiceException("Unexpected response from auth service (HTTP " + status + ")");
        };
    }
}
