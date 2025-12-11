# Lead API Documentation

## Table of Contents
1. [Create Lead API](#create-lead-api)
2. [Get Leads API (with Filters & Pagination)](#get-leads-api)

---

## Create Lead API

### Endpoint
```
POST /leads/add
```

### Description

Creates a new lead in the system with encrypted sensitive information. The API performs duplicate detection based on email and phone number.

### Authentication

Requires authenticated user (Admin/Counselor role)

### Request Headers

```
Content-Type: application/json
Authorization: Bearer <token>
```

### Request Body

```json
{
  "first_name": "Priya",
  "last_name": "Sharma",
  "email": "priya.sharma@email.com",
  "phone_number": "+91 98765 43210",
  "country": "India",
  "status": "HOT",
  "score": 95,
  "lead_source": "Website",
  "campaign": "Summer 2026 MBA Campaign",
  "preferred_countries": "USA,Canada,UK",
  "preferred_courses": "MBA,Computer Science",
  "budget_range": "20-30 Lakhs",
  "intake": "Fall 2026",
  "tags": ["MBA", "USA"],
  "assigned_to": 5,
  "encrypted_personal_details": "U2FsdGVkX1+vupppZksvRf5pq5g5XjFRlipRkwB0K1Y=...",
  "encrypted_academic_details": "U2FsdGVkX1+vupppZksvRf5pq5g5XjFRlipRkwB0K1Y=...",
  "encrypted_preferences": "U2FsdGVkX1+vupppZksvRf5pq5g5XjFRlipRkwB0K1Y=..."
}
```

### Required Fields

- `first_name` (string, not blank)
- `last_name` (string, not blank)
- `email` (string, valid email format)
- `phone_number` (string, valid phone format with country code)

### Optional Fields

- `country` (string)
- `status` (enum: HOT, IMMEDIATE_HOT, WARM, COLD, FEATURE_LEAD, CONTACTED - defaults to WARM)
- `score` (integer: 0-100, defaults to 0)
- `lead_source` (string)
- `campaign` (string: campaign name for tracking and filtering)
- `preferred_countries` (string: comma-separated)
- `preferred_courses` (string: comma-separated)
- `budget_range` (string)
- `intake` (string)
- `tags` (array of strings)
- `assigned_to` (long: user ID of counselor)
- `encrypted_personal_details` (string: encrypted JSON)
- `encrypted_academic_details` (string: encrypted JSON)
- `encrypted_preferences` (string: encrypted JSON)

### Success Response (201 Created)

```json
{
  "data": {
    "id": 1,
    "first_name": "Priya",
    "last_name": "Sharma",
    "email": "priya.sharma@email.com",
    "phone_number": "+91 98765 43210",
    "country": "India",
    "status": "HOT",
    "score": 95,
    "lead_source": "Website",
    "preferred_countries": "USA,Canada,UK",
    "preferred_courses": "MBA,Computer Science",
    "budget_range": "20-30 Lakhs",
    "intake": "Fall 2026",
    "tags": ["MBA", "USA"],
    "assigned_to_id": 5,
    "assigned_to_name": "John Counselor",
    "created_by_id": 2,
    "created_by_name": "Admin User",
    "encrypted_personal_details": "U2FsdGVkX1+vupppZksvRf5pq5g5XjFRlipRkwB0K1Y=...",
    "encrypted_academic_details": "U2FsdGVkX1+vupppZksvRf5pq5g5XjFRlipRkwB0K1Y=...",
    "encrypted_preferences": "U2FsdGVkX1+vupppZksvRf5pq5g5XjFRlipRkwB0K1Y=...",
    "is_duplicate": false,
    "duplicate_of": null,
    "created_at": "2025-12-06T10:30:00.000Z",
    "updated_at": "2025-12-06T10:30:00.000Z"
  },
  "message": "Lead created successfully",
  "statusCode": 201
}
```

### Error Responses

#### 400 Bad Request - Validation Error

```json
{
  "errors": {
    "first_name": "First name is required",
    "email": "Email should be valid"
  },
  "message": "Validation failed",
  "statusCode": 400
}
```

#### 400 Bad Request - Duplicate Lead

```json
{
  "errors": [],
  "message": "Duplicate lead found. Lead already exists with ID: 123",
  "statusCode": 400
}
```

#### 404 Not Found - User Not Found

```json
{
  "errors": [],
  "message": "Assigned counselor not found",
  "statusCode": 404
}
```

#### 500 Internal Server Error

```json
{
  "errors": [],
  "message": "Internal server error",
  "statusCode": 500
}
```

## Encrypted Data Format

### Personal Details Structure (before encryption)

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

### Academic Details Structure (before encryption)

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

### Preferences Structure (before encryption)

```json
{
  "preferredCollege": "Harvard University, MIT, Stanford",
  "additionalNotes": "Looking for scholarship opportunities"
}
```

## Frontend Integration Notes

1. **Encryption**: Frontend should encrypt the sensitive data structures using AES-256 or similar before sending
2. **Tags**: Send as array, will be stored as comma-separated string
3. **Duplicate Detection**: API checks email and phone number for duplicates
4. **Assignment**: `assigned_to` should be the user ID of a counselor
5. **Authentication**: User creating the lead is automatically captured from authentication context

## Business Logic

1. **Duplicate Detection**:

   - Checks if email OR phone number already exists
   - Throws error if duplicate found
   - Returns the ID of existing lead in error message

2. **Default Values**:

   - Status defaults to "WARM" if not provided
   - Score defaults to 0 if not provided
   - isDuplicate is set to false by default

3. **Timestamps**:
   - `created_at` and `updated_at` are automatically set
   - `updated_at` is automatically updated on entity updates

## Testing with cURL

```bash
curl -X POST http://localhost:8080/api/leads \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "first_name": "Priya",
    "last_name": "Sharma",
    "email": "priya.sharma@email.com",
    "phone_number": "+919876543210",
    "country": "India",
    "status": "HOT",
    "score": 95,
    "lead_source": "Website",
    "preferred_countries": "USA,Canada",
    "preferred_courses": "MBA",
    "budget_range": "20-30 Lakhs",
    "intake": "Fall 2026",
    "tags": ["MBA", "USA"],
    "assigned_to": 5
  }'
```

---

## Get Leads API

### Endpoint
```
GET /leads
```

### Description
Retrieves a paginated list of leads with advanced filtering capabilities. Supports search, campaign filtering, date range, score range, status filtering, and tag-based filtering.

### Authentication
Requires authenticated user (Admin or Counselor role)

### Request Headers
```
Content-Type: application/json
Authorization: Bearer <token>
```

### Query Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `search` | string | No | Search by name, email, or phone | `Priya` |
| `campaign` | string | No | Filter by campaign name | `Summer 2026 MBA Campaign` |
| `date_from` | string | No | Filter from date (yyyy-MM-dd) | `2025-01-01` |
| `date_to` | string | No | Filter to date (yyyy-MM-dd) | `2025-12-31` |
| `score_from` | integer | No | Minimum score (0-100) | `80` |
| `score_to` | integer | No | Maximum score (0-100) | `100` |
| `status` | array | No | Filter by status (can be multiple) | `HOT,IMMEDIATE_HOT` |
| `tags` | array | No | Filter by tags (can be multiple) | `MBA,USA` |
| `assigned_to` | long | No | Filter by assigned counselor ID | `5` |
| `page` | integer | No | Page number (0-based, default: 0) | `0` |
| `size` | integer | No | Page size (default: 10) | `20` |
| `sort_by` | string | No | Sort field (default: createdAt) | `score` |
| `sort_direction` | string | No | Sort direction: ASC or DESC (default: DESC) | `DESC` |

### Example Requests

#### 1. Basic - Get all leads (first page)
```bash
GET /leads?page=0&size=10
```

#### 2. Search by name/email/phone
```bash
GET /leads?search=Priya&page=0&size=10
```

#### 3. Filter by campaign
```bash
GET /leads?campaign=Summer%202026%20MBA%20Campaign&page=0&size=10
```

#### 4. Filter by date range
```bash
GET /leads?date_from=2025-01-01&date_to=2025-12-31&page=0&size=10
```

#### 5. Filter by score range
```bash
GET /leads?score_from=80&score_to=100&page=0&size=10
```

#### 6. Filter by status
```bash
GET /leads?status=HOT&status=IMMEDIATE_HOT&page=0&size=10
```

#### 7. Filter by tags
```bash
GET /leads?tags=MBA&tags=USA&page=0&size=10
```

#### 8. Combined filters with sorting
```bash
GET /leads?search=Sharma&campaign=Summer%202026&status=HOT&tags=MBA&score_from=90&sort_by=score&sort_direction=DESC&page=0&size=20
```

#### 9. Filter by assigned counselor
```bash
GET /leads?assigned_to=5&page=0&size=10
```

### Success Response (200 OK)

```json
{
  "data": {
    "leads": [
      {
        "id": 1,
        "name": "Priya Sharma",
        "email": "priya.sharma@email.com",
        "phone_number": "+91 98765 43210",
        "status": "HOT",
        "score": 95,
        "tags": ["MBA", "USA"],
        "assigned_to_name": "John Counselor",
        "is_duplicate": false,
        "created_at": "2025-12-07T10:30:00.000Z"
      },
      {
        "id": 2,
        "name": "Rahul Patel",
        "email": "rahul.patel@email.com",
        "phone_number": "+91 87654 32109",
        "status": "WARM",
        "score": 82,
        "tags": ["Engineering", "Canada"],
        "assigned_to_name": null,
        "is_duplicate": false,
        "created_at": "2025-12-06T15:45:00.000Z"
      }
    ],
    "current_page": 0,
    "page_size": 10,
    "total_elements": 25,
    "total_pages": 3,
    "is_first": true,
    "is_last": false,
    "has_next": true,
    "has_previous": false
  },
  "message": "Leads fetched successfully",
  "statusCode": 200
}
```

### Response Fields

#### Pagination Fields
- `current_page`: Current page number (0-based)
- `page_size`: Number of items per page
- `total_elements`: Total number of leads matching the filters
- `total_pages`: Total number of pages available
- `is_first`: Whether this is the first page
- `is_last`: Whether this is the last page
- `has_next`: Whether there is a next page
- `has_previous`: Whether there is a previous page

#### Lead Fields (in leads array)
- `id`: Lead ID
- `name`: Full name (first_name + last_name)
- `email`: Email address
- `phone_number`: Phone number
- `status`: Lead status
- `score`: Lead score (0-100)
- `tags`: Array of tags
- `assigned_to_name`: Name of assigned counselor (null if unassigned)
- `is_duplicate`: Whether marked as duplicate
- `created_at`: Creation timestamp

### Error Responses

#### 400 Bad Request - Invalid Parameters
```json
{
  "errors": [],
  "message": "Invalid date format for date_from",
  "statusCode": 400
}
```

#### 401 Unauthorized
```json
{
  "errors": [],
  "message": "User not authenticated",
  "statusCode": 401
}
```

#### 403 Forbidden
```json
{
  "errors": [],
  "message": "Access denied. Required role: ADMIN or COUNSELOR",
  "statusCode": 403
}
```

#### 500 Internal Server Error
```json
{
  "errors": [],
  "message": "Error fetching leads: Database connection failed",
  "statusCode": 500
}
```

## Testing with cURL

### Example 1: Get all leads
```bash
curl -X GET "http://localhost:8080/leads?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Example 2: Search and filter
```bash
curl -X GET "http://localhost:8080/leads?search=Priya&status=HOT&status=IMMEDIATE_HOT&tags=MBA&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Example 3: Filter by campaign and date range
```bash
curl -X GET "http://localhost:8080/leads?campaign=Summer%202026%20MBA%20Campaign&date_from=2025-01-01&date_to=2025-12-31&page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Example 4: Filter by score range and sort
```bash
curl -X GET "http://localhost:8080/leads?score_from=90&score_to=100&sort_by=score&sort_direction=DESC&page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Filter Combinations

The API supports combining multiple filters. All filters work together with AND logic:

```
GET /leads?search=Sharma&campaign=Summer2026&status=HOT&tags=MBA&score_from=80&date_from=2025-01-01
```

This will return leads that:
- Match "Sharma" in name, email, or phone AND
- Belong to "Summer2026" campaign AND
- Have HOT status AND
- Have MBA tag AND
- Have score >= 80 AND
- Created after 2025-01-01

## Performance Considerations

1. **Indexed Fields**: The following fields are indexed for faster queries:
   - `email`
   - `phone_number`
   - `status`
   - `campaign`
   - `created_at`
   - Full-text search on: `first_name`, `last_name`, `email`, `tags`

2. **Pagination**: Always use pagination to avoid loading large result sets

3. **Recommended Page Sizes**:
   - Default: 10 items
   - Maximum recommended: 50 items
   - For exports: Use multiple paginated requests

## Frontend Integration Notes

### Example: React/Vue Component

```javascript
async function fetchLeads(filters) {
  const params = new URLSearchParams();
  
  if (filters.search) params.append('search', filters.search);
  if (filters.campaign) params.append('campaign', filters.campaign);
  if (filters.dateFrom) params.append('date_from', filters.dateFrom);
  if (filters.dateTo) params.append('date_to', filters.dateTo);
  if (filters.scoreFrom) params.append('score_from', filters.scoreFrom);
  if (filters.scoreTo) params.append('score_to', filters.scoreTo);
  
  // Add multiple status filters
  filters.status?.forEach(s => params.append('status', s));
  
  // Add multiple tag filters
  filters.tags?.forEach(t => params.append('tags', t));
  
  if (filters.assignedTo) params.append('assigned_to', filters.assignedTo);
  
  params.append('page', filters.page || 0);
  params.append('size', filters.size || 10);
  params.append('sort_by', filters.sortBy || 'createdAt');
  params.append('sort_direction', filters.sortDirection || 'DESC');
  
  const response = await fetch(`/leads?${params.toString()}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
}
```

### Example Usage:
```javascript
const leads = await fetchLeads({
  search: 'Priya',
  campaign: 'Summer 2026 MBA Campaign',
  status: ['HOT', 'IMMEDIATE_HOT'],
  tags: ['MBA', 'USA'],
  scoreFrom: 80,
  page: 0,
  size: 20
});
```
