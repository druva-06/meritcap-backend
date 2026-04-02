# Kubernetes Environments

Environment-isolated manifests for backend deployment:

- dev: k8s/environments/dev
- uat: k8s/environments/uat
- prod: k8s/environments/prod

Each environment includes:

- namespace.yaml
- deployment.yaml
- service.yaml
- ingress.yaml
- secrets.yaml.example

## Apply Order

Use one environment directory at a time.

### dev

1. Create and fill secrets file from template.
2. Apply manifests.

```bash
cp k8s/environments/dev/secrets.yaml.example k8s/environments/dev/secrets.yaml
# edit k8s/environments/dev/secrets.yaml
kubectl apply -f k8s/environments/dev/namespace.yaml
kubectl apply -f k8s/environments/dev/secrets.yaml
kubectl apply -f k8s/environments/dev/deployment.yaml
kubectl apply -f k8s/environments/dev/service.yaml
kubectl apply -f k8s/environments/dev/ingress.yaml
```

### uat

```bash
cp k8s/environments/uat/secrets.yaml.example k8s/environments/uat/secrets.yaml
# edit k8s/environments/uat/secrets.yaml
kubectl apply -f k8s/environments/uat/namespace.yaml
kubectl apply -f k8s/environments/uat/secrets.yaml
kubectl apply -f k8s/environments/uat/deployment.yaml
kubectl apply -f k8s/environments/uat/service.yaml
kubectl apply -f k8s/environments/uat/ingress.yaml
```

### prod

```bash
cp k8s/environments/prod/secrets.yaml.example k8s/environments/prod/secrets.yaml
# edit k8s/environments/prod/secrets.yaml
kubectl apply -f k8s/environments/prod/namespace.yaml
kubectl apply -f k8s/environments/prod/secrets.yaml
kubectl apply -f k8s/environments/prod/deployment.yaml
kubectl apply -f k8s/environments/prod/service.yaml
kubectl apply -f k8s/environments/prod/ingress.yaml
```

## Rollout Checks

```bash
kubectl rollout status deployment/meritcap-backend -n dev --timeout=120s
kubectl rollout status deployment/meritcap-backend -n uat --timeout=120s
kubectl rollout status deployment/meritcap-backend -n prod --timeout=120s
```

## GitHub Actions Compatibility

Current workflow updates image for deployment/meritcap-backend in the target namespace.

- dev branch -> dev namespace
- uat branch -> uat namespace
- main branch -> prod namespace

## Conflict Prevention Notes

- Resource names may repeat across environments because each environment uses a separate namespace.
- Ingress host/path must be globally unique per ingress controller.
- To avoid duplicate routes:
  - k8s/ingress.yaml handles frontend hosts only (meritcap.com, admin.meritcap.com).
  - k8s/environments/prod/ingress.yaml handles backend API host (api.meritcap.com).
