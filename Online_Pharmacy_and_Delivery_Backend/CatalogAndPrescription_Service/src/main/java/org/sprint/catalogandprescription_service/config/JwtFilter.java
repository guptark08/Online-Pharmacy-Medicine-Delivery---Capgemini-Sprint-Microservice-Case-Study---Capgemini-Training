package org.sprint.catalogandprescription_service.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.sprint.catalogandprescription_service.security.JwtTokenRevocationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_USERNAME = "X-Username";

    @Value("${jwt.secret}")
    private String secretKey;

    private final JwtTokenRevocationService jwtTokenRevocationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (jwtTokenRevocationService.isRevoked(claims.getId(), token)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been revoked");
                return;
            }

            String username = claims.getSubject();
            String role = normalizeRole((String) claims.get("role"));
            Long userId = extractUserId(claims.get("userId"));
            String email = claims.get("email", String.class);

            if (!isForwardedIdentityConsistent(request, username, role, userId)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Identity header mismatch");
                return;
            }

            if (username != null && role != null && userId != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                JwtUserPrincipal principal = new JwtUserPrincipal(userId, username, role, email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            log.warn("Invalid JWT token: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Long extractUserId(Object userIdClaim) {
        if (userIdClaim instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (userIdClaim instanceof Long longValue) {
            return longValue;
        }
        if (userIdClaim instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String trimmed = role.trim().toUpperCase(Locale.ROOT);
        return trimmed.startsWith("ROLE_") ? trimmed.substring(5) : trimmed;
    }

    private boolean isForwardedIdentityConsistent(
            HttpServletRequest request,
            String username,
            String role,
            Long userId) {

        String forwardedUserId = request.getHeader(HEADER_USER_ID);
        String forwardedRole = request.getHeader(HEADER_USER_ROLE);
        String forwardedUsername = request.getHeader(HEADER_USERNAME);

        if (forwardedUserId == null && forwardedRole == null && forwardedUsername == null) {
            return true;
        }

        if (forwardedUserId != null && (userId == null || !forwardedUserId.equals(String.valueOf(userId)))) {
            return false;
        }

        if (forwardedUsername != null && (username == null || !forwardedUsername.equals(username))) {
            return false;
        }

        if (forwardedRole != null) {
            String normalizedForwardedRole = normalizeRole(forwardedRole);
            if (normalizedForwardedRole == null || role == null || !normalizedForwardedRole.equals(role)) {
                return false;
            }
        }

        return true;
    }
}
