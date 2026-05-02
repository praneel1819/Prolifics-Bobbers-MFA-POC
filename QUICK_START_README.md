# Quick Start Guide - MFA POC Testing

This guide provides the fastest way to start testing the MFA POC application.

---

## Prerequisites Verified ✅

Your environment has been validated and is ready for testing:

- ✅ **Java 21** installed (backward compatible with Java 8)
- ✅ **Maven 3.9.11** installed and configured
- ✅ **Project builds successfully** (WAR file created)
- ✅ **Test data configured** (6 test users ready)

---

## Option 1: One-Click Start (Recommended)

### Windows PowerShell (Recommended)
```powershell
.\start-testing.ps1
```

### Windows Command Prompt
```cmd
start-testing.bat
```

**What it does:**
1. Validates Java and Maven installation
2. Builds the application
3. Starts embedded Tomcat server
4. Displays test account credentials
5. Opens application at http://localhost:8080/mfa-poc/

**No Tomcat installation required!**

---

## Option 2: Manual Start

### Step 1: Build the Application
```powershell
cd C:\Temp\IBM-BOB\POC\Prolifics-Bobbers-MFA-POC
$env:MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"
mvn clean package
```

### Step 2: Start Embedded Tomcat
```powershell
mvn tomcat7:run
```

### Step 3: Access Application
Open browser: http://localhost:8080/mfa-poc/

---

## Test Accounts

| Username | Password | Status | MFA Setup | Role |
|----------|----------|--------|-----------|------|
| john.smith | Test@123 | ACTIVE | Not configured | USER |
| jane.doe | Test@123 | ACTIVE | Pre-configured | USER |
| admin.user | Admin@123 | ACTIVE | Pre-configured | ADMIN |
| bob.wilson | Test@123 | ACTIVE | Not configured | USER |
| disabled.user | Test@123 | DISABLED | Not configured | USER |

---

## First Test: MFA Setup Flow

1. **Start the application** (using one of the methods above)

2. **Open browser:** http://localhost:8080/mfa-poc/

3. **Login:**
   - Username: `john.smith`
   - Password: `Test@123`

4. **Setup MFA:**
   - You'll be redirected to MFA Setup page
   - QR code will be displayed
   - Open Google Authenticator app on your phone
   - Scan the QR code
   - Enter the 6-digit code from the app
   - Click "Verify and Complete Setup"

5. **Success!**
   - You'll see the Welcome page
   - Your MFA is now configured

---

## Google Authenticator Setup

### If you don't have Google Authenticator:

**iOS:**
1. Open App Store
2. Search "Google Authenticator"
3. Install app by Google LLC

**Android:**
1. Open Google Play Store
2. Search "Google Authenticator"
3. Install app by Google LLC

**Alternative Apps:**
- Microsoft Authenticator
- Authy
- FreeOTP
- Any TOTP-compatible authenticator

---

## Testing Checklist

Follow the comprehensive testing guide:
- See: **TESTING_CHECKLIST.md**
- See: **MANUAL_TESTING_GUIDE.md**

---

## Stopping the Server

Press `Ctrl+C` in the terminal where the server is running.

---

## Troubleshooting

### Port 8080 Already in Use

**Find and kill the process:**
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace <PID> with actual process ID)
taskkill /PID <PID> /F
```

### Build Fails

**Clear Maven cache and rebuild:**
```powershell
mvn clean
mvn clean package -X
```

### QR Code Not Displaying

**Solutions:**
- Check browser console for errors
- Verify internet connection (Google Charts API)
- Use manual secret entry instead

### TOTP Code Always Invalid

**Solutions:**
- Verify device time is synchronized
- Check if code is entered within 30-second window
- Try re-scanning QR code
- Use manual secret entry

---

## Next Steps

1. ✅ Start the application (using quick start script)
2. ✅ Install Google Authenticator on your mobile device
3. ✅ Login with test account: john.smith / Test@123
4. ✅ Complete MFA setup
5. ✅ Follow TESTING_CHECKLIST.md for comprehensive testing

---

## Additional Resources

- **ENVIRONMENT_VALIDATION_REPORT.md** - Complete environment validation details
- **MANUAL_TESTING_GUIDE.md** - Detailed testing scenarios and procedures
- **TESTING_CHECKLIST.md** - Printable testing checklist
- **JAVA8_PORT_CHANGES.md** - Java 8 compatibility information

---

## Support

For issues or questions:
1. Check troubleshooting section above
2. Review MANUAL_TESTING_GUIDE.md
3. Check audit-log.csv for application events
4. Review Tomcat console output

---

**Ready to start testing!** 🚀

Run: `.\start-testing.ps1` or `start-testing.bat`