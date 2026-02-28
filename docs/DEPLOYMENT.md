# Deployment Guide

## Overview

MeritCap runs on a single **VPS** (82.112.234.51) with **MicroK8s** for container orchestration. Docker images are pushed to **GitHub Container Registry (GHCR)** and deployed via **GitHub Actions**.

## Infrastructure

| Component | Details |
|-----------|---------|
| VPS | Hostinger VPS, Ubuntu, 82.112.234.51 |
| Container runtime | MicroK8s v1.28 |
| K8s namespace | `prod` |
| Container registry | `ghcr.io/druva-06/meritcap-{backend,user,admin}` |
| Database | MySQL 8.0 on VPS (localhost:3306) |
| DNS / SSL | Cloudflare (meritcap.com) |
| Auth | AWS Cognito (ap-south-2) |

### Domains

| Domain | Service |
|--------|---------|
| `api.meritcap.com` | Backend API |
| `meritcap.com` / `www.meritcap.com` | User frontend |
| `admin.meritcap.com` | Admin frontend |

## Prerequisites

- SSH access: `ssh -i ~/.ssh/wowcap-deployer deployer@82.112.234.51`
- GitHub repo access (for GHCR and Actions)
- `microk8s kubectl` on the VPS

## CI/CD — GitHub Actions

Pushes to the `main` branch trigger automatic deployment via `.github/workflows/deploy.yml`:

1. Builds the Docker image with Maven (multi-stage)
2. Pushes to GHCR with tags `prod-<sha>` and `prod-latest`
3. SSHs into the VPS and updates the K8s deployment image
4. Waits for rollout to complete

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `PROD_VPS_HOST` | VPS IP address |
| `VPS_SSH_KEY` | SSH private key for `deployer` user |
| `GITHUB_TOKEN` | Auto-provided, used for GHCR login |

## Manual Deployment

### Build & Push Image

```bash
# Login to GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u druva-06 --password-stdin

# Build
docker build -t ghcr.io/druva-06/meritcap-backend:prod-latest .

# Push
docker push ghcr.io/druva-06/meritcap-backend:prod-latest
```

### Deploy to K8s

```bash
ssh -i ~/.ssh/wowcap-deployer deployer@82.112.234.51

# Update image
microk8s kubectl set image deployment/meritcap-backend \
  meritcap-backend=ghcr.io/druva-06/meritcap-backend:prod-latest \
  -n prod

# Watch rollout
microk8s kubectl rollout status deployment/meritcap-backend -n prod

# Check pods
microk8s kubectl get pods -n prod
```

## Kubernetes Manifests

All K8s files are in `k8s/`:

| File | Purpose |
|------|---------|
| `meritcap-deployment.yaml` | Backend deployment (replicas, image, env vars, probes) |
| `meritcap-service.yaml` | Backend ClusterIP service |
| `user-frontend.yaml` | User frontend deployment + service |
| `admin-frontend.yaml` | Admin frontend deployment + service |
| `ingress.yaml` | Nginx ingress rules for all 3 domains |
| `secrets-prod.yaml` | K8s Secret with DB creds, Cognito config, etc. |

### Applying Manifests

```bash
microk8s kubectl apply -f k8s/secrets-prod.yaml -n prod
microk8s kubectl apply -f k8s/meritcap-deployment.yaml -n prod
microk8s kubectl apply -f k8s/meritcap-service.yaml -n prod
microk8s kubectl apply -f k8s/ingress.yaml -n prod
```

## Database Setup (First Time)

```bash
# SSH into VPS
ssh -i ~/.ssh/wowcap-deployer deployer@82.112.234.51

# Create database and user
sudo mysql -e "
  CREATE DATABASE IF NOT EXISTS meritcap;
  CREATE USER IF NOT EXISTS 'meritcap'@'localhost' IDENTIFIED BY '<password>';
  GRANT ALL PRIVILEGES ON meritcap.* TO 'meritcap'@'localhost';
  FLUSH PRIVILEGES;
"

# Run migrations (from local machine, via SSH)
for f in database/migrations/*.sql; do
  cat "$f" | ssh -i ~/.ssh/wowcap-deployer deployer@82.112.234.51 "sudo mysql meritcap"
done

# Seed required data
for f in database/seed/0*.sql; do
  cat "$f" | ssh -i ~/.ssh/wowcap-deployer deployer@82.112.234.51 "sudo mysql meritcap"
done
```

See [database/README.md](../database/README.md) for details on seed data.

## Health Checks

```bash
# From anywhere
curl https://api.meritcap.com/api/actuator/health

# From VPS
microk8s kubectl logs -f deployment/meritcap-backend -n prod
```

## Rollback

```bash
# View rollout history
microk8s kubectl rollout history deployment/meritcap-backend -n prod

# Rollback to previous version
microk8s kubectl rollout undo deployment/meritcap-backend -n prod

# Rollback to specific revision
microk8s kubectl rollout undo deployment/meritcap-backend -n prod --to-revision=2
```

## Troubleshooting

**Pod won't start:**
```bash
microk8s kubectl describe pod <pod-name> -n prod
microk8s kubectl logs <pod-name> -n prod
```

**Database connection failed:**
- Check `secrets-prod.yaml` has correct DB credentials
- Verify MySQL is running: `sudo systemctl status mysql`
- Check from inside the pod: `microk8s kubectl exec -it <pod> -n prod -- env | grep DB`

**Image pull errors:**
- Ensure GHCR secret exists: `microk8s kubectl get secret ghcr-secret -n prod`
- Verify image tag exists: `docker pull ghcr.io/druva-06/meritcap-backend:prod-latest`
