-- V3__add_professor_login_tokens.sql
-- Migration to add professor_login_token table for magic link authentication

CREATE TABLE professor_login_token (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    professor_id BIGINT NOT NULL REFERENCES professor(id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_professor_login_token ON professor_login_token(token);
CREATE INDEX idx_professor_login_token_expires_at ON professor_login_token(expires_at);
CREATE INDEX idx_professor_login_token_professor_id ON professor_login_token(professor_id);
