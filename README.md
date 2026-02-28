# MeritCap Backend

Spring Boot REST API for MeritCap — an educational consultancy platform for managing students, applications, leads, colleges, and courses.

## Tech Stack

- **Framework**: Spring Boot 3.3.3 / Java 17
- **Database**: MySQL 8.0
- **Auth**: AWS Cognito + JWT
- **Build**: Maven
- **Deploy**: Docker → GHCR → MicroK8s on VPS
- **CI/CD**: GitHub Actions

## Project Structure

```
cap-backend/
├── src/main/java/com/meritcap/
│   ├── config/           # Security, CORS, Cognito config
│   ├── controller/       # REST controllers
│   ├── DTOs/             # Request & response DTOs
│   ├── model/            # JPA entities
│   ├── repository/       # Spring Data repositories
│   ├── service/impl/     # Business logic
│   ├── transformer/      # DTO ↔ Entity converters
│   └── utils/            # Helpers
├── src/main/resources/
│   ├── application.yaml           # Shared config
│   ├── application-dev.yaml       # Local dev (env vars)
│   ├── application-uat.yaml       # UAT
│   └── application-prod.yaml      # Production (K8s secrets)
├── database/
│   ├── migrations/       # Schema migration SQL (001–011)
│   └── seed/             # Required seed data (roles, permissions)
├── docs/                 # Architecture, deployment, dev guide, API ref
├── k8s/                  # Kubernetes manifests
├── .github/workflows/    # CI/CD pipeline
├── Dockerfile            # Multi-stage build
└── pom.xml
```

## Quick Start

```bash
# 1. Clone
git clone git@github.com:druva-06/cap-backend.git && cd cap-backend

# 2. Set up credentials
cp .env.example .env   # fill in values

# 3. Start SSH tunnel to VPS MySQL
./start-db-tunnel.sh

# 4. Run
mvn spring-boot:run    # → http://localhost:8080/api/swagger-ui/index.html
```

See [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) for full setup instructions.

## Roles

| Role | Description |
|------|-------------|
| ADMIN | Full system access |
| COUNSELOR | Manage leads and students |
| STUDENT | Own profile and applications |
| COLLEGE | College info and applications |
| SUB_AGENT | Limited partner access |

## API

All endpoints are under `/api`. Auth required via `Authorization: Bearer <jwt>` header.

| Group | Path | Description |
|-------|------|-------------|
| Auth | `/api/auth/*` | Signup, login, password reset |
| Students | `/api/students/*` | Student profiles |
| Leads | `/api/leads/*` | Lead management |
| Admin | `/api/admin/*` | Admin operations |
| Colleges | `/api/colleges/*` | College & course data |

Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

## Database

Run migrations then seed data on a fresh database:

```bash
# Migrations (schema)
for f in database/migrations/*.sql; do mysql -u <user> -p meritcap < "$f"; done

# Seed data (roles, permissions — required for app to function)
cd database/seed && ./run_seed.sh
```

See [database/README.md](database/README.md) for details.

## Deployment

Production runs on a VPS (82.112.234.51) with MicroK8s. Pushes to `main` auto-deploy via GitHub Actions.

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for full deployment guide.

## Documentation

- [Development Guide](docs/DEVELOPMENT_GUIDE.md) — Local setup, SSH tunnel, IDE config
- [Architecture](docs/ARCHITECTURE.md) — System design and patterns
- [Database Schema](docs/DATABASE_SCHEMA.md) — Tables and relationships
- [Deployment](docs/DEPLOYMENT.md) — VPS, K8s, CI/CD
- [API Reference](docs/api/API_REFERENCE.md) — Complete endpoint docs
- [Dev Email Guide](docs/DEV_MODE_EMAIL_GUIDE.md) — Testing emails without SMTP
