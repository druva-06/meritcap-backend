-- Migration 016: Granular per-submenu permissions for Document Config
-- Replaces generic DOCUMENT_CONFIG_VIEW/CREATE/EDIT/DELETE with per-submenu permissions.
-- Each sub-page (Document Types, Profile Requirements, Country Requirements, Countries)
-- gets its own View/Create/Edit/Delete permissions with proper hierarchy metadata.

INSERT IGNORE INTO permissions (name, display_name, description, category, dashboard, submenu, feature, is_active, created_at, updated_at)
VALUES
  -- Document Types submenu
  ('DOCUMENT_TYPE_VIEW',   'View Document Types',   'View document types list and details',  'DOCUMENTS', 'Document Config', 'Document Types',         'View',   TRUE, NOW(), NOW()),
  ('DOCUMENT_TYPE_CREATE', 'Create Document Types', 'Create new document types',             'DOCUMENTS', 'Document Config', 'Document Types',         'Create', TRUE, NOW(), NOW()),
  ('DOCUMENT_TYPE_EDIT',   'Edit Document Types',   'Edit existing document types',          'DOCUMENTS', 'Document Config', 'Document Types',         'Edit',   TRUE, NOW(), NOW()),
  ('DOCUMENT_TYPE_DELETE', 'Delete Document Types', 'Delete document types',                 'DOCUMENTS', 'Document Config', 'Document Types',         'Delete', TRUE, NOW(), NOW()),

  -- Profile Requirements submenu
  ('PROFILE_REQ_VIEW',   'View Profile Requirements',   'View profile requirement rules',    'DOCUMENTS', 'Document Config', 'Profile Requirements', 'View',   TRUE, NOW(), NOW()),
  ('PROFILE_REQ_CREATE', 'Create Profile Requirements', 'Create new profile requirements',   'DOCUMENTS', 'Document Config', 'Profile Requirements', 'Create', TRUE, NOW(), NOW()),
  ('PROFILE_REQ_EDIT',   'Edit Profile Requirements',   'Edit existing profile requirements','DOCUMENTS', 'Document Config', 'Profile Requirements', 'Edit',   TRUE, NOW(), NOW()),
  ('PROFILE_REQ_DELETE', 'Delete Profile Requirements', 'Delete profile requirements',       'DOCUMENTS', 'Document Config', 'Profile Requirements', 'Delete', TRUE, NOW(), NOW()),

  -- Country Requirements submenu
  ('COUNTRY_REQ_VIEW',   'View Country Requirements',   'View country document requirements',  'DOCUMENTS', 'Document Config', 'Country Requirements', 'View',   TRUE, NOW(), NOW()),
  ('COUNTRY_REQ_CREATE', 'Create Country Requirements', 'Create country requirements',         'DOCUMENTS', 'Document Config', 'Country Requirements', 'Create', TRUE, NOW(), NOW()),
  ('COUNTRY_REQ_EDIT',   'Edit Country Requirements',   'Edit existing country requirements',  'DOCUMENTS', 'Document Config', 'Country Requirements', 'Edit',   TRUE, NOW(), NOW()),
  ('COUNTRY_REQ_DELETE', 'Delete Country Requirements', 'Delete country requirements',         'DOCUMENTS', 'Document Config', 'Country Requirements', 'Delete', TRUE, NOW(), NOW()),

  -- Countries submenu
  ('COUNTRY_CONFIG_VIEW',   'View Countries',   'View countries list',    'DOCUMENTS', 'Document Config', 'Countries', 'View',   TRUE, NOW(), NOW()),
  ('COUNTRY_CONFIG_CREATE', 'Create Countries', 'Add new countries',       'DOCUMENTS', 'Document Config', 'Countries', 'Create', TRUE, NOW(), NOW()),
  ('COUNTRY_CONFIG_EDIT',   'Edit Countries',   'Edit existing countries', 'DOCUMENTS', 'Document Config', 'Countries', 'Edit',   TRUE, NOW(), NOW()),
  ('COUNTRY_CONFIG_DELETE', 'Delete Countries', 'Delete countries',        'DOCUMENTS', 'Document Config', 'Countries', 'Delete', TRUE, NOW(), NOW());

-- Assign all VIEW permissions to ADMIN role
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN (
    'DOCUMENT_TYPE_VIEW', 'DOCUMENT_TYPE_CREATE', 'DOCUMENT_TYPE_EDIT', 'DOCUMENT_TYPE_DELETE',
    'PROFILE_REQ_VIEW', 'PROFILE_REQ_CREATE', 'PROFILE_REQ_EDIT', 'PROFILE_REQ_DELETE',
    'COUNTRY_REQ_VIEW', 'COUNTRY_REQ_CREATE', 'COUNTRY_REQ_EDIT', 'COUNTRY_REQ_DELETE',
    'COUNTRY_CONFIG_VIEW', 'COUNTRY_CONFIG_CREATE', 'COUNTRY_CONFIG_EDIT', 'COUNTRY_CONFIG_DELETE'
  );
