-- V5__migrate_project_subtypes.sql
-- Migrate old TYPE_1 and TYPE_2 enum values to new values

-- Replace TYPE_1 with INVESTIGACION (or any valid value)
UPDATE project_subtypes SET sub_type = 'INVESTIGACION' WHERE sub_type = 'TYPE_1';

-- Replace TYPE_2 with INVESTIGACION (or any valid value)
UPDATE project_subtypes SET sub_type = 'INVESTIGACION' WHERE sub_type = 'TYPE_2';

-- Clean up duplicates if a project had both TYPE_1 and TYPE_2
-- (They would now both be INVESTIGACION, creating a duplicate)
DELETE FROM project_subtypes
WHERE (project_id, sub_type) IN (
  SELECT project_id, sub_type
  FROM project_subtypes
  GROUP BY project_id, sub_type
  HAVING COUNT(*) > 1
  LIMIT 1
);
