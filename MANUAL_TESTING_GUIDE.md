# Manual Testing Guide - MFA POC Application

## Prerequisites

### Required Software
1. **Apache Tomcat 9.x** or higher
2. **Java 11** or higher
3. **Google Authenticator App** (on your mobile device)
   - iOS: Download from App Store
   - Android: Download from Google Play Store
4. **Web Browser** (Chrome, Firefox, Edge, or Safari)

### Build the Application
```bash
# Set Maven TLS protocol
$env:MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"

# Build the WAR file
mvn clean package

# The WAR file will be created at: target/mfa-poc-1.0.0.war
```

---

## Deployment Steps

### Option 1: Deploy to Tomcat

1. **Copy WAR file to Tomcat**
   ```bash
   # Copy the WAR file to Tomcat's webapps directory
   copy target\mfa-poc-1.0.0.war "C:\path\to\tomcat\webapps\mfa-poc.war"
   ```

2. **Start Tomcat**
   ```bash
   # Navigate to Tomcat bin directory
   cd C:\path\to\tomcat\bin
   
   # Start Tomcat
   startup.bat
   ```

3. **Verify Deployment**
   - Wait for Tomcat to deploy the application (check logs)
   - Application will be available at: `http://localhost:8080/mfa-poc/`

### Option 2: Run with Maven (Development)

```bash
# Run with embedded Tomcat
mvn tomcat7:run

# Application will be available at: http://localhost:8080/mfa-poc/
```

---

## Test User Accounts

The application comes with pre-configured test users in `src/main/resources/users.csv`:

| Username | Password | Full Name | Status | MFA Setup |
|----------|----------|-----------|--------|-----------|
| admin | Admin@123 | Admin User | ACTIVE | Not configured |
| john.smith | Test@123 | John Smith | ACTIVE | Not configured |
| jane.doe | Test@123 | Jane Doe | ACTIVE | Not configured |
| bob.wilson | Test@123 | Bob Wilson | DISABLED | Not configured |

**Note**: All passwords are BCrypt hashed in the CSV file. The plain text passwords above are for testing only.

---

## Manual Test Scenarios

### Test Scenario 1: First-Time Login (MFA Setup Required)

**Objective**: Test the complete MFA setup flow for a new user.

#### Steps:

1. **Navigate to Login Page**
   - Open browser: `http://localhost:8080/mfa-poc/`
   - Verify the login page displays correctly
   - Check for Prolifics logo and "Multi-Factor Authentication POC" title

2. **Login with Valid Credentials**
   - Username: `john.smith`
   - Password: `Test@123`
   - Click "Sign In"
   - **Expected**: Redirect to MFA Setup page

3. **Setup Google Authenticator**
   - **On MFA Setup Page**:
     - Verify QR code is displayed
     - Verify secret key is shown (Base32 format)
     - Note the secret key for manual entry if needed
   
   - **On Mobile Device**:
     - Open Google Authenticator app
     - Tap "+" or "Add account"
     - Choose "Scan a QR code"
     - Scan the QR code displayed on screen
     - **Alternative**: Choose "Enter a setup key" and manually enter:
       - Account name: `john.smith`
       - Key: (the secret key shown on screen)
       - Time-based: Yes
   
   - **Verify in App**:
     - Account should appear as "Prolifics-MFA-POC (john.smith)"
     - 6-digit code should be displayed
     - Code should refresh every 30 seconds

4. **Complete MFA Setup**
   - Enter the 6-digit code from Google Authenticator
   - Click "Verify and Complete Setup"
   - **Expected**: Redirect to Welcome page
   - **Verify Welcome Page**:
     - Shows "Welcome, John Smith!"
     - Displays username
     - Shows "Logout" button

5. **Verify Audit Log**
   - Check `src/main/resources/audit-log.csv`
   - Should contain entries for:
     - LOGIN_SUCCESS
     - MFA_SETUP
     - MFA_VERIFY_SUCCESS

---

### Test Scenario 2: Login with Existing MFA Setup

**Objective**: Test login flow for user with MFA already configured.

#### Steps:

1. **Logout from Previous Session**
   - Click "Logout" button on Welcome page
   - **Expected**: Redirect to Login page
   - Verify audit log shows LOGOUT entry

2. **Login Again**
   - Username: `john.smith`
   - Password: `Test@123`
   - Click "Sign In"
   - **Expected**: Redirect to MFA Verification page (NOT setup page)

3. **Verify MFA Code**
   - Open Google Authenticator app
   - Find "Prolifics-MFA-POC (john.smith)" account
   - Note the current 6-digit code
   - Enter code in verification page
   - Click "Verify"
   - **Expected**: Redirect to Welcome page

4. **Verify Session**
   - Welcome page should display user information
   - Session should be active
   - Audit log should show MFA_VERIFY_SUCCESS

