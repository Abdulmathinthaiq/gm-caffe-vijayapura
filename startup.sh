#!/bin/sh
set -e

echo "========================================="
echo "GM Caffe - Deployment Debug Info"
echo "========================================="

# Show all MYSQL environment variables
echo "MYSQL Environment Variables:"
echo "  MYSQLHOST: ${MYSQLHOST:-NOT SET}"
echo "  MYSQLPORT: ${MYSQLPORT:-NOT SET}"
echo "  MYSQLDATABASE: ${MYSQLDATABASE:-NOT SET}"
echo "  MYSQLUSER: ${MYSQLUSER:-NOT SET}"
echo "  MYSQLPASSWORD: ${MYSQLPASSWORD:-NOT SET}"

# Show all DB environment variables  
echo "DB Environment Variables:"
echo "  DB_HOST: ${DB_HOST:-NOT SET}"
echo "  DB_PORT: ${DB_PORT:-NOT SET}"
echo "  DB_NAME: ${DB_NAME:-NOT SET}"
echo "  DB_USER: ${DB_USER:-NOT SET}"
echo "  DB_PASSWORD: ${DB_PASSWORD:-NOT SET}"

echo "Other Variables:"
echo "  SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "  PORT: ${PORT:-NOT SET}"
echo "========================================="

# Java version
echo "Java Version:"
java -version 2>&1
echo "========================================="

# Determine database connection parameters
echo "Determining database configuration..."

# Use MYSQLHOST if available, otherwise fall back to DB_HOST
if [ -n "$MYSQLHOST" ]; then
    DB_HOST_VALUE="$MYSQLHOST"
    DB_PORT_VALUE="${MYSQLPORT:-3306}"
    DB_NAME_VALUE="$MYSQLDATABASE"
    DB_USER_VALUE="$MYSQLUSER"
    DB_PASS_VALUE="$MYSQLPASSWORD"
    echo "Using MYSQL* environment variables"
elif [ -n "$DB_HOST" ]; then
    DB_HOST_VALUE="$DB_HOST"
    DB_PORT_VALUE="${DB_PORT:-3306}"
    DB_NAME_VALUE="$DB_NAME"
    DB_USER_VALUE="$DB_USER"
    DB_PASS_VALUE="$DB_PASSWORD"
    echo "Using DB_* environment variables"
else
    echo "ERROR: No database host configured!"
    echo "Neither MYSQLHOST nor DB_HOST is set."
    # Use defaults for testing
    DB_HOST_VALUE="localhost"
    DB_PORT_VALUE="3306"
    DB_NAME_VALUE="railway"
    DB_USER_VALUE="root"
    DB_PASS_VALUE=""
fi

echo "Database Configuration:"
echo "  Host: $DB_HOST_VALUE"
echo "  Port: $DB_PORT_VALUE"
echo "  Database: $DB_NAME_VALUE"
echo "  Username: $DB_USER_VALUE"

# Build the JDBC URL
DB_URL="jdbc:mysql://${DB_HOST_VALUE}:${DB_PORT_VALUE}/${DB_NAME_VALUE}?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true"
echo "  JDBC URL: $DB_URL"
echo "========================================="

echo "Starting GM Caffe Application..."
echo "Command: java -Dspring.datasource.url='$DB_URL' -Dspring.datasource.username='$DB_USER_VALUE' -Dspring.datasource.password='$DB_PASS_VALUE' -jar app.jar"

# Execute Java with explicit system properties
exec java \
    "-Dspring.datasource.url=$DB_URL" \
    "-Dspring.datasource.username=$DB_USER_VALUE" \
    "-Dspring.datasource.password=$DB_PASS_VALUE" \
    -jar app.jar

