-- ============================================
-- POPULATE PERMISSION HIERARCHY
-- Version: 1.0
-- Date: 2026-01-16
-- Purpose: Parse existing permissions into hierarchical structure
-- ============================================

-- ============================================
-- 1. DASHBOARD PERMISSIONS
-- ============================================
UPDATE permissions SET dashboard = 'Dashboard', submenu = NULL, feature = 'View Admin' WHERE name = 'DASHBOARD_VIEW_ADMIN';
UPDATE permissions SET dashboard = 'Dashboard', submenu = NULL, feature = 'View College' WHERE name = 'DASHBOARD_VIEW_COLLEGE';
UPDATE permissions SET dashboard = 'Dashboard', submenu = NULL, feature = 'View Counselor' WHERE name = 'DASHBOARD_VIEW_COUNSELOR';
UPDATE permissions SET dashboard = 'Dashboard', submenu = NULL, feature = 'View Sub-Agent' WHERE name = 'DASHBOARD_VIEW_SUBAGENT';
UPDATE permissions SET dashboard = 'Dashboard', submenu = NULL, feature = 'View Student' WHERE name = 'DASHBOARD_VIEW_STUDENT';

-- ============================================
-- 2. LEADS - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Leads', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_LEADS';

-- ============================================
-- 3. LEADS - Lead Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'View Own' WHERE name = 'LEAD_VIEW_OWN';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'View All' WHERE name = 'LEAD_VIEW_ALL';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'View Assigned' WHERE name = 'LEAD_VIEW_ASSIGNED';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Create' WHERE name = 'LEAD_CREATE';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Edit Own' WHERE name = 'LEAD_EDIT_OWN';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Edit All' WHERE name = 'LEAD_EDIT_ALL';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Delete Own' WHERE name = 'LEAD_DELETE_OWN';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Delete All' WHERE name = 'LEAD_DELETE_ALL';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'View Details' WHERE name = 'LEAD_VIEW_DETAILS';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'View History' WHERE name = 'LEAD_VIEW_HISTORY';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Assign' WHERE name = 'LEAD_ASSIGN';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Convert' WHERE name = 'LEAD_CONVERT';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Lead Management', feature = 'Merge' WHERE name = 'LEAD_MERGE';

-- ============================================
-- 4. LEADS - Communication Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Leads', submenu = 'Communication', feature = 'Call' WHERE name = 'LEAD_CALL';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Communication', feature = 'Send Email' WHERE name = 'LEAD_SEND_EMAIL';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Communication', feature = 'Send SMS' WHERE name = 'LEAD_SEND_SMS';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Communication', feature = 'Follow Up' WHERE name = 'LEAD_FOLLOW_UP';

-- ============================================
-- 5. LEADS - Bulk Operations Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Leads', submenu = 'Bulk Operations', feature = 'Bulk Edit' WHERE name = 'LEAD_BULK_EDIT';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Bulk Operations', feature = 'Bulk Delete' WHERE name = 'LEAD_BULK_DELETE';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Bulk Operations', feature = 'Bulk Assign' WHERE name = 'LEAD_BULK_ASSIGN';
UPDATE permissions SET dashboard = 'Leads', submenu = 'Bulk Operations', feature = 'Export' WHERE name = 'LEAD_EXPORT';

-- ============================================
-- 6. AI CALLING - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'AI Calling', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_AI_CALLING';

-- ============================================
-- 7. AI CALLING - AI Calling Submenu
-- ============================================
UPDATE permissions SET dashboard = 'AI Calling', submenu = 'AI Calling', feature = 'Make Call' WHERE name = 'AI_CALLING_MAKE_CALL';
UPDATE permissions SET dashboard = 'AI Calling', submenu = 'AI Calling', feature = 'View History' WHERE name = 'AI_CALLING_VIEW_HISTORY';
UPDATE permissions SET dashboard = 'AI Calling', submenu = 'AI Calling', feature = 'View Analytics' WHERE name = 'AI_CALLING_VIEW_ANALYTICS';

