@echo off
REM Fix QR Code and Restart Tomcat

echo ========================================
echo Fixing QR Code Issue and Restarting
echo ========================================
echo.

echo The QR code URL encoding has been fixed in TOTPUtil.java
echo.
echo Please stop Tomcat now (Ctrl+C in the other terminal)
echo.
pause

echo.
echo Rebuilding application with QR code fix...
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
echo QR Code fix applied!
echo.
echo Now starting Tomcat...
echo.

call mvn tomcat7:run

@REM Made with Bob
