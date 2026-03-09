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
echo "PORT: ${PORT:-NOT SET}"
echo "========================================="

# JDBC connection parameters optimized for Railway/MySQL
# Using allowPublicKeyRetrieval=true to fix "Public Key Retrieval" errors
# Using connectTimeout to handle network issues
JDBC_PARAMS="?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=30000&socketTimeout=60000"

# Strategy: Use MYSQL_PUBLIC_URL first (public proxy for Railway)
# The internal hostname (mysql-8u1h.railway.internal) has permission issues from containers

if [ -n "$MYSQL_PUBLIC_URL" ]; then
    echo "Using MYSQL_PUBLIC_URL: $MYSQL_PUBLIC_URL"
    
    # Parse MYSQL_PUBLIC_URL - use the public proxy host
    URL="${MYSQL_PUBLIC_URL#mysql://}"
    USERPASS="${URL%%@*}"
    HOSTPORTDB="${URL#*@}"
    
    DB_USER="${USERPASS%%:*}"
    DB_PASS="${USERPASS#*:}"
    HOSTPORT="${HOSTPORTDB%%/*}"
    DB_NAME="${HOSTPORTDB#*/}"
    
    # Extract host and port from the proxy URL
    DB_HOST="${HOSTPORT%%:*}"
    DB_PORT="${HOSTPORT#*:}"
    
    # Use the public proxy for connection with enhanced parameters
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}${JDBC_PARAMS}"
    
    echo "JDBC URL: $JDBC_URL"
    echo "Connecting as user: $DB_USER"
    echo "========================================="
    
    exec java \
        -Dserver.port=${PORT:-3000} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -jar app.jar
        
elif [ -n "$MYSQL_URL" ]; then
    echo "Using MYSQL_URL: $MYSQL_URL"
    # MYSQL_URL format: mysql://user:password@host:port/database
    # Extract values and add connection parameters
    URL="${MYSQL_URL#mysql://}"
    USERPASS="${URL%%@*}"
    HOSTPORTDB="${URL#*@}"
    
    DB_USER="${USERPASS%%:*}"
    DB_PASS="${USERPASS#*:}"
    HOSTPORT="${HOSTPORTDB%%/*}"
    DB_NAME="${HOSTPORTDB#*/}"
    
    DB_HOST="${HOSTPORT%%:*}"
    DB_PORT="${HOSTPORT#*:}"
    
    # Use internal hostname for direct connection with enhanced parameters
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}${JDBC_PARAMS}"
    
    echo "JDBC URL: $JDBC_URL"
    echo "Connecting as user: $DB_USER"
    echo "========================================="
    
    exec java \
        -Dserver.port=${PORT:-3000} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -jar app.jar
        
elif [ -n "$MYSQLHOST" ]; then
    echo "Using MYSQLHOST: $MYSQLHOST"
    
    JDBC_URL="jdbc:mysql://${MYSQLHOST}:${MYSQLPORT:-3306}/${MYSQLDATABASE:-railway}${JDBC_PARAMS}"
    
    echo "JDBC URL: $JDBC_URL"
    echo "========================================="
    
    exec java \
        -Dserver.port=${PORT:-3000} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$MYSQLUSER" \
        -Dspring.datasource.password="$MYSQLPASSWORD" \
        -jar app.jar
else
    echo "ERROR: No MySQL configuration found!"
    echo "Please set one of: MYSQL_PUBLIC_URL, MYSQL_URL, or MYSQLHOST"
    echo "========================================="
    exec java -Dserver.port=${PORT:-3000} -jar app.jar
fi