-- ============================================
-- 8. STUDENTS - Student Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'View All' WHERE name = 'STUDENT_VIEW_ALL';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'View Assigned' WHERE name = 'STUDENT_VIEW_ASSIGNED';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'View Referred' WHERE name = 'STUDENT_VIEW_REFERRED';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'Create' WHERE name = 'STUDENT_CREATE';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'Edit All' WHERE name = 'STUDENT_EDIT_ALL';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'Edit Assigned' WHERE name = 'STUDENT_EDIT_ASSIGNED';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'Delete' WHERE name = 'STUDENT_DELETE';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'View Details' WHERE name = 'STUDENT_VIEW_DETAILS';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'View History' WHERE name = 'STUDENT_VIEW_HISTORY';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'Manage Profile' WHERE name = 'STUDENT_MANAGE_PROFILE';
UPDATE permissions SET dashboard = 'Students', submenu = 'Student Management', feature = 'Archive' WHERE name = 'STUDENT_ARCHIVE';

-- ============================================
-- 9. STUDENTS - Document Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Students', submenu = 'Document Management', feature = 'View Documents' WHERE name = 'STUDENT_VIEW_DOCUMENTS';
UPDATE permissions SET dashboard = 'Students', submenu = 'Document Management', feature = 'Upload Documents' WHERE name = 'STUDENT_UPLOAD_DOCUMENTS';
UPDATE permissions SET dashboard = 'Students', submenu = 'Document Management', feature = 'View Applications' WHERE name = 'STUDENT_VIEW_APPLICATIONS';

-- ============================================
-- 10. STUDENTS - Assignment & Transfer Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Students', submenu = 'Assignment & Transfer', feature = 'Assign' WHERE name = 'STUDENT_ASSIGN';
UPDATE permissions SET dashboard = 'Students', submenu = 'Assignment & Transfer', feature = 'Transfer' WHERE name = 'STUDENT_TRANSFER';
UPDATE permissions SET dashboard = 'Students', submenu = 'Assignment & Transfer', feature = 'Bulk Assign' WHERE name = 'STUDENT_BULK_ASSIGN';
UPDATE permissions SET dashboard = 'Students', submenu = 'Assignment & Transfer', feature = 'Bulk Edit' WHERE name = 'STUDENT_BULK_EDIT';

-- ============================================
-- 11. STUDENTS - Communication Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Students', submenu = 'Communication', feature = 'Send Notifications' WHERE name = 'STUDENT_SEND_NOTIFICATIONS';
UPDATE permissions SET dashboard = 'Students', submenu = 'Communication', feature = 'View Payments' WHERE name = 'STUDENT_VIEW_PAYMENTS';

-- ============================================
-- 12. APPLICATIONS - Application Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'View All' WHERE name = 'APPLICATION_VIEW_ALL';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'View Assigned' WHERE name = 'APPLICATION_VIEW_ASSIGNED';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'View Referred' WHERE name = 'APPLICATION_VIEW_REFERRED';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'View Own' WHERE name = 'APPLICATION_VIEW_OWN';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Create' WHERE name = 'APPLICATION_CREATE';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Edit All' WHERE name = 'APPLICATION_EDIT_ALL';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Edit Assigned' WHERE name = 'APPLICATION_EDIT_ASSIGNED';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Delete' WHERE name = 'APPLICATION_DELETE';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Submit' WHERE name = 'APPLICATION_SUBMIT';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Review' WHERE name = 'APPLICATION_REVIEW';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Update Status' WHERE name = 'APPLICATION_UPDATE_STATUS';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Track' WHERE name = 'APPLICATION_TRACK';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Withdraw' WHERE name = 'APPLICATION_WITHDRAW';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'View History' WHERE name = 'APPLICATION_VIEW_HISTORY';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Application Management', feature = 'Archive' WHERE name = 'APPLICATION_ARCHIVE';

