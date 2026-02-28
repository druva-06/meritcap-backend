-- ============================================================
-- MeritCap - Seed Permissions
-- ============================================================
-- Inserts all 178 permissions across 18 categories.
-- Safe to re-run (uses ON DUPLICATE KEY UPDATE).
--
-- Usage:
--   mysql -u meritcap -p meritcap < database/seed/002_seed_permissions.sql
-- ============================================================

INSERT INTO permissions (name, display_name, description, category, is_active, created_at, updated_at) VALUES

-- DASHBOARD (5)
('DASHBOARD_VIEW_ADMIN', 'View Admin Dashboard', 'Access to admin overview dashboard', 'DASHBOARD', TRUE, NOW(), NOW()),
('DASHBOARD_VIEW_COLLEGE', 'View College Dashboard', 'Access to college-specific dashboard', 'DASHBOARD', TRUE, NOW(), NOW()),
('DASHBOARD_VIEW_COUNSELOR', 'View Counselor Dashboard', 'Access to counselor dashboard', 'DASHBOARD', TRUE, NOW(), NOW()),
('DASHBOARD_VIEW_SUBAGENT', 'View Sub-Agent Dashboard', 'Access to sub-agent dashboard', 'DASHBOARD', TRUE, NOW(), NOW()),
('DASHBOARD_VIEW_STUDENT', 'View Student Dashboard', 'Access to student dashboard', 'DASHBOARD', TRUE, NOW(), NOW()),

