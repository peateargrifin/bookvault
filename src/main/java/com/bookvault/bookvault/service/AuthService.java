package com.bookvault.bookvault.service;

import com.bookvault.bookvault.config.JwtConfig;
import com.bookvault.bookvault.dto.request.LoginRequest;
import com.bookvault.bookvault.dto.request.RegisterRequest;
import com.bookvault.bookvault.dto.response.AuthResponse;
import com.bookvault.bookvault.entity.Author;
import com.bookvault.bookvault.entity.User;
import com.bookvault.bookvault.exception.BusinessException;
import com.bookvault.bookvault.repository.UserRepository;
import com.bookvault.bookvault.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bookvault.bookvault.repository.AuthorRepository;

import java.time.OffsetDateTime;

// 📘 CONCEPT: Video 10 - Service Layer
// Pure business logic — NO knowledge of HTTP, request/response objects
// 🟡 NOVICE: put auth logic directly in controller
// 🏢 PRODUCT: service is independently testable, reusable across controllers
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final EmailJobService emailJobService;


    @Transactional
    public AuthResponse register(RegisterRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        throw new BusinessException("Registration failed. Please try again.");
    }

    User user = User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .role(User.UserRole.USER)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .isActive(true)
            .build();

    User saved = userRepository.save(user);

    // 📘 CONCEPT: Video 10 - Service orchestrating multiple repositories
    // One business action (register) touches two tables (users + authors)
    // 🟡 NOVICE: force user to make separate API call to create author profile
    // 🏢 PRODUCT: auto-create author profile in same transaction
    //             @Transactional ensures BOTH succeed or BOTH rollback
    //             If author creation fails → user also not created → clean state
    Author author = Author.builder()
            .id(saved.getId())  // ← SAME UUID as user
            .name(request.getFullName())
            .email(request.getEmail())
            .status(Author.AuthorStatus.ACTIVE)
            .build();

     authorRepository.save(author);

     log.info("AUTH_REGISTER userId={} role={}", saved.getId(), saved.getRole());

     String token = jwtUtil.generateToken(saved);

     // 📘 CONCEPT: Video 14 - Fire and forget background job
     // Registration returns 201 immediately
     // Welcome email sent asynchronously — failure doesn't affect user
     emailJobService.sendWelcomeEmail(
             saved.getId(), saved.getEmail(), saved.getFullName());

     return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtConfig.getExpirationMs() / 1000)
            .userId(saved.getId())
            .email(saved.getEmail())
            .role(saved.getRole().name())
            .build();
}


    @Transactional
    public AuthResponse login(LoginRequest request) {

        // 📘 CONCEPT: Video 20 - Timing attack prevention
        // Always run passwordEncoder.matches() even if user not found
        // This ensures consistent response time regardless of whether
        // user exists → attacker can't measure timing difference
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        boolean passwordMatches = false;
        if (user != null) {
            passwordMatches = passwordEncoder.matches(
                    request.getPassword(), user.getPasswordHash());
        } else {
            // 📘 CONCEPT: Video 20 - Dummy hash comparison to normalize timing
            // Without this: "user not found" returns in 1ms
            //               "wrong password" returns in 100ms (BCrypt is slow)
            // Attacker measures timing → knows which emails are registered
            passwordEncoder.matches(request.getPassword(),
                    "$2a$10$dummyhashfortimingnormalization00000000000000000000");
        }

        if (user == null || !passwordMatches || !user.getIsActive()) {
            // 📘 CONCEPT: Video 20 - Generic auth error
            // Same message for: user not found, wrong password, inactive account
            // Attacker learns NOTHING about which check failed
            log.warn("AUTH_FAILED email={}", request.getEmail());
            throw new BusinessException("Authentication failed");
        }

        // Update last login timestamp for audit trail
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        log.info("AUTH_SUCCESS userId={} role={}", user.getId(), user.getRole());

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
