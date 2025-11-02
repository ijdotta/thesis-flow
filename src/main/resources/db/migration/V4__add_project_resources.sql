-- V4: Add project resources column (JSON)
-- This migration adds support for storing project resources (external links, files, etc.) as JSON
-- Using TEXT with JSON compatibility for H2 and JSONB for PostgreSQL

ALTER TABLE project
ADD COLUMN resources TEXT NOT NULL DEFAULT '[]';

COMMENT ON COLUMN project.resources IS 'JSON array of project resources. Each resource has: url (string), title (string), description (optional string)';

