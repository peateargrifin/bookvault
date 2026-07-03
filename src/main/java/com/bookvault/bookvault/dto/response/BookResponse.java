package com.bookvault.bookvault.dto.response;

import com.bookvault.bookvault.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private UUID id;
    private String title;
    private String description;
    private String isbn;
    private BigDecimal price;
    private String status;
    private String genre;
    private LocalDate publishedDate;
    private String coverImageUrl;
    private AuthorSummary author;
    private Set<String> tags;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .status(book.getStatus().name())
                .genre(book.getGenre().name())
                .publishedDate(book.getPublishedDate())
                .coverImageUrl(book.getCoverImageUrl())
                .author(AuthorSummary.from(book.getAuthor()))
                .tags(book.getTags() != null
                        ? book.getTags().stream()
                                .map(tag -> tag.getName())
                                .collect(Collectors.toSet())
                        : Set.of())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    // 📘 CONCEPT: Video 7 - Nested DTOs
    // Author info embedded in book response (avoid extra API call)
    // 🟡 NOVICE: return full AuthorResponse inside BookResponse → exposes too much
    // 🏢 PRODUCT: AuthorSummary with only fields needed for this context
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorSummary {
        private UUID id;
        private String name;

        public static AuthorSummary from(com.bookvault.bookvault.entity.Author author) {
            if (author == null) return null;
            return AuthorSummary.builder()
                    .id(author.getId())
                    .name(author.getName())
                    .build();
        }
    }
}
