-- ============================================================
-- MeritCap - Initial Database Seed
-- ============================================================
-- Run this script on a fresh database after Hibernate creates
-- the tables (ddl-auto: update).
--
-- This inserts the required system roles, permissions, and
-- role-permission mappings so the application can function.
--
-- Usage:
--   mysql -u meritcap -p meritcap < database/seed/001_seed_roles.sql
-- ============================================================

-- ============================================
-- 1. SYSTEM ROLES (required for signup/login)
-- ============================================
INSERT INTO roles (name, display_name, description, is_system_role, is_active, created_at, updated_at)
VALUES
  ('ADMIN',     'Administrator',  'Full system access',                          TRUE, TRUE, NOW(), NOW()),
  ('COUNSELOR', 'Counselor',      'Manages students and applications',           TRUE, TRUE, NOW(), NOW()),
  ('STUDENT',   'Student',        'Student user with self-service access',       TRUE, TRUE, NOW(), NOW()),
  ('COLLEGE',   'College',        'College representative with college portal',  TRUE, TRUE, NOW(), NOW()),
  ('SUB_AGENT', 'Sub-Agent',      'Partner agent who refers students',           TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  description  = VALUES(description),
  is_active    = VALUES(is_active),
  updated_at   = NOW();

-- Verify
SELECT id, name, display_name, is_system_role FROM roles ORDER BY id;
