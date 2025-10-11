-- Add career_id column to project table
ALTER TABLE project
ADD COLUMN career_id BIGINT;

-- Assign existing projects to a default career (Software Engineering)
-- This handles the sample projects from V2 seed data
UPDATE project
SET career_id = (SELECT id FROM career WHERE name = 'Software Engineering')
WHERE career_id IS NULL;

-- Add foreign key constraint
ALTER TABLE project
ADD CONSTRAINT fk_project_career
FOREIGN KEY (career_id) REFERENCES career(id) ON DELETE RESTRICT;

-- Create index for performance
CREATE INDEX idx_project_career ON project(career_id);

-- Make career_id NOT NULL now that all existing projects have a career
ALTER TABLE project ALTER COLUMN career_id SET NOT NULL;
