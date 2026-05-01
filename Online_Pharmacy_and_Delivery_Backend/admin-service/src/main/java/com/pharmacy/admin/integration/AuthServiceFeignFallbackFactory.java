package com.pharmacy.admin.integration;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceFeignFallbackFactory implements FallbackFactory<AuthServiceFeignClient> {

    @Override
    public AuthServiceFeignClient create(Throwable cause) {
        return new AuthServiceFeignClient() {
            @Override
            public RemoteApiResponse<RemoteUserResponse> getUserById(String authorizationHeader, Long id) {
                return null;
            }
        };
    }
}
