# Live Demo Credentials - MFA POC

**Created:** May 2, 2026  
**Purpose:** Live demonstration of MFA setup and authentication

---

## 🎯 Demo User Accounts

All demo users have **NO MFA configured** - perfect for demonstrating the complete setup flow with QR code.

### Demo User 1
- **Username:** `demo.user1`
- **Password:** `Demo@123`
- **Full Name:** Demo User 1
- **Email:** demo.user1@prolifics.com
- **Status:** ACTIVE
- **MFA Status:** ❌ Not configured (will show QR code)

### Demo User 2
- **Username:** `demo.user2`
- **Password:** `Demo@123`
- **Full Name:** Demo User 2
- **Email:** demo.user2@prolifics.com
- **Status:** ACTIVE
- **MFA Status:** ❌ Not configured (will show QR code)

### Demo User 3
- **Username:** `demo.user3`
- **Password:** `Demo@123`
- **Full Name:** Demo User 3
- **Email:** demo.user3@prolifics.com
- **Status:** ACTIVE
- **MFA Status:** ❌ Not configured (will show QR code)

---

## 📱 Demo Flow Suggestions

### Scenario 1: First-Time MFA Setup (Recommended)
**Use:** demo.user1

1. Navigate to: http://localhost:8080/mfa-poc/
2. Login with: demo.user1 / Demo@123
3. **Show:** MFA Setup page with QR code
4. **Demonstrate:** Scanning QR code with Google Authenticator
5. **Show:** Secret key for manual entry option
6. **Complete:** Enter 6-digit code and verify
7. **Result:** Welcome page with user information

### Scenario 2: Subsequent Login with MFA
**Use:** demo.user1 (after setup)

1. Logout from welcome page
2. Login again with: demo.user1 / Demo@123
3. **Show:** MFA Verification page (no QR code)
4. **Demonstrate:** Entering current 6-digit code from authenticator
5. **Result:** Successful login to welcome page

### Scenario 3: Invalid MFA Code
**Use:** demo.user2

1. Setup MFA for demo.user2
2. Logout and login again
3. **Demonstrate:** Entering wrong code (e.g., 000000)
4. **Show:** Error message and failed attempt logging
5. **Demonstrate:** Entering correct code
6. **Result:** Successful login

### Scenario 4: Multiple Users
**Use:** demo.user1, demo.user2, demo.user3

1. **Demonstrate:** Setting up MFA for all three users
2. **Show:** Each user has unique QR code and secret
3. **Demonstrate:** All three accounts in Google Authenticator
4. **Show:** Concurrent sessions with different users

---

## 🔐 Additional Test Accounts

### Existing Users (for comparison)

**bob.wilson** - No MFA configured
- Username: `bob.wilson`
- Password: `Test@123`
- Status: ACTIVE, no MFA

**jane.doe** - MFA already configured
- Username: `jane.doe`
- Password: `Test@123`
- Status: ACTIVE, has MFA (will ask for code)

**admin.user** - Admin with MFA
- Username: `admin.user`
- Password: `Admin@123`
- Status: ACTIVE, ADMIN role, has MFA

**disabled.user** - Disabled account
- Username: `disabled.user`
- Password: `Test@123`
- Status: DISABLED (login will fail)

---

## 🎬 Demo Script

### Introduction (2 minutes)
"Today I'll demonstrate our Multi-Factor Authentication proof of concept. This application adds an extra layer of security using Google Authenticator for time-based one-time passwords."

### Demo Part 1: First-Time Setup (3 minutes)
1. "Let me login as a new user who hasn't set up MFA yet."
2. Login with demo.user1 / Demo@123
3. "Notice we're redirected to the MFA setup page with a QR code."
4. "I'll scan this with Google Authenticator on my phone."
5. Show the authenticator app with the new account
6. "Now I'll enter the 6-digit code to complete setup."
7. Enter code and show successful login

### Demo Part 2: Subsequent Login (2 minutes)
1. "Now let me logout and login again."
2. Logout and login with demo.user1 / Demo@123
3. "This time, I'm asked for the verification code directly."
4. Show authenticator app with current code
5. Enter code and show successful login

### Demo Part 3: Security Features (3 minutes)
1. "Let me show what happens with an invalid code."
2. Login with demo.user2 and setup MFA
3. Logout and login again
4. Enter wrong code (000000)
5. "Notice the error message and the attempt is logged."
6. Show audit log with failed attempt
7. Enter correct code and login successfully

### Conclusion (1 minute)
"This demonstrates how MFA adds security while maintaining usability. All authentication events are logged for audit purposes."

---

## 📊 Key Features to Highlight

✅ **QR Code Generation** - Easy setup with mobile app  
✅ **Manual Secret Entry** - Alternative setup method  
✅ **Time-Based Codes** - 30-second rotating codes  
✅ **Clock Drift Tolerance** - ±30 second window  
✅ **Audit Logging** - All events tracked  
✅ **Session Management** - Secure session handling  
✅ **Password Security** - BCrypt hashing  
✅ **Account Status** - Active/disabled account support  

---

## 🚀 Before the Demo

### Preparation Checklist
- [ ] Start Tomcat: `mvn tomcat7:run`
- [ ] Verify application loads: http://localhost:8080/mfa-poc/
- [ ] Have Google Authenticator app ready on mobile device
- [ ] Clear any existing demo.user* accounts from authenticator
- [ ] Test login with demo.user1 to verify it works
- [ ] Have audit-log.csv open to show logging
- [ ] Prepare browser window for easy viewing

### Reset Demo Users (if needed)
If you need to reset the demo users to show QR codes again:

1. Stop Tomcat (Ctrl+C)
2. Edit: `src/main/webapp/WEB-INF/classes/users.csv`
3. Remove MFA secrets from demo.user1, demo.user2, demo.user3 lines
4. Restart Tomcat: `mvn tomcat7:run`

---

## 📝 Talking Points

### Security Benefits
- "MFA reduces account compromise risk by 99.9%"
- "Even if password is stolen, attacker needs physical device"
- "TOTP is industry standard (Google, Microsoft, GitHub use it)"
- "No SMS vulnerabilities - app-based is more secure"

### Implementation Highlights
- "Built with Java 8 for broad compatibility"
- "Uses standard TOTP algorithm (RFC 6238)"
- "BCrypt password hashing with 12 rounds"
- "Comprehensive audit logging for compliance"
- "Session management with timeout protection"

### User Experience
- "One-time setup process"
- "Quick 6-digit code entry"
- "Works offline (no internet needed for codes)"
- "Multiple accounts supported in one app"

---

## 🔧 Troubleshooting During Demo

### If QR Code Doesn't Display
- Use manual secret entry instead
- Show the secret key on screen
- Demonstrate manual entry in authenticator

### If Code is Invalid
- Check device time is synchronized
- Wait for next code (30-second window)
- Show clock drift tolerance feature

### If Port 8080 is Busy
- Application might already be running
- Check with: `netstat -ano | findstr :8080`
- Use alternative port or restart

---

## 📞 Support Information

**Application URL:** http://localhost:8080/mfa-poc/  
**Documentation:** See MANUAL_TESTING_GUIDE.md  
**Troubleshooting:** See TROUBLESHOOTING_GUIDE.md  
**Architecture:** See ARCHITECTURE.md  

---

**Good luck with your demo!** 🎉

Remember: All demo users have password `Demo@123` and NO MFA configured initially.