-- ============================================
-- 13. APPLICATIONS - Document Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Applications', submenu = 'Document Management', feature = 'View Documents' WHERE name = 'APPLICATION_VIEW_DOCUMENTS';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Document Management', feature = 'Upload Documents' WHERE name = 'APPLICATION_UPLOAD_DOCUMENTS';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Document Management', feature = 'Approve Documents' WHERE name = 'APPLICATION_APPROVE_DOCUMENTS';

-- ============================================
-- 14. APPLICATIONS - Bulk Operations Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Applications', submenu = 'Bulk Operations', feature = 'Bulk Update' WHERE name = 'APPLICATION_BULK_UPDATE';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Bulk Operations', feature = 'Assign' WHERE name = 'APPLICATION_ASSIGN';

-- ============================================
-- 15. APPLICATIONS - Communication Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Applications', submenu = 'Communication', feature = 'Send Notifications' WHERE name = 'APPLICATION_SEND_NOTIFICATIONS';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Communication', feature = 'Request Changes' WHERE name = 'APPLICATION_REQUEST_CHANGES';
UPDATE permissions SET dashboard = 'Applications', submenu = 'Communication', feature = 'View Analytics' WHERE name = 'APPLICATION_VIEW_ANALYTICS';

-- ============================================
-- 16. APPLICATIONS - College View
-- ============================================
UPDATE permissions SET dashboard = 'Applications', submenu = 'College View', feature = 'View Own Applications' WHERE name = 'COLLEGE_VIEW_OWN_APPLICATIONS';

-- ============================================
-- 17. COMMUNITY - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Community', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_COMMUNITY';

-- ============================================
-- 18. COMMUNITY - Community Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Community', submenu = 'Community Management', feature = 'View' WHERE name = 'COMMUNITY_VIEW';
UPDATE permissions SET dashboard = 'Community', submenu = 'Community Management', feature = 'Create Post' WHERE name = 'COMMUNITY_CREATE_POST';
UPDATE permissions SET dashboard = 'Community', submenu = 'Community Management', feature = 'Comment' WHERE name = 'COMMUNITY_COMMENT';
UPDATE permissions SET dashboard = 'Community', submenu = 'Community Management', feature = 'Moderate' WHERE name = 'COMMUNITY_MODERATE';

-- ============================================
-- 19. COLLEGES - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_COLLEGES';

-- ============================================
-- 20. COLLEGES - All Colleges Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'All Colleges', feature = 'View' WHERE name = 'COLLEGE_VIEW_ALL';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'All Colleges', feature = 'Create' WHERE name = 'COLLEGE_CREATE';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'All Colleges', feature = 'Edit All' WHERE name = 'COLLEGE_EDIT_ALL';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'All Colleges', feature = 'Delete' WHERE name = 'COLLEGE_DELETE';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'All Colleges', feature = 'View Details' WHERE name = 'COLLEGE_VIEW_DETAILS';

-- ============================================
-- 21. COLLEGES - My Profile Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'My Profile', feature = 'View' WHERE name = 'COLLEGE_VIEW_OWN_PROFILE';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'My Profile', feature = 'Edit' WHERE name = 'COLLEGE_EDIT_OWN_PROFILE';

-- ============================================
-- 22. COLLEGES - Courses Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Courses', feature = 'Manage' WHERE name = 'COLLEGE_MANAGE_COURSES';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Courses', feature = 'View' WHERE name = 'COLLEGE_VIEW_COURSES';

-- ============================================
-- 23. COLLEGES - Intakes Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Intakes', feature = 'View' WHERE name = 'COLLEGE_VIEW_INTAKES';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Intakes', feature = 'Manage' WHERE name = 'COLLEGE_MANAGE_INTAKES';

-- ============================================
-- 24. COLLEGES - Analytics Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Analytics', feature = 'View' WHERE name = 'COLLEGE_VIEW_ANALYTICS';

-- ============================================
-- 25. COLLEGES - Commission Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Commission', feature = 'View' WHERE name = 'COLLEGE_VIEW_COMMISSION';

