# MFA POC - Administrator Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Administrator Responsibilities](#administrator-responsibilities)
3. [User Management](#user-management)
4. [CSV File Management](#csv-file-management)
5. [Password Management](#password-management)
6. [MFA Management](#mfa-management)
7. [Audit Log Management](#audit-log-management)
8. [Security Administration](#security-administration)
9. [Backup and Recovery](#backup-and-recovery)
10. [Troubleshooting](#troubleshooting)
11. [Maintenance Tasks](#maintenance-tasks)
12. [Best Practices](#best-practices)

## Introduction

### Purpose
This guide provides administrators with comprehensive instructions for managing the MFA POC application, including user management, security configuration, and system maintenance.

### Administrator Role
As an administrator, you are responsible for:
- Managing user accounts
- Maintaining data files
- Monitoring security events
- Performing backups
- Troubleshooting issues
- Ensuring system availability

### Prerequisites
- Access to the application server
- File system permissions for CSV files
- Understanding of CSV file format
- Basic command-line knowledge
- Java development environment (for password generation)

## Administrator Responsibilities

### Daily Tasks
- [ ] Monitor audit logs for suspicious activity
- [ ] Review failed login attempts
- [ ] Check system availability
- [ ] Respond to user support requests

### Weekly Tasks
- [ ] Review user account status
- [ ] Analyze security metrics
- [ ] Check disk space usage
- [ ] Verify backup integrity

### Monthly Tasks
- [ ] Rotate audit logs
- [ ] Review and archive old logs
- [ ] Update documentation
- [ ] Security assessment

### As-Needed Tasks
- [ ] Add new users
- [ ] Reset user passwords
- [ ] Reset MFA configurations
- [ ] Disable/enable accounts
- [ ] Investigate security incidents

## User Management

### User Data Structure

Users are stored in `src/main/resources/users.csv`:

```csv
username,password,fullName,email,status,role,mfaSecret
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,ACTIVE,USER,
jane.doe,$2a$12$...,Jane Doe,jane.doe@prolifics.com,ACTIVE,USER,JBSWY3DPEHPK3PXP
```

### Field Descriptions

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| username | String | Yes | Unique identifier | 3-50 chars, alphanumeric + dots/underscores |
| password | String | Yes | BCrypt hash | 60 characters (BCrypt format) |
| fullName | String | Yes | Display name | 1-100 characters |
| email | String | Yes | Contact email | Valid email format |
| status | String | Yes | Account status | ACTIVE or DISABLED |
| role | String | Yes | User role | USER or ADMIN |
| mfaSecret | String | No | TOTP secret | 32 chars Base32 or empty |

### Adding a New User

#### Step 1: Generate Password Hash

```bash
# Navigate to project directory
cd /path/to/mfa-poc

# Compile password generator (if not already compiled)
javac GeneratePasswords.java

# Generate hash for new password
java GeneratePasswords "NewUserPassword123!"
```

**Output**:
```
Password: NewUserPassword123!
BCrypt Hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzOOj/lh.2
```

#### Step 2: Add User to CSV

1. **Stop the application** (to prevent file conflicts):
```bash
# Stop Tomcat
$CATALINA_HOME/bin/shutdown.sh
```

2. **Edit users.csv**:
```bash
# Open users.csv
nano src/main/resources/users.csv

# Or on Windows
notepad src\main\resources\users.csv
```

3. **Add new line** at the end:
```csv
new.user,$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzOOj/lh.2,New User,new.user@prolifics.com,ACTIVE,USER,
```

4. **Save the file**

5. **Restart the application**:
```bash
# Rebuild and restart
mvn clean package
$CATALINA_HOME/bin/startup.sh
```

#### Step 3: Verify User Creation

1. Navigate to login page
2. Try logging in with new credentials
3. Verify user can set up MFA
4. Check audit log for login attempt

### Modifying User Information

#### Change User's Full Name or Email

1. Stop the application
2. Edit `users.csv`
3. Locate the user's line
4. Modify the `fullName` or `email` field
5. Save the file
6. Restart the application

**Example**:
```csv
# Before
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,ACTIVE,USER,SECRET

# After
john.smith,$2a$12$...,John M. Smith,john.m.smith@prolifics.com,ACTIVE,USER,SECRET
```

#### Change User's Role

```csv
# Promote to ADMIN
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,ACTIVE,ADMIN,SECRET

# Demote to USER
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,ACTIVE,USER,SECRET
```

### Disabling a User Account

#### Temporary Disable

1. Stop the application
2. Edit `users.csv`
3. Change status from `ACTIVE` to `DISABLED`
4. Save and restart

**Example**:
```csv
# Before
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,ACTIVE,USER,SECRET

# After
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,DISABLED,USER,SECRET
```

**Result**: User cannot log in; receives "Account disabled" error

#### Permanent Removal

1. Stop the application
2. Edit `users.csv`
3. Delete the entire user line
4. Save and restart

**Warning**: This permanently removes the user. Consider disabling instead.

### Re-enabling a User Account

1. Stop the application
2. Edit `users.csv`
3. Change status from `DISABLED` to `ACTIVE`
4. Save and restart

## CSV File Management

### File Locations

**Development**:
```
src/main/resources/users.csv
src/main/resources/audit-log.csv
```

**Production (deployed)**:
```
$CATALINA_HOME/webapps/mfa-poc/WEB-INF/classes/users.csv
$CATALINA_HOME/webapps/mfa-poc/WEB-INF/classes/audit-log.csv
```

### File Permissions

#### Linux/macOS
```bash
# Set restrictive permissions
chmod 600 src/main/resources/users.csv
chmod 600 src/main/resources/audit-log.csv

# Set ownership
chown tomcat:tomcat src/main/resources/*.csv
```

#### Windows
```powershell
# Remove inheritance and set explicit permissions
icacls src\main\resources\users.csv /inheritance:r
icacls src\main\resources\users.csv /grant:r "SYSTEM:(F)"
icacls src\main\resources\users.csv /grant:r "Administrators:(F)"
```

### CSV File Format Rules

1. **Header Row**: First line must be field names
2. **No Empty Lines**: Remove blank lines
3. **Consistent Columns**: All rows must have same number of fields
4. **No Trailing Commas**: Don't add extra commas
5. **UTF-8 Encoding**: Save files in UTF-8 format
6. **Line Endings**: Use Unix (LF) or Windows (CRLF) consistently

### Validating CSV Files

#### Manual Validation

```bash
# Check for correct number of fields (should be 7 for users.csv)
awk -F',' '{print NF}' src/main/resources/users.csv

# All lines should output: 7

# Check for empty fields
grep ',,' src/main/resources/users.csv

# Should return nothing if all fields are populated
```

#### Automated Validation Script

```bash
#!/bin/bash
# validate-users.sh

CSV_FILE="src/main/resources/users.csv"
EXPECTED_FIELDS=7

echo "Validating $CSV_FILE..."

# Check if file exists
if [ ! -f "$CSV_FILE" ]; then
    echo "ERROR: File not found"
    exit 1
fi

# Check field count
while IFS= read -r line; do
    field_count=$(echo "$line" | awk -F',' '{print NF}')
    if [ "$field_count" -ne "$EXPECTED_FIELDS" ]; then
        echo "ERROR: Line has $field_count fields (expected $EXPECTED_FIELDS)"
        echo "Line: $line"
        exit 1
    fi
done < "$CSV_FILE"

echo "✓ Validation passed"
```

## Password Management

### Password Policy

**Requirements**:
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (!@#$%^&*)

### Generating Secure Passwords

#### Using GeneratePasswords.java

```bash
# Generate hash for a specific password
java GeneratePasswords "UserPassword123!"

# Generate multiple hashes
java GeneratePasswords "Password1!" "Password2!" "Password3!"
```

#### Using Online BCrypt Generators

**Warning**: Only use for development/testing. Never use online tools for production passwords.

1. Visit: https://bcrypt-generator.com/
2. Enter password
3. Select rounds: 12
4. Copy the generated hash

### Resetting User Passwords

#### Process

1. **Generate new password hash**:
```bash
java GeneratePasswords "NewPassword123!"
```

2. **Stop the application**

3. **Update users.csv**:
```csv
# Replace the password hash
john.smith,$2a$12$NEW_HASH_HERE,John Smith,john.smith@prolifics.com,ACTIVE,USER,SECRET
```

4. **Restart the application**

5. **Notify the user** of their new password

6. **Recommend password change** on first login (manual process)

### Password Security Best Practices

1. **Never store plain text passwords**
2. **Use BCrypt with work factor 12 or higher**
3. **Generate unique passwords for each user**
4. **Communicate passwords securely** (encrypted email, secure portal)
5. **Require password change** after reset
6. **Don't reuse passwords** across systems

## MFA Management

### Understanding MFA Secrets

**Format**: Base32-encoded string (32 characters)  
**Example**: `JBSWY3DPEHPK3PXP`

**Storage**: In `mfaSecret` field of users.csv

### MFA States

1. **Not Configured**: `mfaSecret` field is empty
   - User must set up MFA on first login
   
2. **Configured**: `mfaSecret` field contains Base32 string
   - User must verify TOTP code on each login

### Resetting User's MFA

#### When to Reset

- User lost access to authenticator app
- User got a new phone
- User accidentally deleted authenticator entry
- Security incident requires MFA reset

#### Reset Process

1. **Stop the application**

2. **Edit users.csv**:
```csv
# Before (MFA configured)
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,ACTIVE,USER,JBSWY3DPEHPK3PXP

# After (MFA reset)
john.smith,$2a$12$...,John Smith,john.smith@prolifics.com,ACTIVE,USER,
```

3. **Save the file**

4. **Restart the application**

5. **Notify the user**:
   - MFA has been reset
   - They must set up MFA again on next login
   - They will see the QR code setup page

### Forcing MFA Re-enrollment

Same process as resetting MFA - clear the `mfaSecret` field.

### Backup MFA Secrets

**Important**: Store MFA secrets securely for disaster recovery.

```bash
# Extract MFA secrets to secure file
awk -F',' 'NR>1 {print $1","$7}' src/main/resources/users.csv > mfa-secrets-backup.csv

# Encrypt the backup
gpg -c mfa-secrets-backup.csv

# Store encrypted file securely
mv mfa-secrets-backup.csv.gpg /secure/backup/location/

# Delete plain text file
rm mfa-secrets-backup.csv
```

## Audit Log Management

### Audit Log Structure

```csv
timestamp,username,action,status,ipAddress,details
2024-01-15T10:30:45.123456,john.smith,LOGIN_ATTEMPT,SUCCESS,192.168.1.100,Valid credentials
```

### Audit Event Types

| Action | Description | Status Values |
|--------|-------------|---------------|
| LOGIN_ATTEMPT | User submitted credentials | SUCCESS, FAILED |
| LOGIN_SUCCESS | Complete authentication | SUCCESS |
| LOGIN_FAILED | Authentication failed | FAILED |
| MFA_SETUP | MFA configuration initiated | SUCCESS |
| MFA_VERIFY_SUCCESS | Valid TOTP code | SUCCESS |
| MFA_VERIFY_FAILED | Invalid TOTP code | FAILED |
| LOGOUT | User logged out | SUCCESS |

### Viewing Audit Logs

#### View Recent Entries
```bash
# Last 20 entries
tail -20 src/main/resources/audit-log.csv

# Real-time monitoring
tail -f src/main/resources/audit-log.csv
```

#### Search for Specific User
```bash
# All events for john.smith
grep "john.smith" src/main/resources/audit-log.csv

# Failed login attempts for john.smith
grep "john.smith.*LOGIN_FAILED" src/main/resources/audit-log.csv
```

#### Search by Event Type
```bash
# All failed MFA verifications
grep "MFA_VERIFY_FAILED" src/main/resources/audit-log.csv

# All logout events
grep "LOGOUT" src/main/resources/audit-log.csv
```

#### Search by Date
```bash
# Events on specific date
grep "2024-01-15" src/main/resources/audit-log.csv

# Events in date range (requires more complex filtering)
awk -F',' '$1 >= "2024-01-15" && $1 <= "2024-01-20"' src/main/resources/audit-log.csv
```

### Analyzing Audit Logs

#### Count Events by Type
```bash
# Count each event type
awk -F',' 'NR>1 {print $3}' src/main/resources/audit-log.csv | sort | uniq -c
```

**Output**:
```
  45 LOGIN_ATTEMPT
  40 LOGIN_SUCCESS
   5 LOGIN_FAILED
  15 MFA_SETUP
  38 MFA_VERIFY_SUCCESS
   7 MFA_VERIFY_FAILED
  35 LOGOUT
```

#### Failed Login Report
```bash
# Users with failed logins
awk -F',' '$4=="FAILED" {print $2}' src/main/resources/audit-log.csv | sort | uniq -c | sort -rn
```

#### Activity by User
```bash
# Count events per user
awk -F',' 'NR>1 {print $2}' src/main/resources/audit-log.csv | sort | uniq -c | sort -rn
```

### Rotating Audit Logs

#### Monthly Rotation

```bash
#!/bin/bash
# rotate-audit-log.sh

DATE=$(date +%Y%m)
AUDIT_FILE="src/main/resources/audit-log.csv"
BACKUP_DIR="backups/audit-logs"

# Create backup directory
mkdir -p $BACKUP_DIR

# Stop application
$CATALINA_HOME/bin/shutdown.sh

# Copy current log
cp $AUDIT_FILE $BACKUP_DIR/audit-log-$DATE.csv

# Create new log with header
echo "timestamp,username,action,status,ipAddress,details" > $AUDIT_FILE

# Restart application
$CATALINA_HOME/bin/startup.sh

# Compress old log
gzip $BACKUP_DIR/audit-log-$DATE.csv

echo "Audit log rotated: audit-log-$DATE.csv.gz"
```

#### Automated Rotation (Cron)

```bash
# Add to crontab
# Rotate on 1st of each month at 2 AM
0 2 1 * * /path/to/rotate-audit-log.sh
```

### Archiving Old Logs

```bash
# Archive logs older than 90 days
find backups/audit-logs -name "*.csv.gz" -mtime +90 -exec mv {} archive/ \;

# Delete logs older than 1 year
find archive/ -name "*.csv.gz" -mtime +365 -delete
```

## Security Administration

### Monitoring Security Events

#### Daily Security Checks

```bash
#!/bin/bash
# daily-security-check.sh

AUDIT_LOG="src/main/resources/audit-log.csv"
TODAY=$(date +%Y-%m-%d)

echo "=== Security Report for $TODAY ==="
echo

echo "Failed Login Attempts:"
grep "$TODAY.*LOGIN_FAILED" $AUDIT_LOG | wc -l

echo
echo "Failed MFA Verifications:"
grep "$TODAY.*MFA_VERIFY_FAILED" $AUDIT_LOG | wc -l

echo
echo "Users with Multiple Failed Attempts:"
grep "$TODAY.*FAILED" $AUDIT_LOG | awk -F',' '{print $2}' | sort | uniq -c | sort -rn

echo
echo "Unique IP Addresses:"
grep "$TODAY" $AUDIT_LOG | awk -F',' '{print $5}' | sort -u | wc -l
```

#### Alert on Suspicious Activity

```bash
#!/bin/bash
# security-alerts.sh

AUDIT_LOG="src/main/resources/audit-log.csv"
ALERT_EMAIL="admin@prolifics.com"
THRESHOLD=5

# Check for users with excessive failed attempts
SUSPICIOUS=$(grep "$(date +%Y-%m-%d).*FAILED" $AUDIT_LOG | \
             awk -F',' '{print $2}' | sort | uniq -c | \
             awk -v t=$THRESHOLD '$1 > t {print $2}')

if [ ! -z "$SUSPICIOUS" ]; then
    echo "ALERT: Suspicious activity detected" | \
    mail -s "MFA POC Security Alert" $ALERT_EMAIL
fi
```

### IP Address Monitoring

#### View Login Sources
```bash
# Unique IP addresses
awk -F',' 'NR>1 {print $5}' src/main/resources/audit-log.csv | sort -u

# Login attempts by IP
awk -F',' '$3=="LOGIN_ATTEMPT" {print $5}' src/main/resources/audit-log.csv | \
sort | uniq -c | sort -rn
```

#### Detect Unusual Locations

```bash
# IPs with failed attempts
awk -F',' '$4=="FAILED" {print $5}' src/main/resources/audit-log.csv | \
sort | uniq -c | sort -rn
```

### Account Lockout Policy

**Current Implementation**: 5 failed MFA attempts per session

**To Implement Permanent Lockout**:
1. Monitor audit logs for repeated failures
2. Manually disable accounts with suspicious activity
3. Investigate before re-enabling

## Backup and Recovery

### Backup Strategy

#### What to Backup

1. **User Data**: `users.csv`
2. **Audit Logs**: `audit-log.csv`
3. **Application Configuration**: `web.xml`, `pom.xml`
4. **Application Code**: Entire source directory

#### Backup Frequency

- **Daily**: Audit logs
- **Weekly**: User data
- **Monthly**: Full application backup
- **Before Changes**: Always backup before modifications

### Backup Procedures

#### Manual Backup

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/mfa-poc"
DATE=$(date +%Y%m%d-%H%M%S)
BACKUP_PATH="$BACKUP_DIR/backup-$DATE"

# Create backup directory
mkdir -p $BACKUP_PATH

# Backup data files
cp src/main/resources/users.csv $BACKUP_PATH/
cp src/main/resources/audit-log.csv $BACKUP_PATH/

# Backup configuration
cp src/main/webapp/WEB-INF/web.xml $BACKUP_PATH/
cp pom.xml $BACKUP_PATH/

# Create archive
tar -czf $BACKUP_PATH.tar.gz -C $BACKUP_DIR backup-$DATE

# Remove uncompressed backup
rm -rf $BACKUP_PATH

echo "Backup created: $BACKUP_PATH.tar.gz"
```

#### Automated Backup (Cron)

```bash
# Daily backup at 2 AM
0 2 * * * /path/to/backup.sh

# Weekly full backup on Sunday at 3 AM
0 3 * * 0 /path/to/full-backup.sh
```

### Recovery Procedures

#### Restore User Data

```bash
# Stop application
$CATALINA_HOME/bin/shutdown.sh

# Restore from backup
cp /backups/mfa-poc/backup-20240115/users.csv src/main/resources/

# Verify file integrity
cat src/main/resources/users.csv

# Restart application
$CATALINA_HOME/bin/startup.sh
```

#### Restore Audit Logs

```bash
# Restore audit log
cp /backups/mfa-poc/backup-20240115/audit-log.csv src/main/resources/

# Verify restoration
tail -20 src/main/resources/audit-log.csv
```

#### Disaster Recovery

1. **Restore from latest backup**
2. **Verify data integrity**
3. **Test application functionality**
4. **Notify users if needed**
5. **Document incident**

## Troubleshooting

### Common Issues

#### Issue: User Cannot Login

**Diagnosis**:
```bash
# Check if user exists
grep "username" src/main/resources/users.csv

# Check user status
grep "username" src/main/resources/users.csv | awk -F',' '{print $5}'

# Check recent login attempts
grep "username.*LOGIN" src/main/resources/audit-log.csv | tail -5
```

**Solutions**:
- Verify username spelling
- Check if account is ACTIVE
- Reset password if needed
- Check audit log for error details

#### Issue: MFA Not Working

**Diagnosis**:
```bash
# Check if MFA is configured
grep "username" src/main/resources/users.csv | awk -F',' '{print $7}'

# Check MFA verification attempts
grep "username.*MFA_VERIFY" src/main/resources/audit-log.csv | tail -10
```

**Solutions**:
- Verify time synchronization
- Reset MFA configuration
- Check for repeated failures
- Verify secret key format

#### Issue: CSV File Corrupted

**Symptoms**:
- Application won't start
- Users cannot login
- Error in logs

**Recovery**:
```bash
# Restore from backup
cp /backups/mfa-poc/latest/users.csv src/main/resources/

# Validate CSV format
./validate-users.sh

# Restart application
```

## Maintenance Tasks

### Regular Maintenance Checklist

#### Daily
- [ ] Check application availability
- [ ] Review audit logs for anomalies
- [ ] Monitor disk space
- [ ] Respond to user issues

#### Weekly
- [ ] Backup user data
- [ ] Review security metrics
- [ ] Check for failed login patterns
- [ ] Update documentation

#### Monthly
- [ ] Rotate audit logs
- [ ] Archive old backups
- [ ] Review user accounts
- [ ] Security assessment
- [ ] Update dependencies

#### Quarterly
- [ ] Full system backup
- [ ] Disaster recovery test
- [ ] Security audit
- [ ] Performance review

### System Health Checks

```bash
#!/bin/bash
# health-check.sh

echo "=== MFA POC Health Check ==="
echo

# Check if application is running
if curl -s http://localhost:8080/mfa-poc/login > /dev/null; then
    echo "✓ Application is running"
else
    echo "✗ Application is not responding"
fi

# Check CSV files exist
if [ -f "src/main/resources/users.csv" ]; then
    echo "✓ users.csv exists"
    USER_COUNT=$(wc -l < src/main/resources/users.csv)
    echo "  Users: $((USER_COUNT - 1))"
else
    echo "✗ users.csv not found"
fi

if [ -f "src/main/resources/audit-log.csv" ]; then
    echo "✓ audit-log.csv exists"
    LOG_COUNT=$(wc -l < src/main/resources/audit-log.csv)
    echo "  Entries: $((LOG_COUNT - 1))"
else
    echo "✗ audit-log.csv not found"
fi

# Check disk space
DISK_USAGE=$(df -h . | awk 'NR==2 {print $5}' | sed 's/%//')
if [ $DISK_USAGE -lt 80 ]; then
    echo "✓ Disk space OK ($DISK_USAGE%)"
else
    echo "⚠ Disk space warning ($DISK_USAGE%)"
fi
```

## Best Practices

### Security Best Practices

1. **Principle of Least Privilege**
   - Grant minimum necessary permissions
   - Regularly review user roles
   - Disable unused accounts

2. **Regular Audits**
   - Review audit logs daily
   - Investigate anomalies promptly
   - Document security incidents

3. **Backup Strategy**
   - Automate backups
   - Test recovery procedures
   - Store backups securely
   - Keep multiple backup versions

4. **Password Management**
   - Never store plain text passwords
   - Use strong password policy
   - Rotate admin passwords regularly
   - Communicate passwords securely

5. **MFA Management**
   - Backup MFA secrets securely
   - Reset MFA when compromised
   - Monitor MFA failures
   - Educate users on MFA security

### Operational Best Practices

1. **Change Management**
   - Always backup before changes
   - Test changes in development first
   - Document all changes
   - Have rollback plan

2. **Monitoring**
   - Set up automated alerts
   - Review logs regularly
   - Track key metrics
   - Respond to incidents promptly

3. **Documentation**
   - Keep documentation current
   - Document procedures
   - Record incidents and resolutions
   - Maintain runbooks

4. **Communication**
   - Notify users of maintenance
   - Provide clear instructions
   - Respond to inquiries promptly
   - Maintain support channels

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Author**: Administration Team  
**Status**: Final

**Administrator Contact**: admin@prolifics.com