-- Authentication users table and default admin seed
CREATE TABLE auth_user (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    username VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    professor_id BIGINT UNIQUE REFERENCES professor(id) ON DELETE SET NULL
);

CREATE INDEX idx_auth_user_username ON auth_user(username);
CREATE INDEX idx_auth_user_public_id ON auth_user(public_id);

-- Seed default admin credentials (username: admin, password: admin123)
INSERT INTO auth_user (public_id, username, password, role)
VALUES (
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
    'admin',
    '{bcrypt}$2y$10$OaX2C.Y8m08gTMUg28WIg.BI9x014i10DuG3yBEfKChqnhKW2K0km',
    'ADMIN'
);
