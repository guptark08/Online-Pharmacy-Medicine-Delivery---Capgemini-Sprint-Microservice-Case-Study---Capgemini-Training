package com.pharmacy.admin.integration;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
public class TokenRelay {

    public String currentAuthorizationHeader() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            log.warn("No servlet request attributes found");
            return null;
        }

        HttpServletRequest request = servletAttributes.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            log.warn("Authorization header is missing or blank. Available headers: {}", Collections.list(request.getHeaderNames()));
            return null;
        }

        log.info("Authorization header found, length: {}", authorization.length());
        return authorization;
    }
}
