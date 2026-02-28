# Development Guide

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| JDK | 17+ | `brew install openjdk@17` or [Amazon Corretto](https://aws.amazon.com/corretto/) |
| Maven | 3.6+ | `brew install maven` |
| Git | any | `brew install git` |
| SSH key | — | `~/.ssh/wowcap-deployer` (get from team lead) |

No local MySQL installation needed — you connect to the production DB via SSH tunnel.

## Quick Start

```bash
# 1. Clone the repo
git clone git@github.com:druva-06/cap-backend.git
cd cap-backend

# 2. Copy the env file and fill in credentials
cp .env.example .env
# Edit .env with actual values (get from team lead)

# 3. Start the SSH tunnel to the VPS MySQL
./start-db-tunnel.sh
# Runs in background: forwards localhost:3306 → VPS MySQL

# 4. Run the backend
mvn spring-boot:run
# Starts on http://localhost:8080 with profile=dev
```

The app will be available at `http://localhost:8080/api/swagger-ui/index.html`.

## Environment Configuration

The backend uses Spring profiles. The default profile is `dev`.

| Profile | File | Purpose |
|---------|------|---------|
| `dev` | `application-dev.yaml` | Local development (SSH tunnel to prod DB) |
| `uat` | `application-uat.yaml` | UAT environment |
| `prod` | `application-prod.yaml` | Production (K8s secrets) |

### `.env` file

All sensitive values are read from environment variables. The `.env` file (git-ignored) provides them locally:

```bash
# Database (via SSH tunnel)
DB_URL=jdbc:mysql://localhost:3306/meritcap
DB_USERNAME=meritcap
DB_PASSWORD=<get from team>

# AWS Cognito
COGNITO_USER_POOL_ID=ap-south-2_eSP2vGk0K
COGNITO_CLIENT_ID=<get from team>
COGNITO_CLIENT_SECRET=<get from team>
COGNITO_REGION=ap-south-2

# AWS S3
AWS_S3_ACCESS_KEY=<get from team>
AWS_S3_SECRET_KEY=<get from team>
AWS_S3_BUCKET=<get from team>
AWS_S3_REGION=ap-south-1

# Email (dev mode captures emails in API responses, no SMTP needed)
MAIL_USERNAME=unused-in-dev
MAIL_PASSWORD=unused-in-dev
```

### SSH Tunnel

The `start-db-tunnel.sh` script forwards `localhost:3306` to the VPS MySQL:

```bash
ssh -i ~/.ssh/wowcap-deployer -L 3306:localhost:3306 deployer@82.112.234.51 -N
```

If port 3306 is already in use (local MySQL running), either stop it or change the local port in both the tunnel command and `DB_URL`.

## Project Structure

```
com.meritcap/
├── api/              # External API clients
├── config/           # Spring configuration (Security, CORS, etc.)
├── controller/       # REST controllers
├── DTOs/
│   ├── requestDTOs/  # Request DTOs
│   └── responseDTOs/ # Response DTOs
├── enums/            # Enumerations
├── exception/        # Custom exceptions & handlers
├── model/            # JPA entities
├── repository/       # Spring Data JPA repositories
├── service/
│   └── impl/         # Service implementations
├── transformer/      # DTO ↔ Entity converters
├── utils/            # Utility classes
└── validations/      # Custom validators
```

## Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=UserAuthServiceTest

# With coverage
mvn clean verify
```

## Dev-Mode Email

When running with `dev` profile, the `DevEmailService` is active. It captures emails in API responses instead of sending them via SMTP, so you can test invitation flows without configuring an email server.

See [DEV_MODE_EMAIL_GUIDE.md](DEV_MODE_EMAIL_GUIDE.md) for details.

## API Testing

Swagger UI is available at: `http://localhost:8080/api/swagger-ui/index.html`

### cURL examples

```bash
# Health check
curl http://localhost:8080/api/actuator/health

# Student signup
curl -X POST http://localhost:8080/api/auth/signup/student \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Test",
    "last_name": "User",
    "email": "test@example.com",
    "password": "Test@1234",
    "phone_number": "+919876543210"
  }'
```

## IDE Setup

### IntelliJ IDEA

1. Open → select `cap-backend` folder (auto-detects Maven)
2. File → Project Structure → set SDK to Java 17
3. Settings → Build → Compiler → Annotation Processors → enable (for Lombok)
4. Run → Edit Configurations → Spring Boot → main class `com.meritcap.EducationApplication`, active profiles `dev`

### VS Code

Install **Extension Pack for Java** and **Spring Boot Extension Pack**, then add to `.vscode/launch.json`:

```json
{
  "type": "java",
  "name": "MeritCap Backend",
  "request": "launch",
  "mainClass": "com.meritcap.EducationApplication",
  "args": "--spring.profiles.active=dev"
}
```

## Common Issues

| Problem | Solution |
|---------|----------|
| Port 3306 in use | Stop local MySQL: `brew services stop mysql` |
| Port 8080 in use | `lsof -i :8080` then `kill <PID>` |
| Lombok not working | Install Lombok plugin, enable annotation processing, restart IDE |
| SSH tunnel drops | Re-run `./start-db-tunnel.sh` |
| Maven build fails | `mvn dependency:purge-local-repository && mvn clean install -U` |
