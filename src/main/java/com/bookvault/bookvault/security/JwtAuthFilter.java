package com.bookvault.bookvault.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 📘 CONCEPT: Video 10 - Middleware (Filter in Spring Boot)
// Runs BEFORE every request reaches any controller
// 📘 CONCEPT: Video 8 - Stateless JWT authentication
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                // 📘 CONCEPT: Video 10 - Request Context
                // Store authenticated user in SecurityContext
                // Controllers/Services read from here — NEVER from request body
                // 📘 CONCEPT: Video 20 - User ID always from verified JWT
                // Client cannot inject a different userId — it's in the signed token
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContextHolder.getContext().setAuthentication(auth);

                // 📘 CONCEPT: Video 18 - Add userId to MDC for log correlation
                // Every log line in this request will include the userId
                org.slf4j.MDC.put("userId", userId);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 📘 CONCEPT: Video 18 - Clean MDC after request
            // Prevent MDC bleeding into next request on same thread
            org.slf4j.MDC.remove("userId");
        }
    }
}
