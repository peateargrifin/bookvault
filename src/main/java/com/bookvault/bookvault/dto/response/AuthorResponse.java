package com.bookvault.bookvault.dto.response;

import com.bookvault.bookvault.entity.Author;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

// 📘 CONCEPT: Video 7 - Serialization: Java object → JSON
// 📘 CONCEPT: Video 20 - Never expose raw @Entity to client
// 🟡 NOVICE: return Author entity directly
//             exposes: internal field names, lazy loading issues,
//             fields you never wanted public, potential N+1 problems
// 🏢 PRODUCT: dedicated ResponseDTO → full control over what leaves the server
//             Razorpay never exposes internal transaction entity directly
@Data
@Builder
public class AuthorResponse {

    private UUID id;
    private String name;
    private String bio;
    private String email;
    private String status;
    private int bookCount;
    private OffsetDateTime createdAt;

    // 📘 CONCEPT: Video 7 - Manual mapping from Entity → DTO
    // Controls EXACTLY what data leaves the server
    public static AuthorResponse from(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .bio(author.getBio())
                .email(author.getEmail())
                .status(author.getStatus().name())
                .bookCount(author.getBooks() != null ? author.getBooks().size() : 0)
                .createdAt(author.getCreatedAt())
                .build();
    }
}
