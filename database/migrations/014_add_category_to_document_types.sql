-- ============================================================
-- Migration 014: Add category column to document_types
-- ============================================================

ALTER TABLE document_types
    ADD COLUMN category ENUM('PERSONAL','ACADEMIC','FINANCIAL','IMMIGRATION','OTHER')
        NULL AFTER description;

-- Backfill categories for all seeded document types
UPDATE document_types SET category = 'PERSONAL'    WHERE code IN ('PASSPORT','AADHAAR','PAN','NATIONAL_ID','PHOTO','PERSONAL_STATEMENT');
UPDATE document_types SET category = 'ACADEMIC'    WHERE code IN ('TRANSCRIPT','DEGREE_CERTIFICATE','MARKSHEET_10TH','MARKSHEET_12TH','IELTS','TOEFL','PTE','GRE','GMAT','SAT','SOP','LOR','RESUME');
UPDATE document_types SET category = 'FINANCIAL'   WHERE code IN ('BANK_STATEMENT','SCHOLARSHIP_LETTER','INCOME_CERTIFICATE','AFFIDAVIT_OF_SUPPORT','BLOCKED_ACCOUNT','HEALTH_INSURANCE');
UPDATE document_types SET category = 'IMMIGRATION' WHERE code IN ('I20_FORM','CAS_LETTER','COE','STUDY_PERMIT','APS_CERTIFICATE','TB_TEST','POLICE_CLEARANCE');
UPDATE document_types SET category = 'OTHER'       WHERE code IN ('NOC','WORK_EXPERIENCE');