---

### Test Scenario 3: Invalid Login Attempts

**Objective**: Test security validations and error handling.

#### Test 3.1: Wrong Password

1. Navigate to login page
2. Enter:
   - Username: `john.smith`
   - Password: `WrongPassword123`
3. Click "Sign In"
4. **Expected**:
   - Error message: "Invalid username or password"
   - Remain on login page
   - Audit log shows LOGIN_FAILED

#### Test 3.2: Non-Existent User

1. Navigate to login page
2. Enter:
   - Username: `nonexistent`
   - Password: `Test@123`
3. Click "Sign In"
4. **Expected**:
   - Error message: "Invalid username or password"
   - Remain on login page
   - Audit log shows LOGIN_FAILED

#### Test 3.3: Disabled Account

1. Navigate to login page
2. Enter:
   - Username: `bob.wilson`
   - Password: `Test@123`
3. Click "Sign In"
4. **Expected**:
   - Error message: "Account is disabled"
   - Remain on login page
   - Audit log shows LOGIN_FAILED with "Account disabled"

#### Test 3.4: Empty Credentials

1. Navigate to login page
2. Leave username and password empty
3. Click "Sign In"
4. **Expected**:
   - Browser validation prevents submission
   - OR error message: "Username and password are required"

---

### Test Scenario 4: Invalid MFA Codes

**Objective**: Test MFA verification error handling.

#### Test 4.1: Wrong TOTP Code

1. Login with valid credentials (user with MFA setup)
2. On MFA Verification page, enter: `000000`
3. Click "Verify"
4. **Expected**:
   - Error message: "Invalid verification code"
   - Remain on verification page
   - Attempt counter increments
   - Audit log shows MFA_VERIFY_FAILED

#### Test 4.2: Expired TOTP Code

1. Login with valid credentials
2. Wait for code to change in Google Authenticator
3. Enter the OLD code (from previous 30-second window)
4. Click "Verify"
5. **Expected**:
   - Error message: "Invalid verification code"
   - Code should still work if within ±30 second window (clock drift tolerance)

#### Test 4.3: Multiple Failed Attempts

1. Login with valid credentials
2. Enter wrong code 3 times
3. **Expected**:
   - After 3 failed attempts: "Too many failed attempts. Please try again later."
   - Session invalidated
   - Redirect to login page
   - Audit log shows multiple MFA_VERIFY_FAILED entries

#### Test 4.4: Empty TOTP Code

1. Login with valid credentials
2. Leave verification code field empty
3. Click "Verify"
4. **Expected**:
   - Browser validation prevents submission
   - OR error message: "Verification code is required"

---

### Test Scenario 5: Session Management

**Objective**: Test session timeout and security.

#### Test 5.1: Session Timeout

1. Login successfully and reach Welcome page
2. Wait for 30 minutes (session timeout)
3. Try to navigate or refresh page
4. **Expected**:
   - Redirect to login page
   - Message: "Session expired. Please login again."
   - Audit log shows SESSION_TIMEOUT

#### Test 5.2: Direct URL Access Without Login

1. Logout or open new browser session
2. Try to access: `http://localhost:8080/mfa-poc/welcome.jsp`
3. **Expected**:
   - Redirect to login page
   - Cannot access protected pages without authentication

#### Test 5.3: Back Button After Logout

1. Login and reach Welcome page
2. Click Logout
3. Click browser's Back button
4. **Expected**:
   - Redirect to login page
   - Cannot access Welcome page after logout
   - Session is invalidated

---

### Test Scenario 6: QR Code and Secret Key

**Objective**: Test MFA setup with different methods.

#### Test 6.1: Manual Secret Entry

1. Login with new user: `jane.doe` / `Test@123`
2. On MFA Setup page, copy the secret key
3. In Google Authenticator:
   - Choose "Enter a setup key"
   - Account: `jane.doe`
   - Key: (paste the secret key)
   - Type: Time-based
4. Complete setup with generated code
5. **Expected**: Successfully setup and login

#### Test 6.2: QR Code Scan

1. Login with new user: `admin` / `Admin@123`
2. Use mobile device to scan QR code
3. Complete setup with generated code
4. **Expected**: Successfully setup and login

---

### Test Scenario 7: Concurrent Sessions

**Objective**: Test multiple browser sessions.

#### Steps:

1. **Browser 1**: Login as `john.smith`
2. **Browser 2**: Login as `jane.doe`
3. **Verify**:
   - Both sessions should work independently
   - Each user sees their own welcome page
   - Logout in one browser doesn't affect the other

---

### Test Scenario 8: Password Security

**Objective**: Verify password hashing and security.

#### Steps:

1. Check `src/main/resources/users.csv`
2. **Verify**:
   - All passwords are BCrypt hashed (start with `$2a$`)
   - No plain text passwords visible
   - Hash format: `$2a$12$...` (12 rounds)

