# Lead Management System - Data Model Design

## Overview

The Lead model is designed to store lead generation information with a hybrid approach:

- **Searchable/filterable fields** are stored as plain text
- **Sensitive/detailed information** is encrypted by frontend and stored as encrypted strings

## Database Schema

### Table: `leads`

#### Searchable Fields (Plain Text)

These fields are used for filtering, searching, and display in the leads list:

| Field                 | Type         | Purpose                                                              |
| --------------------- | ------------ | -------------------------------------------------------------------- |
| `first_name`          | VARCHAR(255) | Lead's first name - searchable                                       |
| `last_name`           | VARCHAR(255) | Lead's last name - searchable                                        |
| `email`               | VARCHAR(255) | Email - searchable, indexed                                          |
| `phone_number`        | VARCHAR(20)  | Phone - searchable, indexed                                          |
| `country`             | VARCHAR(100) | Country - filterable                                                 |
| `status`              | ENUM         | HOT, IMMEDIATE_HOT, WARM, COLD, FEATURE_LEAD, CONTACTED - filterable |
| `score`               | INT          | Lead score 0-100 - sortable                                          |
| `lead_source`         | VARCHAR(100) | Source of lead - filterable                                          |
| `preferred_countries` | TEXT         | Comma-separated countries - filterable                               |
| `preferred_courses`   | TEXT         | Comma-separated courses - filterable                                 |
| `budget_range`        | VARCHAR(100) | Budget - filterable                                                  |
| `intake`              | VARCHAR(50)  | Intake period - filterable                                           |
| `tags`                | TEXT         | Tags for categorization - searchable                                 |

#### Management Fields

| Field          | Type   | Purpose                         |
| -------------- | ------ | ------------------------------- |
| `assigned_to`  | BIGINT | FK to users (counselor)         |
| `created_by`   | BIGINT | FK to users (admin who created) |
| `is_duplicate` | BIT    | Flag for duplicate detection    |
| `duplicate_of` | BIGINT | Reference to original lead      |

#### Encrypted Fields (Frontend Encrypted)

These fields contain JSON data encrypted by the frontend:

| Field                        | Type | Content                        |
| ---------------------------- | ---- | ------------------------------ |
| `encrypted_personal_details` | TEXT | Personal info JSON (see below) |
| `encrypted_academic_details` | TEXT | Academic info JSON (see below) |
| `encrypted_preferences`      | TEXT | Preferences JSON (see below)   |

#### Timestamps

| Field        | Type        | Purpose               |
| ------------ | ----------- | --------------------- |
| `created_at` | DATETIME(6) | Creation timestamp    |
| `updated_at` | DATETIME(6) | Last update timestamp |

## Encrypted Data Structure

### 1. Personal Details (Encrypted)

```json
{
  "alternatePhoneNumber": "+91 98765 43211",
  "dateOfBirth": "15/05/1998",
  "gender": "MALE",
  "fullAddress": "123 Main Street, Apartment 4B",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001"
}
```

### 2. Academic Details (Encrypted)

```json
{
  "currentEducationLevel": "UNDERGRADUATE",
  "degreeCourse": "B.Tech in Computer Science",
  "universityCollege": "IIT Bombay",
  "percentageCGPA": "8.5 CGPA",
  "yearOfPassing": "2020",
  "workExperience": "3 years at TCS",
  "ieltsScore": "7.5",
  "toeflScore": "100",
  "greScore": "320",
  "gmatScore": "700"
}
```

### 3. Preferences (Encrypted)

```json
{
  "preferredCollege": "Harvard University, MIT, Stanford",
  "additionalNotes": "Looking for scholarship opportunities"
}
```

## Frontend Encryption Flow

1. **Frontend collects form data** in 3 steps
2. **For sensitive fields**, create JSON objects matching the DTOs
3. **Encrypt each JSON** using AES-256 or similar algorithm
4. **Send encrypted strings** to backend in the request
5. **Backend stores encrypted strings** as-is (no decryption)
6. **When displaying details**, backend returns encrypted strings
7. **Frontend decrypts** when needed for full detail view

## Data Flow

### Creating a Lead

```
Frontend Form → Separate sensitive data → Encrypt → Send to API
                ↓                                      ↓
         Plain searchable fields              Encrypted strings
                ↓                                      ↓
         Store as-is in DB columns        Store in encrypted_* columns
```

### Searching/Filtering Leads

```
Search Query → Filter on plain text columns → Return list
               (email, name, status, country, tags, etc.)
```

### Viewing Lead Details

```
Lead ID → Fetch from DB → Return with encrypted strings
                          ↓
                    Frontend decrypts → Display full details
```

## Indexes for Performance

```sql
-- Search indexes
CREATE INDEX idx_email ON leads(email);
CREATE INDEX idx_phone ON leads(phone_number);
CREATE INDEX idx_status ON leads(status);
CREATE INDEX idx_country ON leads(country);
CREATE INDEX idx_created_at ON leads(created_at);

-- Composite indexes
CREATE INDEX idx_lead_status_score ON leads(status, score);

-- Full-text search
CREATE FULLTEXT INDEX idx_lead_search ON leads(first_name, last_name, email, tags);
```

## Security Benefits

1. **Selective Encryption**: Only encrypt what's not needed for filtering
2. **Frontend Encryption**: Backend never sees sensitive data in plain text
3. **Performance**: Searchable fields remain fast for queries
4. **Privacy**: Sensitive info (DOB, addresses, test scores) protected
5. **Compliance**: Easier GDPR/data protection compliance

## Lead Workflow

```
New Lead → WARM (default)
   ↓
First Contact → CONTACTED
   ↓
Counselor evaluates interest level
   ↓
   ├─→ IMMEDIATE_HOT (extremely urgent, needs immediate attention)
   ├─→ HOT (high priority, very interested)
   ├─→ WARM (moderate interest, needs nurturing)
   ├─→ COLD (low interest or not responsive)
   └─→ FEATURE_LEAD (premium/featured lead for special handling)
```

## Duplicate Detection Strategy

1. Check for duplicates by: email OR phone number
2. If duplicate found:
   - Set `is_duplicate = true`
   - Set `duplicate_of = original_lead_id`
   - Show warning to admin
3. Admin can:
   - Merge duplicates
   - Mark as not duplicate
   - Delete duplicate

## Next Steps

1. ✅ **Model Created** - Lead entity with encrypted fields
2. ⏳ **Repository** - Create LeadRepository
3. ⏳ **Service Layer** - Business logic for lead management
4. ⏳ **Controller** - REST APIs for CRUD operations
5. ⏳ **Duplicate Detection** - Service to find and merge duplicates
6. ⏳ **Search/Filter** - Advanced search with multiple criteria
7. ⏳ **Assignment Logic** - Auto-assign or manual assignment to counselors
8. ⏳ **Score Calculation** - Algorithm to calculate lead score
