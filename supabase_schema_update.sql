-- SQL script to synchronize Supabase schema with the latest app requirements

-- 1. Create Announcements table if it doesn't exist
CREATE TABLE IF NOT EXISTS announcements (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp BIGINT NOT NULL,
    is_urgent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Update Payments table
DO $$
BEGIN
    -- Add proof_image_url if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'payments' AND COLUMN_NAME = 'proof_image_url') THEN
        ALTER TABLE payments ADD COLUMN proof_image_url TEXT;
    END IF;

    -- Ensure status column exists and has proper default
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'payments' AND COLUMN_NAME = 'status') THEN
        ALTER TABLE payments ADD COLUMN status TEXT DEFAULT 'Pending';
    END IF;
END $$;

-- 3. Update Complaints table
DO $$
BEGIN
    -- Add category column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'complaints' AND COLUMN_NAME = 'category') THEN
        ALTER TABLE complaints ADD COLUMN category TEXT DEFAULT 'General';
    END IF;

    -- Add created_at as BIGINT for consistency with local Room database (Long)
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'complaints' AND COLUMN_NAME = 'created_at') THEN
        ALTER TABLE complaints ADD COLUMN created_at BIGINT;
    END IF;
END $$;

-- 4. Enable Row Level Security (Optional, but recommended)
-- Note: Adjust these policies based on your specific security requirements
ALTER TABLE announcements ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read access to announcements" ON announcements FOR SELECT USING (true);
CREATE POLICY "Allow service role full access to announcements" ON announcements FOR ALL USING (true);

-- 5. Create Polls table (for future sync if needed)
CREATE TABLE IF NOT EXISTS polls (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    date TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    breakfast_dish_id INTEGER,
    lunch_dish_id INTEGER,
    dinner_dish_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS poll_votes (
    id SERIAL PRIMARY KEY,
    poll_id INTEGER REFERENCES polls(id) ON DELETE CASCADE,
    tenant_id INTEGER NOT NULL,
    breakfast BOOLEAN DEFAULT FALSE,
    lunch BOOLEAN DEFAULT FALSE,
    dinner BOOLEAN DEFAULT FALSE,
    is_veg BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(poll_id, tenant_id)
);

-- 6. Update Tenants table for Aadhaar verification
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tenants' AND COLUMN_NAME = 'is_aadhaar_verified') THEN
        ALTER TABLE tenants ADD COLUMN is_aadhaar_verified BOOLEAN DEFAULT FALSE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tenants' AND COLUMN_NAME = 'aadhaar_number') THEN
        ALTER TABLE tenants ADD COLUMN aadhaar_number TEXT;
    END IF;
END $$;
