@echo off
echo Starting GM Caffe Application...
echo.

REM Set JAVA_HOME to JDK 17
set "JAVA_HOME=C:\Program Files\Java\jdk-17"

REM Verify Java is available
"%JAVA_HOME%\bin\java" -version >nul 2>&1
if errorlevel 1 (
    echo Java not found at JAVA_HOME: %JAVA_HOME%
    echo Trying JDK 25...
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
)

echo Using Java at: %JAVA_HOME%
echo.

cd /d "%~dp0"
mvnw.cmd spring-boot:run
