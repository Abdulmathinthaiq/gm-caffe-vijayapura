#!/bin/sh
set -e

echo "========================================="
echo "GM Caffe - Railway Deployment"
echo "========================================="
echo "Container starting..."

# Print initial environment variables
echo "Initial MYSQLHOST: ${MYSQLHOST:-NOT SET}"
echo "MYSQL_PUBLIC_URL: ${MYSQL_PUBLIC_URL:-NOT SET}"

# Parse MYSQL_PUBLIC_URL to get connection details
# Format: mysql://user:password@host:port/database
if [ -n "$MYSQL_PUBLIC_URL" ]; then
    echo ""
    echo "Parsing MYSQL_PUBLIC_URL..."
    
    # Remove mysql:// prefix
    URL="${MYSQL_PUBLIC_URL#mysql://}"
    
    # Extract user:password (before @)
    USERPASS="${URL%%@*}"
    
    # Extract host:port/database (after @)
    HOSTPORTDB="${URL#*@}"
    
    # Extract user (before :)
    export MYSQLUSER="${USERPASS%%:*}"
    
    # Extract password (after :)
    export MYSQLPASSWORD="${USERPASS#*:}"
    
    # Extract host:port (before /)
    HOSTPORT="${HOSTPORTDB%%/*}"
    
    # Extract database (after /)
    export MYSQLDATABASE="${HOSTPORTDB#*/}"
    
    # Extract host (before :)
    export MYSQLHOST="${HOSTPORT%%:*}"
    
    # Extract port (after :)
    export MYSQLPORT="${HOSTPORT#*:}"
    
    echo "Parsed values:"
    echo "  MYSQLHOST: $MYSQLHOST"
    echo "  MYSQLPORT: $MYSQLPORT"
    echo "  MYSQLDATABASE: $MYSQLDATABASE"
    echo "  MYSQLUSER: $MYSQLUSER"
    echo "  MYSQLPASSWORD: [HIDDEN]"
fi

echo "========================================="
echo "Final Environment Variables:"
echo "  MYSQLHOST: ${MYSQLHOST:-NOT SET}"
echo "  MYSQLPORT: ${MYSQLPORT:-NOT SET}"
echo "  MYSQLDATABASE: ${MYSQLDATABASE:-NOT SET}"
echo "  MYSQLUSER: ${MYSQLUSER:-NOT SET}"
echo "  SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "  PORT: ${PORT:-NOT SET}"
echo "========================================="

# Run the application
echo "Starting application..."
exec java -jar app.jar

