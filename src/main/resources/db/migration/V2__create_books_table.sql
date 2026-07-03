-- ================================================================
-- 📘 CONCEPT: Video 12 - One-to-Many Relationship
-- One author can have many books
-- The "many" side (books) holds the foreign key
-- ================================================================

CREATE TYPE book_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE book_genre  AS ENUM (
    'FICTION', 'NON_FICTION', 'SCIENCE', 'TECHNOLOGY',
    'BIOGRAPHY', 'HISTORY', 'PHILOSOPHY', 'OTHER'
);

CREATE TABLE books (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       TEXT NOT NULL,
    description TEXT,
    isbn        TEXT UNIQUE,
    price       DECIMAL(10, 2) NOT NULL,

    -- 📘 CONCEPT: Video 12 - Enum fields
    status      book_status NOT NULL DEFAULT 'DRAFT',
    genre       book_genre  NOT NULL DEFAULT 'OTHER',

    published_date DATE,
    cover_image_url TEXT,

    -- 📘 CONCEPT: Video 12 - Foreign Key + Referential Integrity
    -- 🟡 NOVICE: store author_id as plain integer, no constraint
    --             → can insert books with non-existent author IDs
    -- 🏢 PRODUCT: REFERENCES enforces author must exist
    --             ON DELETE RESTRICT → can't delete author who has books
    --             protects data integrity automatically at DB level
    author_id   UUID NOT NULL REFERENCES authors(id) ON DELETE RESTRICT,

    -- 📘 CONCEPT: Video 12 - Audit fields on every table
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 📘 CONCEPT: Video 12 - Strategic Indexing
-- Index fields used in WHERE clauses, JOINs, and ORDER BY
-- 🟡 NOVICE: no indexes → every search is full table scan
-- 🏢 PRODUCT: at 10M books without index: genre filter = 30 seconds
--             with index: same filter = 10 milliseconds
CREATE INDEX idx_books_author_id   ON books(author_id);
CREATE INDEX idx_books_status      ON books(status);
CREATE INDEX idx_books_genre       ON books(genre);
CREATE INDEX idx_books_created_at  ON books(created_at DESC);
CREATE INDEX idx_books_isbn        ON books(isbn);
