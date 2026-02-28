-- ============================================================
-- MeritCap - Assign Permissions to Roles
-- ============================================================
-- Run AFTER 001_seed_roles.sql and 002_seed_permissions.sql
--
-- Usage:
--   mysql -u meritcap -p meritcap < database/seed/003_seed_role_permissions.sql
-- ============================================================

-- ============================================
-- ADMIN: Gets ALL permissions
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================
-- COUNSELOR: Lead mgmt, student mgmt, applications, colleges, courses, comms
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'COUNSELOR'
  AND p.name IN (
    'DASHBOARD_VIEW_COUNSELOR',
    'MENU_LEADS', 'LEAD_VIEW_OWN', 'LEAD_CALL', 'LEAD_SEND_EMAIL', 'LEAD_VIEW_DETAILS',
    'LEAD_EDIT_OWN', 'LEAD_CONVERT', 'LEAD_FOLLOW_UP', 'LEAD_VIEW_HISTORY', 'LEAD_SEND_SMS',
    'STUDENT_VIEW_DETAILS', 'STUDENT_VIEW_DOCUMENTS', 'STUDENT_UPLOAD_DOCUMENTS',
    'STUDENT_VIEW_APPLICATIONS', 'STUDENT_VIEW_HISTORY', 'STUDENT_MANAGE_PROFILE',
    'APPLICATION_REVIEW', 'APPLICATION_UPDATE_STATUS', 'APPLICATION_TRACK',
    'APPLICATION_VIEW_DOCUMENTS', 'APPLICATION_UPLOAD_DOCUMENTS', 'APPLICATION_VIEW_HISTORY',
    'MENU_COLLEGES', 'COLLEGE_SEARCH', 'COLLEGE_VIEW_PUBLIC', 'COLLEGE_COMPARE',
    'COURSE_SEARCH', 'COURSE_VIEW_DETAILS', 'COURSE_COMPARE',
    'COMMUNICATION_SEND_BULK', 'COMMUNICATION_VIEW_ANALYTICS',
    'REPORT_VIEW_COUNSELOR', 'REPORT_EXPORT_COUNSELOR',
    'PROFILE_VIEW_OWN', 'PROFILE_EDIT_OWN', 'PROFILE_CHANGE_PASSWORD',
    'SETTINGS_VIEW_OWN', 'SETTINGS_UPDATE_OWN'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================
-- STUDENT: Self-service access only
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'STUDENT'
  AND p.name IN (
    'DASHBOARD_VIEW_STUDENT',
    'COLLEGE_SEARCH', 'COLLEGE_VIEW_PUBLIC', 'COLLEGE_COMPARE',
    'COURSE_SEARCH', 'COURSE_VIEW_DETAILS', 'COURSE_COMPARE',
    'APPLICATION_VIEW_OWN', 'APPLICATION_SUBMIT', 'APPLICATION_TRACK',
    'APPLICATION_VIEW_DOCUMENTS', 'APPLICATION_UPLOAD_DOCUMENTS', 'APPLICATION_WITHDRAW',
    'COMMUNITY_VIEW', 'COMMUNITY_CREATE_POST', 'COMMUNITY_COMMENT',
    'PROFILE_VIEW_OWN', 'PROFILE_EDIT_OWN', 'PROFILE_UPLOAD_DOCUMENTS',
    'PROFILE_DELETE_DOCUMENTS', 'PROFILE_CHANGE_PASSWORD', 'PROFILE_MANAGE_NOTIFICATIONS',
    'SETTINGS_VIEW_OWN', 'SETTINGS_UPDATE_OWN'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================
-- COLLEGE: College portal access
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'COLLEGE'
  AND p.name IN (
    'DASHBOARD_VIEW_COLLEGE',
    'COLLEGE_VIEW_OWN_PROFILE', 'COLLEGE_EDIT_OWN_PROFILE', 'COLLEGE_MANAGE_COURSES',
    'COLLEGE_MANAGE_INTAKES', 'COLLEGE_VIEW_ANALYTICS', 'COLLEGE_MANAGE_DOCUMENTS',
    'COLLEGE_REVIEW_APPLICATIONS', 'COLLEGE_APPROVE_APPLICATIONS', 'COLLEGE_MANAGE_REQUIREMENTS',
    'COLLEGE_MANAGE_SCHOLARSHIPS', 'COLLEGE_VIEW_STUDENTS', 'COLLEGE_VIEW_PARTNERS',
    'COLLEGE_MANAGE_STAFF', 'COLLEGE_VIEW_PAYMENTS', 'COLLEGE_VIEW_INTAKES',
    'REPORT_VIEW_COLLEGE', 'REPORT_EXPORT_COLLEGE',
    'PROFILE_VIEW_OWN', 'PROFILE_EDIT_OWN', 'PROFILE_CHANGE_PASSWORD',
    'SETTINGS_VIEW_OWN', 'SETTINGS_UPDATE_OWN'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================
-- SUB_AGENT: Referral-based access
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'SUB_AGENT'
  AND p.name IN (
    'DASHBOARD_VIEW_SUBAGENT',
    'MENU_LEADS', 'LEAD_VIEW_OWN', 'LEAD_SEND_EMAIL', 'LEAD_VIEW_DETAILS',
    'LEAD_EDIT_OWN', 'LEAD_CONVERT', 'LEAD_FOLLOW_UP',
    'STUDENT_VIEW_REFERRED', 'STUDENT_VIEW_DETAILS',
    'APPLICATION_VIEW_REFERRED', 'APPLICATION_TRACK',
    'COLLEGE_SEARCH', 'COLLEGE_VIEW_PUBLIC', 'COLLEGE_COMPARE',
    'COURSE_SEARCH', 'COURSE_VIEW_DETAILS', 'COURSE_COMPARE',
    'FINANCE_VIEW_OWN_COMMISSIONS', 'FINANCE_VIEW_OWN_INVOICES',
    'REPORT_VIEW_SUBAGENT', 'REPORT_EXPORT_SUBAGENT',
    'PROFILE_VIEW_OWN', 'PROFILE_EDIT_OWN', 'PROFILE_CHANGE_PASSWORD',
    'SETTINGS_VIEW_OWN', 'SETTINGS_UPDATE_OWN'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- Verify
SELECT r.name AS role, COUNT(rp.permission_id) AS permissions
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.role_id
GROUP BY r.name
ORDER BY permissions DESC;
