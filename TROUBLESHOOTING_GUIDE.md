# Troubleshooting Guide - MFA POC Testing

## Current Status

✅ **Build Successful** - The application compiled successfully and WAR file was created.

⏳ **Tomcat Starting** - The `start-testing-simple.bat` script is now at Step 3, starting the embedded Tomcat server.

---

## What's Happening Now

The script executed:
1. ✅ Step 1: Set Maven options
2. ✅ Step 2: Built application successfully (BUILD SUCCESS in 3.978s)
3. ⏳ Step 3: Starting Tomcat server with `mvn tomcat7:run`

**This is normal!** The Tomcat startup can take 30-60 seconds on first run.

---

## What You Should See

### In the Terminal Window

You should see Maven downloading plugins and dependencies, then output like:

```
[INFO] --- tomcat7-maven-plugin:2.2:run (default-cli) @ mfa-poc ---
[INFO] Running war on http://localhost:8080/mfa-poc
[INFO] Creating Tomcat server configuration at C:\Temp\...
[INFO] Starting Tomcat server...
```

### When Tomcat is Ready

You'll see:
```
INFO: Starting ProtocolHandler ["http-bio-8080"]
INFO: Server startup in [xxxx] ms
```

---

## Next Steps

### 1. Wait for Tomcat to Start (30-60 seconds)

Watch the terminal for "Server startup" message.

### 2. Open Your Browser

Once you see the startup message, open:
```
http://localhost:8080/mfa-poc/
```

### 3. Test Login

Use these credentials:
- **Username:** john.smith
- **Password:** Test@123

This account does NOT have MFA configured yet, so you'll go through the complete setup flow.

---

## If Tomcat Doesn't Start

### Check for Port Conflicts

Open a **new** PowerShell window and run:
```powershell
netstat -ano | findstr :8080
```

If port 8080 is already in use, you'll see output. Kill that process:
```powershell
# Find the PID from the netstat output
taskkill /PID <PID> /F
```

### Check for Errors in Terminal

Look for error messages like:
- "Address already in use"
- "Port 8080 is already in use"
- "Failed to start component"

### Try Alternative Port

If port 8080 is blocked, you can modify the pom.xml to use a different port (e.g., 8081).

---

## Manual Start (If Script Fails)

If the script doesn't work, run these commands manually in PowerShell:

```powershell
# Set Maven options
$env:MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"

# Build the application
mvn clean package -DskipTests

# Start Tomcat
mvn tomcat7:run
```

---

## Stopping the Server

When you're done testing:

1. Go to the terminal window where Tomcat is running
2. Press `Ctrl+C`
3. Confirm with `Y` if prompted

---

## Common Issues and Solutions

### Issue: "BUILD FAILURE" During Step 2

**Cause:** Maven dependency download issues or compilation errors

**Solution:**
```powershell
# Clear Maven cache
mvn clean

# Try with debug output
mvn clean package -DskipTests -X
```

### Issue: Port 8080 Already in Use

**Cause:** Another application is using port 8080

**Solution:**
```powershell
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace <PID> with actual process ID)
taskkill /PID <PID> /F
```

### Issue: Browser Shows "Connection Refused"

**Cause:** Tomcat hasn't finished starting yet

**Solution:**
- Wait 30-60 seconds
- Check terminal for "Server startup" message
- Refresh browser

### Issue: 404 Error in Browser

**Cause:** Wrong URL or application not deployed

**Solution:**
- Verify URL: `http://localhost:8080/mfa-poc/` (note the trailing slash)
- Check terminal for deployment errors
- Look for "Deploying web application" message

---

## Verification Checklist

Before testing, verify:

- [ ] Terminal shows "BUILD SUCCESS"
- [ ] Terminal shows "Starting Tomcat server"
- [ ] Terminal shows "Server startup in [xxxx] ms"
- [ ] No error messages in terminal
- [ ] Browser can access http://localhost:8080/mfa-poc/
- [ ] Login page displays correctly

---

## Test Accounts Reference

### Accounts WITHOUT MFA (for setup testing):
- **john.smith** / Test@123 ← **Recommended for first test**
- **bob.wilson** / Test@123

### Accounts WITH MFA (for verification testing):
- **jane.doe** / Test@123
- **admin.user** / Admin@123

### Disabled Account (should fail):
- **disabled.user** / Test@123

---

## Getting Help

If you encounter issues:

1. **Check the terminal output** for error messages
2. **Review this troubleshooting guide**
3. **Check MANUAL_TESTING_GUIDE.md** for detailed testing procedures
4. **Verify environment** with ENVIRONMENT_VALIDATION_REPORT.md

---

## Success Indicators

You'll know everything is working when:

✅ Terminal shows "Server startup in [xxxx] ms"  
✅ Browser loads http://localhost:8080/mfa-poc/  
✅ Login page displays with Prolifics logo  
✅ You can login with john.smith / Test@123  
✅ MFA setup page appears with QR code  

---

**Current Time:** May 2, 2026  
**Status:** Waiting for Tomcat to complete startup  
**Next Action:** Wait for "Server startup" message, then open browser