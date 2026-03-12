@echo off
cd /d "c:\Users\abdul\OneDrive\Desktop\Gm Caffee"
git add startup.sh fix_mysql.bat application-prod.properties Dockerfile railway.toml
git commit -m "Fix: Railway MySQL permissions and deployment configuration"
git push origin master
pause