-- ============================================
-- 26. COLLEGES - Students Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Students', feature = 'View' WHERE name = 'COLLEGE_VIEW_STUDENTS';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Students', feature = 'Manage' WHERE name = 'COLLEGE_MANAGE_STUDENTS';

-- ============================================
-- 27. COLLEGES - Documents Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Documents', feature = 'View' WHERE name = 'COLLEGE_VIEW_DOCUMENTS';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Documents', feature = 'Upload' WHERE name = 'COLLEGE_UPLOAD_DOCUMENTS';

-- ============================================
-- 28. COLLEGES - Notifications Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Notifications', feature = 'Send' WHERE name = 'COLLEGE_SEND_NOTIFICATIONS';
UPDATE permissions SET dashboard = 'Colleges', submenu = 'Notifications', feature = 'View' WHERE name = 'COLLEGE_VIEW_NOTIFICATIONS';

-- ============================================
-- 29. PARTNERS - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Partners', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_PARTNERS';

-- ============================================
-- 30. PARTNERS - Partner Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Partners', submenu = 'Partner Management', feature = 'View All' WHERE name = 'PARTNER_VIEW_ALL';
UPDATE permissions SET dashboard = 'Partners', submenu = 'Partner Management', feature = 'Create' WHERE name = 'PARTNER_CREATE';
UPDATE permissions SET dashboard = 'Partners', submenu = 'Partner Management', feature = 'Edit' WHERE name = 'PARTNER_EDIT';
UPDATE permissions SET dashboard = 'Partners', submenu = 'Partner Management', feature = 'Delete' WHERE name = 'PARTNER_DELETE';
UPDATE permissions SET dashboard = 'Partners', submenu = 'Partner Management', feature = 'View Details' WHERE name = 'PARTNER_VIEW_DETAILS';

-- ============================================
-- 31. PARTNERS - Commission Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Partners', submenu = 'Commission', feature = 'View Performance' WHERE name = 'PARTNER_VIEW_PERFORMANCE';
UPDATE permissions SET dashboard = 'Partners', submenu = 'Commission', feature = 'Manage Commission' WHERE name = 'PARTNER_MANAGE_COMMISSION';

-- ============================================
-- 32. MARKETING - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Marketing', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_MARKETING';

-- ============================================
-- 33. MARKETING - Marketing Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Marketing', submenu = 'Marketing Management', feature = 'View' WHERE name = 'MARKETING_VIEW';
UPDATE permissions SET dashboard = 'Marketing', submenu = 'Marketing Management', feature = 'Create Campaign' WHERE name = 'MARKETING_CREATE_CAMPAIGN';
UPDATE permissions SET dashboard = 'Marketing', submenu = 'Marketing Management', feature = 'Edit Campaign' WHERE name = 'MARKETING_EDIT_CAMPAIGN';
UPDATE permissions SET dashboard = 'Marketing', submenu = 'Marketing Management', feature = 'View Analytics' WHERE name = 'MARKETING_VIEW_ANALYTICS';

-- ============================================
-- 34. FINANCE - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Finance', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_FINANCE';

-- ============================================
-- 35. FINANCE - Finance Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Finance', submenu = 'Finance Management', feature = 'View All' WHERE name = 'FINANCE_VIEW_ALL';
UPDATE permissions SET dashboard = 'Finance', submenu = 'Finance Management', feature = 'View Own Invoices' WHERE name = 'FINANCE_VIEW_OWN_INVOICES';
UPDATE permissions SET dashboard = 'Finance', submenu = 'Finance Management', feature = 'View Own Commissions' WHERE name = 'FINANCE_VIEW_OWN_COMMISSIONS';
UPDATE permissions SET dashboard = 'Finance', submenu = 'Finance Management', feature = 'Create Invoice' WHERE name = 'FINANCE_CREATE_INVOICE';
UPDATE permissions SET dashboard = 'Finance', submenu = 'Finance Management', feature = 'Manage Expenses' WHERE name = 'FINANCE_MANAGE_EXPENSES';

