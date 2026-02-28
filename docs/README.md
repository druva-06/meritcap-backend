# MeritCap Backend — Documentation

## Directory Structure

```
docs/
├── README.md              # This file
├── ARCHITECTURE.md        # System architecture and design patterns
├── DATABASE_SCHEMA.md     # Database schema and relationships
├── DEPLOYMENT.md          # VPS / K8s deployment guide
├── DEVELOPMENT_GUIDE.md   # Local development setup
├── DEV_MODE_EMAIL_GUIDE.md # How dev-mode email capture works
└── api/
    └── API_REFERENCE.md   # Complete REST API reference
```

## Quick Links

| Doc | Audience | Description |
|-----|----------|-------------|
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | All devs | Local setup, SSH tunnel, running the backend |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Backend devs | Layers, packages, design patterns |
| [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) | Backend devs | Tables, relationships, indexes |
| [DEPLOYMENT.md](DEPLOYMENT.md) | DevOps | VPS, MicroK8s, GitHub Actions CI/CD |
| [DEV_MODE_EMAIL_GUIDE.md](DEV_MODE_EMAIL_GUIDE.md) | All devs | Testing invitation flow without SMTP |
| [API_REFERENCE.md](api/API_REFERENCE.md) | Frontend / API consumers | Endpoints, auth, request/response formats |

## Related

- [Database README](../database/README.md) — Migrations and seed data
- [K8s Manifests](../k8s/) — Kubernetes deployment files
