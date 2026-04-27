#!/bin/sh

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo "Stopping containers and removing volumes..."
docker compose down -v

echo "Done. All project volumes have been removed."
