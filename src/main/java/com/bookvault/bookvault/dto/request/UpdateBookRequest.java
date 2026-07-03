package com.bookvault.bookvault.dto.request;

import com.bookvault.bookvault.entity.Book;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

// 📘 CONCEPT: Video 11 - PATCH semantics: all fields optional
// 🟡 NOVICE: use same DTO as create, force client to send all fields
// 🏢 PRODUCT: separate update DTO, every field optional
//             client sends only what changed → bandwidth efficient
@Data
public class UpdateBookRequest {

    @Size(min = 1, max = 300, message = "Title must be between 1 and 300 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal price;

    private Book.BookGenre genre;

    @PastOrPresent(message = "Published date cannot be in the future")
    private LocalDate publishedDate;

    private String coverImageUrl;

    private Set<UUID> tagIds;
}
