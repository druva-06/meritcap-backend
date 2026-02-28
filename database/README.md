# Database Documentation

This directory contains database migrations and seed data for the MeritCap backend.

## Structure

```
database/
├── README.md
├── migrations/          # Schema migration scripts (run once, in order)
│   ├── 001_create_leads_table.sql
│   ├── 002_create_dynamic_roles_table_mysql.sql
│   ├── 003_create_permissions_tables.sql
│   ├── 004_assign_default_role_permissions.sql
│   ├── 005_add_portal_unification_permissions.sql
│   ├── 006_assign_portal_permissions_to_roles.sql
│   ├── 007_create_invited_users_table.sql
│   ├── 008_insert_comprehensive_permissions.sql
│   ├── 009_create_menu_permissions_table.sql
│   ├── 010_add_permission_hierarchy_columns.sql
│   └── 011_populate_permission_hierarchy.sql
└── seed/                # Initial data required for the app to function
    ├── 001_seed_roles.sql
    ├── 002_seed_permissions.sql
    ├── 003_seed_role_permissions.sql
    └── run_seed.sh
```

## Initial Setup (New Database)

After creating the database, run migrations first, then seed data:

```bash
# 1. Run all migrations in order
for f in database/migrations/*.sql; do
  mysql -u <user> -p meritcap < "$f"
done

# 2. Seed required data (roles, permissions, role-permission mappings)
cd database/seed
chmod +x run_seed.sh
./run_seed.sh
```

The seed scripts use `INSERT ... ON DUPLICATE KEY UPDATE` so they are safe to re-run.

## Seed Data

The application requires these rows to exist before users can sign up or use the system:

| Script | What it seeds | Count |
|--------|--------------|-------|
| `001_seed_roles.sql` | System roles (ADMIN, COUNSELOR, STUDENT, COLLEGE, SUB_AGENT) | 5 |
| `002_seed_permissions.sql` | Permissions across 18 categories | 178 |
| `003_seed_role_permissions.sql` | Maps permissions to each role | ~277 mappings |

## Running Migrations

Execute SQL files in order against the database:

```bash
mysql -u <user> -p meritcap < database/migrations/001_create_leads_table.sql
```

## Best Practices

1. **Never modify existing migrations** — create new numbered scripts instead
2. **Test migrations** on a dev database first
3. **Keep migrations idempotent** when possible (use `IF NOT EXISTS`, `ON DUPLICATE KEY UPDATE`)
