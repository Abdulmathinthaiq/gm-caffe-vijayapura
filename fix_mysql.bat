@echo off
REM MySQL Permission Fix for Railway Deployment
REM This script grants MySQL user permissions to connect from Railway's internal network

echo ========================================
echo Fixing MySQL Permissions for Railway
echo ========================================

REM Using Railway's MySQL connection string from fix_mysql.md
REM Note: The password contains special characters, so we use -p without space

"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -h tramway.proxy.rlwy.net -P 28976 -u root -pTyuSNNlWXGgUBOXvBezwCSIYAggSAUdu railway -e "GRANT ALL PRIVILEGES ON railway.* TO 'root'@'100.64.0.%' IDENTIFIED BY 'TyuSNNlWXGgUBOXvBezwCSIYAggSAUdu'; FLUSH PRIVILEGES;"

echo ========================ls================
echo MySQL permissions updated!
echo Please redeploy your Railway app
echo ========================================

pause

