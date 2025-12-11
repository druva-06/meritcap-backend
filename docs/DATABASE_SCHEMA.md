# Database Schema Documentation

## Overview

The CAP Backend uses MySQL 8.0 as the primary database. The schema is designed to support educational consultancy operations including user management, student profiles, lead tracking, and application processing.

## Entity Relationship Diagram

```
┌─────────────┐
│    Users    │
└──────┬──────┘
       │ 1
       │
       │ 1
┌──────┴──────┐     1        ┌──────────────┐
│  Students   │──────────────│  Addresses   │
└──────┬──────┘              └──────────────┘
       │ 1                   (Many per Student)
       │
       ├───────────┬─────────────┬──────────────┬─────────────┐
       │           │             │              │             │
       │ N         │ N           │ N            │ N           │ N
┌──────┴──────┐ ┌─┴─────────┐ ┌─┴──────────┐ ┌─┴────────┐ ┌─┴─────────┐
│  Education  │ │  Abroad   │ │Certifications│ │ Projects │ │  Finance  │
└─────────────┘ │   Exams   │ └──────────────┘ └──────────┘ └───────────┘
                └───────────┘

┌─────────────┐     N           1   ┌──────────────┐
│    Leads    │─────────────────────│    Users     │
└─────────────┘  (assigned_to)      └──────────────┘
                                     (created_by)

┌─────────────┐     N           1   ┌──────────────┐
│  Colleges   │─────────────────────│   Courses    │
└─────────────┘                     └──────────────┘

┌─────────────┐
│Student Reg. │───────┐
└─────────────┘       │
                      │ N to N
                      │
              ┌───────┴────────┐
              │ College/Course │
              └────────────────┘
```

## Core Tables

### 1. users

**Description**: Central user table for all system users (Admin, Counselor, Student, College)

