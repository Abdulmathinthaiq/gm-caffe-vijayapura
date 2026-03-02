@echo off
echo Starting GM Caffe Application...
echo.

:: Check if Maven is available
where mvn >nul 2>nul
if %errorlevel% == 0 (
    echo Maven found, starting with Maven...
    cd /d "%~dp0"
    mvn spring-boot:run
) else (
    echo Maven not found in PATH.
    echo.
    echo Please run the application using VS Code:
    echo 1. Open GmCaffeApplication.java
    echo 2. Click the Run button above 'public static void main'
    echo.
    pause
)
