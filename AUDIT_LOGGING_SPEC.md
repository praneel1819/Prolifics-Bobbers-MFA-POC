# Audit Logging Specification

## Overview

The MFA POC includes comprehensive audit logging to track all security-related activities. All MFA-related events are logged to a separate CSV file (`audit-log.csv`) with detailed information for compliance, security analysis, and troubleshooting.

## Audit Log Model

### AuditLog Class
```java
package com.prolifics.mfa.model;

public class AuditLog {
    private LocalDateTime timestamp;    // When the event occurred
    private String username;            // User performing the action
    private String action;              // Type of action (see Action Types below)
    private String status;              // SUCCESS or FAILED
    private String ipAddress;           // Client IP address
    private String details;             // Additional context
}
```

## Action Types

| Action Type | Description | When Logged |
|-------------|-------------|-------------|
| `LOGIN_ATTEMPT` | User attempts to login | Every login submission |
| `LOGIN_SUCCESS` | Successful authentication | Valid username/password |
| `LOGIN_FAILED` | Failed authentication | Invalid credentials or disabled account |
| `MFA_SETUP` | MFA setup initiated | QR code generated for first-time user |
| `MFA_VERIFY_SUCCESS` | Valid TOTP code | Correct 6-digit code entered |
| `MFA_VERIFY_FAILED` | Invalid TOTP code | Incorrect code or expired |
| `LOGOUT` | User logout | Session invalidated |

## CSV File Format

### File Location
```
src/main/resources/audit-log.csv
```

### CSV Structure
```csv
timestamp,username,action,status,ipAddress,details
2024-01-15T10:30:45,john.smith,LOGIN_ATTEMPT,SUCCESS,192.168.1.100,Valid credentials
2024-01-15T10:30:46,john.smith,MFA_SETUP,SUCCESS,192.168.1.100,QR code generated
2024-01-15T10:31:15,john.smith,MFA_VERIFY_SUCCESS,SUCCESS,192.168.1.100,Valid TOTP code
2024-01-15T10:45:30,jane.doe,LOGIN_ATTEMPT,SUCCESS,192.168.1.101,Valid credentials
2024-01-15T10:45:31,jane.doe,MFA_VERIFY_SUCCESS,SUCCESS,192.168.1.101,Valid TOTP code
2024-01-15T11:00:00,disabled.user,LOGIN_ATTEMPT,FAILED,192.168.1.102,Account disabled
2024-01-15T11:15:20,john.smith,LOGOUT,SUCCESS,192.168.1.100,User logged out
```

## AuditLogger Utility

### Class: AuditLogger
```java
package com.prolifics.mfa.util;

public class AuditLogger {
    private static final String AUDIT_LOG_FILE = "src/main/resources/audit-log.csv";
    private static final Object lock = new Object();
    
    // Core logging method
    public static void log(String username, String action, String status, 
                          String ipAddress, String details);
    
    // Convenience methods
    public static void logLoginAttempt(String username, boolean success, String ipAddress);
    public static void logMFASetup(String username, String ipAddress);
    public static void logMFAVerification(String username, boolean success, String ipAddress);
    public static void logLogout(String username, String ipAddress);
    
    // For testing and reporting
    public static List<AuditLog> getAuditLogs();
}
```

### Thread Safety
- Uses synchronized blocks for file writes
- Prevents concurrent write conflicts
- Ensures data integrity in multi-user scenarios

## Integration Points

### 1. LoginServlet
```java
// Log login attempt
AuditLogger.logLoginAttempt(username, false, request.getRemoteAddr());

// On successful authentication
AuditLogger.logLoginAttempt(username, true, request.getRemoteAddr());

// On disabled account
AuditLogger.log(username, "LOGIN_FAILED", "FAILED", 
                request.getRemoteAddr(), "Account disabled");
```

### 2. MFASetupServlet
```java
// Log MFA setup initiation
AuditLogger.logMFASetup(username, request.getRemoteAddr());

// Log setup completion
AuditLogger.log(username, "MFA_SETUP", "SUCCESS", 
                request.getRemoteAddr(), "MFA configured successfully");
```

### 3. MFAVerifyServlet
```java
// Log verification attempt
AuditLogger.logMFAVerification(username, isValid, request.getRemoteAddr());

// On failed verification
AuditLogger.log(username, "MFA_VERIFY_FAILED", "FAILED", 
                request.getRemoteAddr(), "Invalid TOTP code - attempt " + attemptCount);
```

### 4. LogoutServlet
```java
// Log logout event
AuditLogger.logLogout(username, request.getRemoteAddr());
```

## Security Features

### 1. Tamper Evidence
- Append-only operations
- Timestamp for chronological ordering
- No modification or deletion of existing entries

