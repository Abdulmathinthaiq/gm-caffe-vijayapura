#!/bin/sh
echo "========================================="
echo "Database Configuration Debug Info"
echo "========================================="
echo "MYSQLHOST: ${MYSQLHOST:-NOT SET}"
echo "MYSQLPORT: ${MYSQLPORT:-NOT SET}"
echo "MYSQLDATABASE: ${MYSQLDATABASE:-NOT SET}"
echo "MYSQLUSER: ${MYSQLUSER:-NOT SET}"
echo "MYSQLPASSWORD: ${MYSQLPASSWORD:-NOT SET}"
echo "DB_HOST: ${DB_HOST:-NOT SET}"
echo "DB_USER: ${DB_USER:-NOT SET}"
echo "DB_PASSWORD: ${DB_PASSWORD:-NOT SET}"
echo "========================================="

exec java -jar app.jar

