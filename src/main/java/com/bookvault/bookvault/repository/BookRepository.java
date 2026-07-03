package com.bookvault.bookvault.repository;

import com.bookvault.bookvault.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    // 📘 CONCEPT: Video 20 - BOLA Prevention
    // 🟡 NOVICE: findById(id) → returns book regardless of who owns it
    //             User A can access User B's book by guessing the UUID
    // 🏢 PRODUCT: always scope queries to the requesting user's author context
    //             findByIdAndAuthorId → if book exists but wrong author → empty
    //             → service returns 404 (not 403) → attacker learns nothing
    Optional<Book> findByIdAndAuthorId(UUID id, UUID authorId);

    Page<Book> findByAuthorId(UUID authorId, Pageable pageable);

    Page<Book> findByStatus(Book.BookStatus status, Pageable pageable);

    Page<Book> findByGenre(Book.BookGenre genre, Pageable pageable);

    // 📘 CONCEPT: Video 11 - Filtering + Searching
    // 📘 CONCEPT: Video 12 - Parameterized queries (JPA handles this automatically)
    // 📘 CONCEPT: Video 20 - SQL Injection prevention (JPA uses prepared statements)
    @Query("SELECT b FROM Book b WHERE " +
           "(CAST(:genre as String) IS NULL OR b.genre = :genre) AND " +
           "(CAST(:status as String) IS NULL OR b.status = :status) AND " +
           "(CAST(:authorId as String) IS NULL OR b.author.id = :authorId)")
    Page<Book> findWithFilters(
        @Param("genre") Book.BookGenre genre,
        @Param("status") Book.BookStatus status,
        @Param("authorId") UUID authorId,
        Pageable pageable
    );

    boolean existsByIsbn(String isbn);
}
