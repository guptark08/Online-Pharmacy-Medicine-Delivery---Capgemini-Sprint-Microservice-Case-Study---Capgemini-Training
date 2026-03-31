package com.pharmacy.admin.security;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_USERNAME = "X-Username";

    private final JwtUtil jwtUtil;
    private final JwtTokenRevocationService jwtTokenRevocationService;

    public JwtAuthFilter(JwtUtil jwtUtil, JwtTokenRevocationService jwtTokenRevocationService) {
        this.jwtUtil = jwtUtil;
        this.jwtTokenRevocationService = jwtTokenRevocationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {
            if (!jwtUtil.validateToken(token)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            if (jwtTokenRevocationService != null && jwtTokenRevocationService.isRevoked(claims.getId(), token)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been revoked");
                return;
            }

            String username = claims.getSubject();
            String role = jwtUtil.extractRole(token);
            Long userId = jwtUtil.extractUserId(token);

            if (!isForwardedIdentityConsistent(request, username, role, userId)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Identity header mismatch");
                return;
            }

            if (username == null || role == null || role.isBlank()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                JwtUserPrincipal principal = new JwtUserPrincipal(userId, username, role);
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.debug("Unable to set authentication from JWT: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
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
            String normalizedRole = normalizeRole(role);
            if (normalizedForwardedRole == null || normalizedRole == null || !normalizedForwardedRole.equals(normalizedRole)) {
                return false;
            }
        }

        return true;
    }
}
