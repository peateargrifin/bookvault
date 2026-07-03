-- ================================================================
-- 📘 CONCEPT: Video 12 - Database Triggers
-- Auto-update updated_at on every row update
-- 🟡 NOVICE: manually set updated_at in every service method
--             → developers forget → stale timestamps in DB
-- 🏢 PRODUCT: trigger fires automatically at DB level
--             impossible to forget, always accurate
-- ================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables that have updated_at
CREATE TRIGGER trigger_authors_updated_at
    BEFORE UPDATE ON authors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_books_updated_at
    BEFORE UPDATE ON books
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
