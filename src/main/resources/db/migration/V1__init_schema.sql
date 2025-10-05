-- V1 Baseline schema (clean rewrite)
-- Drop your database before applying this for a pristine start.

-- PERSON
CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL
);
CREATE INDEX idx_person_public_id ON person(public_id);

-- CAREER
CREATE TABLE career (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE INDEX idx_career_public_id ON career(public_id);

-- APPLICATION DOMAIN
CREATE TABLE application_domain (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL
);
CREATE INDEX idx_app_domain_public_id ON application_domain(public_id);

-- TAG
CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NULL
);
CREATE INDEX idx_tag_public_id ON tag(public_id);
CREATE INDEX idx_tag_name ON tag(name);

-- STUDENT (person optional per current entity)
CREATE TABLE student (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    person_id BIGINT REFERENCES person(id) ON DELETE RESTRICT,
    student_id VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE
);
CREATE INDEX idx_student_public_id ON student(public_id);
CREATE INDEX idx_student_email ON student(email);

-- PROFESSOR (person required)
CREATE TABLE professor (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE RESTRICT,
    email VARCHAR(320) NOT NULL UNIQUE
);
CREATE INDEX idx_professor_public_id ON professor(public_id);
CREATE INDEX idx_professor_email ON professor(email);

-- PROJECT (date-only fields)
CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    initial_submission DATE NOT NULL,
    completion DATE NULL,
    application_domain_id BIGINT NULL REFERENCES application_domain(id) ON DELETE SET NULL,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL
);
CREATE INDEX idx_project_public_id ON project(public_id);

-- PROJECT SUBTYPES (ElementCollection for subType)
CREATE TABLE project_subtypes (
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    sub_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (project_id, sub_type)
);

-- PROJECT TAGS (join table) - explicit mapping in entity
CREATE TABLE project_tags (
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE RESTRICT,
    PRIMARY KEY (project_id, tag_id)
);

-- PROJECT PARTICIPANT
CREATE TABLE project_participant (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    project_id BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE RESTRICT,
    participant_role VARCHAR(50) NOT NULL,
    CONSTRAINT uc_project_person_role UNIQUE (project_id, person_id, participant_role)
);
CREATE INDEX idx_project_participant_public_id ON project_participant(public_id);

-- STUDENT CAREER (bridge) unique pair
CREATE TABLE student_career (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    career_id BIGINT NOT NULL REFERENCES career(id) ON DELETE RESTRICT,
    CONSTRAINT uc_student_career UNIQUE (student_id, career_id)
);
CREATE INDEX idx_student_career_public_id ON student_career(public_id);
