@echo off
echo Pushing to GitHub...
cd /d "%~dp0"
git add .
git commit -m "Fix production config - clean application-prod.properties"
git push origin main
pause