### 2. Data Captured
- **Timestamp**: Precise time of event (ISO 8601 format)
- **Username**: User identifier
- **Action**: Specific event type
- **Status**: Success or failure
- **IP Address**: Client location
- **Details**: Additional context

### 3. Privacy Considerations
- No sensitive data (passwords, TOTP codes) logged
- IP addresses for security analysis only
- Compliant with data protection requirements

## Use Cases

### 1. Security Monitoring
- Track failed login attempts
- Identify brute force attacks
- Monitor suspicious activity patterns
- Detect account compromise

### 2. Compliance & Auditing
- Demonstrate access controls
- Provide audit trail for compliance
- Support security assessments
- Meet regulatory requirements

### 3. Troubleshooting
- Debug authentication issues
- Verify MFA setup completion
- Track user activity timeline
- Identify system problems

### 4. Reporting
- Generate activity reports
- Analyze usage patterns
- Create security dashboards
- Support incident response

## Sample Audit Log Queries

### Find all failed login attempts
```
Filter: action = "LOGIN_FAILED"
```

### Track user activity
```
Filter: username = "john.smith"
Sort: timestamp DESC
```

### Identify multiple failed MFA attempts
```
Filter: action = "MFA_VERIFY_FAILED"
Group by: username
Having: count > 3
```

### Recent logout events
```
Filter: action = "LOGOUT"
Sort: timestamp DESC
Limit: 10
```

## Testing Requirements

### Unit Tests (AuditLoggerTest.java)
```java
- testLogEntry(): Verify log entry creation
- testThreadSafety(): Concurrent write operations
- testFileAppend(): Ensure append-only behavior
- testCSVFormat(): Validate CSV structure
- testGetAuditLogs(): Read and parse logs
```

### Integration Tests
```java
- testLoginAuditTrail(): Complete login flow logging
- testMFASetupAudit(): MFA setup event logging
- testMFAVerifyAudit(): Verification attempt logging
- testLogoutAudit(): Logout event logging
- testFailedAttempts(): Failed authentication logging
```

## Admin Guide Integration

### Viewing Audit Logs
1. Navigate to `src/main/resources/audit-log.csv`
2. Open with text editor or spreadsheet application
3. Filter and sort as needed
4. Export for analysis tools

### Log Rotation (Future Enhancement)
- Implement daily/weekly log rotation
- Archive old logs
- Compress archived files
- Maintain retention policy

### Analysis Tools (Future Enhancement)
- Web-based log viewer
- Real-time monitoring dashboard
- Alert system for suspicious activity
- Export to SIEM systems

## Performance Considerations

### File I/O Optimization
- Buffered writes for efficiency
- Minimal lock contention
- Asynchronous logging (future enhancement)

### Storage Management
- Monitor file size growth
- Implement log rotation
- Archive old entries
- Compress historical data

## Compliance & Standards

### Alignment with Standards
- **NIST 800-53**: AU-2 (Audit Events), AU-3 (Content of Audit Records)
- **ISO 27001**: A.12.4.1 (Event Logging)
- **PCI DSS**: Requirement 10 (Track and monitor all access)
- **GDPR**: Article 30 (Records of processing activities)

### Audit Trail Requirements Met
✅ Who: Username captured
✅ What: Action type recorded
✅ When: Timestamp logged
✅ Where: IP address tracked
✅ Result: Status (success/failure)
✅ Context: Details provided

## Documentation Deliverables

### 1. Admin Guide
- How to access audit logs
- Understanding log entries
- Common queries and filters
- Troubleshooting with logs

### 2. Security Documentation
- Audit logging capabilities
- Compliance alignment
- Security monitoring procedures
- Incident response using logs

### 3. API Documentation
- AuditLogger class methods
- Integration examples
- Best practices
- Error handling

## Future Enhancements

### Phase 2 Considerations
1. **Database Storage**: Move from CSV to database
2. **Real-time Monitoring**: Live dashboard
3. **Alerting**: Automated alerts for suspicious activity
4. **Log Rotation**: Automated archival
5. **SIEM Integration**: Export to security tools
6. **Advanced Analytics**: ML-based anomaly detection
7. **Compliance Reports**: Automated report generation

---

## Summary

The audit logging feature provides:
- ✅ Comprehensive tracking of all MFA activities
- ✅ Separate CSV file for audit trail
- ✅ Thread-safe operations
- ✅ Compliance-ready logging
- ✅ Security monitoring capabilities
- ✅ Troubleshooting support
- ✅ Tamper-evident design

This feature demonstrates tangible security and provides the foundation for compliance, monitoring, and incident response capabilities.