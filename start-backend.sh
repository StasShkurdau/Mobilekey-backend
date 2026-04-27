#!/bin/sh

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo "Starting infrastructure (PostgreSQL, Redis)..."
docker compose up -d

echo "Waiting for PostgreSQL to be ready..."
until docker exec mobilekey-postgres pg_isready -U mobilekey -q 2>/dev/null; do
  sleep 1
done
echo "PostgreSQL is ready."

echo "Waiting for Redis to be ready..."
until docker exec mobilekey-redis redis-cli ping 2>/dev/null | grep -q PONG; do
  sleep 1
done
echo "Redis is ready."

echo "Building application..."
./gradlew bootJar

echo "Starting application..."
java -jar build/libs/mobilekey-backend-0.0.1-SNAPSHOT.jar
