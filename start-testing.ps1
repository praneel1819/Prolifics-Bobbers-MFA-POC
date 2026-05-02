# Quick Start Script for MFA POC Manual Testing
# This script uses Maven's embedded Tomcat for quick testing without installing Tomcat

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MFA POC - Quick Start Testing Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is installed
try {
    $mvnVersion = mvn -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
} catch {
    Write-Host "ERROR: Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven first" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
} catch {
    Write-Host "ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java first" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "[1/3] Checking environment..." -ForegroundColor Green
Write-Host ""
java -version
Write-Host ""
mvn -version
Write-Host ""

Write-Host "[2/3] Building application..." -ForegroundColor Green
Write-Host ""
$env:MAVEN_OPTS = "-Dhttps.protocols=TLSv1.2"
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Write-Host "Check the error messages above" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Build Successful!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "[3/3] Starting embedded Tomcat server..." -ForegroundColor Green
Write-Host ""
Write-Host "Application will be available at:" -ForegroundColor Cyan
Write-Host "  http://localhost:8080/mfa-poc/" -ForegroundColor Yellow
Write-Host ""
Write-Host "Test Accounts:" -ForegroundColor Cyan
Write-Host "  Username: john.smith   Password: Test@123" -ForegroundColor White
Write-Host "  Username: jane.doe     Password: Test@123" -ForegroundColor White
Write-Host "  Username: admin.user   Password: Admin@123" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Start embedded Tomcat
mvn tomcat7:run

# Made with Bob
