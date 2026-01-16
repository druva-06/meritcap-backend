-- Migration: Create Roles Table and Migrate from Enum to Dynamic Roles (MySQL Version)
-- Date: 2025-12-27
-- Description: Convert role from enum to dynamic roles table

-- Step 1: Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_roles_name (name),
    INDEX idx_roles_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Insert default/system roles
INSERT INTO roles (name, display_name, description, is_active, is_system_role) VALUES
('ADMIN', 'Administrator', 'Full system access with all permissions', TRUE, TRUE),
('COUNSELOR', 'Counselor', 'Student counseling and lead management', TRUE, TRUE),
('STUDENT', 'Student', 'Student user with access to applications and services', TRUE, TRUE),
('COLLEGE', 'College Representative', 'College/University representative access', TRUE, TRUE),
('SUB_AGENT', 'Sub Agent', 'Sub-agent with limited partner access', TRUE, TRUE)
ON DUPLICATE KEY UPDATE name=name; -- Prevents duplicate key error if roles already exist

-- Step 3: Check if role_id column exists, if not add it
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'role_id'
);

-- Add role_id column if it doesn't exist
SET @add_column = IF(@column_exists = 0,
    'ALTER TABLE users ADD COLUMN role_id BIGINT',
    'SELECT "Column role_id already exists" AS message'
);

PREPARE stmt FROM @add_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Migrate existing role data from enum/varchar to role_id (if old role column exists)
-- Check if old role column exists
SET @old_role_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'role'
);

-- Migrate data if old role column exists
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN') WHERE role = 'ADMIN' AND @old_role_exists > 0;
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COUNSELOR') WHERE role = 'COUNSELOR' AND @old_role_exists > 0;
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT') WHERE role = 'STUDENT' AND @old_role_exists > 0;
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COLLEGE') WHERE role = 'COLLEGE' AND @old_role_exists > 0;
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'SUB_AGENT') WHERE role = 'SUB_AGENT' AND @old_role_exists > 0;

-- For any users without role_id, set them to STUDENT by default
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT') WHERE role_id IS NULL;

-- Step 5: Make role_id NOT NULL
ALTER TABLE users MODIFY COLUMN role_id BIGINT NOT NULL;

-- Step 6: Add foreign key constraint (drop first if exists to avoid errors)
SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
    AND CONSTRAINT_NAME = 'fk_users_role_id'
);

SET @drop_fk = IF(@fk_exists > 0,
    'ALTER TABLE users DROP FOREIGN KEY fk_users_role_id',
    'SELECT "FK does not exist, skipping drop" AS message'
);

PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add the foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id);

-- Step 7: Create index on users.role_id for performance
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- Step 8: Drop old role column (optional - commented out for safety)
-- ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Verification queries (optional - uncomment to run)
-- SELECT 'Roles table created' AS status;
-- SELECT * FROM roles;
-- SELECT COUNT(*) AS total_users, role_id FROM users GROUP BY role_id;

-- Success message
SELECT 'Migration completed successfully!' AS status,
       (SELECT COUNT(*) FROM roles) AS total_roles,
       (SELECT COUNT(*) FROM users WHERE role_id IS NOT NULL) AS users_with_roles;
