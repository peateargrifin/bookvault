package com.bookvault.bookvault.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// 📘 CONCEPT: Video 10 - Middleware (Filter) in request lifecycle
// Runs FIRST before any other filter including Spring Security
// 📘 CONCEPT: Video 18 - Request logging for observability
// 🟡 NOVICE: no logging → "why is this endpoint slow?" → impossible to debug
// 🏢 PRODUCT: every request logged with timing → instant debugging
//             Swiggy/Zomato log millions of requests/day this way
//             When prod issue happens → grep by requestId → full trace instantly

// @Order(1) = runs before all other filters including JwtAuthFilter
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // 📘 CONCEPT: Video 18 - Request ID (Trace concept)
        // Every request gets a unique ID — stored in MDC
        // MDC = Mapped Diagnostic Context (thread-local log context)
        // Every log.info/warn/error in this request thread includes requestId
        // 🟡 NOVICE: no request ID → 1000 concurrent requests → logs mixed up
        //             impossible to trace one user's full request journey
        // 🏢 PRODUCT: requestId ties together ALL logs for one request
        //             Controller log + Service log + Repository log = same ID
        //             In New Relic: search requestId → see entire request trace
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        // Add requestId to response headers so client can reference it
        // 📘 CONCEPT: Video 20 - X-Request-Id for support/debugging
        // When user reports an issue → they give you X-Request-Id
        // You search logs by that ID → instantly find exactly what happened
        response.setHeader("X-Request-Id", requestId);

        String method = request.getMethod();
        String uri    = request.getRequestURI();
        String query  = request.getQueryString();
        String fullUri = query != null ? uri + "?" + query : uri;

        // 📘 CONCEPT: Video 18 - Structured log fields
        // Log at DEBUG for request start (too noisy for prod INFO level)
        log.debug("REQUEST_START method={} uri={} ip={}",
                method, fullUri, getClientIp(request));

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status    = response.getStatus();

            // 📘 CONCEPT: Video 18 - Log level based on response status
            // 2xx → DEBUG (normal, don't clutter prod logs)
            // 4xx → WARN  (client error, worth noting)
            // 5xx → ERROR (server error, always want to see this)
            // 🏢 PRODUCT: Grafana/New Relic alerts trigger on ERROR logs
            //             5xx rate > 1% → PagerDuty alert to on-call engineer
            if (status >= 500) {
                log.error("REQUEST_END method={} uri={} status={} duration={}ms",
                        method, fullUri, status, duration);
            } else if (status >= 400) {
                log.warn("REQUEST_END method={} uri={} status={} duration={}ms",
                        method, fullUri, status, duration);
            } else {
                log.debug("REQUEST_END method={} uri={} status={} duration={}ms",
                        method, fullUri, status, duration);
            }

            // 📘 CONCEPT: Video 18 - Always clean MDC after request
            // Without this: MDC from request A bleeds into request B
            // on same thread (thread pool reuses threads)
            MDC.clear();
        }
    }

    // Skip logging for actuator/health (called every 10s by load balancer)
    // 📘 CONCEPT: Video 18 - Avoid log noise from health checks
    // 🏢 PRODUCT: health checks run every 10s → 8,640 useless log lines/day
    //             filter them out → logs contain only meaningful traffic
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/health")
            || uri.startsWith("/actuator/info");
    }

    private String getClientIp(HttpServletRequest request) {
        // 📘 CONCEPT: Video 20 - Real IP behind load balancer/proxy
        // 🟡 NOVICE: request.getRemoteAddr() → always returns load balancer IP
        // 🏢 PRODUCT: X-Forwarded-For header contains real client IP
        //             set by Nginx/AWS ALB before forwarding to your server
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