-- LEADS (13)
('MENU_LEADS', 'Access Leads Menu', 'Can access leads management section', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_VIEW_OWN', 'View Own Leads', 'Can view only own created/referred leads', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_CALL', 'Call Leads', 'Can make calls to leads', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_SEND_EMAIL', 'Send Email to Leads', 'Can send emails to leads', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_VIEW_DETAILS', 'View Lead Details', 'Can view detailed lead information', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_EDIT_OWN', 'Edit Own Leads', 'Can edit only own leads', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_CONVERT', 'Convert Leads', 'Can convert leads to students', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_FOLLOW_UP', 'Follow Up Leads', 'Can add follow-up activities to leads', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_VIEW_HISTORY', 'View Lead History', 'Can view lead activity history', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_SEND_SMS', 'Send SMS to Leads', 'Can send SMS to leads', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_BULK_EDIT', 'Bulk Edit Leads', 'Can edit multiple leads at once', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_BULK_DELETE', 'Bulk Delete Leads', 'Can delete multiple leads at once', 'LEADS', TRUE, NOW(), NOW()),
('LEAD_MERGE', 'Merge Leads', 'Can merge duplicate lead records', 'LEADS', TRUE, NOW(), NOW()),

-- AI CALLING (4)
('MENU_AI_CALLING', 'Access AI Calling', 'Can access AI calling features', 'AI_CALLING', TRUE, NOW(), NOW()),
('AI_CALLING_MAKE_CALL', 'Make AI Calls', 'Can initiate AI-powered calls', 'AI_CALLING', TRUE, NOW(), NOW()),
('AI_CALLING_VIEW_HISTORY', 'View AI Call History', 'Can view AI call logs', 'AI_CALLING', TRUE, NOW(), NOW()),
('AI_CALLING_VIEW_ANALYTICS', 'View AI Call Analytics', 'Can view AI call performance', 'AI_CALLING', TRUE, NOW(), NOW()),

-- STUDENTS (14)
('STUDENT_VIEW_REFERRED', 'View Referred Students', 'Can view only referred students', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_VIEW_DETAILS', 'View Student Details', 'Can view detailed student information', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_VIEW_DOCUMENTS', 'View Student Documents', 'Can view student uploaded documents', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_UPLOAD_DOCUMENTS', 'Upload Student Documents', 'Can upload documents for students', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_VIEW_APPLICATIONS', 'View Student Applications', 'Can view applications submitted by student', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_ASSIGN', 'Assign Students', 'Can assign students to counselors', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_TRANSFER', 'Transfer Students', 'Can transfer students between counselors', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_BULK_ASSIGN', 'Bulk Assign Students', 'Can bulk assign students', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_BULK_EDIT', 'Bulk Edit Students', 'Can edit multiple students at once', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_VIEW_HISTORY', 'View Student History', 'Can view student activity history', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_MANAGE_PROFILE', 'Manage Student Profile', 'Can manage complete student profile', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_SEND_NOTIFICATIONS', 'Send Student Notifications', 'Can send notifications to students', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_VIEW_PAYMENTS', 'View Student Payments', 'Can view student payment history', 'STUDENTS', TRUE, NOW(), NOW()),
('STUDENT_ARCHIVE', 'Archive Students', 'Can archive student records', 'STUDENTS', TRUE, NOW(), NOW()),

-- APPLICATIONS (17)
('APPLICATION_VIEW_REFERRED', 'View Referred Applications', 'Can view applications for referred students', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_VIEW_OWN', 'View Own Applications', 'Can view own applications', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_SUBMIT', 'Submit Applications', 'Can submit applications for review', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_REVIEW', 'Review Applications', 'Can review and evaluate applications', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_UPDATE_STATUS', 'Update Application Status', 'Can update application status', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_TRACK', 'Track Applications', 'Can track application progress', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_VIEW_DOCUMENTS', 'View Application Documents', 'Can view documents attached to applications', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_UPLOAD_DOCUMENTS', 'Upload Application Documents', 'Can upload documents for applications', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_WITHDRAW', 'Withdraw Applications', 'Can withdraw submitted applications', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_BULK_UPDATE', 'Bulk Update Applications', 'Can update multiple applications at once', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_VIEW_HISTORY', 'View Application History', 'Can view application activity history', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_SEND_NOTIFICATIONS', 'Send Application Notifications', 'Can send notifications about applications', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_VIEW_ANALYTICS', 'View Application Analytics', 'Can view application statistics', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_ASSIGN', 'Assign Applications', 'Can assign applications to reviewers', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_APPROVE_DOCUMENTS', 'Approve Application Documents', 'Can approve/reject application documents', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_REQUEST_CHANGES', 'Request Application Changes', 'Can request changes to applications', 'APPLICATIONS', TRUE, NOW(), NOW()),
('APPLICATION_ARCHIVE', 'Archive Applications', 'Can archive application records', 'APPLICATIONS', TRUE, NOW(), NOW()),

-- COMMUNITY (5)
('MENU_COMMUNITY', 'Access Community', 'Can access community features', 'COMMUNITY', TRUE, NOW(), NOW()),
('COMMUNITY_VIEW', 'View Community Posts', 'Can view community posts', 'COMMUNITY', TRUE, NOW(), NOW()),
('COMMUNITY_CREATE_POST', 'Create Community Posts', 'Can create new posts', 'COMMUNITY', TRUE, NOW(), NOW()),
('COMMUNITY_COMMENT', 'Comment on Posts', 'Can comment on posts', 'COMMUNITY', TRUE, NOW(), NOW()),
('COMMUNITY_MODERATE', 'Moderate Community', 'Can moderate community content', 'COMMUNITY', TRUE, NOW(), NOW()),

-- COLLEGES (20)
('MENU_COLLEGES', 'Access Colleges Menu', 'Can access colleges management', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_OWN_PROFILE', 'View Own College Profile', 'Can view own college profile', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_EDIT_OWN_PROFILE', 'Edit Own College Profile', 'Can edit own college details', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_MANAGE_COURSES', 'Manage College Courses', 'Can add/edit/delete courses', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_MANAGE_INTAKES', 'Manage College Intakes', 'Can manage intake dates and capacity', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_ANALYTICS', 'View College Analytics', 'Can view enrollment analytics', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_COMMISSION', 'View Commission Structure', 'Can view commission rates', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_MANAGE_DOCUMENTS', 'Manage College Documents', 'Can upload/manage documents', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_REVIEW_APPLICATIONS', 'Review College Applications', 'Can review applications', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_APPROVE_APPLICATIONS', 'Approve College Applications', 'Can approve/reject applications', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_MANAGE_REQUIREMENTS', 'Manage Course Requirements', 'Can set admission requirements', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_MANAGE_SCHOLARSHIPS', 'Manage Scholarships', 'Can manage scholarship offerings', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_STUDENTS', 'View College Students', 'Can view enrolled students', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_PARTNERS', 'View College Partners', 'Can view partner agents', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_MANAGE_STAFF', 'Manage College Staff', 'Can manage college staff accounts', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_PAYMENTS', 'View College Payments', 'Can view payment transactions', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_INTAKES', 'View Intake Calendar', 'Can view intake dates', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_SEARCH', 'Search Colleges', 'Can search for colleges', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_VIEW_PUBLIC', 'View Public College Info', 'Can view public college info', 'COLLEGES', TRUE, NOW(), NOW()),
('COLLEGE_COMPARE', 'Compare Colleges', 'Can compare multiple colleges', 'COLLEGES', TRUE, NOW(), NOW()),

-- COURSES (6)
('COURSE_SEARCH', 'Search Courses', 'Can search for courses', 'COURSES', TRUE, NOW(), NOW()),
('COURSE_VIEW_DETAILS', 'View Course Details', 'Can view detailed course info', 'COURSES', TRUE, NOW(), NOW()),
('COURSE_MANAGE_CURRICULUM', 'Manage Course Curriculum', 'Can manage curriculum details', 'COURSES', TRUE, NOW(), NOW()),
('COURSE_MANAGE_FEES', 'Manage Course Fees', 'Can set and update course fees', 'COURSES', TRUE, NOW(), NOW()),
('COURSE_COMPARE', 'Compare Courses', 'Can compare multiple courses', 'COURSES', TRUE, NOW(), NOW()),
('COURSE_VIEW_ANALYTICS', 'View Course Analytics', 'Can view course enrollment stats', 'COURSES', TRUE, NOW(), NOW()),

-- PARTNERS (8)
('MENU_PARTNERS', 'Access Partners Menu', 'Can access partners management', 'PARTNERS', TRUE, NOW(), NOW()),
('PARTNER_VIEW_ALL', 'View All Partners', 'Can view all partner records', 'PARTNERS', TRUE, NOW(), NOW()),
('PARTNER_CREATE', 'Create Partners', 'Can add new partners', 'PARTNERS', TRUE, NOW(), NOW()),
('PARTNER_EDIT', 'Edit Partners', 'Can edit partner info', 'PARTNERS', TRUE, NOW(), NOW()),
('PARTNER_DELETE', 'Delete Partners', 'Can remove partners', 'PARTNERS', TRUE, NOW(), NOW()),
('PARTNER_VIEW_PERFORMANCE', 'View Partner Performance', 'Can view partner metrics', 'PARTNERS', TRUE, NOW(), NOW()),
('PARTNER_MANAGE_COMMISSION', 'Manage Partner Commission', 'Can set commission rates', 'PARTNERS', TRUE, NOW(), NOW()),
('PARTNER_VIEW_PAYMENTS', 'View Partner Payments', 'Can view partner payments', 'PARTNERS', TRUE, NOW(), NOW()),

-- MARKETING (10)
('MENU_MARKETING', 'Access Marketing Menu', 'Can access marketing section', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_VIEW', 'View Marketing Campaigns', 'Can view campaigns', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_CREATE', 'Create Marketing Campaigns', 'Can create campaigns', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_EDIT', 'Edit Marketing Campaigns', 'Can edit campaigns', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_DELETE', 'Delete Marketing Campaigns', 'Can delete campaigns', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_VIEW_ANALYTICS', 'View Marketing Analytics', 'Can view campaign performance', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_MANAGE_WEBINARS', 'Manage Webinars', 'Can manage webinars', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_MANAGE_EVENTS', 'Manage Events', 'Can manage events', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_MANAGE_CONTENT', 'Manage Marketing Content', 'Can manage marketing content', 'MARKETING', TRUE, NOW(), NOW()),
('MARKETING_VIEW_BUDGET', 'View Marketing Budget', 'Can view marketing budget', 'MARKETING', TRUE, NOW(), NOW()),

-- FINANCE (12)
('MENU_FINANCE', 'Access Finance Menu', 'Can access finance section', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_VIEW_ALL', 'View All Transactions', 'Can view all transactions', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_VIEW_OWN_COMMISSIONS', 'View Own Commissions', 'Can view own earnings', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_VIEW_OWN_INVOICES', 'View Own Invoices', 'Can view own invoices', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_CREATE_INVOICE', 'Create Invoices', 'Can create invoices', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_EDIT_INVOICE', 'Edit Invoices', 'Can edit invoices', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_APPROVE_INVOICE', 'Approve Invoices', 'Can approve payments', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_MANAGE_EXPENSES', 'Manage Expenses', 'Can manage expenses', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_VIEW_REPORTS', 'View Financial Reports', 'Can view financial reports', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_EXPORT_DATA', 'Export Financial Data', 'Can export financial data', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_MANAGE_PAYMENTS', 'Manage Payments', 'Can process payments', 'FINANCE', TRUE, NOW(), NOW()),
('FINANCE_VIEW_ANALYTICS', 'View Financial Analytics', 'Can view financial analytics', 'FINANCE', TRUE, NOW(), NOW()),

-- HR (8)
('MENU_HR', 'Access HR Menu', 'Can access HR management', 'HR', TRUE, NOW(), NOW()),
('HR_VIEW_ALL', 'View All HR Records', 'Can view all employee records', 'HR', TRUE, NOW(), NOW()),
('HR_MANAGE_LEAVE', 'Manage Leave', 'Can manage leave requests', 'HR', TRUE, NOW(), NOW()),
('HR_VIEW_ATTENDANCE', 'View Attendance', 'Can view attendance records', 'HR', TRUE, NOW(), NOW()),
('HR_MANAGE_ATTENDANCE', 'Manage Attendance', 'Can mark and edit attendance', 'HR', TRUE, NOW(), NOW()),
('HR_MANAGE_TRAINING', 'Manage Training', 'Can manage training programs', 'HR', TRUE, NOW(), NOW()),
('HR_VIEW_PAYROLL', 'View Payroll', 'Can view payroll info', 'HR', TRUE, NOW(), NOW()),
('HR_MANAGE_DOCUMENTS', 'Manage HR Documents', 'Can manage employee documents', 'HR', TRUE, NOW(), NOW()),

-- ASSETS (6)
('MENU_ASSETS', 'Access Assets Menu', 'Can access assets management', 'ASSETS', TRUE, NOW(), NOW()),
('ASSET_VIEW', 'View Assets', 'Can view asset inventory', 'ASSETS', TRUE, NOW(), NOW()),
('ASSET_CREATE', 'Add Assets', 'Can add new assets', 'ASSETS', TRUE, NOW(), NOW()),
('ASSET_EDIT', 'Edit Assets', 'Can edit asset info', 'ASSETS', TRUE, NOW(), NOW()),
('ASSET_DELETE', 'Delete Assets', 'Can remove assets', 'ASSETS', TRUE, NOW(), NOW()),
('ASSET_ASSIGN', 'Assign Assets', 'Can assign assets to employees', 'ASSETS', TRUE, NOW(), NOW()),

-- REPORTS (11)
('MENU_REPORTS', 'Access Reports Menu', 'Can access reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_VIEW_COLLEGE', 'View College Reports', 'Can view college reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_VIEW_COUNSELOR', 'View Counselor Reports', 'Can view counselor reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_VIEW_SUBAGENT', 'View Sub-Agent Reports', 'Can view sub-agent reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_EXPORT_ALL', 'Export All Reports', 'Can export any report', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_EXPORT_COLLEGE', 'Export College Reports', 'Can export college reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_EXPORT_COUNSELOR', 'Export Counselor Reports', 'Can export counselor reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_EXPORT_SUBAGENT', 'Export Sub-Agent Reports', 'Can export sub-agent reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_VIEW_FINANCIAL', 'View Financial Reports', 'Can view financial reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_VIEW_OPERATIONAL', 'View Operational Reports', 'Can view operational reports', 'REPORTS', TRUE, NOW(), NOW()),
('REPORT_SCHEDULE', 'Schedule Reports', 'Can schedule automated reports', 'REPORTS', TRUE, NOW(), NOW()),

-- ROLES & PERMISSIONS (9)
('MENU_ROLES_PERMISSIONS', 'Access Roles & Permissions', 'Can access roles management', 'ROLES', TRUE, NOW(), NOW()),
('USER_ASSIGN_PERMISSIONS', 'Assign User Permissions', 'Can assign permissions to users', 'ROLES', TRUE, NOW(), NOW()),
('USER_REVOKE_PERMISSIONS', 'Revoke User Permissions', 'Can revoke user permissions', 'ROLES', TRUE, NOW(), NOW()),
('ROLE_ASSIGN_PERMISSIONS', 'Assign Role Permissions', 'Can assign permissions to roles', 'ROLES', TRUE, NOW(), NOW()),
('ROLE_REVOKE_PERMISSIONS', 'Revoke Role Permissions', 'Can revoke permissions from roles', 'ROLES', TRUE, NOW(), NOW()),
('USER_VIEW_PERMISSIONS', 'View User Permissions', 'Can view user permissions', 'ROLES', TRUE, NOW(), NOW()),
('ROLE_VIEW_PERMISSIONS', 'View Role Permissions', 'Can view role permissions', 'ROLES', TRUE, NOW(), NOW()),
('PERMISSION_ASSIGN_TO_MULTIPLE', 'Bulk Assign Permissions', 'Can bulk assign permissions', 'ROLES', TRUE, NOW(), NOW()),
('ROLE_MANAGE_HIERARCHY', 'Manage Role Hierarchy', 'Can manage role hierarchy', 'ROLES', TRUE, NOW(), NOW()),

-- COMMUNICATION (5)
('COMMUNICATION_SEND_BULK', 'Send Bulk Messages', 'Can send bulk emails/SMS', 'COMMUNICATION', TRUE, NOW(), NOW()),
('COMMUNICATION_MANAGE_TEMPLATES', 'Manage Templates', 'Can manage message templates', 'COMMUNICATION', TRUE, NOW(), NOW()),
('COMMUNICATION_SCHEDULE', 'Schedule Communications', 'Can schedule messages', 'COMMUNICATION', TRUE, NOW(), NOW()),
('COMMUNICATION_VIEW_ANALYTICS', 'View Communication Analytics', 'Can view communication metrics', 'COMMUNICATION', TRUE, NOW(), NOW()),
('COMMUNICATION_MANAGE_PREFERENCES', 'Manage Comm Preferences', 'Can manage communication preferences', 'COMMUNICATION', TRUE, NOW(), NOW()),

-- SETTINGS (5)
('SETTINGS_VIEW_ALL', 'View System Settings', 'Can view all settings', 'SETTINGS', TRUE, NOW(), NOW()),
('SETTINGS_UPDATE_ALL', 'Update System Settings', 'Can update system settings', 'SETTINGS', TRUE, NOW(), NOW()),
('SETTINGS_VIEW_OWN', 'View Own Settings', 'Can view personal settings', 'SETTINGS', TRUE, NOW(), NOW()),
('SETTINGS_UPDATE_OWN', 'Update Own Settings', 'Can update personal settings', 'SETTINGS', TRUE, NOW(), NOW()),
('SETTINGS_MANAGE_INTEGRATIONS', 'Manage Integrations', 'Can manage third-party integrations', 'SETTINGS', TRUE, NOW(), NOW()),

-- PROFILE (6)
('PROFILE_VIEW_OWN', 'View Own Profile', 'Can view own profile', 'PROFILE', TRUE, NOW(), NOW()),
('PROFILE_EDIT_OWN', 'Edit Own Profile', 'Can edit own profile', 'PROFILE', TRUE, NOW(), NOW()),
('PROFILE_UPLOAD_DOCUMENTS', 'Upload Profile Documents', 'Can upload own documents', 'PROFILE', TRUE, NOW(), NOW()),
('PROFILE_DELETE_DOCUMENTS', 'Delete Profile Documents', 'Can delete own documents', 'PROFILE', TRUE, NOW(), NOW()),
('PROFILE_CHANGE_PASSWORD', 'Change Password', 'Can change own password', 'PROFILE', TRUE, NOW(), NOW()),
('PROFILE_MANAGE_NOTIFICATIONS', 'Manage Notifications', 'Can manage notification settings', 'PROFILE', TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  description  = VALUES(description),
  category     = VALUES(category),
  is_active    = VALUES(is_active),
  updated_at   = NOW();

-- Verify
SELECT category, COUNT(*) AS count FROM permissions GROUP BY category ORDER BY category;
