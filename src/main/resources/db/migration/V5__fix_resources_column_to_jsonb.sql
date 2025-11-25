-- V5: Fix project resources column to use JSONB instead of varchar(255)
-- Drop and recreate the resources column as JSONB for proper JSON support

ALTER TABLE project
DROP COLUMN resources;

ALTER TABLE project
ADD COLUMN resources JSONB NOT NULL DEFAULT '[]'::jsonb;

COMMENT ON COLUMN project.resources IS 'JSON array of project resources stored as JSONB for native JSON support and unlimited storage';

