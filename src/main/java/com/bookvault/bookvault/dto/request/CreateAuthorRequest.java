package com.bookvault.bookvault.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

// 📘 CONCEPT: Video 9 - Validation at entry point
// 📘 CONCEPT: Video 7 - Deserialization: JSON → Java object
// 🟡 NOVICE: receive Map<String, Object> or raw String, validate manually
// 🏢 PRODUCT: typed DTO + @Valid → Spring validates before method body runs
@Data
public class CreateAuthorRequest {

    // 📘 CONCEPT: Video 9 - Syntactic validation (format check)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    // 📘 CONCEPT: Video 9 - @Email is syntactic validation
    // 📘 CONCEPT: Video 20 - email stored lowercase (transformation)
    //             prevents duplicate accounts with same email different case
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    // Transformation: normalize email before it hits service layer
    // 📘 CONCEPT: Video 9 - Transformation pipeline
    // 🏢 PRODUCT: always normalize at entry point, never trust client casing
    public String getEmail() {
        return email != null ? email.toLowerCase().trim() : null;
    }

    public String getName() {
        return name != null ? name.trim() : null;
    }
}