---

## Verification Checklist

### Functional Testing
- [ ] Login with valid credentials works
- [ ] Login with invalid credentials fails appropriately
- [ ] MFA setup flow completes successfully
- [ ] QR code displays and scans correctly
- [ ] Manual secret entry works
- [ ] TOTP code verification works
- [ ] Invalid TOTP codes are rejected
- [ ] Logout functionality works
- [ ] Session timeout works
- [ ] Disabled accounts cannot login

### Security Testing
- [ ] Passwords are BCrypt hashed
- [ ] Direct URL access is blocked without authentication
- [ ] Session is invalidated after logout
- [ ] Back button doesn't allow access after logout
- [ ] Failed login attempts are logged
- [ ] MFA codes expire after 30 seconds
- [ ] Clock drift tolerance (±30 seconds) works

### UI/UX Testing
- [ ] All pages display correctly
- [ ] Error messages are clear and helpful
- [ ] Forms validate input properly
- [ ] Responsive design works on different screen sizes
- [ ] QR code is clearly visible
- [ ] Secret key is readable

### Audit Logging
- [ ] All login attempts are logged
- [ ] MFA setup events are logged
- [ ] MFA verification attempts are logged
- [ ] Logout events are logged
- [ ] Failed attempts are logged with details
- [ ] Audit log file is created and updated

---

## Troubleshooting

### Issue: QR Code Not Displaying

**Solution**:
- Check browser console for errors
- Verify Google Charts API is accessible
- Try manual secret entry instead

### Issue: TOTP Code Always Invalid

**Solution**:
- Verify device time is synchronized (NTP)
- Check if code is entered within 30-second window
- Ensure secret key was entered correctly
- Try re-scanning QR code

### Issue: Application Not Starting

**Solution**:
- Check Tomcat logs: `logs/catalina.out`
- Verify Java 11+ is installed
- Ensure port 8080 is not in use
- Check CSV files exist in `src/main/resources/`

### Issue: CSV File Not Found

**Solution**:
- Ensure `users.csv` exists in `src/main/resources/`
- Verify file has correct header row
- Check file permissions

### Issue: Session Timeout Too Quick

**Solution**:
- Modify `web.xml` session timeout:
  ```xml
  <session-config>
    <session-timeout>30</session-timeout> <!-- minutes -->
  </session-config>
  ```

---

## Test Data Reset

To reset test data and start fresh:

1. **Stop Tomcat**

2. **Reset Users CSV**
   - Delete `src/main/resources/users.csv`
   - Run: `java GeneratePasswords.java`
   - This regenerates users with fresh passwords

3. **Clear Audit Log**
   - Delete or backup `src/main/resources/audit-log.csv`
   - File will be recreated on next application start

4. **Rebuild and Redeploy**
   ```bash
   mvn clean package
   # Copy new WAR to Tomcat
   ```

5. **Restart Tomcat**

---

## Performance Testing

### Load Testing Considerations

1. **Concurrent Users**: Test with 10-50 concurrent users
2. **Response Time**: Login should complete in < 2 seconds
3. **TOTP Validation**: Should complete in < 500ms
4. **Session Management**: Monitor memory usage with multiple sessions

### Tools for Load Testing
- Apache JMeter
- Gatling
- Apache Bench (ab)

---

## Security Testing

### Recommended Security Tests

1. **SQL Injection**: Try SQL injection in login fields
2. **XSS**: Try JavaScript injection in input fields
3. **CSRF**: Test cross-site request forgery protection
4. **Session Fixation**: Test session ID regeneration
5. **Brute Force**: Test account lockout after failed attempts

---

## Browser Compatibility

Test on the following browsers:
- [ ] Google Chrome (latest)
- [ ] Mozilla Firefox (latest)
- [ ] Microsoft Edge (latest)
- [ ] Safari (latest)

---

## Mobile Testing

Test Google Authenticator integration on:
- [ ] iOS devices
- [ ] Android devices
- [ ] Different screen sizes

---

## Reporting Issues

When reporting issues, include:
1. Steps to reproduce
2. Expected behavior
3. Actual behavior
4. Browser and version
5. Screenshots (if applicable)
6. Relevant log entries from:
   - Tomcat logs
   - Browser console
   - `audit-log.csv`

---

## Success Criteria

The application passes manual testing if:
- ✅ All test scenarios complete successfully
- ✅ No critical or high-severity bugs found
- ✅ Security validations work correctly
- ✅ Audit logging captures all events
- ✅ User experience is smooth and intuitive
- ✅ Error messages are clear and helpful

---

**Document Version**: 1.0.0  
**Last Updated**: 2026-05-01  
**Author**: Prolifics MFA POC Team