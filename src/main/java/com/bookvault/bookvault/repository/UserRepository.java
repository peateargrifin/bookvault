package com.bookvault.bookvault.repository;

import com.bookvault.bookvault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // 📘 CONCEPT: Video 8 - Used by Spring Security to load user during login
    // 📘 CONCEPT: Video 20 - Generic error message if not found
    //             Service returns "Authentication failed" (never "User not found")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
