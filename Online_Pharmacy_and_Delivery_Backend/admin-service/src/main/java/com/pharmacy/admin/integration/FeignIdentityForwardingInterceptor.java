package com.pharmacy.admin.integration;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class FeignIdentityForwardingInterceptor implements RequestInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_USERNAME = "X-Username";

    @Override
    public void apply(RequestTemplate template) {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            
            String userId = request.getHeader(HEADER_USER_ID);
            String userRole = request.getHeader(HEADER_USER_ROLE);
            String username = request.getHeader(HEADER_USERNAME);
            
            if (userId != null) {
                template.header(HEADER_USER_ID, userId);
            }
            if (userRole != null) {
                template.header(HEADER_USER_ROLE, userRole);
            }
            if (username != null) {
                template.header(HEADER_USERNAME, username);
            }
        }
    }
}