DROP INDEX IF EXISTS idx_books_isbn;
DROP INDEX IF EXISTS idx_books_created_at;
DROP INDEX IF EXISTS idx_books_genre;
DROP INDEX IF EXISTS idx_books_status;
DROP INDEX IF EXISTS idx_books_author_id;
DROP TABLE IF EXISTS books;
DROP TYPE IF EXISTS book_genre;
DROP TYPE IF EXISTS book_status;
