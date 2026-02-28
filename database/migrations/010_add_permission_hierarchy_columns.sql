-- ============================================
-- PERMISSION HIERARCHY ENHANCEMENT
-- Version: 1.0
-- Date: 2026-01-16
-- Purpose: Add hierarchical structure to permissions
-- ============================================

-- Add hierarchy columns to permissions table
ALTER TABLE permissions
ADD COLUMN dashboard VARCHAR(100) COMMENT 'Top-level dashboard (e.g., Colleges, Students, Applications)',
ADD COLUMN submenu VARCHAR(100) NULL COMMENT 'Second-level submenu (e.g., All Colleges, Partner Colleges)',
ADD COLUMN feature VARCHAR(100) COMMENT 'Third-level feature/action (e.g., View, Create, Edit, Delete)';

-- Add indexes for hierarchy queries
CREATE INDEX idx_permissions_dashboard ON permissions(dashboard);
CREATE INDEX idx_permissions_submenu ON permissions(submenu);
CREATE INDEX idx_permissions_feature ON permissions(feature);
CREATE INDEX idx_permissions_hierarchy ON permissions(dashboard, submenu, feature);

-- Add comment to table
ALTER TABLE permissions COMMENT = 'Permissions with hierarchical structure: Dashboard → Submenu → Feature';

-- ============================================
-- VERIFICATION QUERY
-- Run to verify column additions
-- ============================================
-- DESCRIBE permissions;
-- SELECT dashboard, submenu, feature, name 
-- FROM permissions 
-- WHERE dashboard IS NOT NULL 
-- LIMIT 10;

-- ============================================
-- SUMMARY
-- ============================================
-- Added Columns:
-- - dashboard: Top-level navigation item
-- - submenu: Optional second-level navigation
-- - feature: Specific action/capability
--
-- Indexes Created:
-- - idx_permissions_dashboard
-- - idx_permissions_submenu
-- - idx_permissions_feature
-- - idx_permissions_hierarchy (composite)
-- ============================================
