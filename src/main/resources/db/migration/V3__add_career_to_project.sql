-- Add career_id column to project table if it doesn't already exist
ALTER TABLE project 
ADD COLUMN IF NOT EXISTS career_id BIGINT;

-- Assign existing projects to a default career (Software Engineering)
UPDATE project
SET career_id = (SELECT id FROM career WHERE name = 'Software Engineering' LIMIT 1)
WHERE career_id IS NULL;

-- Add foreign key constraint if not already present
ALTER TABLE project
ADD CONSTRAINT fk_project_career
FOREIGN KEY (career_id) REFERENCES career(id) ON DELETE RESTRICT;

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_project_career ON project(career_id);

-- Make career_id NOT NULL now that all existing projects have a career
ALTER TABLE project ALTER COLUMN career_id SET NOT NULL;


