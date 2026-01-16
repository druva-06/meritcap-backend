-- MySQL Bootstrap: Quick fix for authentication issue
-- Run this directly in MySQL to create roles and update users

-- Create roles table
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
    updated_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default roles
INSERT INTO roles (name, display_name, description, is_active, is_system_role) VALUES
('ADMIN', 'Administrator', 'Full system access with all permissions', TRUE, TRUE),
('COUNSELOR', 'Counselor', 'Student counseling and lead management', TRUE, TRUE),
('STUDENT', 'Student', 'Student user with access to applications and services', TRUE, TRUE),
('COLLEGE', 'College Representative', 'College/University representative access', TRUE, TRUE),
('SUB_AGENT', 'Sub Agent', 'Sub-agent with limited partner access', TRUE, TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Add role_id column if it doesn't exist
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'users' 
               AND COLUMN_NAME = 'role_id');

SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE users ADD COLUMN role_id BIGINT AFTER email',
    'SELECT "role_id column already exists"');
    
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing users with role_id (assuming they have a 'role' column)
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN' LIMIT 1) WHERE role = 'ADMIN';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COUNSELOR' LIMIT 1) WHERE role = 'COUNSELOR';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT' LIMIT 1) WHERE role = 'STUDENT';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COLLEGE' LIMIT 1) WHERE role = 'COLLEGE';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'SUB_AGENT' LIMIT 1) WHERE role = 'SUB_AGENT';

-- Set default for any NULL role_id
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT' LIMIT 1) WHERE role_id IS NULL;

-- Show results
SELECT 'Roles created:' as info;
SELECT * FROM roles;

SELECT 'Users with roles:' as info;
SELECT u.id, u.email, u.username, r.name as role_name, r.display_name 
FROM users u 
LEFT JOIN roles r ON u.role_id = r.id 
LIMIT 10;
