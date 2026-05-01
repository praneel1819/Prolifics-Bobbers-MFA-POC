# MFA POC - Design Document

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Component Design](#component-design)
4. [Data Models](#data-models)
5. [Sequence Diagrams](#sequence-diagrams)
6. [Database Schema](#database-schema)
7. [UI Design](#ui-design)
8. [Security Design](#security-design)

## Overview

### Purpose
This document provides detailed design specifications for the Multi-Factor Authentication (MFA) Proof of Concept (POC) application. The system demonstrates secure authentication using username/password combined with Time-based One-Time Password (TOTP) verification via Google Authenticator.

### Scope
- J2EE web application using Servlets and JSP
- CSV-based data persistence
- BCrypt password hashing
- TOTP-based MFA implementation
- Comprehensive audit logging
- Professional UI/UX design

### Design Principles
- **Security First**: All authentication mechanisms follow industry best practices
- **Simplicity**: POC focuses on core MFA functionality
- **Maintainability**: Clean code structure with clear separation of concerns
- **Testability**: Components designed for unit and integration testing
- **User Experience**: Intuitive interface with clear instructions

## System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                        Browser (Client)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ login.jsp│  │mfa-setup │  │mfa-verify│  │welcome.jsp│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/HTTPS
┌─────────────────────────────────────────────────────────────┐
│                    Apache Tomcat Server                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                   Web Layer (Servlets)                │  │
│  │  ┌────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐  │  │
│  │  │ Login  │ │MFASetup  │ │MFAVerify │ │ Logout  │  │  │
│  │  │Servlet │ │ Servlet  │ │ Servlet  │ │ Servlet │  │  │
│  │  └────────┘ └──────────┘ └──────────┘ └─────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                            │                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Business Logic Layer                     │  │
│  │  ┌────────────┐  ┌──────────┐  ┌──────────────┐    │  │
│  │  │ Password   │  │   TOTP   │  │ AuditLogger  │    │  │
│  │  │   Util     │  │   Util   │  │              │    │  │
│  │  └────────────┘  └──────────┘  └──────────────┘    │  │
│  └──────────────────────────────────────────────────────┘  │
│                            │                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                Data Access Layer                      │  │
│  │  ┌──────────────────────────────────────────────┐   │  │
│  │  │         CSVUserRepository                     │   │  │
│  │  └──────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    File System (CSV Files)                   │
│  ┌──────────────┐              ┌──────────────┐            │
│  │  users.csv   │              │audit-log.csv │            │
│  └──────────────┘              └──────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **Java**: JDK 11
- **Web Framework**: Java Servlets 4.0, JSP 2.3
- **Build Tool**: Maven 3.6+
- **Application Server**: Apache Tomcat 9.0
- **Security Libraries**:
  - BCrypt (jbcrypt 0.4) - Password hashing
  - java-otp 0.4.0 - TOTP implementation
  - Google ZXing 3.5.1 - QR code generation
- **Testing**:
  - JUnit 5.9.3 - Unit testing
  - JaCoCo - Code coverage
- **Frontend**: HTML5, CSS3, JSTL 1.2

## Component Design

### 1. Model Layer

#### User Model
```java
public class User {
    private String username;      // Unique identifier
    private String password;      // BCrypt hashed
    private String fullName;      // Display name
    private String email;         // Contact email
    private String status;        // ACTIVE/DISABLED
    private String role;          // USER/ADMIN
    private String mfaSecret;     // TOTP secret (nullable)
    
    // Business logic methods
    public boolean isActive()
    public boolean hasMFA()
}
```

**Responsibilities**:
- Encapsulate user data
- Provide status checking methods
- Support MFA configuration state

#### AuditLog Model
```java
public class AuditLog {
    private LocalDateTime timestamp;
    private String username;
    private String action;        // LOGIN_ATTEMPT, MFA_SETUP, etc.
    private String status;        // SUCCESS/FAILED
    private String ipAddress;
    private String details;
    
    // Serialization methods
    public String toCSVString()
    public static AuditLog fromCSVString(String csv)
}
```

**Responsibilities**:
- Capture security events
- Support CSV serialization
- Maintain audit trail integrity

### 2. Repository Layer

#### CSVUserRepository
```java
public class CSVUserRepository {
    private static final String CSV_FILE = "users.csv";
    private static final Object FILE_LOCK = new Object();
    
    public List<User> loadUsers()
    public void saveUsers(List<User> users)
    public User findByUsername(String username)
    public void updateMFASecret(String username, String secret)
}
```

**Design Patterns**:
- **Singleton**: Single instance per application
- **Repository Pattern**: Abstracts data access
- **Thread-Safe**: Synchronized file operations

**CSV Format**:
```
username,password,fullName,email,status,role,mfaSecret
john.smith,$2a$12$...,John Smith,john@example.com,ACTIVE,USER,
```

### 3. Utility Layer

#### PasswordUtil
```java
public class PasswordUtil {
    private static final int BCRYPT_STRENGTH = 12;
    
    public static String hashPassword(String plainPassword)
    public static boolean verifyPassword(String plain, String hashed)
    public static String generateSecurePassword()
}
```

**Security Features**:
- BCrypt with work factor 12
- Salt automatically generated
- Constant-time comparison

#### TOTPUtil
```java
public class TOTPUtil {
    private static final int SECRET_LENGTH = 32;
    private static final String ISSUER = "Prolifics";
    
    public static String generateSecret()
    public static String generateQRCodeURL(String username, String secret)
    public static boolean validateTOTP(String secret, String code)
    public static String getTOTPCode(String secret)
}
```

**TOTP Configuration**:
- Algorithm: SHA1
- Time step: 30 seconds
- Code length: 6 digits
- Time window: ±1 period (tolerance)

#### AuditLogger
```java
public class AuditLogger {
    private static final String AUDIT_FILE = "audit-log.csv";
    private static final Object FILE_LOCK = new Object();
    
    public static void log(String username, String action, 
                          String status, String ip, String details)
    public static void logLoginAttempt(String username, 
                                      boolean success, String ip)
    public static void logMFASetup(String username, String ip)
    public static void logMFAVerification(String username, 
                                         boolean success, String ip)
    public static void logLogout(String username, String ip)
}
```

**Audit Events**:
- LOGIN_ATTEMPT
- LOGIN_SUCCESS
- LOGIN_FAILED
- MFA_SETUP
- MFA_VERIFY_SUCCESS
- MFA_VERIFY_FAILED
- LOGOUT

### 4. Web Layer (Servlets)

#### LoginServlet
```java
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
}
```

**Flow**:
1. Validate credentials
2. Check user status
3. Determine MFA requirement
4. Route to appropriate page
5. Log all attempts

#### MFASetupServlet
```java
@WebServlet("/mfa-setup")
public class MFASetupServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
}
```

**Flow**:
1. Generate TOTP secret
2. Create QR code URL
3. Display setup page
4. Verify initial code
5. Save secret to CSV

#### MFAVerifyServlet
```java
@WebServlet("/mfa-verify")
public class MFAVerifyServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
}
```

**Flow**:
1. Display verification form
2. Validate TOTP code
3. Track failed attempts
4. Grant or deny access
5. Log verification result

#### LogoutServlet
```java
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
}
```

**Flow**:
1. Log logout event
2. Invalidate session
3. Clear attributes
4. Redirect to login

## Data Models

### User Entity
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | String | Yes | Unique identifier, primary key |
| password | String | Yes | BCrypt hashed password |
| fullName | String | Yes | User's full name for display |
| email | String | Yes | Contact email address |
| status | String | Yes | ACTIVE or DISABLED |
| role | String | Yes | USER or ADMIN |
| mfaSecret | String | No | Base32 encoded TOTP secret |

### AuditLog Entity
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| timestamp | LocalDateTime | Yes | ISO 8601 format |
| username | String | Yes | User performing action |
| action | String | Yes | Event type |
| status | String | Yes | SUCCESS or FAILED |
| ipAddress | String | Yes | Client IP address |
| details | String | No | Additional context |

## Sequence Diagrams

### First-Time Login with MFA Setup
```
User          Browser        LoginServlet    MFASetupServlet    CSVRepository    AuditLogger
 │               │                │                 │                 │              │
 │──Enter creds─>│                │                 │                 │              │
 │               │──POST /login──>│                 │                 │              │
 │               │                │──findUser()────>│                 │              │
 │               │                │<─User (no MFA)──│                 │              │
 │               │                │──────────────────────────────────>│              │
 │               │                │                 │                 │  log LOGIN   │
 │               │<─Redirect to───│                 │                 │              │
 │               │   mfa-setup    │                 │                 │              │
 │               │                │                 │                 │              │
 │               │──GET /mfa-setup────────────────>│                 │              │
 │               │                │                 │──generateSecret()              │
 │               │                │                 │──generateQR()                  │
 │               │                │                 │──────────────────────────────>│
 │               │                │                 │                 │  log SETUP   │
 │               │<─QR Code page──────────────────│                 │              │
 │<─Display QR───│                │                 │                 │              │
 │               │                │                 │                 │              │
 │──Scan & enter─>│                │                 │                 │              │
 │    TOTP code  │──POST /mfa-setup────────────────>│                 │              │
 │               │                │                 │──validateTOTP()                │
 │               │                │                 │──updateMFA()───>│              │
 │               │                │                 │──────────────────────────────>│
 │               │                │                 │                 │  log SUCCESS │
 │               │<─Redirect to welcome─────────────│                 │              │
 │<─Welcome page─│                │                 │                 │              │
```

### Returning User Login with MFA
```
User          Browser        LoginServlet    MFAVerifyServlet   CSVRepository    AuditLogger
 │               │                │                 │                 │              │
 │──Enter creds─>│                │                 │                 │              │
 │               │──POST /login──>│                 │                 │              │
 │               │                │──findUser()────>│                 │              │
 │               │                │<─User (has MFA)─│                 │              │
 │               │                │──────────────────────────────────>│              │
 │               │                │                 │                 │  log LOGIN   │
 │               │<─Redirect to───│                 │                 │              │
 │               │   mfa-verify   │                 │                 │              │
 │               │                │                 │                 │              │
 │               │──GET /mfa-verify────────────────>│                 │              │
 │               │<─Verify page────────────────────│                 │              │
 │<─Enter code───│                │                 │                 │              │
 │               │                │                 │                 │              │
 │──Enter TOTP──>│                │                 │                 │              │
 │               │──POST /mfa-verify───────────────>│                 │              │
 │               │                │                 │──validateTOTP()                │
 │               │                │                 │──────────────────────────────>│
 │               │                │                 │                 │  log VERIFY  │
 │               │<─Redirect to welcome─────────────│                 │              │
 │<─Welcome page─│                │                 │                 │              │
```

### Failed MFA Verification
```
User          Browser        MFAVerifyServlet    AuditLogger
 │               │                 │                 │
 │──Wrong code──>│                 │                 │
 │               │──POST /mfa-verify──────────────>│
 │               │                 │──validateTOTP() (fails)
 │               │                 │──────────────────────────────>│
 │               │                 │                 │  log FAILED  │
 │               │<─Error message──│                 │              │
 │<─Try again────│                 │                 │              │
 │               │                 │                 │              │
 │──Wrong code──>│                 │                 │              │
 │   (5 times)   │──POST (5th)────>│                 │              │
 │               │                 │──────────────────────────────>│
 │               │                 │                 │  log LOCKED  │
 │               │<─Redirect login─│                 │              │
 │<─Session end──│                 │                 │              │
```

## Database Schema

### CSV File Structure

#### users.csv
```csv
username,password,fullName,email,status,role,mfaSecret
john.smith,$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzOOj/lh.2,John Smith,john.smith@prolifics.com,ACTIVE,USER,
jane.doe,$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi,Jane Doe,jane.doe@prolifics.com,ACTIVE,USER,JBSWY3DPEHPK3PXP
admin.user,$2a$12$8kSC8fQIRw3fjzOER2H6YOe8Yf5lzZf7aIcnwZ7bK5vGlMWWxHDK6,Admin User,admin@prolifics.com,ACTIVE,ADMIN,JBSWY3DPEHPK3PXQ
disabled.user,$2a$12$DpLXVJK8V1dNRckqn7S.qeKe3P5ul8KPA/Alr0bFBq2/p6rZiOWoS,Disabled User,disabled@prolifics.com,DISABLED,USER,
bob.wilson,$2a$12$wm3gg6V8fHMF4JXJKBNxaOBYg0Nu.urF0VzNTwCAq5K4nU2JgU.Ka,Bob Wilson,bob.wilson@prolifics.com,ACTIVE,USER,
```

**Field Constraints**:
- username: Unique, 3-50 characters
- password: BCrypt hash (60 characters)
- fullName: 1-100 characters
- email: Valid email format
- status: ACTIVE or DISABLED
- role: USER or ADMIN
- mfaSecret: Base32 string (32 characters) or empty

#### audit-log.csv
```csv
timestamp,username,action,status,ipAddress,details
2024-01-15T10:30:45.123456,john.smith,LOGIN_ATTEMPT,SUCCESS,192.168.1.100,Valid credentials
2024-01-15T10:30:46.234567,john.smith,MFA_SETUP,SUCCESS,192.168.1.100,QR code generated for MFA setup
2024-01-15T10:31:15.345678,john.smith,MFA_VERIFY_SUCCESS,SUCCESS,192.168.1.100,Initial TOTP verification successful
2024-01-15T10:31:15.456789,john.smith,LOGIN_SUCCESS,SUCCESS,192.168.1.100,User logged in with MFA
```

**Field Constraints**:
- timestamp: ISO 8601 format with microseconds
- username: Matches user in users.csv
- action: Predefined action types
- status: SUCCESS or FAILED
- ipAddress: IPv4 or IPv6 format
- details: Free text, max 500 characters

## UI Design

### Design System

#### Color Palette
```css
/* Primary Colors */
--primary-blue: #0066cc;      /* Prolifics brand blue */
--primary-dark: #004999;      /* Darker blue for hover */
--primary-light: #3385d6;     /* Lighter blue for accents */

/* Neutral Colors */
--white: #ffffff;
--light-gray: #f8f9fa;
--medium-gray: #e9ecef;
--dark-gray: #333333;
--text-gray: #666666;

/* Status Colors */
--success-green: #28a745;
--error-red: #dc3545;
--warning-yellow: #ffc107;
--info-blue: #17a2b8;

/* Background */
--gradient-start: #667eea;
--gradient-end: #764ba2;
```

#### Typography
```css
/* Font Family */
font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;

/* Font Sizes */
--font-xs: 12px;
--font-sm: 14px;
--font-md: 16px;
--font-lg: 18px;
--font-xl: 24px;
--font-xxl: 32px;

/* Font Weights */
--weight-normal: 400;
--weight-medium: 500;
--weight-bold: 700;
```

#### Spacing
```css
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 16px;
--spacing-lg: 24px;
--spacing-xl: 32px;
--spacing-xxl: 48px;
```

### Page Layouts

#### Login Page (login.jsp)
```
┌─────────────────────────────────────────────────────┐
│ [Logo]                                              │
│                                                     │
│                                                     │
│              ┌─────────────────────┐               │
│              │   MFA POC Login     │               │
│              │                     │               │
│              │  Username: [_____]  │               │
│              │  Password: [_____]  │               │
│              │                     │               │
│              │    [Login Button]   │               │
│              │                     │               │
│              │  [Error Message]    │               │
│              └─────────────────────┘               │
│                                                     │
└─────────────────────────────────────────────────────┘
```

#### MFA Setup Page (mfa-setup.jsp)
```
┌─────────────────────────────────────────────────────┐
│ [Logo]                                              │
│                                                     │
│         Set Up Multi-Factor Authentication          │
│                                                     │
│              ┌─────────────────┐                   │
│              │                 │                   │
│              │   [QR Code]     │                   │
│              │                 │                   │
│              └─────────────────┘                   │
│                                                     │
│  1. Download Google Authenticator                   │
│  2. Scan the QR code above                         │
│  3. Enter the 6-digit code below                   │
│                                                     │
│         Enter Code: [______]                        │
│                                                     │
│            [Continue Button]                        │
│                                                     │
│  Manual Entry: JBSWY3DPEHPK3PXP                    │
│                                                     │
└─────────────────────────────────────────────────────┘
```

#### MFA Verify Page (mfa-verify.jsp)
```
┌─────────────────────────────────────────────────────┐
│ [Logo]                                              │
│                                                     │
│         Two-Factor Authentication                   │
│                                                     │
│         Welcome back, John Smith!                   │
│                                                     │
│    Enter the 6-digit code from your                │
│         authenticator app                           │
│                                                     │
│              [______]                               │
│                                                     │
│            [Verify Button]                          │
│                                                     │
│         [Error Message]                             │
│                                                     │
│         [Back to Login]                             │
│                                                     │
└─────────────────────────────────────────────────────┘
```

#### Welcome Page (welcome.jsp)
```
┌─────────────────────────────────────────────────────┐
│ [Logo]                          [Logout Button]     │
│                                                     │
│         Welcome, John Smith!                        │
│                                                     │
│  ┌───────────────────────────────────────────┐    │
│  │         User Information                   │    │
│  │                                            │    │
│  │  Full Name: John Smith                     │    │
│  │  Email: john.smith@prolifics.com          │    │
│  │  Role: USER                                │    │
│  │  Status: ACTIVE                            │    │
│  │  MFA Enabled: Yes                          │    │
│  │                                            │    │
│  └───────────────────────────────────────────┘    │
│                                                     │
│  You have successfully logged in with              │
│  multi-factor authentication.                      │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Responsive Design

#### Breakpoints
```css
/* Mobile */
@media (max-width: 576px) {
    .container { width: 100%; padding: 16px; }
    .card { width: 100%; }
}

/* Tablet */
@media (min-width: 577px) and (max-width: 768px) {
    .container { width: 90%; }
    .card { width: 500px; }
}

/* Desktop */
@media (min-width: 769px) {
    .container { width: 80%; max-width: 1200px; }
    .card { width: 400px; }
}
```

## Security Design

### Authentication Flow

#### Password Security
1. **Hashing**: BCrypt with work factor 12
2. **Salt**: Automatically generated per password
3. **Storage**: Only hashed passwords stored
4. **Validation**: Constant-time comparison

#### TOTP Security
1. **Secret Generation**: Cryptographically secure random
2. **Secret Storage**: Base32 encoded in CSV
3. **Time Synchronization**: 30-second window
4. **Tolerance**: ±1 time period
5. **Code Length**: 6 digits

### Session Management

#### Session Attributes
```java
// Authentication state
session.setAttribute("user", userObject);
session.setAttribute("authenticated", true);
session.setAttribute("mfaVerified", true);

// Temporary MFA setup
session.setAttribute("mfaSecret", secret);
session.setAttribute("qrCodeURL", url);
```

#### Session Security
- **Timeout**: 30 minutes of inactivity
- **HttpOnly**: Cookies not accessible via JavaScript
- **Secure**: HTTPS-only cookies (production)
- **Regeneration**: New session ID after login
- **Invalidation**: Complete cleanup on logout

### Input Validation

#### Server-Side Validation
```java
// Username validation
if (username == null || username.trim().isEmpty()) {
    error = "Username is required";
}

// Password validation
if (password == null || password.length() < 8) {
    error = "Password must be at least 8 characters";
}

// TOTP code validation
if (!totpCode.matches("\\d{6}")) {
    error = "Invalid code format";
}
```

#### XSS Prevention
```jsp
<!-- Use JSTL c:out to escape output -->
<c:out value="${user.fullName}" />

<!-- Or JSP expression with escapeXml -->
${fn:escapeXml(errorMessage)}
```

### Audit Logging

#### Events Logged
1. **LOGIN_ATTEMPT**: Every login attempt (success/failure)
2. **LOGIN_SUCCESS**: Successful authentication
3. **LOGIN_FAILED**: Failed authentication with reason
4. **MFA_SETUP**: MFA configuration initiated
5. **MFA_VERIFY_SUCCESS**: Valid TOTP code
6. **MFA_VERIFY_FAILED**: Invalid TOTP code with attempt count
7. **LOGOUT**: User logout

#### Log Format
```
timestamp,username,action,status,ipAddress,details
2024-01-15T10:30:45.123456,john.smith,LOGIN_ATTEMPT,SUCCESS,192.168.1.100,Valid credentials
```

#### Security Considerations
- **Immutable**: Append-only log file
- **Thread-Safe**: Synchronized writes
- **Tamper-Evident**: Chronological timestamps
- **Complete**: All security events captured

### Threat Mitigation

#### Brute Force Protection
- Rate limiting: Max 5 MFA attempts
- Account lockout: Session invalidation after 5 failures
- Audit logging: All failed attempts recorded

#### Session Hijacking
- Session regeneration after login
- HttpOnly and Secure cookies
- Session timeout
- IP address logging

#### SQL Injection
- N/A: CSV-based storage
- Input validation still applied

#### XSS (Cross-Site Scripting)
- Output escaping in JSP
- JSTL c:out usage
- Content Security Policy headers

#### CSRF (Cross-Site Request Forgery)
- Session-based authentication
- Same-origin policy
- POST for state-changing operations

## Performance Considerations

### File I/O Optimization
- **Buffered Streams**: BufferedReader/Writer for CSV
- **Synchronized Access**: Prevent concurrent file corruption
- **Minimal Reads**: Cache user data in session
- **Append-Only**: Efficient audit log writes

### Session Management
- **Lazy Loading**: Load user data only when needed
- **Attribute Cleanup**: Remove temporary attributes after use
- **Timeout**: Automatic cleanup of inactive sessions

### QR Code Generation
- **On-Demand**: Generate only during MFA setup
- **In-Memory**: No file storage required
- **URL-Based**: Lightweight data URL format

## Extensibility

### Future Enhancements
1. **Database Migration**: Replace CSV with RDBMS
2. **Email Notifications**: Alert on security events
3. **Backup Codes**: Alternative MFA method
4. **Admin Dashboard**: User management UI
5. **API Endpoints**: RESTful API for integration
6. **Mobile App**: Native authenticator app
7. **Biometric Auth**: Fingerprint/Face ID support
8. **SSO Integration**: SAML/OAuth support

### Design for Change
- **Repository Pattern**: Easy database swap
- **Utility Classes**: Reusable security functions
- **Configuration**: Externalized settings
- **Modular Design**: Independent components

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Author**: Development Team  
**Status**: Final