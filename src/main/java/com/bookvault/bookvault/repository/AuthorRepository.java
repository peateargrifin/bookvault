package com.bookvault.bookvault.repository;

import com.bookvault.bookvault.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// 📘 CONCEPT: Video 10 - Repository Layer
// 🟡 NOVICE: write SQL queries directly in controller
// 🏢 PRODUCT: repository = single responsibility → one method = one DB operation
//             service orchestrates multiple repos, never writes SQL
@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {

    Optional<Author> findByEmail(String email);

    Page<Author> findByStatus(Author.AuthorStatus status, Pageable pageable);

    boolean existsByEmail(String email);
}
