-- Migration: Create Roles Table and Migrate from Enum to Dynamic Roles
-- Date: 2025-12-27
-- Description: Convert role from enum to dynamic roles table

-- Step 1: Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Step 2: Insert default/system roles
INSERT INTO roles (name, display_name, description, is_active, is_system_role) VALUES
('ADMIN', 'Administrator', 'Full system access with all permissions', TRUE, TRUE),
('COUNSELOR', 'Counselor', 'Student counseling and lead management', TRUE, TRUE),
('STUDENT', 'Student', 'Student user with access to applications and services', TRUE, TRUE),
('COLLEGE', 'College Representative', 'College/University representative access', TRUE, TRUE),
('SUB_AGENT', 'Sub Agent', 'Sub-agent with limited partner access', TRUE, TRUE);

-- Step 3: Add role_id column to users table (nullable initially for migration)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id BIGINT;

-- Step 4: Create foreign key constraint (will be added after data migration)
-- ALTER TABLE users ADD CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id);

-- Step 5: Migrate existing role data from enum to role_id
-- Map enum values to role IDs
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN') WHERE role = 'ADMIN';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COUNSELOR') WHERE role = 'COUNSELOR';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT') WHERE role = 'STUDENT';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COLLEGE') WHERE role = 'COLLEGE';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'SUB_AGENT') WHERE role = 'SUB_AGENT';

-- Step 6: Make role_id NOT NULL after migration
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;

-- Step 7: Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id);

-- Step 8: Drop old role column (optional - keep for rollback capability initially)
-- ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Step 9: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_roles_is_active ON roles(is_active);

-- Step 10: Add comments
COMMENT ON TABLE roles IS 'Dynamic roles table for flexible role management';
COMMENT ON COLUMN roles.name IS 'Unique role name (uppercase, e.g., ADMIN, COUNSELOR)';
COMMENT ON COLUMN roles.display_name IS 'Human-readable role name';
COMMENT ON COLUMN roles.is_system_role IS 'System roles cannot be deleted or modified';
COMMENT ON COLUMN users.role_id IS 'Foreign key reference to roles table';
