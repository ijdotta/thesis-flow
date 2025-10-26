-- V6: Rebuild seed data with proper career assignments
-- This replaces V2 seed data but ensures projects have careers

-- Clear existing seed data first (if any rows exist from baseline)
DELETE FROM project_tags WHERE project_id IN (SELECT id FROM project WHERE public_id IN ('99999999-9999-9999-9999-999999999999', '88888888-8888-8888-8888-888888888888'));
DELETE FROM project_subtypes WHERE project_id IN (SELECT id FROM project WHERE public_id IN ('99999999-9999-9999-9999-999999999999', '88888888-8888-8888-8888-888888888888'));
DELETE FROM project_participant WHERE project_id IN (SELECT id FROM project WHERE public_id IN ('99999999-9999-9999-9999-999999999999', '88888888-8888-8888-8888-888888888888'));
DELETE FROM project WHERE public_id IN ('99999999-9999-9999-9999-999999999999', '88888888-8888-8888-8888-888888888888');

-- Ensure projects have careers assigned (for sample projects from V2)
UPDATE project SET career_id = (SELECT id FROM career WHERE name = 'Software Engineering' LIMIT 1) WHERE career_id IS NULL;
