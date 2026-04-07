-- ============================================================
-- Migration 013: Document Configuration Tables
-- Creates countries, document_types, profile_document_requirements,
-- country_document_requirements tables and adds country_id FK to colleges.
-- Also seeds document config permissions.
-- ============================================================

-- -----------------------------------------------------------
-- 1. COUNTRIES TABLE
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS countries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL COMMENT 'ISO 3166-1 alpha-2 or alpha-3 code',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE KEY uk_countries_name (name),
    UNIQUE KEY uk_countries_code (code),
    INDEX idx_countries_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed countries (matching the hardcoded list in meritcap-admin/lib/sample-data.ts + India)
INSERT IGNORE INTO countries (name, code, is_active) VALUES
('USA', 'US', TRUE),
('UK', 'GB', TRUE),
('Canada', 'CA', TRUE),
('Australia', 'AU', TRUE),
('Germany', 'DE', TRUE),
('France', 'FR', TRUE),
('Netherlands', 'NL', TRUE),
('Singapore', 'SG', TRUE),
('Ireland', 'IE', TRUE),
('New Zealand', 'NZ', TRUE),
('Switzerland', 'CH', TRUE),
('Sweden', 'SE', TRUE),
('Denmark', 'DK', TRUE),
('Norway', 'NO', TRUE),
('Finland', 'FI', TRUE),
('India', 'IN', TRUE);

-- -----------------------------------------------------------
-- 2. ADD country_id FK to colleges (backward-compatible)
-- -----------------------------------------------------------
ALTER TABLE colleges
    ADD COLUMN IF NOT EXISTS country_id BIGINT NULL COMMENT 'FK to countries table' AFTER country;

ALTER TABLE colleges
    ADD CONSTRAINT IF NOT EXISTS fk_colleges_country
    FOREIGN KEY (country_id) REFERENCES countries(id);

-- Backfill country_id from existing country string (case-insensitive match)
UPDATE colleges c
    INNER JOIN countries ct ON LOWER(TRIM(c.country)) = LOWER(TRIM(ct.name))
SET c.country_id = ct.id
WHERE c.country IS NOT NULL AND c.country_id IS NULL AND c.is_deleted = FALSE;

-- -----------------------------------------------------------
-- 3. DOCUMENT_TYPES TABLE (master registry)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS document_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL COMMENT 'Human-readable name, e.g. Passport',
    code VARCHAR(60) NOT NULL COMMENT 'Machine key used in documents.document_type, e.g. PASSPORT',
    description VARCHAR(500),
    allow_multiple BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Can student upload multiple files of this type?',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE KEY uk_document_types_code (code),
    INDEX idx_document_types_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed document types (codes match values used in documents.document_type)
INSERT IGNORE INTO document_types (name, code, description, allow_multiple, is_active) VALUES
-- Identity documents
('Passport', 'PASSPORT', 'Valid passport copy', FALSE, TRUE),
('Aadhaar Card', 'AADHAAR', 'Indian national ID (Aadhaar)', FALSE, TRUE),
('PAN Card', 'PAN', 'Indian Permanent Account Number card', FALSE, TRUE),
('National ID', 'NATIONAL_ID', 'Government-issued national identity document', FALSE, TRUE),

-- Academic documents
('Academic Transcripts', 'TRANSCRIPT', 'Official academic transcripts from all institutions', TRUE, TRUE),
('Degree Certificate', 'DEGREE_CERTIFICATE', 'Graduation degree certificate', TRUE, TRUE),
('10th Marksheet', 'MARKSHEET_10TH', '10th grade/SSC marksheet', FALSE, TRUE),
('12th Marksheet', 'MARKSHEET_12TH', '12th grade/HSC marksheet', FALSE, TRUE),

-- Language & test scores
('IELTS Score Report', 'IELTS', 'Official IELTS test score report', FALSE, TRUE),
('TOEFL Score Report', 'TOEFL', 'Official TOEFL test score report', FALSE, TRUE),
('PTE Score Report', 'PTE', 'Official PTE Academic score report', FALSE, TRUE),
('GRE Score Report', 'GRE', 'Official GRE score report', FALSE, TRUE),
('GMAT Score Report', 'GMAT', 'Official GMAT score report', FALSE, TRUE),
('SAT Score Report', 'SAT', 'Official SAT score report', FALSE, TRUE),

-- Application documents
('Statement of Purpose (SOP)', 'SOP', 'Applicant statement of purpose or personal statement', FALSE, TRUE),
('Letter of Recommendation (LOR)', 'LOR', 'Academic or professional letter of recommendation', TRUE, TRUE),
('Resume / CV', 'RESUME', 'Applicant curriculum vitae or resume', FALSE, TRUE),
('Personal Statement', 'PERSONAL_STATEMENT', 'Personal statement for UK/EU applications', FALSE, TRUE),

