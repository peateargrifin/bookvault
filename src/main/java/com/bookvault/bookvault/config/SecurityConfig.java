package com.bookvault.bookvault.config;

import com.bookvault.bookvault.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
// 📘 CONCEPT: Video 8 + Video 20 - Enable @PreAuthorize on methods
// Allows: @PreAuthorize("hasRole('ADMIN')") on controller methods
// This is what prevents BFLA (broken function level authorization)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/info",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // 📘 CONCEPT: Video 20 - Default deny
                // Everything not explicitly permitted is blocked
                // New endpoints are protected by default → safe by default
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 📘 CONCEPT: Video 20 - BCrypt slow hashing
        // Default strength = 10 → ~100ms to hash
        // 🟡 NOVICE: store plain text or use MD5/SHA256 (instant to crack)
        // 🏢 PRODUCT: BCrypt → billions of guesses/second → decades to crack
        return new BCryptPasswordEncoder();
    }
}
