package com.bookvault.bookvault.controller;

import com.bookvault.bookvault.dto.request.LoginRequest;
import com.bookvault.bookvault.dto.request.RegisterRequest;
import com.bookvault.bookvault.dto.response.AuthResponse;
import com.bookvault.bookvault.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// 📘 CONCEPT: Video 10 - Controller responsibilities:
// 1. Accept HTTP request
// 2. Validate input (@Valid)
// 3. Extract data (path params, body, headers)
// 4. Call service
// 5. Return appropriate HTTP status code
// NO business logic here — that all lives in AuthService
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication",
     description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    // 📘 CONCEPT: Video 5 - POST for creation, 201 status code
    // 📘 CONCEPT: Video 11 - /api/v1/ versioning prefix
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