-- Financial documents
('Bank Statement', 'BANK_STATEMENT', 'Bank statements showing sufficient funds', TRUE, TRUE),
('Scholarship Letter', 'SCHOLARSHIP_LETTER', 'Proof of scholarship or funding award', FALSE, TRUE),
('Income Certificate', 'INCOME_CERTIFICATE', 'Family/sponsor income certificate', FALSE, TRUE),
('Affidavit of Support', 'AFFIDAVIT_OF_SUPPORT', 'Sponsor affidavit of financial support', FALSE, TRUE),

-- Visa / immigration documents
('I-20 Form', 'I20_FORM', 'Form I-20 for F-1 US student visa', FALSE, TRUE),
('CAS Letter', 'CAS_LETTER', 'Confirmation of Acceptance for Studies (UK)', FALSE, TRUE),
('COE (Confirmation of Enrollment)', 'COE', 'Confirmation of Enrollment for Australia', FALSE, TRUE),
('Study Permit', 'STUDY_PERMIT', 'Canadian study permit', FALSE, TRUE),
('APS Certificate', 'APS_CERTIFICATE', 'Academic Evaluation Certificate for Germany', FALSE, TRUE),
('Blocked Account Proof', 'BLOCKED_ACCOUNT', 'German blocked account / Sperrkonto proof', FALSE, TRUE),
('TB Test Certificate', 'TB_TEST', 'Tuberculosis test certificate (required for UK)', FALSE, TRUE),
('Health Insurance Certificate', 'HEALTH_INSURANCE', 'Health/medical insurance certificate', FALSE, TRUE),

-- Miscellaneous
('NOC / No Objection Certificate', 'NOC', 'No objection certificate from employer or institution', FALSE, TRUE),
('Work Experience Letter', 'WORK_EXPERIENCE', 'Letter confirming employment/internship experience', TRUE, TRUE),
('Police Clearance Certificate', 'POLICE_CLEARANCE', 'Certificate of good conduct / police clearance', FALSE, TRUE),
('Photo', 'PHOTO', 'Passport-size photograph', FALSE, TRUE);

-- -----------------------------------------------------------
-- 4. PROFILE_DOCUMENT_REQUIREMENTS (global, for profile completion)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS profile_document_requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type_id BIGINT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    min_count INT NOT NULL DEFAULT 1 COMMENT 'Minimum number of uploads required',
    display_order INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (document_type_id) REFERENCES document_types(id),
    UNIQUE KEY uk_profile_doc_req_type (document_type_id),
    INDEX idx_profile_doc_req_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed: basic profile documents every student needs
INSERT IGNORE INTO profile_document_requirements (document_type_id, is_required, min_count, display_order)
SELECT id, TRUE, 1, 1 FROM document_types WHERE code = 'PASSPORT';
INSERT IGNORE INTO profile_document_requirements (document_type_id, is_required, min_count, display_order)
SELECT id, TRUE, 1, 2 FROM document_types WHERE code = 'AADHAAR';
INSERT IGNORE INTO profile_document_requirements (document_type_id, is_required, min_count, display_order)
SELECT id, TRUE, 1, 3 FROM document_types WHERE code = 'TRANSCRIPT';
INSERT IGNORE INTO profile_document_requirements (document_type_id, is_required, min_count, display_order)
SELECT id, TRUE, 1, 4 FROM document_types WHERE code = 'PHOTO';

-- -----------------------------------------------------------
-- 5. COUNTRY_DOCUMENT_REQUIREMENTS
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS country_document_requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_id BIGINT NOT NULL,
    document_type_id BIGINT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    min_count INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (country_id) REFERENCES countries(id),
    FOREIGN KEY (document_type_id) REFERENCES document_types(id),
    UNIQUE KEY uk_country_doc_req (country_id, document_type_id),
    INDEX idx_country_doc_req_country (country_id),
    INDEX idx_country_doc_req_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed country requirements matching the previously hardcoded frontend logic
-- USA
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 1 FROM countries c, document_types dt WHERE c.code='US' AND dt.code='PASSPORT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 2 FROM countries c, document_types dt WHERE c.code='US' AND dt.code='TRANSCRIPT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 3 FROM countries c, document_types dt WHERE c.code='US' AND dt.code='SOP';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 3, 4 FROM countries c, document_types dt WHERE c.code='US' AND dt.code='LOR';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 5 FROM countries c, document_types dt WHERE c.code='US' AND dt.code='BANK_STATEMENT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 6 FROM countries c, document_types dt WHERE c.code='US' AND dt.code='I20_FORM';

