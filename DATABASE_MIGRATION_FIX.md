# Database Migration Guide - Role System

## Issue

The login is failing with "model.Role with id 0" because the database migration for the dynamic roles system hasn't been executed yet.

## Your Database Info (from application-dev.properties)

- **Type**: MySQL
- **Host**: 82.112.234.51:3306
- **Database**: cap_dev
- **Username**: cap_dev
- **Password**: zmhCuJR.uX3mB]4

## Quick Fix

### Option 1: Run Migration Manually (Recommended for Development)

1. **Connect to your MySQL database**:

   ```bash
   mysql -h 82.112.234.51 -P 3306 -u cap_dev -p cap_dev
   # Enter password when prompted: zmhCuJR.uX3mB]4
   ```

2. **Run the migration script**:

   ```bash
   source /Users/druva/Documents/Personal/wowcap/website/cap-backend/database/migrations/002_create_dynamic_roles_table_mysql.sql
   ```

   Or copy and paste the SQL from the file directly into MySQL console.

### Option 2: Using a Database Client

1. Open your database client (MySQL Workbench, DBeaver, DataGrip, etc.)
2. Connect to: `82.112.234.51:3306` with username `cap_dev`
3. Select database: `cap_dev`
4. Open and execute the MySQL migration script (see below)

### Option 3: Using Command Line

```bash
cd /Users/druva/Documents/Personal/wowcap/website/cap-backend

mysql -h 82.112.234.51 -P 3306 -u cap_dev -p'zmhCuJR.uX3mB]4' cap_dev < database/migrations/002_create_dynamic_roles_table_mysql.sql
```

## What the Migration Does

1. Creates the `roles` table
2. Inserts 5 default system roles (ADMIN, COUNSELOR, STUDENT, COLLEGE, SUB_AGENT)
3. Adds `role_id` column to `users` table
4. Migrates existing role data from the old `role` enum column to `role_id`
5. Creates foreign key constraint
6. Adds indexes for performance

## After Running the Migration

1. **Verify the roles table exists**:

   ```sql
   SELECT * FROM roles;
   ```

   You should see 5 rows:

   - ADMIN
   - COUNSELOR
   - STUDENT
   - COLLEGE
   - SUB_AGENT

2. **Verify the users table has role_id column**:

   ```sql
   \d users
   ```

   You should see a `role_id` column.

3. **Restart your Spring Boot application**

4. **Test login again** - The error should be resolved!

## Troubleshooting

### Error: "relation 'roles' already exists"

- The migration was partially run before. Check if roles are in the table:
  ```sql
  SELECT * FROM roles;
  ```

### Error: "column role_id already exists"

- Skip the ALTER TABLE commands that add the column
- Just ensure the foreign key constraint is added

### Error: "column role does not exist"

- The old `role` column doesn't exist, which is fine if you're starting fresh
- Comment out or skip Step 5 (the UPDATE statements)

### Users can't login after migration

- Check if existing users have valid role_id:
  ```sql
  SELECT id, email, role_id FROM users;
  ```
- If role_id is NULL, manually set it:
  ```sql
  UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT') WHERE role_id IS NULL;
  ```

## Database Connection Info

Check your application properties for database connection details:

- File: `src/main/resources/application-dev.properties` (or application.yaml)
- Look for:
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`

## Future: Automated Migrations

Consider adding Flyway or Liquibase to automate database migrations:

### Flyway Setup (pom.xml)

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### application.yaml

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

Then move SQL files to `src/main/resources/db/migration/` with naming:

- `V1__initial_schema.sql`
- `V2__create_dynamic_roles_table.sql`

---

**Run the migration and your login issue will be fixed!** 🚀
