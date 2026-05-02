@echo off
REM Rebuild application with new demo users

echo ========================================
echo Rebuilding MFA POC with Demo Users
echo ========================================
echo.

echo Please stop Tomcat if it's running (Ctrl+C in the other terminal)
echo.
pause

echo.
echo Cleaning previous build...
call mvn clean

echo.
echo Building with new demo users...
set "MAVEN_OPTS=-Dhttps.protocols=TLSv1.2"
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build Successful!
echo ========================================
echo.
echo New demo users added:
echo   - demo.user1 / Demo@123
echo   - demo.user2 / Demo@123
echo   - demo.user3 / Demo@123
echo.
echo Now start Tomcat:
echo   mvn tomcat7:run
echo.
echo Or use the quick start script:
echo   start-testing-simple.bat
echo.
pause

@REM Made with Bob
