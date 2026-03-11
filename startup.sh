#!/bin/sh
set -e

echo "========================================="
echo "GM Caffe - Railway Deployment"
echo "========================================="
echo "Container starting..."

# Print key environment variables for debugging
echo "MYSQLHOST: ${MYSQLHOST:-NOT SET}"
echo "MYSQLPORT: ${MYSQLPORT:-NOT SET}"
echo "MYSQLDATABASE: ${MYSQLDATABASE:-NOT SET}"
echo "MYSQLUSER: ${MYSQLUSER:-NOT SET}"
echo "MYSQL_PUBLIC_URL: ${MYSQL_PUBLIC_URL:-NOT SET}"
echo "MYSQL_URL: ${MYSQL_URL:-NOT SET}"
echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "PORT: ${PORT:-8080}"
echo "========================================="

# JDBC connection parameters optimized for Railway/MySQL
JDBC_PARAMS="useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=30000&socketTimeout=60000"

# Use MYSQL_PUBLIC_URL if available (public proxy for Railway)
if [ -n "$MYSQL_PUBLIC_URL" ]; then
    echo "Using MYSQL_PUBLIC_URL: $MYSQL_PUBLIC_URL"
    
    URL="${MYSQL_PUBLIC_URL#mysql://}"
    USERPASS="${URL%%@*}"
    HOSTPORTDB="${URL#*@}"
    
    DB_USER="${USERPASS%%:*}"
    DB_PASS="${USERPASS#*:}"
    DB_NAME="${HOSTPORTDB##*/}"
    HOSTPORT="${HOSTPORTDB%%/*}"
    
    if echo "$HOSTPORT" | grep -q ':'; then
        DB_HOST="${HOSTPORT%%:*}"
        DB_PORT="${HOSTPORT#*:}"
    else
        DB_HOST="$HOSTPORT"
        DB_PORT="3306"
    fi
    
    if [ -z "$DB_USER" ] || [ -z "$DB_HOST" ] || [ -z "$DB_NAME" ]; then
        echo "ERROR: Failed to parse MYSQL_PUBLIC_URL properly"
        exit 1
    fi
    
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?${JDBC_PARAMS}"
    
    echo "Parsed connection details:"
    echo "  JDBC URL: $JDBC_URL"
    echo "  Database: $DB_NAME"
    echo "  Connecting as user: $DB_USER"
    echo "========================================="
    
    exec java \
        -Dserver.port=${PORT:-8080} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -Dspring.jpa.hibernate.ddl-auto=update \
        -jar app.jar
        
elif [ -n "$MYSQL_URL" ]; then
    echo "Using MYSQL_URL: $MYSQL_URL"
    
    URL="${MYSQL_URL#mysql://}"
    USERPASS="${URL%%@*}"
    HOSTPORTDB="${URL#*@}"
    
    DB_USER="${USERPASS%%:*}"
    DB_PASS="${USERPASS#*:}"
    DB_NAME="${HOSTPORTDB##*/}"
    HOSTPORT="${HOSTPORTDB%%/*}"
    
    if echo "$HOSTPORT" | grep -q ':'; then
        DB_HOST="${HOSTPORT%%:*}"
        DB_PORT="${HOSTPORT#*:}"
    else
        DB_HOST="$HOSTPORT"
        DB_PORT="3306"
    fi
    
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?${JDBC_PARAMS}"
    
    echo "Parsed connection details:"
    echo "  JDBC URL: $JDBC_URL"
    echo "  Database: $DB_NAME"
    echo "  Connecting as user: $DB_USER"
    echo "========================================="
    
    exec java \
        -Dserver.port=${PORT:-8080} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -Dspring.jpa.hibernate.ddl-auto=update \
        -jar app.jar
        
elif [ -n "$MYSQLHOST" ]; then
    echo "Using MYSQLHOST: $MYSQLHOST"
    
    JDBC_URL="jdbc:mysql://${MYSQLHOST}:${MYSQLPORT:-3306}/${MYSQLDATABASE:-railway}?${JDBC_PARAMS}"
    
    echo "JDBC URL: $JDBC_URL"
    echo "========================================="
    
    exec java \
        -Dserver.port=${PORT:-8080} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="${MYSQLUSER:-root}" \
        -Dspring.datasource.password="$MYSQLPASSWORD" \
        -Dspring.jpa.hibernate.ddl-auto=update \
        -jar app.jar
else
    echo "ERROR: No MySQL configuration found!"
    echo "Please set one of: MYSQL_PUBLIC_URL, MYSQL_URL, or MYSQLHOST"
    echo "========================================="
    exec java -Dserver.port=${PORT:-8080} -jar app.jar
fi
