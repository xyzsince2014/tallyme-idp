#!/bin/bash

# common defensive idiom
set -euo pipefail

echo "⌛  Build & Deploy..."

# Execute Docker build inside the Minikube environment
eval $(minikube docker-env)

docker image rm tallyme-idp:dev 2>/dev/null || true
docker build -t tallyme-idp:dev app

# tell K8s to remake pods with the new docker image
kubectl rollout restart deployment tallyme-idp

echo "✅ Done."
