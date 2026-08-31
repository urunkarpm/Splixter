-- ==========================================================
-- SPLIXTER - Supabase Database Schema for Trip & Expense Splitter
-- ==========================================================
-- Instructions:
-- 1. Go to your Supabase project dashboard: https://app.supabase.com
-- 2. Open SQL Editor
-- 3. Paste and run this script
-- ==========================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. LOBBIES (Trips / Groups)
CREATE TABLE IF NOT EXISTS lobbies (
    code VARCHAR(20) PRIMARY KEY,               -- Unique lobby code e.g. "SPLIX-4892" or "849201"
    name TEXT NOT NULL,                          -- Name of the trip e.g. "Goa Vacation"
    host_person_id TEXT NOT NULL,                -- Person ID of the creator
    currency_symbol VARCHAR(10) DEFAULT '₹',     -- Currency symbol e.g. ₹, $, €, £
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    is_archived BOOLEAN DEFAULT FALSE
);

-- 2. LOBBY MEMBERS (Participants in a Trip)
CREATE TABLE IF NOT EXISTS lobby_members (
    id TEXT PRIMARY KEY,                         -- Person ID (UUID generated on client)
    lobby_code VARCHAR(20) NOT NULL REFERENCES lobbies(code) ON DELETE CASCADE,
    name TEXT NOT NULL,
    color BIGINT NOT NULL,                       -- ARGB Color format (e.g. 0xFF6C5CE7)
    emoji TEXT DEFAULT '😎',
    phone_number TEXT,
    upi_id TEXT,
    is_host BOOLEAN DEFAULT FALSE,
    joined_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    UNIQUE(lobby_code, id)
);

-- 3. TRIP EXPENSES (Atomic Expense Entries)
CREATE TABLE IF NOT EXISTS trip_expenses (
    id TEXT PRIMARY KEY,                         -- Expense ID (UUID generated on client)
    lobby_code VARCHAR(20) NOT NULL REFERENCES lobbies(code) ON DELETE CASCADE,
    title TEXT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    category TEXT DEFAULT 'General',             -- Food, Transport, Stay, Activity, Shopping, Fuel, Other
    paid_by_person_id TEXT NOT NULL,             -- Person ID who paid
    split_with_person_ids TEXT[] DEFAULT '{}',   -- Array of person IDs involved (empty = all members)
    split_type TEXT DEFAULT 'EQUAL',             -- EQUAL, EXACT, PERCENTAGE
    split_details JSONB DEFAULT '{}'::jsonb,     -- Optional custom amounts / percentages
    receipt_url TEXT,                            -- Optional receipt photo URL
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    updated_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE             -- Soft delete flag for sync propagation
);

-- 4. SETTLEMENTS (Recorded Debt Settlements)
CREATE TABLE IF NOT EXISTS trip_settlements (
    id TEXT PRIMARY KEY,                         -- Settlement ID (UUID)
    lobby_code VARCHAR(20) NOT NULL REFERENCES lobbies(code) ON DELETE CASCADE,
    from_person_id TEXT NOT NULL,                -- Debtor person ID
    to_person_id TEXT NOT NULL,                  -- Creditor person ID
    amount NUMERIC(12, 2) NOT NULL,              -- Settled amount
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    transaction_ref TEXT                         -- Optional UPI transaction reference
);

-- 5. TRIP ACTIVITIES (Live Activity & Audit Stream)
CREATE TABLE IF NOT EXISTS trip_activities (
    id TEXT PRIMARY KEY,                         -- Activity ID (UUID)
    lobby_code VARCHAR(20) NOT NULL REFERENCES lobbies(code) ON DELETE CASCADE,
    actor_person_id TEXT NOT NULL,               -- Person ID performing the action
    actor_name TEXT NOT NULL,                    -- Display name of actor
    action_type TEXT NOT NULL,                   -- EXPENSE_ADDED, EXPENSE_DELETED, SETTLEMENT_PAID, MEMBER_JOINED
    description TEXT NOT NULL,                   -- Human-readable message e.g. "Rahul added 'Goa Shack Dinner' (₹3,400)"
    amount NUMERIC(12, 2) DEFAULT 0.00,          -- Associated amount (if any)
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- Indexes for lightning fast lookups
CREATE INDEX IF NOT EXISTS idx_lobby_members_code ON lobby_members(lobby_code);
CREATE INDEX IF NOT EXISTS idx_trip_expenses_code ON trip_expenses(lobby_code);
CREATE INDEX IF NOT EXISTS idx_trip_settlements_code ON trip_settlements(lobby_code);
CREATE INDEX IF NOT EXISTS idx_trip_activities_code ON trip_activities(lobby_code);

-- Enable Row Level Security (RLS)
ALTER TABLE lobbies ENABLE ROW LEVEL SECURITY;
ALTER TABLE lobby_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE trip_expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE trip_settlements ENABLE ROW LEVEL SECURITY;
ALTER TABLE trip_activities ENABLE ROW LEVEL SECURITY;

-- Safely drop existing policies if re-running script to avoid conflict error 42710
DROP POLICY IF EXISTS "Allow public read-write on lobbies" ON lobbies;
DROP POLICY IF EXISTS "Allow public read-write on lobby_members" ON lobby_members;
DROP POLICY IF EXISTS "Allow public read-write on trip_expenses" ON trip_expenses;
DROP POLICY IF EXISTS "Allow public read-write on trip_settlements" ON trip_settlements;
DROP POLICY IF EXISTS "Allow public read-write on trip_activities" ON trip_activities;

-- Allow anonymous public access via anon key for lobby code collaboration
CREATE POLICY "Allow public read-write on lobbies" ON lobbies FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow public read-write on lobby_members" ON lobby_members FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow public read-write on trip_expenses" ON trip_expenses FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow public read-write on trip_settlements" ON trip_settlements FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow public read-write on trip_activities" ON trip_activities FOR ALL USING (true) WITH CHECK (true);

-- Safely enable Realtime publication for all tables
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'lobbies'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE lobbies;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'lobby_members'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE lobby_members;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'trip_expenses'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE trip_expenses;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'trip_settlements'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE trip_settlements;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'trip_activities'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE trip_activities;
    END IF;
END $$;
