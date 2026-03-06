#!/bin/sh
echo "========================================="
echo "GM Caffe - Railway Deployment Debug Info"
echo "========================================="
echo "MYSQLHOST: ${MYSQLHOST:-NOT SET}"
echo "MYSQLPORT: ${MYSQLPORT:-NOT SET}"
echo "MYSQLDATABASE: ${MYSQLDATABASE:-NOT SET}"
echo "MYSQLUSER: ${MYSQLUSER:-NOT SET}"
echo "MYSQLPASSWORD: ${MYSQLPASSWORD:-NOT SET}"
echo "---"
echo "DB_HOST: ${DB_HOST:-NOT SET}"
echo "DB_PORT: ${DB_PORT:-NOT SET}"
echo "DB_NAME: ${DB_NAME:-NOT SET}"
echo "DB_USER: ${DB_USER:-NOT SET}"
echo "DB_PASSWORD: ${DB_PASSWORD:-NOT SET}"
echo "---"
echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "PORT: ${PORT:-NOT SET}"
echo "========================================="

# Print Java version for debugging
echo "Java Version Info:"
java -version 2>&1
echo "========================================="

# Determine database host (use MYSQLHOST if set, otherwise DB_HOST)
if [ -n "$MYSQLHOST" ]; then
    DB_HOST_VALUE="$MYSQLHOST"
    DB_PORT_VALUE="${MYSQLPORT:-3306}"
    DB_NAME_VALUE="$MYSQLDATABASE"
    DB_USER_VALUE="$MYSQLUSER"
    DB_PASS_VALUE="$MYSQLPASSWORD"
elif [ -n "$DB_HOST" ]; then
    DB_HOST_VALUE="$DB_HOST"
    DB_PORT_VALUE="${DB_PORT:-3306}"
    DB_NAME_VALUE="$DB_NAME"
    DB_USER_VALUE="$DB_USER"
    DB_PASS_VALUE="$DB_PASSWORD"
else
    echo "ERROR: No database host configured! Neither MYSQLHOST nor DB_HOST is set."
    DB_HOST_VALUE="localhost"
    DB_PORT_VALUE="3306"
    DB_NAME_VALUE="railway"
    DB_USER_VALUE="root"
    DB_PASS_VALUE=""
fi

# Build database URL
DB_URL="jdbc:mysql://${DB_HOST_VALUE}:${DB_PORT_VALUE}/${DB_NAME_VALUE}?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true"

echo "Resolved Database Host: $DB_HOST_VALUE"
echo "Resolved Database Port: $DB_PORT_VALUE"
echo "Resolved Database Name: $DB_NAME_VALUE"
echo "Resolved Database User: $DB_USER_VALUE"
echo "Database URL: $DB_URL"

# Run the application with explicit system properties
echo "Starting GM Caffe Application..."
exec java \
    -Dspring.datasource.url="$DB_URL" \
    -Dspring.datasource.username="$DB_USER_VALUE" \
    -Dspring.datasource.password="$DB_PASS_VALUE" \
    -jar app.jar

