-- ================================================================
-- 📘 CONCEPT: Video 12 - Many-to-Many Relationship
-- A book can have many tags, a tag can belong to many books
-- Implemented via a linking table (book_tags)
-- ================================================================

CREATE TABLE tags (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 📘 CONCEPT: Video 12 - Linking Table (Many-to-Many)
-- 🟡 NOVICE: store tags as comma-separated text in books table
--             → impossible to query "all books with tag X" efficiently
-- 🏢 PRODUCT: proper linking table with composite primary key
--             guarantees a book can't have the same tag twice
CREATE TABLE book_tags (
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    tag_id  UUID NOT NULL REFERENCES tags(id)  ON DELETE CASCADE,

    -- 📘 CONCEPT: Video 12 - Composite Primary Key
    -- Combination of book_id + tag_id must be unique
    -- Prevents same tag being added to same book twice
    PRIMARY KEY (book_id, tag_id)
);

CREATE INDEX idx_book_tags_book_id ON book_tags(book_id);
CREATE INDEX idx_book_tags_tag_id  ON book_tags(tag_id);

-- Seed some default tags
INSERT INTO tags (name) VALUES
    ('bestseller'),
    ('award-winning'),
    ('new-release'),
    ('classic'),
    ('recommended');