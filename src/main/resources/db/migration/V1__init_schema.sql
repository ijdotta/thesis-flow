-- V1: Complete schema with all data and constraints
-- Comprehensive migration consolidating all schema creation, data seeding, and constraints

-- ============================================================================
-- CORE SCHEMA CREATION (Idempotent)
-- ============================================================================

-- PERSON
CREATE TABLE IF NOT EXISTS person (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_person_public_id ON person(public_id);

-- CAREER
CREATE TABLE IF NOT EXISTS career (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_career_public_id ON career(public_id);

-- APPLICATION DOMAIN
CREATE TABLE IF NOT EXISTS application_domain (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL
);
CREATE INDEX IF NOT EXISTS idx_app_domain_public_id ON application_domain(public_id);

-- TAG
CREATE TABLE IF NOT EXISTS tag (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL
);
CREATE INDEX IF NOT EXISTS idx_tag_public_id ON tag(public_id);
CREATE INDEX IF NOT EXISTS idx_tag_name ON tag(name);

-- STUDENT
CREATE TABLE IF NOT EXISTS student (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    person_id BIGINT REFERENCES person(id) ON DELETE RESTRICT,
    student_id VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_student_public_id ON student(public_id);
CREATE INDEX IF NOT EXISTS idx_student_email ON student(email);

-- PROFESSOR
CREATE TABLE IF NOT EXISTS professor (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE RESTRICT,
    email VARCHAR(320) NOT NULL UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_professor_public_id ON professor(public_id);
CREATE INDEX IF NOT EXISTS idx_professor_email ON professor(email);

-- PROJECT with career_id column
CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    initial_submission DATE NOT NULL,
    completion DATE NULL,
    career_id BIGINT NOT NULL REFERENCES career(id) ON DELETE RESTRICT,
    application_domain_id BIGINT NULL REFERENCES application_domain(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_project_public_id ON project(public_id);
CREATE INDEX IF NOT EXISTS idx_project_career ON project(career_id);

-- PROJECT SUBTYPES
CREATE TABLE IF NOT EXISTS project_subtypes (
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    sub_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (project_id, sub_type)
);

-- PROJECT TAGS
CREATE TABLE IF NOT EXISTS project_tags (
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE RESTRICT,
    PRIMARY KEY (project_id, tag_id)
);

-- PROJECT PARTICIPANT
CREATE TABLE IF NOT EXISTS project_participant (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE RESTRICT,
    participant_role VARCHAR(50) NOT NULL,
    CONSTRAINT uc_project_person_role UNIQUE (project_id, person_id, participant_role)
);
CREATE INDEX IF NOT EXISTS idx_project_participant_public_id ON project_participant(public_id);

-- STUDENT CAREER
CREATE TABLE IF NOT EXISTS student_career (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    career_id BIGINT NOT NULL REFERENCES career(id) ON DELETE RESTRICT,
    CONSTRAINT uc_student_career UNIQUE (student_id, career_id)
);
CREATE INDEX IF NOT EXISTS idx_student_career_public_id ON student_career(public_id);

-- AUTH USER
CREATE TABLE IF NOT EXISTS auth_user (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    username VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    professor_id BIGINT UNIQUE REFERENCES professor(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_auth_user_username ON auth_user(username);
CREATE INDEX IF NOT EXISTS idx_auth_user_public_id ON auth_user(public_id);

-- ============================================================================
-- SEED DATA (Idempotent with ON CONFLICT)
-- ============================================================================

-- PERSONS
INSERT INTO person (public_id, name, lastname) VALUES
 ('11111111-1111-1111-1111-111111111111','Alice','Smith'),
 ('22222222-2222-2222-2222-222222222222','Bob','Johnson'),
 ('33333333-3333-3333-3333-333333333333','Carol','Garcia'),
 ('44444444-4444-4444-4444-444444444444','David','Martinez'),
 ('55555555-5555-5555-5555-555555555555','Eve','Lopez')
ON CONFLICT (public_id) DO NOTHING;

-- CAREERS
INSERT INTO career (public_id, name) VALUES
 ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1','Software Engineering'),
 ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2','Computer Science'),
 ('10b219c2-26ac-4e89-8896-f4b24948bcaa','Legacy Dataset')
ON CONFLICT (public_id) DO NOTHING;

-- APPLICATION DOMAINS
INSERT INTO application_domain (public_id, name, description) VALUES
 ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1','Healthcare','Healthcare related projects'),
 ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','FinTech','Financial technology domain')
ON CONFLICT (public_id) DO NOTHING;

-- TAGS
INSERT INTO tag (public_id, name, description) VALUES
 ('cccccccc-cccc-cccc-cccc-ccccccccccc1','Kotlin','Kotlin language'),
 ('cccccccc-cccc-cccc-cccc-ccccccccccc2','AI','Artificial Intelligence'),
 ('cccccccc-cccc-cccc-cccc-ccccccccccc3','Web','Web technologies')
ON CONFLICT (public_id) DO NOTHING;

-- STUDENTS
INSERT INTO student (public_id, person_id, student_id, email) VALUES
 ('dddddddd-dddd-dddd-dddd-ddddddddddd1',(SELECT id FROM person WHERE public_id='11111111-1111-1111-1111-111111111111'),'S001','alice@student.test'),
 ('dddddddd-dddd-dddd-dddd-ddddddddddd2',(SELECT id FROM person WHERE public_id='22222222-2222-2222-2222-222222222222'),'S002','bob@student.test')
ON CONFLICT (public_id) DO NOTHING;

-- PROFESSORS
INSERT INTO professor (public_id, person_id, email) VALUES
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',(SELECT id FROM person WHERE public_id='33333333-3333-3333-3333-333333333333'),'carol.prof@univ.test'),
 ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2',(SELECT id FROM person WHERE public_id='44444444-4444-4444-4444-444444444444'),'david.prof@univ.test')
ON CONFLICT (public_id) DO NOTHING;

-- STUDENT CAREERS
INSERT INTO student_career (public_id, student_id, career_id) VALUES
 ('fffffff1-ffff-ffff-ffff-fffffffffff1', (SELECT id FROM student WHERE public_id='dddddddd-dddd-dddd-dddd-ddddddddddd1'), (SELECT id FROM career WHERE public_id='aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1')),
 ('fffffff2-ffff-ffff-ffff-fffffffffff2', (SELECT id FROM student WHERE public_id='dddddddd-dddd-dddd-dddd-ddddddddddd2'), (SELECT id FROM career WHERE public_id='aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2'))
ON CONFLICT (public_id) DO NOTHING;

-- PROJECTS with career_id
INSERT INTO project (public_id, title, type, initial_submission, completion, career_id, application_domain_id, created_at, updated_at) VALUES
 ('99999999-9999-9999-9999-999999999999','Sample Thesis Project','THESIS',CURRENT_DATE,NULL,(SELECT id FROM career WHERE name='Software Engineering'),(SELECT id FROM application_domain WHERE public_id='bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1'),CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
 ('88888888-8888-8888-8888-888888888888','Web Platform Final Project','FINAL_PROJECT',CURRENT_DATE,NULL,(SELECT id FROM career WHERE name='Software Engineering'),(SELECT id FROM application_domain WHERE public_id='bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2'),CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT (public_id) DO NOTHING;

-- PROJECT SUBTYPES
INSERT INTO project_subtypes (project_id, sub_type) VALUES
 ((SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), 'TYPE_1'),
 ((SELECT id FROM project WHERE public_id='88888888-8888-8888-8888-888888888888'), 'TYPE_2')
ON CONFLICT DO NOTHING;

-- PROJECT PARTICIPANTS
INSERT INTO project_participant (public_id, project_id, person_id, participant_role) VALUES
 ('77777777-7777-7777-7777-777777777771', (SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM person WHERE public_id='11111111-1111-1111-1111-111111111111'), 'STUDENT'),
 ('77777777-7777-7777-7777-777777777772', (SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM person WHERE public_id='33333333-3333-3333-3333-333333333333'), 'DIRECTOR')
ON CONFLICT (public_id) DO NOTHING;

-- PROJECT TAG LINKS
INSERT INTO project_tags (project_id, tag_id) VALUES
 ((SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM tag WHERE public_id='cccccccc-cccc-cccc-cccc-ccccccccccc1')),
 ((SELECT id FROM project WHERE public_id='99999999-9999-9999-9999-999999999999'), (SELECT id FROM tag WHERE public_id='cccccccc-cccc-cccc-cccc-ccccccccccc2'))
ON CONFLICT DO NOTHING;

-- ============================================================================
-- ADMIN USER SEEDING
-- ============================================================================

-- Seed default admin credentials (username: admin, password: admin123)
INSERT INTO auth_user (public_id, username, password, role)
VALUES (
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
    'admin',
    '{bcrypt}$2y$10$OaX2C.Y8m08gTMUg28WIg.BI9x014i10DuG3yBEfKChqnhKW2K0km',
    'ADMIN'
)
ON CONFLICT (username) DO NOTHING;
