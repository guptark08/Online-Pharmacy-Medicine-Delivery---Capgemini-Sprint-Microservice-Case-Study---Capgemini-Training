package com.pharmacy.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class SimpleRateLimitFilter implements GlobalFilter, Ordered {

    private static final long WINDOW_MILLIS = 60_000L;

    private final Map<String, SlidingWindowCounter> counters = new ConcurrentHashMap<>();

    @Value("${gateway.ratelimit.auth-per-minute:120}")
    private int authRequestsPerMinute;

    @Value("${gateway.ratelimit.catalog-per-minute:300}")
    private int catalogRequestsPerMinute;

    @Value("${gateway.ratelimit.order-per-minute:150}")
    private int orderRequestsPerMinute;

    @Value("${gateway.ratelimit.admin-per-minute:60}")
    private int adminRequestsPerMinute;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        String routeKey = resolveRouteKey(path);
        if (routeKey == null) {
            return chain.filter(exchange);
        }

        int limit = resolveLimit(routeKey);
        if (limit <= 0) {
            return chain.filter(exchange);
        }

        String clientIp = extractClientIp(request);
        String counterKey = routeKey + ':' + clientIp;

        SlidingWindowCounter counter = counters.computeIfAbsent(counterKey, key -> new SlidingWindowCounter());
        if (!counter.tryAcquire(limit)) {
            return tooManyRequests(exchange, "Rate limit exceeded for " + routeKey + " route");
        }

        if (counters.size() > 20_000) {
            counters.entrySet().removeIf(entry -> entry.getValue().isStale());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private String resolveRouteKey(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("/api/auth/")) {
            return "auth";
        }
        if (path.startsWith("/api/catalog/")) {
            return "catalog";
        }
        if (path.startsWith("/api/orders/")) {
            return "order";
        }
        if (path.startsWith("/api/admin/")) {
            return "admin";
        }
        return null;
    }

    private int resolveLimit(String routeKey) {
        return switch (routeKey.toLowerCase(Locale.ROOT)) {
            case "auth" -> authRequestsPerMinute;
            case "catalog" -> catalogRequestsPerMinute;
            case "order" -> orderRequestsPerMinute;
            case "admin" -> adminRequestsPerMinute;
            default -> 0;
        };
    }

    private String extractClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        var remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String payload = "{\"success\":false,\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(payload.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private static final class SlidingWindowCounter {
        private final Deque<Long> requestTimes = new ArrayDeque<>();

        synchronized boolean tryAcquire(int limit) {
            long now = System.currentTimeMillis();
            long windowStart = now - WINDOW_MILLIS;
            while (!requestTimes.isEmpty() && requestTimes.peekFirst() < windowStart) {
                requestTimes.pollFirst();
            }

            if (requestTimes.size() >= limit) {
                return false;
            }

            requestTimes.addLast(now);
            return true;
        }

        synchronized boolean isStale() {
            if (requestTimes.isEmpty()) {
                return true;
            }
            return requestTimes.peekLast() < (System.currentTimeMillis() - WINDOW_MILLIS);
        }
    }
}
