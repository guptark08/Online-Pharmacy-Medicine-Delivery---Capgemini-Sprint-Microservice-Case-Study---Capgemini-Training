package com.pharmacy.apigateway.swagger;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtRoleResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void initialize() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public SwaggerUserContext resolveFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return SwaggerUserContext.anonymous();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return SwaggerUserContext.anonymous();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = Optional.ofNullable(claims.get("role", String.class)).orElse("CUSTOMER");
            if (claims.getSubject() == null || claims.getExpiration() == null) {
                return SwaggerUserContext.anonymous();
            }
            return SwaggerUserContext.authenticated(role);
        } catch (JwtException | IllegalArgumentException ex) {
            return SwaggerUserContext.anonymous();
        }
    }

    public SwaggerUserContext resolveFromHeaders(HttpHeaders headers) {
        return resolveFromAuthorizationHeader(headers.getFirst(HttpHeaders.AUTHORIZATION));
    }
}
