#!/bin/sh
echo "========================================="
echo "GM Caffe - Railway Deployment Debug Info"
echo "========================================="
echo "MYSQLHOST: ${MYSQLHOST:-NOT SET}"
echo "MYSQLPORT: ${MYSQLPORT:-NOT SET}"
echo "MYSQLDATABASE: ${MYSQLDATABASE:-NOT SET}"
echo "MYSQLUSER: ${MYSQLUSER:-NOT SET}"
echo "MYSQLPASSWORD: ${MYSQLPASSWORD:-NOT SET}"
echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "PORT: ${PORT:-NOT SET}"
echo "========================================="

# Print Java version for debugging
echo "Java Version Info:"
java -version 2>&1
echo "========================================="

# Run the application
echo "Starting GM Caffe Application..."
exec java -jar app.jar

