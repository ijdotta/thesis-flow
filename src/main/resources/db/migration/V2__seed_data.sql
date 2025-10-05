-- V2 Initial seed data (fresh database only)
-- Deterministic UUIDs for stable references in documentation/tests.
-- Apply ONLY on an empty schema created by V1. Not idempotent by design.

-- PERSONS
INSERT INTO person (public_id, name, lastname) VALUES
 ('11111111-1111-1111-1111-111111111111','Alice','Smith'),
 ('22222222-2222-2222-2222-222222222222','Bob','Johnson'),
 ('33333333-3333-3333-3333-333333333333','Carol','Garcia'),
 ('44444444-4444-4444-4444-444444444444','David','Martinez'),
 ('55555555-5555-5555-5555-555555555555','Eve','Lopez');

-- CAREERS
INSERT INTO career (public_id, name) VALUES
 ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1','Software Engineering'),
 ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2','Computer Science');

-- APPLICATION DOMAINS
INSERT INTO application_domain (public_id, name, description) VALUES
 ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1','Healthcare','Healthcare related projects'),
 ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','FinTech','Financial technology domain');

-- TAGS
INSERT INTO tag (public_id, name, description) VALUES
 ('cccccccc-cccc-cccc-cccc-ccccccccccc1','Kotlin','Kotlin language'),
 ('cccccccc-cccc-cccc-cccc-ccccccccccc2','AI','Artificial Intelligence'),
 ('cccccccc-cccc-cccc-cccc-ccccccccccc3','Web','Web technologies');

-- STUDENTS (link to PERSON) - fetch FK id via subselects
INSERT INTO student (public_id, person_id, student_id, email) VALUES
 ('dddddddd-dddd-dddd-dddd-ddddddddddd1',(SELECT id FROM person WHERE public_id='11111111-1111-1111-1111-111111111111'),'S001','alice@student.test'),
 ('dddddddd-dddd-dddd-dddd-ddddddddddd2',(SELECT id FROM person WHERE public_id='22222222-2222-2222-2222-222222222222'),'S002','bob@student.test');

-- PROFESSORS
INSERT INTO professor (public_id, person_id, email) VALUES
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',(SELECT id FROM person WHERE public_id='33333333-3333-3333-3333-333333333333'),'carol.prof@univ.test'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',(SELECT id FROM person WHERE public_id='44444444-4444-4444-4444-444444444444'),'david.prof@univ.test');

-- STUDENT CAREERS (bridge)
INSERT INTO student_career (public_id, student_id, career_id) VALUES
 ('fffffff1-ffff-ffff-ffff-fffffffffff1', (SELECT id FROM student WHERE public_id='dddddddd-dddd-dddd-dddd-ddddddddddd1'), (SELECT id FROM career WHERE public_id='aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1')),
 ('fffffff2-ffff-ffff-ffff-fffffffffff2', (SELECT id FROM student WHERE public_id='dddddddd-dddd-dddd-dddd-ddddddddddd2'), (SELECT id FROM career WHERE public_id='aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2'));

-- PRIMARY PROJECT
INSERT INTO project (public_id, title, type, initial_submission, completion, application_domain_id, created_at, updated_at) VALUES (
 '99999999-9999-9999-9999-999999999999',
 'Sample Thesis Project',
 'THESIS',
 CURRENT_DATE,
 NULL,
 (SELECT id FROM application_domain WHERE public_id='bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1'),
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP
);

-- PROJECT SUBTYPES
INSERT INTO project_subtypes (project_id, sub_type)
VALUES ((SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), 'TYPE_1');

-- PROJECT PARTICIPANTS (Alice student, Carol director)
INSERT INTO project_participant (public_id, project_id, person_id, participant_role) VALUES
 ('77777777-7777-7777-7777-777777777771', (SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM person WHERE public_id='11111111-1111-1111-1111-111111111111'), 'STUDENT'),
 ('77777777-7777-7777-7777-777777777772', (SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM person WHERE public_id='33333333-3333-3333-3333-333333333333'), 'DIRECTOR');

-- PROJECT TAG LINKS (Kotlin + AI)
INSERT INTO project_tags (project_id, tag_id) VALUES
 ((SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM tag WHERE public_id='cccccccc-cccc-cccc-cccc-ccccccccccc1')),
 ((SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM tag WHERE public_id='cccccccc-cccc-cccc-cccc-ccccccccccc2'));

-- OPTIONAL: Add a second project to diversify dataset
INSERT INTO project (public_id, title, type, initial_submission, completion, application_domain_id, created_at, updated_at) VALUES (
 '88888888-8888-8888-8888-888888888888',
 'Web Platform Final Project',
 'FINAL_PROJECT',
 CURRENT_DATE,
 NULL,
 (SELECT id FROM application_domain WHERE public_id='bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2'),
 CURRENT_TIMESTAMP,
 CURRENT_TIMESTAMP
);
INSERT INTO project_subtypes (project_id, sub_type)
VALUES ((SELECT id FROM project WHERE public_id='88888888-8888-8888-8888-888888888888'), 'TYPE_2');
