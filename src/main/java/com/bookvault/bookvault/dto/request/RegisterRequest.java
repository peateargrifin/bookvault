package com.bookvault.bookvault.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    // 📘 CONCEPT: Video 20 - Password complexity requirements
    // 🟡 NOVICE: accept any string as password
    // 🏢 PRODUCT: minimum complexity → harder to brute force
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain uppercase, lowercase and a number"
    )
    private String password;

    public String getEmail() {
        return email != null ? email.toLowerCase().trim() : null;
    }
}
