package com.bookvault.bookvault.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 📘 CONCEPT: Video 12 - ISBN uniqueness enforced at DB level
    // 📘 CONCEPT: Video 20 - Duplicate ISBN → 409 Conflict (not 500)
    @Column(name = "isbn", unique = true)
    private String isbn;

    // 📘 CONCEPT: Video 12 - DECIMAL for price, never FLOAT
    // 🟡 NOVICE: use double/float for price → floating point errors
    //             0.1 + 0.2 = 0.30000000000000004 in float arithmetic
    // 🏢 PRODUCT: BigDecimal → exact representation, no rounding errors
    //             critical for financial calculations (Razorpay, Stripe use this)
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookStatus status = BookStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    @Builder.Default
    private BookGenre genre = BookGenre.OTHER;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // 📘 CONCEPT: Video 12 - Many-to-One (Foreign Key)
    // 📘 CONCEPT: Video 20 - BOLA Prevention
    // When fetching books, ALWAYS filter by author ownership
    // Never: SELECT * FROM books WHERE id = ?
    // Always: SELECT * FROM books WHERE id = ? AND author_id = ?
    // 🟡 NOVICE: store author_id as plain Long, no JPA relationship
    // 🏢 PRODUCT: @ManyToOne with referential integrity → DB prevents orphan books
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    // 📘 CONCEPT: Video 12 - Many-to-Many via linking table
    // A book can have many tags, a tag belongs to many books
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_tags",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum BookStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public enum BookGenre {
        FICTION, NON_FICTION, SCIENCE, TECHNOLOGY,
        BIOGRAPHY, HISTORY, PHILOSOPHY, OTHER
    }
}
