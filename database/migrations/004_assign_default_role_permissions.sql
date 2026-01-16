-- Assign default permissions to ADMIN role
-- First, get the role ID for ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- Assign basic permissions to COUNSELOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'COUNSELOR'
AND p.name IN (
    'LEAD_VIEW_ASSIGNED',
    'LEAD_CREATE',
    'LEAD_EDIT',
    'STUDENT_VIEW_ASSIGNED',
    'STUDENT_CREATE',
    'STUDENT_EDIT',
    'APPLICATION_VIEW_ASSIGNED',
    'APPLICATION_CREATE',
    'APPLICATION_EDIT',
    'COLLEGE_VIEW_ALL',
    'COURSE_VIEW_ALL',
    'COMMUNICATION_SEND_EMAIL',
    'COMMUNICATION_SEND_SMS',
    'COMMUNICATION_VIEW_HISTORY',
    'DOCUMENT_VIEW_ALL',
    'DOCUMENT_UPLOAD',
    'DOCUMENT_DOWNLOAD'
)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- Assign basic permissions to STUDENT role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'STUDENT'
AND p.name IN (
    'COLLEGE_VIEW_ALL',
    'COURSE_VIEW_ALL',
    'DOCUMENT_VIEW_ALL',
    'DOCUMENT_UPLOAD'
)
ON DUPLICATE KEY UPDATE role_id = role_id;
