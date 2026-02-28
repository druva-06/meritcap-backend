#!/bin/bash
# ============================================================
# SSH Tunnel to VPS MySQL Database
# ============================================================
# This script creates an SSH tunnel so your local Spring Boot
# backend can connect to the VPS MySQL database via localhost:3306
#
# Usage:
#   ./start-db-tunnel.sh          # default: forwards to prod DB
#   
# Keep this terminal open while running the backend locally.
# Press Ctrl+C to close the tunnel when done.
# ============================================================

VPS_HOST="82.112.234.51"
VPS_USER="deployer"
SSH_KEY="$HOME/.ssh/wowcap-deployer"
LOCAL_PORT=3306
REMOTE_PORT=3306

echo "=============================================="
echo "  MeritCap - SSH Tunnel to VPS MySQL"
echo "=============================================="
echo ""
echo "  Local:  localhost:${LOCAL_PORT}"
echo "  Remote: ${VPS_HOST}:${REMOTE_PORT}"
echo "  DB:     meritcap"
echo ""
echo "  Tunnel is active. Keep this terminal open."
echo "  Press Ctrl+C to close."
echo "=============================================="
echo ""

ssh -i "$SSH_KEY" \
    -L ${LOCAL_PORT}:localhost:${REMOTE_PORT} \
    -N \
    -o ServerAliveInterval=60 \
    -o ServerAliveCountMax=3 \
    ${VPS_USER}@${VPS_HOST}
