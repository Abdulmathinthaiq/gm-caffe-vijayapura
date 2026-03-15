@echo off
cd /d "c:\Users\abdul\OneDrive\Desktop\Gm Caffee"
git add startup.sh fix_mysql.bat src/main/resources/application-prod.properties Dockerfile railway.toml MYSQL_FIX.md TODO.md push_fix.bat
git pull origin master --rebase
git commit -m "Fix: Railway MySQL permissions + deployment files (fix_mysql.bat, startup.sh, properties, docs)"
git push origin master
echo.
echo ========================================
echo Files pushed to Railway! Check deployment logs.
echo ========================================
pause

