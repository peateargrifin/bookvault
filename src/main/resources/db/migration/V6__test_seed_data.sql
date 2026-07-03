-- ================================================================
-- 📘 CONCEPT: Video 12 - Seeding test data via migrations
-- This creates author records for existing users who have none
-- Safe to run multiple times (ON CONFLICT DO NOTHING)
-- ================================================================

-- Create author records for any users that don't have one
INSERT INTO authors (id, name, email, status, created_at, updated_at)
SELECT
    u.id,
    u.full_name,
    u.email,
    'ACTIVE',
    NOW(),
    NOW()
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM authors a WHERE a.id = u.id
)
ON CONFLICT (id) DO NOTHING;