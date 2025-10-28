-- V1: Clean schema creation for initial deployment
-- This migration assumes an empty database (drop schema before running)

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

-- PROJECT
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
