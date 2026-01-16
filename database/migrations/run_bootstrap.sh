#!/bin/bash

# MySQL Connection Details
DB_HOST="82.112.234.51"
DB_PORT="3306"
DB_NAME="cap_dev"
DB_USER="cap_dev"
DB_PASS="zmhCuJR.uX3mB]4"

echo "========================================="
echo "Bootstrap Roles - MySQL Quick Fix"
echo "========================================="
echo ""
echo "Run these SQL statements in your MySQL client:"
echo ""
echo "mysql -h $DB_HOST -u $DB_USER -p$DB_PASS $DB_NAME"
echo ""
echo "Then copy and paste the following SQL:"
echo ""
cat << 'EOF'

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
    updated_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 2: Insert default roles
INSERT INTO roles (name, display_name, description, is_active, is_system_role) VALUES
('ADMIN', 'Administrator', 'Full system access with all permissions', TRUE, TRUE),
('COUNSELOR', 'Counselor', 'Student counseling and lead management', TRUE, TRUE),
('STUDENT', 'Student', 'Student user with access to applications and services', TRUE, TRUE),
('COLLEGE', 'College Representative', 'College/University representative access', TRUE, TRUE),
('SUB_AGENT', 'Sub Agent', 'Sub-agent with limited partner access', TRUE, TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Step 3: Check if role_id column exists, if not add it
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id BIGINT;

-- Step 4: Update users with existing roles
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'ADMIN' LIMIT 1) WHERE role = 'ADMIN';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COUNSELOR' LIMIT 1) WHERE role = 'COUNSELOR';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT' LIMIT 1) WHERE role = 'STUDENT';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'COLLEGE' LIMIT 1) WHERE role = 'COLLEGE';
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'SUB_AGENT' LIMIT 1) WHERE role = 'SUB_AGENT';

-- Step 5: Set default role for users without one
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT' LIMIT 1) WHERE role_id IS NULL;

-- Step 6: Verify
SELECT 'Roles created successfully!' as message;
SELECT * FROM roles;

SELECT 'User roles updated!' as message;
SELECT u.id, u.email, r.name as role, r.display_name 
FROM users u 
LEFT JOIN roles r ON u.role_id = r.id 
LIMIT 10;

EOF

echo ""
echo "========================================="
echo "After running the SQL, restart your Spring Boot application"
echo "========================================="