-- UK
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 1 FROM countries c, document_types dt WHERE c.code='GB' AND dt.code='PASSPORT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 2 FROM countries c, document_types dt WHERE c.code='GB' AND dt.code='TRANSCRIPT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 3 FROM countries c, document_types dt WHERE c.code='GB' AND dt.code='PERSONAL_STATEMENT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 2, 4 FROM countries c, document_types dt WHERE c.code='GB' AND dt.code='LOR';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 5 FROM countries c, document_types dt WHERE c.code='GB' AND dt.code='CAS_LETTER';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 6 FROM countries c, document_types dt WHERE c.code='GB' AND dt.code='TB_TEST';

-- Canada
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 1 FROM countries c, document_types dt WHERE c.code='CA' AND dt.code='PASSPORT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 2 FROM countries c, document_types dt WHERE c.code='CA' AND dt.code='TRANSCRIPT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 3 FROM countries c, document_types dt WHERE c.code='CA' AND dt.code='SOP';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 2, 4 FROM countries c, document_types dt WHERE c.code='CA' AND dt.code='LOR';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 5 FROM countries c, document_types dt WHERE c.code='CA' AND dt.code='BANK_STATEMENT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 6 FROM countries c, document_types dt WHERE c.code='CA' AND dt.code='STUDY_PERMIT';

-- Australia
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 1 FROM countries c, document_types dt WHERE c.code='AU' AND dt.code='PASSPORT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 2 FROM countries c, document_types dt WHERE c.code='AU' AND dt.code='TRANSCRIPT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 3 FROM countries c, document_types dt WHERE c.code='AU' AND dt.code='SOP';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 2, 4 FROM countries c, document_types dt WHERE c.code='AU' AND dt.code='LOR';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 5 FROM countries c, document_types dt WHERE c.code='AU' AND dt.code='COE';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 6 FROM countries c, document_types dt WHERE c.code='AU' AND dt.code='HEALTH_INSURANCE';

-- Germany
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 1 FROM countries c, document_types dt WHERE c.code='DE' AND dt.code='PASSPORT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 2 FROM countries c, document_types dt WHERE c.code='DE' AND dt.code='TRANSCRIPT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 3 FROM countries c, document_types dt WHERE c.code='DE' AND dt.code='SOP';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 4 FROM countries c, document_types dt WHERE c.code='DE' AND dt.code='APS_CERTIFICATE';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 5 FROM countries c, document_types dt WHERE c.code='DE' AND dt.code='BLOCKED_ACCOUNT';

-- India
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 1 FROM countries c, document_types dt WHERE c.code='IN' AND dt.code='AADHAAR';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, TRUE, 1, 2 FROM countries c, document_types dt WHERE c.code='IN' AND dt.code='TRANSCRIPT';
INSERT IGNORE INTO country_document_requirements (country_id, document_type_id, is_required, min_count, display_order)
SELECT c.id, dt.id, FALSE, 1, 3 FROM countries c, document_types dt WHERE c.code='IN' AND dt.code='LOR';

-- -----------------------------------------------------------
-- 6. PERMISSIONS for Document Configuration
-- -----------------------------------------------------------
INSERT IGNORE INTO permissions (name, display_name, description, category, dashboard, submenu, feature, is_active, created_at, updated_at)
VALUES
('MENU_DOCUMENT_CONFIG', 'Document Configuration Menu', 'Access Document Configuration menu in admin panel', 'DOCUMENTS', 'Document Configuration', NULL, 'Menu', TRUE, NOW(), NOW()),
('DOCUMENT_CONFIG_VIEW', 'View Document Configuration', 'View document types and country/profile requirements', 'DOCUMENTS', 'Document Configuration', NULL, 'View', TRUE, NOW(), NOW()),
('DOCUMENT_CONFIG_CREATE', 'Create Document Configuration', 'Create document types and requirements', 'DOCUMENTS', 'Document Configuration', NULL, 'Create', TRUE, NOW(), NOW()),
('DOCUMENT_CONFIG_EDIT', 'Edit Document Configuration', 'Edit document types and requirements', 'DOCUMENTS', 'Document Configuration', NULL, 'Edit', TRUE, NOW(), NOW()),
('DOCUMENT_CONFIG_DELETE', 'Delete Document Configuration', 'Delete document types and requirements', 'DOCUMENTS', 'Document Configuration', NULL, 'Delete', TRUE, NOW(), NOW());

-- Assign all 5 permissions to ADMIN role
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN (
    'MENU_DOCUMENT_CONFIG',
    'DOCUMENT_CONFIG_VIEW',
    'DOCUMENT_CONFIG_CREATE',
    'DOCUMENT_CONFIG_EDIT',
    'DOCUMENT_CONFIG_DELETE'
  );