-- ============================================
-- 36. HR - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'HR', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_HR';

-- ============================================
-- 37. HR - HR Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'HR', submenu = 'HR Management', feature = 'View All' WHERE name = 'HR_VIEW_ALL';
UPDATE permissions SET dashboard = 'HR', submenu = 'HR Management', feature = 'Manage Employees' WHERE name = 'HR_MANAGE_EMPLOYEES';

-- ============================================
-- 38. ASSETS - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Assets', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_ASSETS';

-- ============================================
-- 39. ASSETS - Asset Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Assets', submenu = 'Asset Management', feature = 'View' WHERE name = 'ASSET_VIEW';
UPDATE permissions SET dashboard = 'Assets', submenu = 'Asset Management', feature = 'Create' WHERE name = 'ASSET_CREATE';
UPDATE permissions SET dashboard = 'Assets', submenu = 'Asset Management', feature = 'Edit' WHERE name = 'ASSET_EDIT';
UPDATE permissions SET dashboard = 'Assets', submenu = 'Asset Management', feature = 'Delete' WHERE name = 'ASSET_DELETE';

-- ============================================
-- 40. REPORTS - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Reports', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_REPORTS';

-- ============================================
-- 41. REPORTS - Report Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Reports', submenu = 'Report Management', feature = 'View All' WHERE name = 'REPORT_VIEW_ALL';
UPDATE permissions SET dashboard = 'Reports', submenu = 'Report Management', feature = 'View College' WHERE name = 'REPORT_VIEW_COLLEGE';
UPDATE permissions SET dashboard = 'Reports', submenu = 'Report Management', feature = 'View Counselor' WHERE name = 'REPORT_VIEW_COUNSELOR';
UPDATE permissions SET dashboard = 'Reports', submenu = 'Report Management', feature = 'View Sub-Agent' WHERE name = 'REPORT_VIEW_SUBAGENT';
UPDATE permissions SET dashboard = 'Reports', submenu = 'Report Management', feature = 'Generate' WHERE name = 'REPORT_GENERATE';
UPDATE permissions SET dashboard = 'Reports', submenu = 'Report Management', feature = 'Export' WHERE name = 'REPORT_EXPORT';
UPDATE permissions SET dashboard = 'Reports', submenu = 'Report Management', feature = 'Schedule' WHERE name = 'REPORT_SCHEDULE';

-- ============================================
-- 42. ROLES & PERMISSIONS - Main Menu Access
-- ============================================
UPDATE permissions SET dashboard = 'Roles & Permissions', submenu = NULL, feature = 'Access Menu' WHERE name = 'MENU_ROLES_PERMISSIONS';

-- ============================================
-- 43. ROLES & PERMISSIONS - Role Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Roles & Permissions', submenu = 'Role Management', feature = 'View' WHERE name = 'USER_VIEW_ROLES';
UPDATE permissions SET dashboard = 'Roles & Permissions', submenu = 'Role Management', feature = 'Manage' WHERE name = 'USER_MANAGE_ROLES';

-- ============================================
-- 44. ROLES & PERMISSIONS - Permission Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Roles & Permissions', submenu = 'Permission Management', feature = 'View' WHERE name = 'USER_VIEW_PERMISSIONS';
UPDATE permissions SET dashboard = 'Roles & Permissions', submenu = 'Permission Management', feature = 'Manage' WHERE name = 'USER_MANAGE_PERMISSIONS';

-- ============================================
-- 45. USERS - User Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'View All' WHERE name = 'USER_VIEW_ALL';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'View Own' WHERE name = 'USER_VIEW_OWN';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'Create' WHERE name = 'USER_CREATE';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'Edit All' WHERE name = 'USER_EDIT_ALL';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'Edit Own' WHERE name = 'USER_EDIT_OWN';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'Delete' WHERE name = 'USER_DELETE';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'Activate/Deactivate' WHERE name = 'USER_ACTIVATE_DEACTIVATE';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'Assign Roles' WHERE name = 'USER_ASSIGN_ROLES';
UPDATE permissions SET dashboard = 'Users', submenu = 'User Management', feature = 'View Activity' WHERE name = 'USER_VIEW_ACTIVITY';

