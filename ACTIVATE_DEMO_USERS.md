# Activate Demo Users - Quick Guide

## ✅ Demo Users Added to Live CSV

The 3 demo users have been added to the running application's CSV file:
- `src/main/webapp/WEB-INF/classes/users.csv`

However, the application may have cached the user data in memory.

---

## 🔄 Option 1: Restart Tomcat (Recommended)

The most reliable way to activate the demo users:

1. **Stop Tomcat:**
   - Go to the terminal where Tomcat is running
   - Press `Ctrl+C`
   - Wait for shutdown to complete

2. **Restart Tomcat:**
   ```cmd
   mvn tomcat7:run
   ```

3. **Test Login:**
   - Go to: http://localhost:8080/mfa-poc/
   - Login with: demo.user1 / Demo@123
   - Should work immediately!

---

## 🔄 Option 2: Try Without Restart (May Work)

Some applications reload CSV files on each request. Try logging in:

1. **Open browser** (or new incognito window)
2. **Go to:** http://localhost:8080/mfa-poc/
3. **Login with:**
   - Username: `demo.user1`
   - Password: `Demo@123`

If you get "Invalid username or password", proceed to Option 1 (restart).

---

## ✅ Verification

Once Tomcat restarts, you should be able to login with:

| Username | Password | Expected Result |
|----------|----------|-----------------|
| demo.user1 | Demo@123 | MFA Setup page with QR code |
| demo.user2 | Demo@123 | MFA Setup page with QR code |
| demo.user3 | Demo@123 | MFA Setup page with QR code |

---

## 📝 What Was Done

1. ✅ Generated BCrypt password hashes for 3 demo users
2. ✅ Added users to `src/main/resources/users.csv` (source file)
3. ✅ Added users to `src/main/webapp/WEB-INF/classes/users.csv` (live file)
4. ⏳ Waiting for application to reload or restart

---

## 🎯 Ready for Demo

After restart, all 3 demo users will be available with:
- **Password:** Demo@123
- **Status:** ACTIVE
- **MFA:** Not configured (will show QR code)

Perfect for your live demonstration!

---

**Quick Restart Command:**
```cmd
# Press Ctrl+C to stop, then run:
mvn tomcat7:run