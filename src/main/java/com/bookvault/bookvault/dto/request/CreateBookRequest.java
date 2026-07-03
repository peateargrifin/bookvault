package com.bookvault.bookvault.dto.request;

import com.bookvault.bookvault.entity.Book;
import com.bookvault.bookvault.validation.ValidIsbn;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class CreateBookRequest {

    // 📘 CONCEPT: Video 9 - Syntactic + Type validation
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 300, message = "Title must be between 1 and 300 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    // 📘 CONCEPT: Video 9 - Custom validator (ISBN format check)
    @ValidIsbn
    private String isbn;

    // 📘 CONCEPT: Video 9 - Semantic validation (price must make sense)
    // 🟡 NOVICE: no min check → negative prices stored in DB
    // 🏢 PRODUCT: price >= 0 enforced at entry point, never reaches DB
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @DecimalMax(value = "99999.99", message = "Price seems unreasonably high")
    private BigDecimal price;

    @NotNull(message = "Genre is required")
    private Book.BookGenre genre;

    // 📘 CONCEPT: Video 9 - Semantic validation (can't publish a future-dated book)
    // @PastOrPresent ensures date makes logical sense
    @PastOrPresent(message = "Published date cannot be in the future")
    private LocalDate publishedDate;

    private String coverImageUrl;

    private Set<UUID> tagIds;

    // Transformation
    public String getTitle() {
        return title != null ? title.trim() : null;
    }
}
