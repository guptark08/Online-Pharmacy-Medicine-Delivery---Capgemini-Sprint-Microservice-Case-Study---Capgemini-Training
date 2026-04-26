package com.orderanddelivery.integration;

import java.util.Map;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.orderanddelivery.exception.ExternalServiceException;

@Component
public class AuthFeignFallbackFactory implements FallbackFactory<AuthFeignClient> {

    @Override
    public AuthFeignClient create(Throwable cause) {
        return new AuthFeignClient() {
            @Override
            public Map<String, Object> getUserById(String authorizationHeader, Long userId) {
                throw new ExternalServiceException(
                        "Auth service fallback triggered while fetching user by id",
                        cause);
            }

            @Override
            public Map<String, Object> getCurrentUserAddresses(String authorizationHeader) {
                throw new ExternalServiceException(
                        "Auth service fallback triggered while fetching user addresses",
                        cause);
            }

            @Override
            public Map<String, Object> addAddress(String authorizationHeader, Map<String, Object> requestBody) {
                throw new ExternalServiceException(
                        "Auth service fallback triggered while adding user address",
                        cause);
            }
        };
    }
}
