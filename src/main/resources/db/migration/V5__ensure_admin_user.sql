-- Ensure admin user exists (idempotent)
-- Username: admin, Password: admin123 (bcrypt hashed)
INSERT INTO auth_user (public_id, username, password, role)
VALUES (
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
    'admin',
    '{bcrypt}$2y$10$OaX2C.Y8m08gTMUg28WIg.BI9x014i10DuG3yBEfKChqnhKW2K0km',
    'ADMIN'
)
ON CONFLICT (username) DO NOTHING;
