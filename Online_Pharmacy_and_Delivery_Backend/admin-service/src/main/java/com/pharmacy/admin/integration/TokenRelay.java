package com.pharmacy.admin.integration;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class TokenRelay {

    public String currentAuthorizationHeader() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        HttpServletRequest request = servletAttributes.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        return authorization;
    }
}
