#!/bin/bash

# Database Migration Script for Role System
# This script runs the MySQL migration to create the roles table

echo "🚀 Starting database migration for Role System..."
echo ""

# Database credentials (from application-dev.properties)
DB_HOST="82.112.234.51"
DB_PORT="3306"
DB_NAME="cap_dev"
DB_USER="cap_dev"
DB_PASS="zmhCuJR.uX3mB]4"

# Migration file
MIGRATION_FILE="database/migrations/002_create_dynamic_roles_table_mysql.sql"

echo "📊 Database Connection Info:"
echo "   Host: $DB_HOST:$DB_PORT"
echo "   Database: $DB_NAME"
echo "   User: $DB_USER"
echo ""

# Check if migration file exists
if [ ! -f "$MIGRATION_FILE" ]; then
    echo "❌ Error: Migration file not found: $MIGRATION_FILE"
    exit 1
fi

echo "📄 Running migration: $MIGRATION_FILE"
echo ""

# Run the migration
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$MIGRATION_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration completed successfully!"
    echo ""
    echo "🔍 Verifying migration..."
    echo ""
    
    # Verify roles were created
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SELECT * FROM roles;"
    
    echo ""
    echo "✨ Next steps:"
    echo "   1. Restart your Spring Boot application"
    echo "   2. Test the login functionality"
    echo "   3. The 'model.Role with id 0' error should be resolved!"
    echo ""
else
    echo ""
    echo "❌ Migration failed! Please check the error messages above."
    echo ""
    echo "💡 Troubleshooting tips:"
    echo "   1. Check if you can connect to the database:"
    echo "      mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME"
    echo ""
    echo "   2. Check if the roles table already exists:"
    echo "      mysql> SHOW TABLES LIKE 'roles';"
    echo ""
    echo "   3. If the table exists, check its contents:"
    echo "      mysql> SELECT * FROM roles;"
    echo ""
    exit 1
fi
