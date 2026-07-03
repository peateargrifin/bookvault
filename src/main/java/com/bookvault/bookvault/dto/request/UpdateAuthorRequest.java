package com.bookvault.bookvault.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

// 📘 CONCEPT: Video 11 - PATCH semantics: all fields optional
@Data
public class UpdateAuthorRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;
}
