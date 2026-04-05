#!/bin/bash

# Fix Cognito Authentication Flow Configuration
# This script enables the required authentication flows in the Cognito User Pool Client

set -e

echo "🔧 Fixing Cognito Authentication Flow Configuration..."

# Cognito configuration from environment
USER_POOL_ID="${COGNITO_USER_POOL_ID}"
CLIENT_ID="${COGNITO_CLIENT_ID}"
AWS_REGION="${AWS_REGION:-ap-south-2}"

if [ -z "$USER_POOL_ID" ] || [ -z "$CLIENT_ID" ]; then
    echo "❌ Missing required environment variables: COGNITO_USER_POOL_ID, COGNITO_CLIENT_ID"
    exit 1
fi

echo "📋 Current configuration:"
echo "  User Pool ID: $USER_POOL_ID"
echo "  Client ID: $CLIENT_ID"
echo "  Region: $AWS_REGION"

echo "🔍 Checking current authentication flows..."

# Get current client configuration
aws cognito-idp describe-user-pool-client \
    --user-pool-id "$USER_POOL_ID" \
    --client-id "$CLIENT_ID" \
    --region "$AWS_REGION" > /tmp/client-config.json

# Check current auth flows
current_flows=$(jq -r '.UserPoolClient.ExplicitAuthFlows[]' /tmp/client-config.json 2>/dev/null || echo "none")
echo "Current auth flows: $current_flows"

echo "🛠️  Updating authentication flows to enable USER_PASSWORD_AUTH..."

# Update the client to enable required authentication flows
aws cognito-idp update-user-pool-client \
    --user-pool-id "$USER_POOL_ID" \
    --client-id "$CLIENT_ID" \
    --region "$AWS_REGION" \
    --explicit-auth-flows "ALLOW_USER_PASSWORD_AUTH" "ALLOW_ADMIN_USER_PASSWORD_AUTH" "ALLOW_REFRESH_TOKEN_AUTH" "ALLOW_USER_SRP_AUTH"

echo "✅ Authentication flows updated successfully!"

echo "🔍 Verifying updated configuration..."

# Verify the update
aws cognito-idp describe-user-pool-client \
    --user-pool-id "$USER_POOL_ID" \
    --client-id "$CLIENT_ID" \
    --region "$AWS_REGION" | jq '.UserPoolClient.ExplicitAuthFlows'

echo "✅ Cognito authentication flow configuration fix complete!"
echo "🚀 Email/password login should now work in production"

# Clean up
rm -f /tmp/client-config.json