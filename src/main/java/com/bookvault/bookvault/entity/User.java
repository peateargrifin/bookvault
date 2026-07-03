package com.bookvault.bookvault.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

// 📘 CONCEPT: Video 8 - UserDetails interface
// Spring Security uses this to load user during authentication
// Implementing it means Spring can directly use our User entity for auth
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // 📘 CONCEPT: Video 20 - Password Storage
    // 🟡 NOVICE: store plain text password → catastrophic on DB breach
    // 🏢 PRODUCT: BCrypt hash with salt → even DB breach reveals nothing
    //             BCryptPasswordEncoder configured in SecurityConfig
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // 📘 CONCEPT: Video 8 + Video 20 - RBAC
    // 🟡 NOVICE: check if email = "admin@bookvault.com"
    // 🏢 PRODUCT: role in DB → @PreAuthorize("hasRole('ADMIN')") on endpoints
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 📘 CONCEPT: Video 18 - Audit fields
    // Track when user last logged in → useful for security monitoring
    // Sudden login from different country → trigger 2FA or alert
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ── UserDetails interface implementation ──────────────────────────────
    // Spring Security calls these methods during authentication

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 📘 CONCEPT: Video 8 - RBAC via Spring Security
        // "ROLE_" prefix required by Spring Security for hasRole() checks
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        // We use email as username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return isActive; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }

    public enum UserRole {
        USER, ADMIN
    }
}
