-- ============================================================
-- MeritCap - Seed Admin User
-- ============================================================
-- Inserts an admin user into the users table.
-- Requires roles to be seeded first (001_seed_roles.sql).
--
-- Usage:
--   mysql -u meritcap -p meritcap < database/seed/004_seed_admin_user.sql
-- ============================================================

INSERT INTO users (
    first_name,
    last_name,
    username,
    email,
    phone_number,
    role_id,
    failed_login_attempts,
    account_locked,
    created_at,
    updated_at
)
SELECT
    'Admin',
    'User',
    'technology.cap18',
    'technology.cap18@gmail.com',
    '0000000000',
    r.id,
    0,
    false,
    NOW(6),
    NOW(6)
FROM roles r
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE
    role_id = VALUES(role_id),
    updated_at = NOW(6);

-- Verify
SELECT id, first_name, last_name, email, role_id
FROM users
WHERE email = 'technology.cap18@gmail.com';
