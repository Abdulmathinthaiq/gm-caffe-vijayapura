#!/bin/sh
set -e

echo "========================================="
echo "GM Caffe - Railway Deployment Debug"
echo "========================================="
echo "Container starting..."

# Debug: Print all env vars
echo "Environment variables:"
env | grep -E '^(MYSQL|DB_|SPRING|PORT)' | sort || true

echo "========================================="
echo "Starting application..."
echo "========================================="

# Run the application - let Spring handle the configuration
# Just pass the PORT variable
exec java -jar app.jar

