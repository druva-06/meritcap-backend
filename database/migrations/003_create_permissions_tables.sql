-- Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create role_permissions junction table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_additional_permissions junction table
CREATE TABLE IF NOT EXISTS user_additional_permissions (
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default permissions
INSERT INTO permissions (name, display_name, description, category, is_active) VALUES
-- Lead Management Permissions
('LEAD_VIEW_ALL', 'View All Leads', 'Can view all leads in the system', 'LEADS', TRUE),
('LEAD_VIEW_ASSIGNED', 'View Assigned Leads', 'Can view only assigned leads', 'LEADS', TRUE),
('LEAD_CREATE', 'Create Leads', 'Can create new leads', 'LEADS', TRUE),
('LEAD_EDIT', 'Edit Leads', 'Can edit lead information', 'LEADS', TRUE),
('LEAD_DELETE', 'Delete Leads', 'Can delete leads', 'LEADS', TRUE),
('LEAD_ASSIGN', 'Assign Leads', 'Can assign leads to counselors', 'LEADS', TRUE),
('LEAD_TRANSFER', 'Transfer Leads', 'Can transfer leads between counselors', 'LEADS', TRUE),
('LEAD_BULK_ASSIGN', 'Bulk Assign Leads', 'Can bulk assign leads', 'LEADS', TRUE),
('LEAD_EXPORT', 'Export Leads', 'Can export lead data', 'LEADS', TRUE),
('LEAD_IMPORT', 'Import Leads', 'Can import lead data', 'LEADS', TRUE),

-- User Management Permissions
('USER_VIEW_ALL', 'View All Users', 'Can view all users', 'USERS', TRUE),
('USER_CREATE', 'Create Users', 'Can create new users', 'USERS', TRUE),
('USER_EDIT', 'Edit Users', 'Can edit user information', 'USERS', TRUE),
('USER_DELETE', 'Delete Users', 'Can delete users', 'USERS', TRUE),
('USER_MANAGE_ROLES', 'Manage User Roles', 'Can assign/change user roles', 'USERS', TRUE),
('USER_MANAGE_PERMISSIONS', 'Manage User Permissions', 'Can assign/revoke user permissions', 'USERS', TRUE),
('USER_RESET_PASSWORD', 'Reset User Password', 'Can reset user passwords', 'USERS', TRUE),
('USER_LOCK_UNLOCK', 'Lock/Unlock Users', 'Can lock or unlock user accounts', 'USERS', TRUE),

-- Student Management Permissions
('STUDENT_VIEW_ALL', 'View All Students', 'Can view all students', 'STUDENTS', TRUE),
('STUDENT_VIEW_ASSIGNED', 'View Assigned Students', 'Can view only assigned students', 'STUDENTS', TRUE),
('STUDENT_CREATE', 'Create Students', 'Can create student profiles', 'STUDENTS', TRUE),
('STUDENT_EDIT', 'Edit Students', 'Can edit student information', 'STUDENTS', TRUE),
('STUDENT_DELETE', 'Delete Students', 'Can delete student profiles', 'STUDENTS', TRUE),
('STUDENT_EXPORT', 'Export Students', 'Can export student data', 'STUDENTS', TRUE),

-- Application Management Permissions
('APPLICATION_VIEW_ALL', 'View All Applications', 'Can view all applications', 'APPLICATIONS', TRUE),
('APPLICATION_VIEW_ASSIGNED', 'View Assigned Applications', 'Can view only assigned applications', 'APPLICATIONS', TRUE),
('APPLICATION_CREATE', 'Create Applications', 'Can create new applications', 'APPLICATIONS', TRUE),
('APPLICATION_EDIT', 'Edit Applications', 'Can edit application details', 'APPLICATIONS', TRUE),
('APPLICATION_DELETE', 'Delete Applications', 'Can delete applications', 'APPLICATIONS', TRUE),
('APPLICATION_APPROVE', 'Approve Applications', 'Can approve/reject applications', 'APPLICATIONS', TRUE),
('APPLICATION_EXPORT', 'Export Applications', 'Can export application data', 'APPLICATIONS', TRUE),

-- College Management Permissions
('COLLEGE_VIEW_ALL', 'View All Colleges', 'Can view all colleges', 'COLLEGES', TRUE),
('COLLEGE_CREATE', 'Create Colleges', 'Can add new colleges', 'COLLEGES', TRUE),
('COLLEGE_EDIT', 'Edit Colleges', 'Can edit college information', 'COLLEGES', TRUE),
('COLLEGE_DELETE', 'Delete Colleges', 'Can delete colleges', 'COLLEGES', TRUE),

-- Course Management Permissions
('COURSE_VIEW_ALL', 'View All Courses', 'Can view all courses', 'COURSES', TRUE),
('COURSE_CREATE', 'Create Courses', 'Can add new courses', 'COURSES', TRUE),
('COURSE_EDIT', 'Edit Courses', 'Can edit course information', 'COURSES', TRUE),
('COURSE_DELETE', 'Delete Courses', 'Can delete courses', 'COURSES', TRUE),

-- Report Permissions
('REPORT_VIEW_ALL', 'View All Reports', 'Can view all reports', 'REPORTS', TRUE),
('REPORT_EXPORT', 'Export Reports', 'Can export report data', 'REPORTS', TRUE),
('REPORT_ANALYTICS', 'View Analytics', 'Can view analytics dashboards', 'REPORTS', TRUE),

-- Communication Permissions
('COMMUNICATION_SEND_EMAIL', 'Send Emails', 'Can send emails to leads/students', 'COMMUNICATION', TRUE),
('COMMUNICATION_SEND_SMS', 'Send SMS', 'Can send SMS to leads/students', 'COMMUNICATION', TRUE),
('COMMUNICATION_VIEW_HISTORY', 'View Communication History', 'Can view communication logs', 'COMMUNICATION', TRUE),

-- Document Management Permissions
('DOCUMENT_VIEW_ALL', 'View All Documents', 'Can view all documents', 'DOCUMENTS', TRUE),
('DOCUMENT_UPLOAD', 'Upload Documents', 'Can upload documents', 'DOCUMENTS', TRUE),
('DOCUMENT_DELETE', 'Delete Documents', 'Can delete documents', 'DOCUMENTS', TRUE),
('DOCUMENT_DOWNLOAD', 'Download Documents', 'Can download documents', 'DOCUMENTS', TRUE),

-- System Settings Permissions
('SETTINGS_VIEW', 'View Settings', 'Can view system settings', 'SETTINGS', TRUE),
('SETTINGS_EDIT', 'Edit Settings', 'Can modify system settings', 'SETTINGS', TRUE),

-- Role & Permission Management
('ROLE_VIEW_ALL', 'View All Roles', 'Can view all roles', 'ROLES', TRUE),
('ROLE_CREATE', 'Create Roles', 'Can create new roles', 'ROLES', TRUE),
('ROLE_EDIT', 'Edit Roles', 'Can edit role information', 'ROLES', TRUE),
('ROLE_DELETE', 'Delete Roles', 'Can delete roles', 'ROLES', TRUE),
('PERMISSION_VIEW_ALL', 'View All Permissions', 'Can view all permissions', 'PERMISSIONS', TRUE),
('PERMISSION_CREATE', 'Create Permissions', 'Can create new permissions', 'PERMISSIONS', TRUE),
('PERMISSION_EDIT', 'Edit Permissions', 'Can edit permission information', 'PERMISSIONS', TRUE),
('PERMISSION_DELETE', 'Delete Permissions', 'Can delete permissions', 'PERMISSIONS', TRUE)
ON DUPLICATE KEY UPDATE 
    display_name = VALUES(display_name),
    description = VALUES(description),
    category = VALUES(category),
    is_active = VALUES(is_active);
