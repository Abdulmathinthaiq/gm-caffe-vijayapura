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

# Try to parse MYSQL_PUBLIC_URL if available (for Railway external access)
# Format: mysql://user:password@host:port/database
if [ -n "$MYSQL_PUBLIC_URL" ]; then
    echo "Using MYSQL_PUBLIC_URL for database connection..."
    # Extract components from MYSQL_PUBLIC_URL
    # Remove mysql:// prefix
    URL="${MYSQL_PUBLIC_URL#mysql://}"
    # Extract user:password@host:port/database
    USERPASS="${URL%%@*}"
    HOSTPORTDB="${URL#*@}"
    
    DB_USER="${USERPASS%%:*}"
    DB_PASS="${USERPASS#*:}"
    
    HOSTPORT="${HOSTPORTDB%/*}"
    DB_HOST="${HOSTPORT%%:*}"
    DB_PORT="${HOSTPORT#*:}"
    DB_NAME="${HOSTPORTDB#*/}"
    
    echo "Parsed from MYSQL_PUBLIC_URL:"
    echo "  DB_HOST: $DB_HOST"
    echo "  DB_PORT: $DB_PORT"
    echo "  DB_NAME: $DB_NAME"
    echo "  DB_USER: $DB_USER"
    
    # Run with explicit system properties
    exec java \
        -Dspring.datasource.url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -jar app.jar
else
    echo "Using standard environment variables..."
    exec java -jar app.jar
fi

