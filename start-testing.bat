@echo off
REM Quick Start Script for MFA POC Manual Testing
REM This script uses Maven's embedded Tomcat for quick testing without installing Tomcat

echo ========================================
echo MFA POC - Quick Start Testing Script
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven first
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java first
    pause
    exit /b 1
)

echo [1/3] Checking environment...
echo.
java -version 2>&1
echo.
mvn -version 2>&1
echo.

echo [2/3] Building application...
echo.
echo Setting Maven options...
set "MAVEN_OPTS=-Dhttps.protocols=TLSv1.2"
echo Running: mvn clean package -DskipTests
echo.

call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo ERROR: Build failed!
    echo ========================================
    echo Check the error messages above
    echo.
    echo Common issues:
    echo - Maven repository connection problems
    echo - Missing dependencies
    echo - Compilation errors
    echo.
    echo Try running manually:
    echo   mvn clean package -DskipTests -X
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build Successful!
echo ========================================
echo.
echo WAR file created at: target\mfa-poc.war
echo.
echo [3/3] Starting embedded Tomcat server...
echo.
echo Application will be available at:
echo   http://localhost:8080/mfa-poc/
echo.
echo Test Accounts (without MFA configured):
echo   Username: john.smith   Password: Test@123
echo   Username: bob.wilson   Password: Test@123
echo.
echo Test Accounts (with MFA already configured):
echo   Username: jane.doe     Password: Test@123
echo   Username: admin.user   Password: Admin@123
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

REM Start embedded Tomcat
echo Starting Tomcat...
call mvn tomcat7:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo ERROR: Failed to start Tomcat
    echo ========================================
    echo.
    echo Common issues:
    echo - Port 8080 already in use
    echo - Maven plugin configuration error
    echo.
    echo Try checking if port 8080 is available:
    echo   netstat -ano ^| findstr :8080
    echo.
    pause
    exit /b 1
)

@REM Made with Bob
