# MFA Setup Solution - Viewing QR Code

## Problem Identified

The **john.smith** account already has MFA configured from a previous test session. The MFA secret in the running application is:
```
NTOOK6ZH4K6WP2NYNZCDDYSMDJJSGSYP
```

This is why you're being asked for a 6-digit code instead of seeing the QR code setup page.

---

## Solution 1: Use bob.wilson Account (Recommended)

**bob.wilson** does NOT have MFA configured yet and will show the QR code setup page.

### Steps:
1. Logout from current session (or open new incognito/private browser window)
2. Go to: http://localhost:8080/mfa-poc/
3. Login with:
   - **Username:** bob.wilson
   - **Password:** Test@123
4. You will be redirected to the MFA Setup page with QR code
5. Scan the QR code with Google Authenticator
6. Complete the setup

---

## Solution 2: Reset john.smith Account

If you want to use john.smith for testing, you need to clear the MFA secret.

### Option A: Manual File Edit (Quick)

1. Stop Tomcat (Ctrl+C in the terminal)
2. Edit this file: `src/main/webapp/WEB-INF/classes/users.csv`
3. Find the john.smith line (line 2)
4. Remove the MFA secret at the end:

**Change from:**
```csv
john.smith,$2a$12$TI5fSTZcKBzTSYT4dxcxJOQSqEsIToGLXgkljn0UYGEDj6uVkSOpS,John Smith,john.smith@prolifics.com,ACTIVE,USER,NTOOK6ZH4K6WP2NYNZCDDYSMDJJSGSYP
```

**Change to:**
```csv
john.smith,$2a$12$TI5fSTZcKBzTSYT4dxcxJOQSqEsIToGLXgkljn0UYGEDj6uVkSOpS,John Smith,john.smith@prolifics.com,ACTIVE,USER,
```

5. Save the file
6. Restart Tomcat: `mvn tomcat7:run`
7. Login with john.smith / Test@123
8. You will now see the QR code setup page

### Option B: Rebuild and Redeploy (Clean)

1. Stop Tomcat (Ctrl+C)
2. Clean the project:
```powershell
mvn clean
```
3. Rebuild:
```powershell
$env:MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"
mvn clean package -DskipTests
```
4. Restart:
```powershell
mvn tomcat7:run
```
5. Login with john.smith / Test@123

---

## Solution 3: Use Existing MFA Secret (If You Have It)

If you previously set up john.smith in Google Authenticator, you can:

1. Open Google Authenticator app
2. Look for "Prolifics-MFA-POC (john.smith)"
3. Use the 6-digit code shown
4. Enter it on the verification page

**Note:** The secret is `NTOOK6ZH4K6WP2NYNZCDDYSMDJJSGSYP` if you need to manually add it.

---

## Quick Reference: Accounts Without MFA

These accounts will show the QR code setup page:

| Username | Password | Status |
|----------|----------|--------|
| bob.wilson | Test@123 | ✅ Ready for MFA setup |
| disabled.user | Test@123 | ❌ Account disabled (cannot login) |

---

## Recommended Testing Flow

### Test 1: First-Time MFA Setup (QR Code)
1. Use **bob.wilson** / Test@123
2. See QR code on setup page
3. Scan with Google Authenticator
4. Complete setup
5. Verify login works with MFA

### Test 2: Existing MFA Login
1. Logout
2. Login again with **bob.wilson** / Test@123
3. Enter 6-digit code from authenticator
4. Verify successful login

### Test 3: Manual Secret Entry
1. Reset bob.wilson (remove MFA secret from CSV)
2. Login with bob.wilson / Test@123
3. Copy the secret key from setup page
4. Manually enter in Google Authenticator
5. Complete setup

---

## Understanding the Issue

The application stores MFA secrets in the CSV file. When you complete MFA setup:

1. User logs in successfully
2. System generates a new MFA secret
3. QR code is displayed
4. User scans QR code
5. User enters verification code
6. **System saves the MFA secret to users.csv**
7. Next login will require MFA verification

The CSV file at `src/main/webapp/WEB-INF/classes/users.csv` is the **live data** that the running application uses. This file gets updated when users complete MFA setup.

---

## File Locations Explained

- **src/main/resources/users.csv** - Source file (template)
- **target/classes/users.csv** - Compiled into WAR file
- **src/main/webapp/WEB-INF/classes/users.csv** - Live data used by running app

When you run `mvn tomcat7:run`, it uses the files from the exploded WAR directory, which includes `src/main/webapp/WEB-INF/classes/`.

---

## Next Steps

**Recommended:** Use bob.wilson account to test MFA setup with QR code.

1. Open browser (or new incognito window)
2. Go to: http://localhost:8080/mfa-poc/
3. Login: bob.wilson / Test@123
4. You will see the MFA Setup page with QR code
5. Scan with Google Authenticator
6. Complete setup

---

**Created:** May 2, 2026  
**Issue:** john.smith already has MFA configured  
**Solution:** Use bob.wilson or reset john.smith's MFA secret