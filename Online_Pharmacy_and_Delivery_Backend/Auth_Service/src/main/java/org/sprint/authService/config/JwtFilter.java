package org.sprint.authService.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.sprint.authService.security.JwtTokenRevocationService;
import org.sprint.authService.util.RoleNormalizer;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_USER_ROLE = "X-User-Role";
	private static final String HEADER_USERNAME = "X-Username";

	private final JwtService jwtService;
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
			if (SecurityContextHolder.getContext().getAuthentication() == null && jwtService.validateToken(token)) {
				Claims claims = jwtService.extractAllClaims(token);

				if (jwtTokenRevocationService.isRevoked(claims.getId(), token)) {
					SecurityContextHolder.clearContext();
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("Token has been revoked");
					return;
				}

				String username = claims.getSubject();
				String roleClaim = claims.get("role", String.class);
				String normalizedRole = RoleNormalizer.normalizeOrDefault(roleClaim);
				Long userId = extractUserId(claims.get("userId"));

				if (!isForwardedIdentityConsistent(request, username, normalizedRole, userId)) {
					SecurityContextHolder.clearContext();
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("Identity header mismatch");
					return;
				}

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
						null, List.of(new SimpleGrantedAuthority(RoleNormalizer.toAuthority(normalizedRole))));

				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception ex) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private Long extractUserId(Object userIdClaim) {
		if (userIdClaim instanceof Long longValue) {
			return longValue;
		}
		if (userIdClaim instanceof Integer intValue) {
			return intValue.longValue();
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

	private boolean isForwardedIdentityConsistent(HttpServletRequest request, String username, String role,
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
			String normalizedForwardedRole = RoleNormalizer.normalize(forwardedRole);
			if (normalizedForwardedRole == null || role == null || !normalizedForwardedRole.equals(role)) {
				return false;
			}
		}

		return true;
	}
}
