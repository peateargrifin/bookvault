package com.bookvault.bookvault.middleware;

import com.bookvault.bookvault.config.RateLimitConfig;
import com.bookvault.bookvault.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

// 📘 CONCEPT: Video 13 + Video 14 - Rate limiting via Redis counters
// 📘 CONCEPT: Video 20 - Security: prevent brute force attacks
// 🟡 NOVICE: no rate limiting → bot tries 1M passwords/second → accounts compromised
// 🏢 PRODUCT: layered rate limiting:
//             Layer 1: per-IP (100 req/min) → stops bots using single IP
//             Layer 2: stricter on /auth (10 req/min) → stops password brute force
@Component
@Order(2) // runs after RequestLoggingFilter (order 1) but before JwtAuthFilter
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final CacheService cacheService;
    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip  = getClientIp(request);
        String uri = request.getRequestURI();

        // 📘 CONCEPT: Video 20 - Stricter limits on auth endpoints
        // General API: 100 req/min
        // Auth endpoints: 10 req/min (brute force protection)
        boolean isAuthEndpoint = uri.startsWith("/api/v1/auth");
        int limit = isAuthEndpoint
                ? 10
                : rateLimitConfig.getRequestsPerMinute();

        String cacheKey = isAuthEndpoint
                ? "rate_limit:auth:" + ip
                : "rate_limit:api:"  + ip;

        // 📘 CONCEPT: Video 13 - Redis INCR with TTL
        // Atomic increment → no race conditions under high load
        // TTL = 1 minute window → counter resets automatically
        long requestCount = cacheService.incrementWithTtl(
                cacheKey, Duration.ofMinutes(1));

        // Add rate limit headers (like GitHub API does)
        // 📘 CONCEPT: Video 11 - Informative response headers
        // Client knows how many requests they have left
        response.setHeader("X-RateLimit-Limit",
                String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, limit - requestCount)));

        if (requestCount > limit) {
            // 📘 CONCEPT: Video 5 - 429 Too Many Requests
            // 📘 CONCEPT: Video 20 - Rate limiting as security mechanism
            log.warn("RATE_LIMIT_EXCEEDED ip={} uri={} count={}",
                    ip, uri, requestCount);

            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                Map.of(
                    "status", 429,
                    "message", "Too many requests. Please slow down.",
                    "timestamp", OffsetDateTime.now().toString()
                )
            ));
            return; // stop filter chain — don't process request
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Skip rate limiting for health checks and swagger
        return uri.startsWith("/actuator")
            || uri.startsWith("/swagger-ui")
            || uri.startsWith("/api-docs")
            || uri.startsWith("/v3/api-docs");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