```sql
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_locked` bit(1) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `failed_login_attempts` int DEFAULT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','COUNSELOR','STUDENT','COLLEGE') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_email` (`email`),
  UNIQUE KEY `UK_phone_number` (`phone_number`),
  UNIQUE KEY `UK_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

**Indexes**:

- Primary key on `id`
- Unique on `email`, `phone_number`, `username`

**Relationships**:

- One-to-One with `students`
- One-to-Many with `leads` (as creator)
- One-to-Many with `leads` (as assignee)

### 2. students

**Description**: Student profile information (shares ID with users table via @MapsId)

```sql
CREATE TABLE `students` (
  `id` bigint NOT NULL,
  `alternate_phone_number` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `graduation_level` enum('DIPLOMA','HIGH_SCHOOL','INTERMEDIATE','OTHER','PHD','POSTGRADUATE','UNDERGRADUATE') DEFAULT NULL,
  `profile_status` enum('ACTIVE','INACTIVE','SUSPENDED') DEFAULT NULL,
  `profile_completion` int NOT NULL DEFAULT '0',
  `seo_id` bigint DEFAULT NULL,
  `birth_certificate_file` varchar(255) DEFAULT NULL,
  `aadhaar_card_file` varchar(255) DEFAULT NULL,
  `pan_card_file` varchar(255) DEFAULT NULL,
  `passport_file` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_seo_id` (`seo_id`),
  CONSTRAINT `FK_student_user` FOREIGN KEY (`id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_student_seo` FOREIGN KEY (`seo_id`) REFERENCES `seo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

**Key Points**:

- `id` is the primary key AND foreign key to `users.id`
- Uses `@MapsId` in JPA for shared primary key
- Profile completion tracked as percentage

**Relationships**:

- One-to-One with `users`
- One-to-Many with `addresses`, `student_educations`, `abroad_exams`, etc.

### 3. leads

**Description**: Lead generation and tracking system

```sql
CREATE TABLE `leads` (
  `id` bigint NOT NULL AUTO_INCREMENT,

  -- Personal Information (Searchable)
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `country` varchar(100) DEFAULT NULL,

  -- Lead Management
  `status` enum('HOT','IMMEDIATE_HOT','WARM','COLD','FEATURE_LEAD','CONTACTED') NOT NULL DEFAULT 'WARM',
  `score` int DEFAULT 0,
  `lead_source` varchar(100) DEFAULT NULL,

  -- Preferences
  `preferred_countries` text DEFAULT NULL,
  `preferred_courses` text DEFAULT NULL,
  `budget_range` varchar(100) DEFAULT NULL,
  `intake` varchar(50) DEFAULT NULL,
  `tags` text DEFAULT NULL,

  -- Assignment
  `assigned_to` bigint DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,

  -- Encrypted Data
  `encrypted_personal_details` text DEFAULT NULL,
  `encrypted_academic_details` text DEFAULT NULL,
  `encrypted_preferences` text DEFAULT NULL,

  -- Duplicate Tracking
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
  KEY `idx_created_at` (`created_at`),
  KEY `idx_lead_score` (`score`),
  KEY `idx_lead_status_score` (`status`, `score`),
  FULLTEXT KEY `idx_lead_search` (`first_name`, `last_name`, `email`, `tags`),

  CONSTRAINT `FK_lead_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK_lead_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `FK_lead_duplicate_of` FOREIGN KEY (`duplicate_of`) REFERENCES `leads` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

**Special Features**:

- Full-text search on name and tags
- Composite index on status and score
- Encrypted fields for sensitive data
- Duplicate tracking system

### 4. addresses

**Description**: Physical addresses for students

```sql
CREATE TABLE `addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `pincode` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `address_type` enum('PERMANENT','TEMPORARY') DEFAULT NULL,
  `student_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_address_student` (`student_id`),
  CONSTRAINT `FK_address_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 5. colleges

**Description**: Educational institutions

```sql
CREATE TABLE `colleges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `country` varchar(255) NOT NULL,
  `state` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `college_type` varchar(100) DEFAULT NULL,
  `ranking` int DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_country` (`country`),
  KEY `idx_ranking` (`ranking`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 6. courses

**Description**: Academic programs offered by colleges

```sql
CREATE TABLE `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `college_id` bigint NOT NULL,
  `degree_level` enum('DIPLOMA','UNDERGRADUATE','POSTGRADUATE','PHD') NOT NULL,
  `duration_years` decimal(3,1) DEFAULT NULL,
  `field_of_study` varchar(255) DEFAULT NULL,
  `tuition_fee` decimal(12,2) DEFAULT NULL,
  `currency` varchar(10) DEFAULT NULL,
  `intake_months` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_course_college` (`college_id`),
  KEY `idx_degree_level` (`degree_level`),
  KEY `idx_field_of_study` (`field_of_study`),
  CONSTRAINT `FK_course_college` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

## Supporting Tables

### student_educations

Student's educational background

```sql
CREATE TABLE `student_educations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `institution_name` varchar(255) NOT NULL,
  `degree` varchar(255) NOT NULL,
  `field_of_study` varchar(255) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `grade_percentage` decimal(5,2) DEFAULT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_education_student` (`student_id`),
  CONSTRAINT `FK_education_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### abroad_exams

Standardized test scores (IELTS, TOEFL, GRE, GMAT)

```sql
CREATE TABLE `abroad_exams` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `exam_name` enum('IELTS','TOEFL','GRE','GMAT','SAT','ACT','PTE') NOT NULL,
  `score` varchar(50) NOT NULL,
  `exam_date` date DEFAULT NULL,
  `validity_date` date DEFAULT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_exam_student` (`student_id`),
  CONSTRAINT `FK_exam_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### certifications

Professional certifications

```sql
CREATE TABLE `certifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `certification_name` varchar(255) NOT NULL,
  `issuing_organization` varchar(255) NOT NULL,
  `issue_date` date DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `credential_id` varchar(255) DEFAULT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_certification_student` (`student_id`),
  CONSTRAINT `FK_certification_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### projects

Student projects and work experience

```sql
CREATE TABLE `projects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `technologies_used` text DEFAULT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_project_student` (`student_id`),
  CONSTRAINT `FK_project_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### finance

Financial information for students

```sql
CREATE TABLE `finance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `annual_income` decimal(15,2) DEFAULT NULL,
  `currency` varchar(10) DEFAULT NULL,
  `financial_aid_required` bit(1) DEFAULT b'0',
  `loan_required` bit(1) DEFAULT b'0',
  `sponsorship_details` text DEFAULT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_finance_student` (`student_id`),
  CONSTRAINT `FK_finance_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### wishlist

Student's wishlist of colleges/courses

```sql
CREATE TABLE `wishlist` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_wishlist_student` (`student_id`),
  CONSTRAINT `FK_wishlist_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### student_college_course_registration

Student applications to colleges/courses

```sql
CREATE TABLE `student_college_course_registrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `college_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `application_status` enum('DRAFT','SUBMITTED','UNDER_REVIEW','ACCEPTED','REJECTED','WAITLISTED') NOT NULL,
  `application_date` datetime(6) DEFAULT NULL,
  `decision_date` datetime(6) DEFAULT NULL,
  `intake` varchar(50) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_registration_student` (`student_id`),
  KEY `FK_registration_college` (`college_id`),
  KEY `FK_registration_course` (`course_id`),
  CONSTRAINT `FK_registration_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FK_registration_college` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`),
  CONSTRAINT `FK_registration_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Indexing Strategy

### Primary Indexes

- All tables have primary key indexes on `id`
- Automatically created by `AUTO_INCREMENT`

### Unique Indexes

- User email, phone, username
- Student-User relationship
- One-to-one relationships (finance, wishlist)

### Foreign Key Indexes

- Automatically created on all foreign key columns
- Improves join performance

### Search Indexes

- Full-text search on lead names and tags
- Composite indexes on frequently queried combinations
- Date indexes for time-based queries

### Performance Indexes

```sql
-- Leads
CREATE INDEX idx_lead_score ON leads(score);
CREATE INDEX idx_lead_status_score ON leads(status, score);
CREATE FULLTEXT INDEX idx_lead_search ON leads(first_name, last_name, email, tags);

-- Colleges
CREATE INDEX idx_college_country ON colleges(country);
CREATE INDEX idx_college_ranking ON colleges(ranking);

-- Courses
CREATE INDEX idx_course_degree_level ON courses(degree_level);
CREATE INDEX idx_course_field ON courses(field_of_study);
```

## Data Types and Constraints

### Standard Fields

- `id`: `bigint AUTO_INCREMENT` - Primary key
- `created_at`: `datetime(6)` - Creation timestamp with microseconds
- `updated_at`: `datetime(6)` - Update timestamp with microseconds
- Email: `varchar(255)` with UNIQUE constraint
- Phone: `varchar(255)` with UNIQUE constraint

### Enums

- Role: ADMIN, COUNSELOR, STUDENT, COLLEGE
- Gender: MALE, FEMALE, OTHER
- Status: ACTIVE, INACTIVE, SUSPENDED
- Lead Status: HOT, IMMEDIATE_HOT, WARM, COLD, FEATURE_LEAD, CONTACTED

### Decimal Types

- Money: `decimal(12,2)` - Up to 999,999,999.99
- Percentage: `decimal(5,2)` - 0.00 to 999.99
- Duration: `decimal(3,1)` - 0.0 to 99.9 years

## Referential Integrity

### CASCADE Options

**ON DELETE CASCADE**:

- `students.id` → `users.id` (Delete student when user deleted)
- Child records in student relationships

**ON DELETE SET NULL**:

- `leads.assigned_to` → `users.id` (Keep lead, remove assignment)
- `leads.created_by` → `users.id` (Keep lead, remove creator reference)

**ON DELETE RESTRICT** (Default):

- Cannot delete parent if children exist
- Used for core relationships

## Query Optimization Tips

### Use Indexes

```sql
-- Good - Uses index
SELECT * FROM leads WHERE email = 'test@example.com';

-- Bad - Full table scan
SELECT * FROM leads WHERE LOWER(email) = 'test@example.com';
```

### Pagination

```sql
-- Use LIMIT and OFFSET
SELECT * FROM leads
WHERE status = 'HOT'
ORDER BY score DESC
LIMIT 20 OFFSET 0;
```

### Join Optimization

```sql
-- Fetch only needed columns
SELECT l.id, l.first_name, u.email
FROM leads l
INNER JOIN users u ON l.assigned_to = u.id
WHERE l.status = 'HOT';
```

## Backup and Recovery

### Backup Strategy

- Daily full backups
- Hourly incremental backups
- Transaction log backups every 15 minutes
- Retention: 30 days

### Backup Commands

```bash
# Full backup
mysqldump -u root -p cap_db > backup_$(date +%Y%m%d).sql

# Specific tables
mysqldump -u root -p cap_db users students leads > critical_backup.sql

# With compression
mysqldump -u root -p cap_db | gzip > backup_$(date +%Y%m%d).sql.gz
```

### Restore Commands

```bash
# Restore full backup
mysql -u root -p cap_db < backup_20251207.sql

# Restore compressed
gunzip < backup_20251207.sql.gz | mysql -u root -p cap_db
```

## Database Maintenance

### Regular Tasks

1. **Analyze Tables**

```sql
ANALYZE TABLE users, students, leads, colleges, courses;
```

2. **Optimize Tables**

```sql
OPTIMIZE TABLE leads;
```

3. **Check for Fragmentation**

```sql
SELECT table_name, data_free, data_length
FROM information_schema.tables
WHERE table_schema = 'cap_db' AND data_free > 0;
```

4. **Monitor Slow Queries**

```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

## Data Migration Scripts

See `database/migrations/` directory for all migration scripts.

### Migration Naming Convention

```
{number}_{description}.sql
```

### Example Migration

```sql
-- 002_add_lead_tags.sql
ALTER TABLE leads ADD COLUMN tags TEXT DEFAULT NULL;
CREATE FULLTEXT INDEX idx_lead_tags ON leads(tags);
```

## Performance Monitoring

### Key Metrics to Monitor

- Query response time
- Index usage
- Table locks
- Deadlocks
- Connection pool utilization
- Slow query count

### Monitoring Queries

```sql
-- Show processlist
SHOW FULL PROCESSLIST;

-- Table sizes
SELECT
  table_name,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'cap_db'
ORDER BY size_mb DESC;

-- Index usage
SELECT * FROM sys.schema_unused_indexes WHERE object_schema = 'cap_db';
```

## Security Considerations

### Access Control

- Principle of least privilege
- Separate credentials for different environments
- Application user has limited permissions
- Admin user for maintenance only

### Data Protection

- Sensitive data encrypted at application level
- SSL/TLS for database connections
- No passwords or secrets in schema
- Regular security audits

### Audit Logging

- Track all DDL changes
- Monitor failed login attempts
- Log data modifications
- Regular audit review

## Troubleshooting

### Common Issues

**Deadlocks**

```sql
-- Show InnoDB status
SHOW ENGINE INNODB STATUS;
```

**Connection Issues**

```sql
-- Check max connections
SHOW VARIABLES LIKE 'max_connections';

-- Current connections
SHOW STATUS LIKE 'Threads_connected';
```

**Performance Issues**

```sql
-- Identify slow queries
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;

-- Check table locks
SHOW OPEN TABLES WHERE In_use > 0;
```

## Future Enhancements

### Planned Changes

1. Partitioning for leads table (by created_at)
2. Read replicas for reporting
3. Archival strategy for old data
4. Enhanced audit logging
5. Time-series data for analytics

## References

- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/en/)
- [InnoDB Storage Engine](https://dev.mysql.com/doc/refman/8.0/en/innodb-storage-engine.html)
- [MySQL Performance Tuning](https://dev.mysql.com/doc/refman/8.0/en/optimization.html)
