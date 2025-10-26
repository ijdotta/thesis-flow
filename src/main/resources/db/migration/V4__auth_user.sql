-- Authentication users table and default admin seed
-- (CREATE TABLE IF NOT EXISTS in case Hibernate already created it)
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

-- Seed default admin credentials (username: admin, password: admin123)
-- Using ON CONFLICT to handle idempotency
INSERT INTO auth_user (public_id, username, password, role)
VALUES (
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
    'admin',
    '{bcrypt}$2y$10$OaX2C.Y8m08gTMUg28WIg.BI9x014i10DuG3yBEfKChqnhKW2K0km',
    'ADMIN'
)
ON CONFLICT (username) DO NOTHING;

