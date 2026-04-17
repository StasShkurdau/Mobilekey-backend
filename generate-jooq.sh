#!/bin/sh

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo "Starting PostgreSQL..."
docker compose up -d postgres

echo "Waiting for PostgreSQL to be ready..."
until docker exec mobilekey-postgres pg_isready -U mobilekey -q 2>/dev/null; do
  sleep 1
done
echo "PostgreSQL is ready."

echo "Applying Flyway migrations and generating jOOQ classes..."
./gradlew --no-daemon flywayMigrate generateJooq

echo "Done. Generated sources are in generated/jooq/src/main/kotlin"
