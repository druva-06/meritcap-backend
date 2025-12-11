-- Lead Management Table
-- Migration: Create leads table
-- Date: 2025-12-07

CREATE TABLE `leads` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  -- Personal Information (Searchable/Filterable)
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `country` varchar(100) DEFAULT NULL,
  
  -- Lead Management Fields
  `status` enum('HOT','IMMEDIATE_HOT','WARM','COLD','FEATURE_LEAD','CONTACTED') NOT NULL DEFAULT 'WARM',
  `score` int DEFAULT 0,
  `lead_source` varchar(100) DEFAULT NULL,
  `campaign` varchar(100) DEFAULT NULL,
  
  -- Preferences (for filtering/searching)
  `preferred_countries` text DEFAULT NULL,
  `preferred_courses` text DEFAULT NULL,
  `budget_range` varchar(100) DEFAULT NULL,
  `intake` varchar(50) DEFAULT NULL,
  `tags` text DEFAULT NULL,
  
  -- Assignment
  `assigned_to` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  
  -- Encrypted sensitive data (frontend encrypts before sending)
  `encrypted_personal_details` text DEFAULT NULL,
  `encrypted_academic_details` text DEFAULT NULL,
  `encrypted_preferences` text DEFAULT NULL,
  
  -- Duplicate tracking
  `is_duplicate` bit(1) DEFAULT b'0',
  `duplicate_of` bigint DEFAULT NULL,
  
  -- Timestamps
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  
  PRIMARY KEY (`id`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone_number`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_country` (`country`),
  KEY `idx_campaign` (`campaign`),
  KEY `idx_created_at` (`created_at`),
  
  CONSTRAINT `FK_lead_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK_lead_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK_lead_duplicate_of` FOREIGN KEY (`duplicate_of`) REFERENCES `leads` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create indexes for better search performance
CREATE INDEX idx_lead_score ON leads(score);
CREATE INDEX idx_lead_status_score ON leads(status, score);
CREATE FULLTEXT INDEX idx_lead_search ON leads(first_name, last_name, email, tags);
