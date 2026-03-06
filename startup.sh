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
echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT SET}"
echo "========================================="
echo "Environment vars passed to Java:"
printenv | grep -E "^(MYSQL|DB_|SPRING)" || echo "No MYSQL/DB variables found!"
echo "========================================="

exec java -jar -XshowSettings:properties app.jar 2>&1 | grep -E "(java.vendor|java.home|datasource.url|spring.datasource)" || true

