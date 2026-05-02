# Manual Testing Checklist - MFA POC

**Project:** Multi-Factor Authentication Proof of Concept  
**Version:** 1.0.0  
**Date:** May 2, 2026

---

## Pre-Testing Setup

### Environment Validation
- [x] Java 8+ installed (Java 21 confirmed)
- [x] Maven installed (Maven 3.9.11 confirmed)
- [x] Project builds successfully
- [x] WAR file created (target/mfa-poc.war)
- [ ] **Tomcat installed OR using Maven embedded Tomcat**
- [ ] **Google Authenticator app installed on mobile device**
- [x] Test user data configured
- [x] Audit log initialized

### Quick Start Options

#### Option 1: Maven Embedded Tomcat (Recommended for Testing)
```powershell
# Run the quick start script
.\start-testing.ps1

# Or manually:
mvn tomcat7:run
```

#### Option 2: Tomcat Installation
- [ ] Download Tomcat 9 from https://tomcat.apache.org/download-90.cgi
- [ ] Install or extract Tomcat
- [ ] Copy WAR file to webapps directory
- [ ] Start Tomcat service

---

## Test Execution Checklist

### Test Scenario 1: First-Time Login (MFA Setup)
**User:** john.smith / Test@123

- [ ] Navigate to http://localhost:8080/mfa-poc/
- [ ] Login page displays correctly
- [ ] Enter credentials and click "Sign In"
- [ ] Redirected to MFA Setup page
- [ ] QR code displays correctly
- [ ] Secret key is visible and readable
- [ ] Scan QR code with Google Authenticator
- [ ] Account appears in authenticator app
- [ ] Enter 6-digit code from app
- [ ] Click "Verify and Complete Setup"
- [ ] Redirected to Welcome page
- [ ] Welcome message shows correct name
- [ ] Audit log contains LOGIN_SUCCESS, MFA_SETUP, MFA_VERIFY_SUCCESS

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

### Test Scenario 2: Login with Existing MFA
**User:** john.smith / Test@123 (after setup)

- [ ] Click "Logout" button
- [ ] Redirected to login page
- [ ] Enter credentials and click "Sign In"
- [ ] Redirected to MFA Verification page (NOT setup)
- [ ] Open Google Authenticator app
- [ ] Enter current 6-digit code
- [ ] Click "Verify"
- [ ] Redirected to Welcome page
- [ ] Session is active
- [ ] Audit log shows MFA_VERIFY_SUCCESS

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

### Test Scenario 3: Invalid Login Attempts

#### 3.1: Wrong Password
**User:** john.smith / WrongPassword123

- [ ] Navigate to login page
- [ ] Enter username and wrong password
- [ ] Click "Sign In"
- [ ] Error message: "Invalid username or password"
- [ ] Remain on login page
- [ ] Audit log shows LOGIN_FAILED

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 3.2: Non-Existent User
**User:** nonexistent / Test@123

- [ ] Navigate to login page
- [ ] Enter non-existent username
- [ ] Click "Sign In"
- [ ] Error message: "Invalid username or password"
- [ ] Remain on login page
- [ ] Audit log shows LOGIN_FAILED

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 3.3: Disabled Account
**User:** disabled.user / Test@123

- [ ] Navigate to login page
- [ ] Enter disabled user credentials
- [ ] Click "Sign In"
- [ ] Error message: "Account is disabled"
- [ ] Remain on login page
- [ ] Audit log shows LOGIN_FAILED with "Account disabled"

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 3.4: Empty Credentials

- [ ] Navigate to login page
- [ ] Leave username and password empty
- [ ] Click "Sign In"
- [ ] Browser validation prevents submission OR error message displayed

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

### Test Scenario 4: Invalid MFA Codes

#### 4.1: Wrong TOTP Code
**User:** john.smith / Test@123 (with MFA setup)

- [ ] Login with valid credentials
- [ ] On MFA Verification page, enter: 000000
- [ ] Click "Verify"
- [ ] Error message: "Invalid verification code"
- [ ] Remain on verification page
- [ ] Audit log shows MFA_VERIFY_FAILED

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 4.2: Expired TOTP Code

- [ ] Login with valid credentials
- [ ] Wait for code to change in Google Authenticator
- [ ] Enter OLD code (from previous 30-second window)
- [ ] Click "Verify"
- [ ] Code rejected OR accepted within ±30 second tolerance

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 4.3: Multiple Failed Attempts

- [ ] Login with valid credentials
- [ ] Enter wrong code 3 times
- [ ] After 3 attempts: "Too many failed attempts" message
- [ ] Session invalidated
- [ ] Redirected to login page
- [ ] Audit log shows multiple MFA_VERIFY_FAILED entries

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 4.4: Empty TOTP Code

- [ ] Login with valid credentials
- [ ] Leave verification code field empty
- [ ] Click "Verify"
- [ ] Browser validation prevents submission OR error message displayed

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

### Test Scenario 5: Session Management

#### 5.1: Session Timeout

- [ ] Login successfully and reach Welcome page
- [ ] Wait for 30 minutes (or configured timeout)
- [ ] Try to navigate or refresh page
- [ ] Redirected to login page
- [ ] Message: "Session expired. Please login again."
- [ ] Audit log shows SESSION_TIMEOUT

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 5.2: Direct URL Access Without Login

- [ ] Logout or open new browser session
- [ ] Try to access: http://localhost:8080/mfa-poc/welcome.jsp
- [ ] Redirected to login page
- [ ] Cannot access protected pages without authentication

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 5.3: Back Button After Logout

