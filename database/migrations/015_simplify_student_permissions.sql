-- Migration 015: Add unified student and application view permissions
-- Replaces multi-permission frontend checks with single canonical permissions.
-- Existing STUDENT_VIEW_ALL/ASSIGNED/REFERRED remain for backend data-scoping.

INSERT IGNORE INTO permissions (name, display_name, description, category, dashboard, submenu, feature, is_active, created_at, updated_at)
VALUES
  ('STUDENT_VIEW',     'View Students',     'Access the Students section in admin portal', 'STUDENTS',     'Students', 'Student Management', 'View', TRUE, NOW(), NOW()),
  ('APPLICATION_VIEW', 'View Applications', 'Access Applications tab within student detail', 'APPLICATIONS', 'Students', 'Applications',       'View', TRUE, NOW(), NOW());

-- Assign STUDENT_VIEW to roles that work with students
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE p.name = 'STUDENT_VIEW'
  AND r.name IN ('COUNSELOR', 'SUB_AGENT', 'MANAGER', 'COLLEGE');

-- Assign APPLICATION_VIEW to roles that process applications
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE p.name = 'APPLICATION_VIEW'
  AND r.name IN ('COUNSELOR', 'SUB_AGENT', 'MANAGER');
