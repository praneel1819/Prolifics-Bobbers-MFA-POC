# MFA POC - API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Base URL](#base-url)
3. [Authentication Flow](#authentication-flow)
4. [Endpoints](#endpoints)
5. [Request/Response Formats](#requestresponse-formats)
6. [Error Handling](#error-handling)
7. [Session Management](#session-management)
8. [Examples](#examples)

## Overview

This document describes the HTTP endpoints provided by the MFA POC application. The application uses traditional servlet-based architecture with session-based authentication.

### API Characteristics
- **Protocol**: HTTP/HTTPS
- **Architecture**: Servlet-based (not RESTful)
- **Authentication**: Session-based with cookies
- **Content Type**: `application/x-www-form-urlencoded` for forms
- **Response Type**: HTML (JSP rendered)

### Conventions
- All endpoints use standard HTTP methods (GET, POST)
- Form submissions use POST method
- Redirects use HTTP 302 status code
- Session cookies are HttpOnly and Secure (in production)

## Base URL

### Development
```
http://localhost:8080/mfa-poc
```

### Production
```
https://your-domain.com/mfa-poc
```

## Authentication Flow

### Flow Diagram
```
1. User → /login (GET)
2. User → /login (POST with credentials)
3. Server validates credentials
4. If valid and no MFA:
   → /mfa-setup (GET)
   → /mfa-setup (POST with TOTP)
   → /welcome
5. If valid and has MFA:
   → /mfa-verify (GET)
   → /mfa-verify (POST with TOTP)
   → /welcome
6. User → /logout
   → /login
```

## Endpoints

### 1. Login Endpoint

#### GET /login
Display the login form.

**URL**: `/login`  
**Method**: `GET`  
**Authentication**: None required

**Query Parameters**: None

**Response**: HTML login page

**Example**:
```http
GET /mfa-poc/login HTTP/1.1
Host: localhost:8080
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<!DOCTYPE html>
<html>
<head>
    <title>MFA POC - Login</title>
    ...
</head>
<body>
    <form method="post" action="login">
        <input type="text" name="username" />
        <input type="password" name="password" />
        <button type="submit">Login</button>
    </form>
</body>
</html>
```

---

#### POST /login
Authenticate user with username and password.

**URL**: `/login`  
**Method**: `POST`  
**Authentication**: None required  
**Content-Type**: `application/x-www-form-urlencoded`

**Form Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | Yes | User's username |
| password | String | Yes | User's password (plain text) |

**Success Response**:
- **Code**: 302 Found
- **Location**: `/mfa-setup` (if no MFA configured) or `/mfa-verify` (if MFA configured)
- **Session**: Creates session with user object

**Error Response**:
- **Code**: 200 OK
- **Body**: Login page with error message

**Validation Rules**:
- Username: Not empty, 3-50 characters
- Password: Not empty, minimum 8 characters
- User status: Must be ACTIVE

**Example Request**:
```http
POST /mfa-poc/login HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=john.smith&password=SecurePass123!
```

**Success Response**:
```http
HTTP/1.1 302 Found
Location: /mfa-poc/mfa-setup
Set-Cookie: JSESSIONID=ABC123...; Path=/mfa-poc; HttpOnly
```

**Error Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<!DOCTYPE html>
<html>
<body>
    <div class="error">Invalid username or password</div>
    <form method="post" action="login">
        ...
    </form>
</body>
</html>
```

**Audit Log Entry**:
```csv
2024-01-15T10:30:45.123456,john.smith,LOGIN_ATTEMPT,SUCCESS,192.168.1.100,Valid credentials
```

---

### 2. MFA Setup Endpoint

#### GET /mfa-setup
Display MFA setup page with QR code.

**URL**: `/mfa-setup`  
**Method**: `GET`  
**Authentication**: Session required (authenticated user)

**Prerequisites**:
- User must be logged in
- User must not have MFA configured

**Response**: HTML page with QR code and instructions

**Session Attributes Set**:
- `mfaSecret`: Generated TOTP secret
- `qrCodeURL`: Data URL for QR code image

**Example**:
```http
GET /mfa-poc/mfa-setup HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=ABC123...
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<!DOCTYPE html>
<html>
<body>
    <h2>Set Up Multi-Factor Authentication</h2>
    <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..." />
    <p>Secret: JBSWY3DPEHPK3PXP</p>
    <form method="post" action="mfa-setup">
        <input type="text" name="totpCode" maxlength="6" />
        <button type="submit">Continue</button>
    </form>
</body>
</html>
```

**Audit Log Entry**:
```csv
2024-01-15T10:30:46.234567,john.smith,MFA_SETUP,SUCCESS,192.168.1.100,QR code generated for MFA setup
```

---

#### POST /mfa-setup
Verify initial TOTP code and complete MFA setup.

**URL**: `/mfa-setup`  
**Method**: `POST`  
**Authentication**: Session required  
**Content-Type**: `application/x-www-form-urlencoded`

**Form Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| totpCode | String | Yes | 6-digit TOTP code from authenticator app |

**Success Response**:
- **Code**: 302 Found
- **Location**: `/welcome`
- **Action**: Saves MFA secret to user record

**Error Response**:
- **Code**: 200 OK
- **Body**: MFA setup page with error message

**Validation Rules**:
- TOTP code: Exactly 6 digits
- Code must be valid for current time window

**Example Request**:
```http
POST /mfa-poc/mfa-setup HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=ABC123...
Content-Type: application/x-www-form-urlencoded

totpCode=123456
```

**Success Response**:
```http
HTTP/1.1 302 Found
Location: /mfa-poc/welcome
```

**Error Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<!DOCTYPE html>
<html>
<body>
    <div class="error">Invalid verification code. Please try again.</div>
    <img src="data:image/png;base64,..." />
    <form method="post" action="mfa-setup">
        ...
    </form>
</body>
</html>
```

**Audit Log Entries**:
```csv
2024-01-15T10:31:15.345678,john.smith,MFA_VERIFY_SUCCESS,SUCCESS,192.168.1.100,Initial TOTP verification successful
2024-01-15T10:31:15.456789,john.smith,LOGIN_SUCCESS,SUCCESS,192.168.1.100,User logged in with MFA
```

---

### 3. MFA Verify Endpoint

#### GET /mfa-verify
Display TOTP verification form.

**URL**: `/mfa-verify`  
**Method**: `GET`  
**Authentication**: Session required (authenticated user with MFA)

**Prerequisites**:
- User must be logged in
- User must have MFA configured

**Response**: HTML page with TOTP input form

**Example**:
```http
GET /mfa-poc/mfa-verify HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=ABC123...
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<!DOCTYPE html>
<html>
<body>
    <h2>Two-Factor Authentication</h2>
    <p>Welcome back, John Smith!</p>
    <p>Enter the 6-digit code from your authenticator app</p>
    <form method="post" action="mfa-verify">
        <input type="text" name="totpCode" maxlength="6" />
        <button type="submit">Verify</button>
    </form>
</body>
</html>
```

---

#### POST /mfa-verify
Verify TOTP code for returning user.

**URL**: `/mfa-verify`  
**Method**: `POST`  
**Authentication**: Session required  
**Content-Type**: `application/x-www-form-urlencoded`

**Form Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| totpCode | String | Yes | 6-digit TOTP code from authenticator app |

**Success Response**:
- **Code**: 302 Found
- **Location**: `/welcome`
- **Session**: Sets `mfaVerified` attribute to true

**Error Response**:
- **Code**: 200 OK
- **Body**: MFA verify page with error message
- **Note**: After 5 failed attempts, session is invalidated

**Validation Rules**:
- TOTP code: Exactly 6 digits
- Code must be valid for current time window
- Maximum 5 attempts per session

**Example Request**:
```http
POST /mfa-poc/mfa-verify HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=ABC123...
Content-Type: application/x-www-form-urlencoded

totpCode=654321
```

**Success Response**:
```http
HTTP/1.1 302 Found
Location: /mfa-poc/welcome
```

**Error Response (Attempt 1-4)**:
```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<!DOCTYPE html>
<html>
<body>
    <div class="error">Invalid verification code. Please try again. (Attempt 1 of 5)</div>
    <form method="post" action="mfa-verify">
        ...
    </form>
</body>
</html>
```

**Error Response (Attempt 5)**:
```http
HTTP/1.1 302 Found
Location: /mfa-poc/login?error=Too+many+failed+attempts
```

**Audit Log Entries**:

Success:
```csv
2024-01-15T10:45:31.567890,jane.doe,MFA_VERIFY_SUCCESS,SUCCESS,192.168.1.101,TOTP verification successful
2024-01-15T10:45:31.678901,jane.doe,LOGIN_SUCCESS,SUCCESS,192.168.1.101,User logged in with MFA
```

Failure:
```csv
2024-01-15T10:46:15.789012,jane.doe,MFA_VERIFY_FAILED,FAILED,192.168.1.101,Invalid TOTP code (attempt 1 of 5)
```

---

### 4. Welcome Endpoint

#### GET /welcome
Display welcome page for authenticated users.

**URL**: `/welcome`  
**Method**: `GET`  
**Authentication**: Session required (fully authenticated with MFA)

**Prerequisites**:
- User must be logged in
- MFA must be verified

**Response**: HTML welcome page with user information

**Example**:
```http
GET /mfa-poc/welcome HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=ABC123...
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<!DOCTYPE html>
<html>
<body>
    <h1>Welcome, John Smith!</h1>
    <div class="user-info">
        <p>Email: john.smith@prolifics.com</p>
        <p>Role: USER</p>
        <p>Status: ACTIVE</p>
        <p>MFA Enabled: Yes</p>
    </div>
    <a href="logout">Logout</a>
</body>
</html>
```

**Unauthorized Access**:
```http
HTTP/1.1 302 Found
Location: /mfa-poc/login
```

---

### 5. Logout Endpoint

#### GET /logout
Logout user and invalidate session.

**URL**: `/logout`  
**Method**: `GET`  
**Authentication**: Session required

**Response**:
- **Code**: 302 Found
- **Location**: `/login`
- **Action**: Invalidates session and clears all attributes

**Example**:
```http
GET /mfa-poc/logout HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=ABC123...
```

**Response**:
```http
HTTP/1.1 302 Found
Location: /mfa-poc/login
Set-Cookie: JSESSIONID=; Path=/mfa-poc; Max-Age=0
```

**Audit Log Entry**:
```csv
2024-01-15T11:15:20.890123,john.smith,LOGOUT,SUCCESS,192.168.1.100,User logged out successfully
```

---

## Request/Response Formats

### Form Data Format
All POST requests use `application/x-www-form-urlencoded`:

```
username=john.smith&password=SecurePass123!
```

### HTML Response Format
All responses are HTML pages rendered by JSP:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Page Title</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <!-- Page content -->
</body>
</html>
```

### Error Message Format
Errors are displayed as HTML elements:

```html
<div class="error-message">
    Error description here
</div>
```

## Error Handling

### HTTP Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful page load or form submission with validation errors |
| 302 | Found | Redirect after successful operation |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Session expired or not authenticated |
| 403 | Forbidden | Access denied (disabled account) |
| 404 | Not Found | Invalid URL |
| 500 | Internal Server Error | Server-side error |

### Error Scenarios

#### 1. Invalid Credentials
**Endpoint**: POST /login  
**Error**: "Invalid username or password"  
**HTTP Code**: 200 (displays error on login page)  
**Audit Log**: LOGIN_FAILED

#### 2. Disabled Account
**Endpoint**: POST /login  
**Error**: "Your account has been disabled. Please contact administrator."  
**HTTP Code**: 200  
**Audit Log**: LOGIN_FAILED with details

#### 3. Invalid TOTP Code
**Endpoint**: POST /mfa-verify  
**Error**: "Invalid verification code. Please try again. (Attempt X of 5)"  
**HTTP Code**: 200  
**Audit Log**: MFA_VERIFY_FAILED

#### 4. Too Many Failed Attempts
**Endpoint**: POST /mfa-verify  
**Error**: "Too many failed attempts. Please login again."  
**HTTP Code**: 302 (redirect to login)  
**Audit Log**: MFA_VERIFY_FAILED with attempt count

#### 5. Session Expired
**Endpoint**: Any authenticated endpoint  
**Error**: Redirect to login  
**HTTP Code**: 302  
**Audit Log**: None

#### 6. Missing Required Field
**Endpoint**: POST /login, /mfa-setup, /mfa-verify  
**Error**: "All fields are required"  
**HTTP Code**: 200  
**Audit Log**: None

## Session Management

### Session Attributes

| Attribute | Type | Description | Set By | Cleared By |
|-----------|------|-------------|--------|------------|
| user | User | Authenticated user object | LoginServlet | LogoutServlet |
| authenticated | Boolean | User credentials verified | LoginServlet | LogoutServlet |
| mfaVerified | Boolean | MFA verification complete | MFASetupServlet, MFAVerifyServlet | LogoutServlet |
| mfaSecret | String | Temporary TOTP secret | MFASetupServlet | MFASetupServlet (after verification) |
| qrCodeURL | String | QR code data URL | MFASetupServlet | MFASetupServlet (after verification) |
| failedAttempts | Integer | Failed MFA attempts | MFAVerifyServlet | MFAVerifyServlet (on success) |

### Session Lifecycle

```
1. User visits /login
   → No session or new session created

2. User submits credentials
   → Session attributes set:
     - user: User object
     - authenticated: true

3. User completes MFA
   → Session attributes updated:
     - mfaVerified: true
     - mfaSecret: removed
     - qrCodeURL: removed

4. User accesses /welcome
   → Session validated:
     - authenticated: true
     - mfaVerified: true

5. User clicks logout
   → Session invalidated
   → All attributes cleared
```

### Session Timeout
- **Default**: 30 minutes of inactivity
- **Configuration**: web.xml `<session-timeout>`
- **Behavior**: Automatic redirect to login page

### Session Security
- **HttpOnly**: Cookies not accessible via JavaScript
- **Secure**: HTTPS-only in production
- **SameSite**: Strict (prevents CSRF)
- **Regeneration**: New session ID after login

## Examples

### Example 1: First-Time Login Flow

#### Step 1: Display Login Page
```http
GET /mfa-poc/login HTTP/1.1
Host: localhost:8080

→ 200 OK (login.jsp)
```

#### Step 2: Submit Credentials
```http
POST /mfa-poc/login HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=john.smith&password=SecurePass123!

→ 302 Found
→ Location: /mfa-poc/mfa-setup
→ Set-Cookie: JSESSIONID=...
```

#### Step 3: Display MFA Setup
```http
GET /mfa-poc/mfa-setup HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...

→ 200 OK (mfa-setup.jsp with QR code)
```

#### Step 4: Verify TOTP Code
```http
POST /mfa-poc/mfa-setup HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...
Content-Type: application/x-www-form-urlencoded

totpCode=123456

→ 302 Found
→ Location: /mfa-poc/welcome
```

#### Step 5: Display Welcome Page
```http
GET /mfa-poc/welcome HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...

→ 200 OK (welcome.jsp)
```

---

### Example 2: Returning User Login Flow

#### Step 1: Submit Credentials
```http
POST /mfa-poc/login HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=jane.doe&password=Welcome2024!

→ 302 Found
→ Location: /mfa-poc/mfa-verify
→ Set-Cookie: JSESSIONID=...
```

#### Step 2: Display MFA Verify
```http
GET /mfa-poc/mfa-verify HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...

→ 200 OK (mfa-verify.jsp)
```

#### Step 3: Verify TOTP Code
```http
POST /mfa-poc/mfa-verify HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...
Content-Type: application/x-www-form-urlencoded

totpCode=654321

→ 302 Found
→ Location: /mfa-poc/welcome
```

---

### Example 3: Failed Login Attempt

```http
POST /mfa-poc/login HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=john.smith&password=WrongPassword

→ 200 OK
→ Body: login.jsp with error message
→ Audit: LOGIN_FAILED
```

---

### Example 4: Failed MFA Verification

```http
POST /mfa-poc/mfa-verify HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...
Content-Type: application/x-www-form-urlencoded

totpCode=000000

→ 200 OK
→ Body: mfa-verify.jsp with error
→ Audit: MFA_VERIFY_FAILED (attempt 1 of 5)
```

---

### Example 5: Logout

```http
GET /mfa-poc/logout HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...

→ 302 Found
→ Location: /mfa-poc/login
→ Set-Cookie: JSESSIONID=; Max-Age=0
→ Audit: LOGOUT
```

---

## Testing with cURL

### Login
```bash
curl -i -X POST http://localhost:8080/mfa-poc/login \
  -d "username=john.smith&password=SecurePass123!" \
  -c cookies.txt
```

### MFA Setup
```bash
curl -i -X GET http://localhost:8080/mfa-poc/mfa-setup \
  -b cookies.txt
```

### MFA Verify
```bash
curl -i -X POST http://localhost:8080/mfa-poc/mfa-verify \
  -d "totpCode=123456" \
  -b cookies.txt
```

### Logout
```bash
curl -i -X GET http://localhost:8080/mfa-poc/logout \
  -b cookies.txt
```

---

## Integration Guidelines

### Session-Based Integration
1. Maintain session cookies across requests
2. Follow redirects (302 responses)
3. Parse HTML responses for error messages
4. Handle session timeout gracefully

### Security Considerations
1. Always use HTTPS in production
2. Validate SSL certificates
3. Store session cookies securely
4. Implement request timeout
5. Log all API interactions

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Author**: API Team  
**Status**: Final