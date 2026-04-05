#!/bin/bash

# Fix Cognito Domain Configuration in Kubernetes Production Environment
# This script adds the missing COGNITO_DOMAIN to the production secrets

set -e

echo "🔧 Fixing Cognito Domain Configuration in Production..."

# Cognito domain value
COGNITO_DOMAIN="ap-south-2esp2vgk0k.auth.ap-south-2.amazoncognito.com"

echo "📝 Adding COGNITO_DOMAIN to production secrets..."

# Patch the existing secret to add the missing COGNITO_DOMAIN
microk8s kubectl patch secret meritcap-backend-secrets -n prod --type='merge' -p="{\"data\":{\"COGNITO_DOMAIN\":\"$(echo -n "${COGNITO_DOMAIN}" | base64 -w 0)\"}}"

echo "✅ COGNITO_DOMAIN added to production secrets"

echo "🔄 Restarting backend deployment to pick up new configuration..."

# Restart the deployment to pick up the new environment variable
microk8s kubectl rollout restart deployment/meritcap-backend -n prod

echo "⏳ Waiting for deployment to be ready..."

# Wait for rollout to complete
microk8s kubectl rollout status deployment/meritcap-backend -n prod --timeout=300s

echo "✅ Deployment restarted successfully"

echo "🔍 Checking pod status..."
microk8s kubectl get pods -n prod -l app=meritcap-backend

echo "✅ Cognito domain configuration fix complete!"
echo "🚀 Google OAuth should now be working in production"

# Optional: Test the Google OAuth endpoint
echo "🧪 Testing Google OAuth endpoint..."
curl -s "https://api.meritcap.com/auth/google/url?redirectUri=https://meritcap.com/auth/callback" || echo "❌ Test failed - check logs"