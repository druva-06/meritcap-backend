#!/bin/bash
# ============================================================
# MeritCap - Run Database Seed Scripts
# ============================================================
# Seeds the database with required roles, permissions, and
# role-permission mappings.
#
# Prerequisites:
#   - SSH tunnel running: ./start-db-tunnel.sh
#   - OR direct access to the database
#
# Usage:
#   ./database/seed/run_seed.sh                    # via SSH tunnel (localhost)
#   ./database/seed/run_seed.sh 82.112.234.51      # direct connection
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DB_HOST="${1:-localhost}"
DB_PORT="${2:-3306}"
DB_NAME="${3:-meritcap}"

echo "=============================================="
echo "  MeritCap - Database Seed"
echo "=============================================="
echo "  Host: ${DB_HOST}:${DB_PORT}"
echo "  Database: ${DB_NAME}"
echo "=============================================="
echo ""

read -p "MySQL username: " DB_USER
read -sp "MySQL password: " DB_PASS
echo ""
echo ""

run_sql() {
    local file="$1"
    local name="$(basename "$file")"
    echo "Running ${name}..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$file"
    echo "  ✓ ${name} complete"
    echo ""
}

run_sql "${SCRIPT_DIR}/001_seed_roles.sql"
run_sql "${SCRIPT_DIR}/002_seed_permissions.sql"
run_sql "${SCRIPT_DIR}/003_seed_role_permissions.sql"
run_sql "${SCRIPT_DIR}/004_seed_admin_user.sql"

echo "=============================================="
echo "  ✓ Database seed complete!"
echo "=============================================="
