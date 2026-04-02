# Environment Strategy

This backend supports multiple runtime environments using Spring profiles and environment variables.

## Profiles

- dev: Local development profile.
- uat: UAT profile.
- prod: Production profile.

Default profile is controlled by application.yaml:

- SPRING_PROFILES_ACTIVE with fallback to prod.

## Local Run with Production Profile

Use .env.prod.local.example as a template:

1. Copy and fill values:
   - cp .env.prod.local.example .env.prod.local
2. Source env vars:
   - source .env.prod.local
3. Start app in prod profile:
   - ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

## GitHub Actions Multi-Environment Deploy

Workflow: .github/workflows/deploy.yml

Branch to environment mapping:

- dev branch -> dev environment + dev namespace
- uat branch -> uat environment + uat namespace
- main branch -> prod environment + prod namespace

### Required GitHub Environment Secrets

Create three GitHub Environments in the repository:

- dev
- uat
- prod

For each environment define:

- VPS_HOST
- VPS_SSH_KEY

The workflow uses the same secret names per environment and chooses the target environment based on branch.

## Kubernetes Secrets

Application runtime secrets should be provided to pods through Kubernetes Secret resources per namespace.

Expected keys match application-\*.yaml placeholders:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- MAIL_HOST
- MAIL_PORT
- MAIL_USERNAME
- MAIL_PASSWORD
- COGNITO_USER_POOL_ID
- COGNITO_CLIENT_ID
- COGNITO_CLIENT_SECRET
- AWS_REGION
- IAM_MERITCAP_ACCESS_KEY_ID
- IAM_MERITCAP_SECRET_ACCESS_KEY
- S3_BUCKET_NAME
- FRONTEND_URL
- SEND_ACTUAL_EMAILS
- SPRING_PROFILES_ACTIVE

## Environment-Isolated Kubernetes Manifests

Use environment-specific manifests under:

- k8s/environments/dev
- k8s/environments/uat
- k8s/environments/prod

Each directory contains separate namespace, deployment, service, ingress, and secrets template files.

See k8s/environments/README.md for apply order and rollout commands.
