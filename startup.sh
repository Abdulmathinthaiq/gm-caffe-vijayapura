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
echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "PORT: ${PORT:-NOT SET}"
echo "========================================="

# Use internal MySQL hostname for Railway internal networking
# Priority: MYSQLHOST (internal) > MYSQL_PUBLIC_URL (proxy)
if [ -n "$MYSQLHOST" ] && [ "$MYSQLHOST" != "mysql.railway.internal" ]; then
    # MYSQLHOST is set and is NOT the internal hostname, use it
    echo "Using MYSQLHOST: $MYSQLHOST"
    DB_HOST="$MYSQLHOST"
    DB_PORT="${MYSQLPORT:-3306}"
    DB_NAME="${MYSQLDATABASE:-railway}"
    DB_USER="$MYSQLUSER"
    DB_PASS="$MYSQLPASSWORD"
    
    JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    
    exec java \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$DB_USER" \
        -Dspring.datasource.password="$DB_PASS" \
        -jar app.jar
        
elif [ -n "$MYSQLHOST" ]; then
    # MYSQLHOST is mysql.railway.internal - use internal networking
    echo "Using internal MySQL hostname: $MYSQLHOST"
    
    JDBC_URL="jdbc:mysql://${MYSQLHOST}:${MYSQLPORT:-3306}/${MYSQLDATABASE:-railway}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    
    echo "JDBC URL: $JDBC_URL"
    
    exec java \
        -Dspring.datasource.url="$JDBC_URL" \
        -Dspring.datasource.username="$MYSQLUSER" \
        -Dspring.datasource.password="$MYSQLPASSWORD" \
        -jar app.jar
else
    echo "MYSQLHOST not set, falling back to MYSQL_PUBLIC_URL..."
    
    # Parse MYSQL_PUBLIC_URL to get connection details
    # Format: mysql://user:password@host:port/database
    if [ -n "$MYSQL_PUBLIC_URL" ]; then
        echo "Parsing MYSQL_PUBLIC_URL: $MYSQL_PUBLIC_URL"
        
        # Remove mysql:// prefix
        URL="${MYSQL_PUBLIC_URL#mysql://}"
        
        # Extract user:password (before @)
        USERPASS="${URL%%@*}"
        
        # Extract host:port/database (after @)
        HOSTPORTDB="${URL#*@}"
        
        # Extract user (before :)
        DB_USER="${USERPASS%%:*}"
        
        # Extract password (after :)
        DB_PASS="${USERPASS#*:}"
        
        # Extract host:port (before /)
        HOSTPORT="${HOSTPORTDB%%/*}"
        
        # Extract database (after /)
        DB_NAME="${HOSTPORTDB#*/}"
        
        # Extract host (before :)
        DB_HOST="${HOSTPORT%%:*}"
        
        # Extract port (after :)
        DB_PORT="${HOSTPORT#*:}"
        
        # Construct full JDBC URL with additional parameters
        JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
        
        echo "Parsed values:"
        echo "  DB_HOST: $DB_HOST"
        echo "  DB_PORT: $DB_PORT"
        echo "  DB_NAME: $DB_NAME"
        echo "  DB_USER: $DB_USER"
        echo "  JDBC_URL: $JDBC_URL"
        
        # Run with explicit system properties
        exec java \
            -Dspring.datasource.url="$JDBC_URL" \
            -Dspring.datasource.username="$DB_USER" \
            -Dspring.datasource.password="$DB_PASS" \
            -jar app.jar
    else
        echo "No MySQL configuration found, using environment variables..."
        exec java -jar app.jar
    fi
fi
