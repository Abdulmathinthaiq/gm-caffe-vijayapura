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
    
    # Use the public proxy for connection
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    echo "Connecting as user: $DB_USER"
    
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
    
    # Use internal hostname for direct connection
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    echo "Connecting as user: $DB_USER"
    
    exec java \
        -Dserver.port=${PORT:-3000} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -jar app.jar
        
elif [ -n "$MYSQLHOST" ]; then
    echo "Using MYSQLHOST: $MYSQLHOST"
    
    JDBC_URL="jdbc:mysql://${MYSQLHOST}:${MYSQLPORT:-3306}/${MYSQLDATABASE:-railway}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    
    exec java \
        -Dserver.port=${PORT:-3000} \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$MYSQLUSER" \
        -Dspring.datasource.password="$MYSQLPASSWORD" \
        -jar app.jar
else
    echo "No MySQL configuration found, using environment variables..."
    exec java -Dserver.port=${PORT:-3000} -jar app.jar
fi