- [ ] Login and reach Welcome page
- [ ] Click Logout
- [ ] Click browser's Back button
- [ ] Redirected to login page
- [ ] Cannot access Welcome page after logout
- [ ] Session is invalidated

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

### Test Scenario 6: QR Code and Secret Key

#### 6.1: Manual Secret Entry
**User:** jane.doe / Test@123

- [ ] Login with new user
- [ ] On MFA Setup page, copy the secret key
- [ ] In Google Authenticator, choose "Enter a setup key"
- [ ] Enter account name: jane.doe
- [ ] Paste secret key
- [ ] Select "Time-based"
- [ ] Complete setup with generated code
- [ ] Successfully setup and login

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

#### 6.2: QR Code Scan
**User:** admin.user / Admin@123

- [ ] Login with new user
- [ ] Use mobile device to scan QR code
- [ ] Account appears in authenticator app
- [ ] Complete setup with generated code
- [ ] Successfully setup and login

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

### Test Scenario 7: Concurrent Sessions

- [ ] Browser 1: Login as john.smith
- [ ] Browser 2: Login as jane.doe
- [ ] Both sessions work independently
- [ ] Each user sees their own welcome page
- [ ] Logout in one browser doesn't affect the other

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

### Test Scenario 8: Password Security

- [ ] Open src/main/resources/users.csv
- [ ] All passwords are BCrypt hashed (start with $2a$)
- [ ] No plain text passwords visible
- [ ] Hash format: $2a$12$... (12 rounds)

**Result:** ☐ PASS ☐ FAIL  
**Notes:**

---

## Security Testing Checklist

### Authentication Security
- [ ] Passwords are BCrypt hashed in storage
- [ ] Invalid credentials show generic error message
- [ ] No information leakage about user existence
- [ ] Disabled accounts cannot login
- [ ] Session tokens are secure

### MFA Security
- [ ] TOTP codes expire after 30 seconds
- [ ] Clock drift tolerance (±30 seconds) works
- [ ] Invalid codes are rejected
- [ ] Multiple failed attempts trigger lockout
- [ ] Secret keys are properly generated

### Session Security
- [ ] Direct URL access blocked without authentication
- [ ] Session invalidated after logout
- [ ] Back button doesn't allow access after logout
- [ ] Session timeout works correctly
- [ ] Concurrent sessions are isolated

### Audit Logging
- [ ] All login attempts logged
- [ ] MFA setup events logged
- [ ] MFA verification attempts logged
- [ ] Logout events logged
- [ ] Failed attempts logged with details
- [ ] Audit log file created and updated

---

## UI/UX Testing Checklist

### Visual Design
- [ ] All pages display correctly
- [ ] Prolifics logo visible
- [ ] Consistent styling across pages
- [ ] QR code clearly visible
- [ ] Secret key readable
- [ ] Forms properly aligned

### User Experience
- [ ] Error messages are clear and helpful
- [ ] Forms validate input properly
- [ ] Navigation is intuitive
- [ ] Loading states are clear
- [ ] Success messages are visible

### Responsive Design
- [ ] Desktop browser (1920x1080)
- [ ] Laptop browser (1366x768)
- [ ] Tablet view (768x1024)
- [ ] Mobile view (375x667)

---

## Browser Compatibility Testing

Test on the following browsers:
- [ ] Google Chrome (latest)
- [ ] Mozilla Firefox (latest)
- [ ] Microsoft Edge (latest)
- [ ] Safari (latest) - if available

---

## Mobile Testing

Test Google Authenticator integration on:
- [ ] iOS devices (iPhone)
- [ ] Android devices
- [ ] Different screen sizes
- [ ] QR code scanning works
- [ ] Manual entry works

---

## Performance Testing

### Response Times
- [ ] Login completes in < 2 seconds
- [ ] TOTP validation completes in < 500ms
- [ ] Page loads are fast
- [ ] No noticeable delays

### Load Testing (Optional)
- [ ] Test with 10 concurrent users
- [ ] Test with 50 concurrent users
- [ ] Monitor memory usage
- [ ] Check for memory leaks

---

## Test Data Verification

### Users CSV
- [ ] File exists: src/main/resources/users.csv
- [ ] Header row correct
- [ ] All test users present
- [ ] Passwords are hashed
- [ ] Status values correct

### Audit Log CSV
- [ ] File exists: src/main/resources/audit-log.csv
- [ ] Header row correct
- [ ] Events are logged
- [ ] Timestamps are accurate
- [ ] Details are captured

---

## Issue Tracking

### Critical Issues Found
1. 
2. 
3. 

### High Priority Issues
1. 
2. 
3. 

### Medium Priority Issues
1. 
2. 
3. 

### Low Priority Issues
1. 
2. 
3. 

---

## Test Summary

**Total Test Scenarios:** 8  
**Test Cases Passed:** ___  
**Test Cases Failed:** ___  
**Test Cases Blocked:** ___  
**Test Cases Not Executed:** ___

**Overall Status:** ☐ PASS ☐ FAIL ☐ BLOCKED

---

## Sign-Off

**Tester Name:** _______________________  
**Date:** _______________________  
**Signature:** _______________________

**Reviewer Name:** _______________________  
**Date:** _______________________  
**Signature:** _______________________

---

## Notes and Observations

Additional comments, observations, or recommendations:

---

**Document Version:** 1.0.0  
**Last Updated:** May 2, 2026  
**Project:** MFA POC - Multi-Factor Authentication Proof of Concept