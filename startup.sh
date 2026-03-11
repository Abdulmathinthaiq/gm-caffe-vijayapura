#!/bin/sh
set -e

echo "========================================="
echo "GM Caffe - Deployment"
echo "========================================="
echo "Container starting..."

# Print key environment variables for debugging
echo "PORT: ${PORT:-3000}"
echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "========================================="

# Create data directory for H2 database persistence
mkdir -p /app/data

echo "Starting application with H2 database..."
echo "========================================="

exec java \
    -Dserver.port=${PORT:-3000} \
    -jar app.jar
