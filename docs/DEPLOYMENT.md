# MFA POC - Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Building the Application](#building-the-application)
5. [Deployment](#deployment)
6. [Verification](#verification)
7. [Troubleshooting](#troubleshooting)
8. [Maintenance](#maintenance)
9. [Backup and Recovery](#backup-and-recovery)
10. [Security Hardening](#security-hardening)

## Prerequisites

### System Requirements

#### Hardware
- **CPU**: 2+ cores recommended
- **RAM**: 4GB minimum, 8GB recommended
- **Disk Space**: 500MB for application and dependencies
- **Network**: Internet connection for Maven dependencies

#### Software
- **Operating System**: 
  - Windows 10/11
  - Linux (Ubuntu 20.04+, CentOS 7+)
  - macOS 10.15+
- **Java Development Kit (JDK)**: 11 or higher
- **Apache Maven**: 3.6.0 or higher
- **Apache Tomcat**: 9.0.x
- **Git**: 2.x (for source code management)

### Required Tools

#### 1. Java JDK 11+
**Windows**:
```powershell
# Download from Oracle or use OpenJDK
# Verify installation
java -version
javac -version
```

**Linux**:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-11-jdk

# CentOS/RHEL
sudo yum install java-11-openjdk-devel

# Verify
java -version
```

**macOS**:
```bash
# Using Homebrew
brew install openjdk@11

# Verify
java -version
```

#### 2. Apache Maven
**Windows**:
```powershell
# Download from https://maven.apache.org/download.cgi
# Extract to C:\Program Files\Apache\maven
# Add to PATH: C:\Program Files\Apache\maven\bin

# Verify
mvn -version
```

**Linux**:
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# Verify
mvn -version
```

**macOS**:
```bash
# Using Homebrew
brew install maven

# Verify
mvn -version
```

#### 3. Apache Tomcat 9
**Download**:
```bash
# Download from https://tomcat.apache.org/download-90.cgi
# Extract to desired location
```

**Windows**:
```powershell
# Extract to C:\Program Files\Apache\Tomcat9
# Set CATALINA_HOME environment variable
```

**Linux/macOS**:
```bash
# Extract to /opt/tomcat9
sudo tar xzvf apache-tomcat-9.*.tar.gz -C /opt/
sudo mv /opt/apache-tomcat-9.* /opt/tomcat9

# Set permissions
sudo chmod +x /opt/tomcat9/bin/*.sh
```

### Environment Variables

#### Windows
```powershell
# Set JAVA_HOME
setx JAVA_HOME "C:\Program Files\Java\jdk-11"

# Set MAVEN_HOME
setx MAVEN_HOME "C:\Program Files\Apache\maven"

# Set CATALINA_HOME
setx CATALINA_HOME "C:\Program Files\Apache\Tomcat9"

# Update PATH
setx PATH "%PATH%;%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%CATALINA_HOME%\bin"
```

#### Linux/macOS
```bash
# Add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export MAVEN_HOME=/usr/share/maven
export CATALINA_HOME=/opt/tomcat9
export PATH=$PATH:$JAVA_HOME/bin:$MAVEN_HOME/bin:$CATALINA_HOME/bin

# Reload
source ~/.bashrc
```

## Installation

### 1. Clone Repository
```bash
# Clone the project
git clone https://github.com/prolifics/mfa-poc.git
cd mfa-poc

# Or download and extract ZIP
# https://github.com/prolifics/mfa-poc/archive/main.zip
```

### 2. Verify Project Structure
```bash
mfa-poc/
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── resources/
│   │   └── webapp/
│   └── test/
├── pom.xml
├── README.md
└── docs/
```

### 3. Install Dependencies
```bash
# Download all Maven dependencies
mvn clean install

# This will:
# - Download all required libraries
# - Compile source code
# - Run unit tests
# - Package WAR file
```

### 4. Verify Installation
```bash
# Check if WAR file is created
ls -l target/mfa-poc.war

# Expected output:
# -rw-r--r-- 1 user group 15728640 Jan 15 10:30 target/mfa-poc.war
```

## Configuration

### 1. User Data Configuration

#### users.csv
Location: `src/main/resources/users.csv`

```csv
username,password,fullName,email,status,role,mfaSecret
john.smith,$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzOOj/lh.2,John Smith,john.smith@prolifics.com,ACTIVE,USER,
jane.doe,$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi,Jane Doe,jane.doe@prolifics.com,ACTIVE,USER,JBSWY3DPEHPK3PXP
```

**Field Descriptions**:
- `username`: Unique identifier (3-50 characters)
- `password`: BCrypt hashed password (use GeneratePasswords.java)
- `fullName`: User's display name
- `email`: Contact email address
- `status`: ACTIVE or DISABLED
- `role`: USER or ADMIN
- `mfaSecret`: TOTP secret (empty for new users)

#### Generate Password Hashes
```bash
# Compile password generator
javac GeneratePasswords.java

# Generate hash for a password
java GeneratePasswords "YourPassword123!"

# Output:
# Password: YourPassword123!
# BCrypt Hash: $2a$12$...
```

### 2. Audit Log Configuration

#### audit-log.csv
Location: `src/main/resources/audit-log.csv`

```csv
timestamp,username,action,status,ipAddress,details
```

**Note**: This file is auto-created if it doesn't exist. Initial header row is required.

### 3. Application Configuration

#### web.xml
Location: `src/main/webapp/WEB-INF/web.xml`

**Session Timeout** (default: 30 minutes):
```xml
<session-config>
    <session-timeout>30</session-timeout>
</session-config>
```

**Servlet Mappings**:
```xml
<servlet-mapping>
    <servlet-name>LoginServlet</servlet-name>
    <url-pattern>/login</url-pattern>
</servlet-mapping>
```

### 4. Tomcat Configuration

#### server.xml
Location: `$CATALINA_HOME/conf/server.xml`

**HTTP Connector** (default port 8080):
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
```

**HTTPS Connector** (production):
```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/keystore.jks"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

#### context.xml
Location: `$CATALINA_HOME/conf/context.xml`

```xml
<Context>
    <!-- Session cookie configuration -->
    <CookieProcessor className="org.apache.tomcat.util.http.Rfc6265CookieProcessor"
                     sameSiteCookies="strict" />
</Context>
```

## Building the Application

### Development Build
```bash
# Clean and compile
mvn clean compile

# Run unit tests
mvn test

# Package WAR file
mvn package

# Skip tests (faster)
mvn package -DskipTests
```

### Production Build
```bash
# Full build with tests and reports
mvn clean verify

# Generate code coverage report
mvn clean test jacoco:report

# View coverage report
# Open target/site/jacoco/index.html in browser
```

### Build Profiles

#### Development Profile
```bash
mvn clean package -Pdev
```

#### Production Profile
```bash
mvn clean package -Pprod
```

### Build Output
```
target/
├── mfa-poc.war              # Deployable WAR file
├── classes/                 # Compiled classes
├── test-classes/            # Test classes
├── surefire-reports/        # Test reports
└── site/jacoco/             # Coverage reports
```

## Deployment

### Method 1: Maven Tomcat Plugin (Development)

#### Start Application
```bash
# Start embedded Tomcat
mvn tomcat7:run

# Application will be available at:
# http://localhost:8080/mfa-poc
```

#### Stop Application
```bash
# Press Ctrl+C in terminal
```

### Method 2: Standalone Tomcat (Production)

#### Deploy WAR File
```bash
# Copy WAR to Tomcat webapps directory
cp target/mfa-poc.war $CATALINA_HOME/webapps/

# Or on Windows
copy target\mfa-poc.war "%CATALINA_HOME%\webapps\"
```

#### Start Tomcat
**Linux/macOS**:
```bash
# Start Tomcat
$CATALINA_HOME/bin/startup.sh

# Check logs
tail -f $CATALINA_HOME/logs/catalina.out
```

**Windows**:
```powershell
# Start Tomcat
%CATALINA_HOME%\bin\startup.bat

# Check logs
type %CATALINA_HOME%\logs\catalina.out
```

#### Stop Tomcat
**Linux/macOS**:
```bash
$CATALINA_HOME/bin/shutdown.sh
```

**Windows**:
```powershell
%CATALINA_HOME%\bin\shutdown.bat
```

### Method 3: Tomcat Manager (Web Interface)

#### Enable Manager App
Edit `$CATALINA_HOME/conf/tomcat-users.xml`:
```xml
<tomcat-users>
    <role rolename="manager-gui"/>
    <role rolename="manager-script"/>
    <user username="admin" password="admin123" roles="manager-gui,manager-script"/>
</tomcat-users>
```

#### Deploy via Manager
1. Navigate to `http://localhost:8080/manager/html`
2. Login with credentials
3. Scroll to "WAR file to deploy"
4. Select `target/mfa-poc.war`
5. Click "Deploy"

### Method 4: Docker (Optional)

#### Dockerfile
```dockerfile
FROM tomcat:9-jdk11

# Copy WAR file
COPY target/mfa-poc.war /usr/local/tomcat/webapps/

# Expose port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
```

#### Build and Run
```bash
# Build Docker image
docker build -t mfa-poc:latest .

# Run container
docker run -d -p 8080:8080 --name mfa-poc mfa-poc:latest

# View logs
docker logs -f mfa-poc

# Stop container
docker stop mfa-poc
```

## Verification

### 1. Check Application Status

#### Verify Deployment
```bash
# Check if WAR is deployed
ls -l $CATALINA_HOME/webapps/mfa-poc.war
ls -l $CATALINA_HOME/webapps/mfa-poc/

# Check Tomcat logs
tail -f $CATALINA_HOME/logs/catalina.out
```

#### Expected Log Output
```
INFO: Deploying web application archive [mfa-poc.war]
INFO: Deployment of web application archive [mfa-poc.war] has finished in [1,234] ms
```

### 2. Access Application

#### Login Page
```
URL: http://localhost:8080/mfa-poc/login
Expected: Login form displayed
```

#### Test Credentials
```
Username: john.smith
Password: SecurePass123!
```

### 3. Verify Functionality

#### Test Checklist
- [ ] Login page loads
- [ ] Valid credentials accepted
- [ ] Invalid credentials rejected
- [ ] MFA setup page displays QR code
- [ ] TOTP verification works
- [ ] Welcome page shows user info
- [ ] Logout works correctly
- [ ] Audit log entries created

### 4. Health Check Script

#### health-check.sh
```bash
#!/bin/bash

# Check if application is running
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/mfa-poc/login)

if [ $HTTP_CODE -eq 200 ]; then
    echo "✓ Application is running"
    exit 0
else
    echo "✗ Application is not responding (HTTP $HTTP_CODE)"
    exit 1
fi
```

## Troubleshooting

### Common Issues

#### 1. Port Already in Use
**Error**: `Address already in use: bind`

**Solution**:
```bash
# Find process using port 8080
# Linux/macOS
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change Tomcat port in server.xml
```

#### 2. Java Version Mismatch
**Error**: `Unsupported class file major version`

**Solution**:
```bash
# Check Java version
java -version

# Ensure JDK 11 or higher
# Update JAVA_HOME if needed
```

#### 3. Maven Build Fails
**Error**: `Failed to execute goal`

**Solution**:
```bash
# Clean Maven cache
mvn clean

# Force update dependencies
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

#### 4. CSV File Not Found
**Error**: `FileNotFoundException: users.csv`

**Solution**:
```bash
# Ensure CSV files exist in resources
ls -l src/main/resources/users.csv
ls -l src/main/resources/audit-log.csv

# Rebuild application
mvn clean package
```

#### 5. QR Code Not Displaying
**Error**: QR code image broken

**Solution**:
- Check browser console for errors
- Verify ZXing library in WAR file
- Check servlet logs for exceptions
- Ensure Base64 encoding is correct

#### 6. Session Timeout Issues
**Error**: User logged out unexpectedly

**Solution**:
```xml
<!-- Increase session timeout in web.xml -->
<session-config>
    <session-timeout>60</session-timeout>
</session-config>
```

#### 7. TOTP Validation Fails
**Error**: "Invalid TOTP code" for correct code

**Solution**:
- Check server time synchronization
- Verify time zone settings
- Ensure 30-second time window
- Check for clock skew

```bash
# Sync system time (Linux)
sudo ntpdate pool.ntp.org

# Windows
w32tm /resync
```

### Log Files

#### Application Logs
```bash
# Tomcat catalina.out
tail -f $CATALINA_HOME/logs/catalina.out

# Application-specific logs
tail -f $CATALINA_HOME/logs/localhost.*.log
```

#### Audit Logs
```bash
# View audit log
cat src/main/resources/audit-log.csv

# Monitor in real-time
tail -f src/main/resources/audit-log.csv
```

### Debug Mode

#### Enable Debug Logging
Edit `$CATALINA_HOME/conf/logging.properties`:
```properties
# Set log level to FINE
.level = FINE
com.prolifics.mfa.level = FINE
```

#### Restart Tomcat
```bash
$CATALINA_HOME/bin/shutdown.sh
$CATALINA_HOME/bin/startup.sh
```

## Maintenance

### Regular Tasks

#### 1. Log Rotation
```bash
# Rotate audit logs (monthly)
mv audit-log.csv audit-log-$(date +%Y%m).csv
echo "timestamp,username,action,status,ipAddress,details" > audit-log.csv
```

#### 2. User Management
```bash
# Add new user
# 1. Generate password hash
java GeneratePasswords "NewPassword123!"

# 2. Add to users.csv
echo "new.user,$2a$12$...,New User,new@example.com,ACTIVE,USER," >> users.csv

# 3. Restart application
```

#### 3. Security Updates
```bash
# Update dependencies
mvn versions:display-dependency-updates

# Update specific dependency
mvn versions:use-latest-versions

# Rebuild and redeploy
mvn clean package
```

#### 4. Performance Monitoring
```bash
# Check Tomcat threads
curl http://localhost:8080/manager/status/all

# Monitor memory usage
jstat -gc <tomcat-pid> 1000

# Check application metrics
# View audit-log.csv for usage patterns
```

### Backup Procedures

#### 1. Data Backup
```bash
# Backup user data
cp src/main/resources/users.csv backups/users-$(date +%Y%m%d).csv

# Backup audit logs
cp src/main/resources/audit-log.csv backups/audit-log-$(date +%Y%m%d).csv
```

#### 2. Configuration Backup
```bash
# Backup Tomcat configuration
tar -czf tomcat-config-$(date +%Y%m%d).tar.gz $CATALINA_HOME/conf/

# Backup application configuration
tar -czf app-config-$(date +%Y%m%d).tar.gz src/main/webapp/WEB-INF/
```

#### 3. Automated Backup Script
```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/mfa-poc"
DATE=$(date +%Y%m%d)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup data files
cp src/main/resources/users.csv $BACKUP_DIR/users-$DATE.csv
cp src/main/resources/audit-log.csv $BACKUP_DIR/audit-log-$DATE.csv

# Compress old backups (older than 30 days)
find $BACKUP_DIR -name "*.csv" -mtime +30 -exec gzip {} \;

echo "Backup completed: $DATE"
```

### Recovery Procedures

#### 1. Restore User Data
```bash
# Stop application
$CATALINA_HOME/bin/shutdown.sh

# Restore from backup
cp backups/users-20240115.csv src/main/resources/users.csv

# Restart application
$CATALINA_HOME/bin/startup.sh
```

#### 2. Restore Audit Logs
```bash
# Restore audit log
cp backups/audit-log-20240115.csv src/main/resources/audit-log.csv
```

## Security Hardening

### 1. Production Configuration

#### Remove Default Applications
```bash
# Remove Tomcat default apps
rm -rf $CATALINA_HOME/webapps/ROOT
rm -rf $CATALINA_HOME/webapps/docs
rm -rf $CATALINA_HOME/webapps/examples
rm -rf $CATALINA_HOME/webapps/host-manager
rm -rf $CATALINA_HOME/webapps/manager
```

#### Disable Directory Listing
Edit `$CATALINA_HOME/conf/web.xml`:
```xml
<servlet>
    <servlet-name>default</servlet-name>
    <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
    <init-param>
        <param-name>listings</param-name>
        <param-value>false</param-value>
    </init-param>
</servlet>
```

### 2. SSL/TLS Configuration

#### Generate Keystore
```bash
# Generate self-signed certificate (development)
keytool -genkey -alias tomcat -keyalg RSA -keystore keystore.jks

# For production, use CA-signed certificate
```

#### Configure HTTPS
Edit `$CATALINA_HOME/conf/server.xml`:
```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/keystore.jks"
                     certificateKeystorePassword="changeit"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

### 3. File Permissions

#### Linux/macOS
```bash
# Set restrictive permissions
chmod 600 src/main/resources/users.csv
chmod 600 src/main/resources/audit-log.csv
chmod 700 $CATALINA_HOME/conf

# Set Tomcat ownership
chown -R tomcat:tomcat $CATALINA_HOME
```

### 4. Firewall Configuration

#### Linux (iptables)
```bash
# Allow only HTTP/HTTPS
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8443 -j ACCEPT
```

#### Windows Firewall
```powershell
# Allow Tomcat
netsh advfirewall firewall add rule name="Tomcat HTTP" dir=in action=allow protocol=TCP localport=8080
netsh advfirewall firewall add rule name="Tomcat HTTPS" dir=in action=allow protocol=TCP localport=8443
```

### 5. Security Headers

#### Add to web.xml
```xml
<filter>
    <filter-name>httpHeaderSecurity</filter-name>
    <filter-class>org.apache.catalina.filters.HttpHeaderSecurityFilter</filter-class>
    <init-param>
        <param-name>antiClickJackingOption</param-name>
        <param-value>SAMEORIGIN</param-value>
    </init-param>
</filter>
```

## Performance Tuning

### JVM Options

#### catalina.sh / catalina.bat
```bash
# Add to JAVA_OPTS
JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx2048m"
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
```

### Tomcat Tuning

#### server.xml
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           maxThreads="200"
           minSpareThreads="25"
           maxConnections="10000"
           acceptCount="100" />
```

## Monitoring

### Application Metrics
- Login success/failure rate
- MFA setup completion rate
- Average response time
- Active sessions
- Audit log growth

### System Metrics
- CPU usage
- Memory usage
- Disk I/O
- Network traffic
- Thread count

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Author**: DevOps Team  
**Status**: Final