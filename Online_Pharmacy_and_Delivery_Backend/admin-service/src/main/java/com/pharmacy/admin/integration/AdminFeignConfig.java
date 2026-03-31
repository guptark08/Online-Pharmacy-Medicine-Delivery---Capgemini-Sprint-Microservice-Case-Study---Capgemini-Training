package com.pharmacy.admin.integration;

import com.pharmacy.admin.exception.BadRequestException;
import com.pharmacy.admin.exception.ResourceNotFoundException;

import feign.Response;
import feign.codec.ErrorDecoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminFeignConfig {

    @Bean
    public ErrorDecoder adminFeignErrorDecoder() {
        return (String methodKey, Response response) -> {
            int status = response.status();
            String endpoint = methodKey == null ? "remote service" : methodKey;

            if (status == 404) {
                return new ResourceNotFoundException("Remote endpoint not found: " + endpoint);
            }
            if (status == 400) {
                return new BadRequestException("Invalid request while calling: " + endpoint);
            }
            if (status == 401 || status == 403) {
                return new BadRequestException("Authorization failed while calling: " + endpoint);
            }
            return new IllegalStateException("Remote service call failed (HTTP " + status + ") for " + endpoint);
        };
    }
}
