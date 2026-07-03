package com.bookvault.bookvault.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private UUID userId;
    private String email;
    private String role;

    // 📘 CONCEPT: Video 20 - Never include sensitive data in response
    // 🟡 NOVICE: include passwordHash, full user object in auth response
    // 🏢 PRODUCT: only token + minimal user info needed by client
    //             client can call /me endpoint for full profile if needed
}
