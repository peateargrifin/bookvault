package com.bookvault.bookvault.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "authors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    // 📘 CONCEPT: Video 12 - UUID Primary Keys
    // 🟡 NOVICE: use @GeneratedValue(strategy = IDENTITY) → sequential 1,2,3...
    //             exposes record count, easy to enumerate all authors
    // 🏢 PRODUCT: UUID → unpredictable, safe in URLs, works across distributed DBs
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // 📘 CONCEPT: Video 20 - Security
    // Email is unique — enforced at DB level via migration index
    // If duplicate email attempted → DB throws constraint violation
    // GlobalExceptionHandler catches it → returns 409 Conflict (never 500)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AuthorStatus status = AuthorStatus.ACTIVE;

    // 📘 CONCEPT: Video 12 - Triggers handle updatedAt automatically
    // 🟡 NOVICE: manually set updatedAt in every service method → devs forget
    // 🏢 PRODUCT: DB trigger fires on every UPDATE → always accurate
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // 📘 CONCEPT: Video 12 - One-to-Many relationship
    // One author → many books
    // mappedBy = "author" refers to the @ManyToOne field in Book entity
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Book> books = new ArrayList<>();

    public enum AuthorStatus {
        ACTIVE, INACTIVE
    }
}
