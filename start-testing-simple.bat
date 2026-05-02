@echo off
REM Simplified Quick Start Script for MFA POC Manual Testing

echo ========================================
echo MFA POC - Simple Start Script
echo ========================================
echo.

echo Step 1: Setting Maven options...
set "MAVEN_OPTS=-Dhttps.protocols=TLSv1.2"
echo Done.
echo.

echo Step 2: Building application...
echo This may take a few minutes on first run...
echo.

mvn clean package -DskipTests

echo.
echo Step 3: Starting Tomcat...
echo.
echo Application will be at: http://localhost:8080/mfa-poc/
echo.
echo Test Account: john.smith / Test@123
echo.
echo Press Ctrl+C to stop
echo.

mvn tomcat7:run

pause

@REM Made with Bob
