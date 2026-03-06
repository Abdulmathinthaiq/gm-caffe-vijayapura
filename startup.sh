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

# Strategy: Use MYSQL_URL if available (most reliable), otherwise fall back to MYSQL_PUBLIC_URL
# The internal hostname (mysql.railway.internal) may have permission issues

if [ -n "$MYSQL_URL" ]; then
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
    
    # Use internal hostname for direct connection (if permissions allow)
    # Otherwise use the public proxy
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    echo "Connecting as user: $DB_USER"
    
    exec java \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -jar app.jar
        
elif [ -n "$MYSQL_PUBLIC_URL" ]; then
    echo "Using MYSQL_PUBLIC_URL: $MYSQL_PUBLIC_URL"
    
    # Parse MYSQL_PUBLIC_URL
    URL="${MYSQL_PUBLIC_URL#mysql://}"
    USERPASS="${URL%%@*}"
    HOSTPORTDB="${URL#*@}"
    
    DB_USER="${USERPASS%%:*}"
    DB_PASS="${USERPASS#*:}"
    HOSTPORT="${HOSTPORTDB%%/*}"
    DB_NAME="${HOSTPORTDB#*/}"
    
    DB_HOST="${HOSTPORT%%:*}"
    DB_PORT="${HOSTPORT#*:}"
    
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    echo "Connecting as user: $DB_USER"
    
    exec java \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -jar app.jar
        
elif [ -n "$MYSQLHOST" ]; then
    echo "Using MYSQLHOST: $MYSQLHOST"
    
    JDBC_URL="jdbc:mysql://${MYSQLHOST}:${MYSQLPORT:-3306}/${MYSQLDATABASE:-railway}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    
    exec java \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$MYSQLUSER" \
        -Dspring.datasource.password="$MYSQLPASSWORD" \
        -jar app.jar
else
    echo "No MySQL configuration found, using environment variables..."
    exec java -jar app.jar
fi
