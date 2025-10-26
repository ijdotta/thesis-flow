-- V5: Add auth user support and ensure all data consistency
-- This migration adds the auth_user table and ensures proper constraints

-- Create auth_user table if it doesn't exist
CREATE TABLE IF NOT EXISTS auth_user (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    username VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    professor_id BIGINT UNIQUE REFERENCES professor(id) ON DELETE SET NULL
);

-- Create indexes if they don't exist
CREATE INDEX IF NOT EXISTS idx_auth_user_username ON auth_user(username);
CREATE INDEX IF NOT EXISTS idx_auth_user_public_id ON auth_user(public_id);

-- Ensure career_id column exists on project table
ALTER TABLE project 
ADD COLUMN IF NOT EXISTS career_id BIGINT;

-- Add career_id foreign key constraint if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_project_career'
    ) THEN
        ALTER TABLE project
        ADD CONSTRAINT fk_project_career
        FOREIGN KEY (career_id) REFERENCES career(id) ON DELETE RESTRICT;
    END IF;
END $$;

-- Create index if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_project_career ON project(career_id);

-- Assign all projects to a default career if needed
UPDATE project
SET career_id = (SELECT id FROM career WHERE name = 'Software Engineering' LIMIT 1)
WHERE career_id IS NULL;

-- Make career_id NOT NULL
ALTER TABLE project ALTER COLUMN career_id SET NOT NULL;

-- Ensure admin user exists
INSERT INTO auth_user (public_id, username, password, role)
VALUES (
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
    'admin',
    '{bcrypt}$2y$10$OaX2C.Y8m08gTMUg28WIg.BI9x014i10DuG3yBEfKChqnhKW2K0km',
    'ADMIN'
)
ON CONFLICT (username) DO NOTHING;
