-- ================================================================
-- 📘 CONCEPT: Video 8 - Authentication
-- Users table stores credentials for JWT auth
-- 🟡 NOVICE: hardcode "admin"/"password123" in Java code
-- 🏢 PRODUCT: users in DB, BCrypt hashed passwords, never plain text
-- ================================================================

CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,

    -- 📘 CONCEPT: Video 8 - RBAC (Role Based Access Control)
    -- 🟡 NOVICE: check if email = "admin@bookvault.com" for admin access
    -- 🏢 PRODUCT: role stored in DB, checked via @PreAuthorize on endpoints
    role          user_role NOT NULL DEFAULT 'USER',

    full_name     TEXT NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,

    -- 📘 CONCEPT: Video 8 - Security audit fields
    last_login_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email      ON users(email);
CREATE INDEX idx_users_role       ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at DESC);