-- ============================================
-- 46. COMMUNICATION - Communication Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Communication', submenu = 'Communication Management', feature = 'Send Email' WHERE name = 'COMMUNICATION_SEND_EMAIL';
UPDATE permissions SET dashboard = 'Communication', submenu = 'Communication Management', feature = 'Send SMS' WHERE name = 'COMMUNICATION_SEND_SMS';
UPDATE permissions SET dashboard = 'Communication', submenu = 'Communication Management', feature = 'Send Bulk Email' WHERE name = 'COMMUNICATION_SEND_BULK_EMAIL';
UPDATE permissions SET dashboard = 'Communication', submenu = 'Communication Management', feature = 'Send Bulk SMS' WHERE name = 'COMMUNICATION_SEND_BULK_SMS';
UPDATE permissions SET dashboard = 'Communication', submenu = 'Communication Management', feature = 'View History' WHERE name = 'COMMUNICATION_VIEW_HISTORY';

-- ============================================
-- 47. DOCUMENTS - Document Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Documents', submenu = 'Document Management', feature = 'View All' WHERE name = 'DOCUMENT_VIEW_ALL';
UPDATE permissions SET dashboard = 'Documents', submenu = 'Document Management', feature = 'View Own' WHERE name = 'DOCUMENT_VIEW_OWN';
UPDATE permissions SET dashboard = 'Documents', submenu = 'Document Management', feature = 'Upload' WHERE name = 'DOCUMENT_UPLOAD';
UPDATE permissions SET dashboard = 'Documents', submenu = 'Document Management', feature = 'Download' WHERE name = 'DOCUMENT_DOWNLOAD';
UPDATE permissions SET dashboard = 'Documents', submenu = 'Document Management', feature = 'Delete' WHERE name = 'DOCUMENT_DELETE';
UPDATE permissions SET dashboard = 'Documents', submenu = 'Document Management', feature = 'Approve/Reject' WHERE name = 'DOCUMENT_APPROVE_REJECT';

-- ============================================
-- 48. SETTINGS - Settings Management Submenu
-- ============================================
UPDATE permissions SET dashboard = 'Settings', submenu = 'Settings Management', feature = 'View All' WHERE name = 'SETTINGS_VIEW_ALL';
UPDATE permissions SET dashboard = 'Settings', submenu = 'Settings Management', feature = 'View Own' WHERE name = 'SETTINGS_VIEW_OWN';
UPDATE permissions SET dashboard = 'Settings', submenu = 'Settings Management', feature = 'Edit System' WHERE name = 'SETTINGS_EDIT_SYSTEM';
UPDATE permissions SET dashboard = 'Settings', submenu = 'Settings Management', feature = 'Edit Own' WHERE name = 'SETTINGS_EDIT_OWN';

-- ============================================
-- VERIFICATION QUERY
-- Run to verify hierarchy population
-- ============================================
-- SELECT dashboard, submenu, feature, name, display_name 
-- FROM permissions 
-- WHERE dashboard IS NOT NULL 
-- ORDER BY dashboard, submenu, feature;

-- SELECT dashboard, COUNT(*) as permission_count
-- FROM permissions
-- WHERE dashboard IS NOT NULL
-- GROUP BY dashboard
-- ORDER BY dashboard;

-- ============================================
-- SUMMARY
-- ============================================
-- Hierarchy Structure:
-- - 18 Dashboards (Leads, Students, Applications, Colleges, etc.)
-- - 48 Submenus (All Colleges, Lead Management, Document Management, etc.)
-- - 180+ Features (View, Create, Edit, Delete, etc.)
--
-- Access Pattern:
-- Dashboard → Submenu → Feature
-- Example: Colleges → All Colleges → View
-- ============================================
