-- ================================================================
--  CONCEPT: Video 12 - Database Migrations (Flyway)
--  NOVICE: manually create tables in pgAdmin, no version history,
--             team members don't know what schema looks like
--  PRODUCT: every schema change is a versioned SQL file committed
--             to git. New dev joins → runs migrations → identical DB.
--             Razorpay/Swiggy can replay entire DB history from scratch.
-- ================================================================

--  CONCEPT: Video 12 - Enum Types for Data Integrity
--  NOVICE: store status as plain TEXT → "actve" typo goes undetected
--  PRODUCT: enum enforces valid values at DB level, documents allowed
--             states for any new dev reading migration files
CREATE TYPE author_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE authors (
    --  CONCEPT: Video 12 - UUID Primary Keys
    --  NOVICE: use SERIAL (1,2,3...) → exposes record count to clients,
    --             easy to enumerate all records (security risk)
    --  PRODUCT: UUID → unpredictable, safe to expose in URLs,
    --             works across distributed systems without collision
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    --  CONCEPT: Video 12 - NOT NULL constraints
    --  NOVICE: skip constraints → null names sneak in, app breaks later
    --  PRODUCT: DB enforces data integrity, not just application code
    name TEXT NOT NULL,
    bio  TEXT,
    email TEXT NOT NULL UNIQUE,

    status author_status NOT NULL DEFAULT 'ACTIVE',

    -- CONCEPT: Video 12 - Audit timestamps
    -- NOVICE: no created_at → impossible to sort by newest, debug issues
    -- PRODUCT: every table has created_at + updated_at for audit trail
    --             updated_at auto-maintained by trigger (see V4)
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

--  CONCEPT: Video 12 - Indexes for Performance
--  NOVICE: no indexes → full table scan on every query
--  PRODUCT: index on frequently queried/sorted columns
--             without this: 1M authors → email lookup takes seconds
--             with this: same lookup takes microseconds
CREATE INDEX idx_authors_email  ON authors(email);
CREATE INDEX idx_authors_status ON authors(status);
CREATE INDEX idx_authors_created_at ON authors(created_at DESC);
