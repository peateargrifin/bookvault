--  CONCEPT: Video 12 - Down Migrations (Rollback)
-- Run this manually in TablePlus if you need to undo V1
-- PRODUCT: every migration has a corresponding undo script
--             allows rolling back to any previous DB state

DROP INDEX IF EXISTS idx_authors_created_at;
DROP INDEX IF EXISTS idx_authors_status;
DROP INDEX IF EXISTS idx_authors_email;
DROP TABLE IF EXISTS authors;
DROP TYPE IF EXISTS author_status;
