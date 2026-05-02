# QR Code Not Displaying - Solution

## Problem
The QR code image shows as broken/not loading on the MFA Setup page.

## Root Cause
The application uses Google Charts API to generate QR codes:
```
https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=otpauth://...
```

This can fail due to:
1. **No internet connection** - Google Charts API requires internet access
2. **Firewall/proxy blocking** - Corporate networks may block Google APIs
3. **URL encoding issue** - The otpauth URL needs proper encoding

---

## ✅ Solution 1: Use Manual Secret Entry (Immediate Workaround)

The MFA setup page displays the secret key below the QR code. You can manually enter this in Google Authenticator:

### Steps:
1. On the MFA Setup page, look for **"Manual Entry Key:"**
2. Copy the secret key (e.g., `JBSWY3DPEHPK3PXP`)
3. Open Google Authenticator app
4. Tap **"+"** → **"Enter a setup key"**
5. Enter:
   - **Account name:** demo.user1 (or your username)
   - **Your key:** (paste the secret key)
   - **Type of key:** Time based
6. Tap **"Add"**
7. Enter the 6-digit code on the web page

**This works without needing the QR code!**

---

## ✅ Solution 2: Fix URL Encoding (Code Fix)

The issue is in line 89 of TOTPUtil.java - the otpauth URL needs to be URL-encoded.

### Current Code (Line 86-90):
```java
return String.format(
    "https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=%s",
    otpauthURL
);
```

### Fixed Code:
```java
String encodedOtpauth = URLEncoder.encode(otpauthURL, "UTF-8");
return String.format(
    "https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=%s",
    encodedOtpauth
);
```

---

## ✅ Solution 3: Test Internet Connectivity

Check if you can access Google Charts API:

### Test in Browser:
Open this URL in your browser:
```
https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=test
```

**Expected:** You should see a QR code with "test" encoded  
**If it fails:** Your network is blocking Google Charts API

---

## ✅ Solution 4: Use Alternative QR Code Generator

If Google Charts is blocked, you can use a local QR code library.

### Option A: Add QR Code Library to pom.xml
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.1</version>
</dependency>
```

### Option B: Generate QR Code as Data URL
This would generate the QR code on the server and embed it as a base64 data URL (no external API needed).

---

## 🎯 Recommended for Demo

**Use Manual Secret Entry Method (Solution 1)**

This is the most reliable method and demonstrates an important feature:
- Shows that MFA works even without QR scanning
- Demonstrates the manual entry option
- No dependency on external services
- Works in all network environments

### Demo Script:
1. "Notice the QR code here - normally you'd scan this with your phone"
2. "But I'll demonstrate the manual entry method instead"
3. "This is useful if you can't scan QR codes or prefer manual entry"
4. "Here's the secret key displayed on screen"
5. "I'll enter this manually in Google Authenticator"
6. [Show manual entry in app]
7. "Now I'll enter the 6-digit code to complete setup"

---

## 🔧 Quick Fix for Your Demo

### Immediate Action:
1. **Don't worry about the broken QR code image**
2. **Use the secret key shown below it**
3. **Manually enter in Google Authenticator**
4. **This is actually a feature to demonstrate!**

### Secret Keys for Demo Users:
When you login with demo.user1, demo.user2, or demo.user3, the page will show their unique secret keys. Just copy and manually enter them.

---

## 📝 Testing the Fix

If you want to fix the URL encoding issue:

1. Stop Tomcat (Ctrl+C)
2. Edit `src/main/java/com/prolifics/mfa/util/TOTPUtil.java`
3. Find line 86-90 in the `generateQRCodeURL` method
4. Add URL encoding:
```java
String encodedOtpauth = URLEncoder.encode(otpauthURL, "UTF-8");
return String.format(
    "https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=%s",
    encodedOtpauth
);
```
5. Rebuild: `mvn clean package -DskipTests`
6. Restart: `mvn tomcat7:run`
7. Test login with demo.user1

---

## 🌐 Network Troubleshooting

### Check if Google Charts is accessible:
```powershell
# Test DNS resolution
nslookup chart.googleapis.com

# Test connectivity
curl https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=test
```

### Common Network Issues:
- Corporate firewall blocking googleapis.com
- Proxy configuration needed
- No internet connection
- DNS resolution failure

---

## ✅ Summary

**For your demo RIGHT NOW:**
- ✅ Use manual secret entry (it's displayed on the page)
- ✅ This demonstrates an important MFA feature
- ✅ No code changes needed
- ✅ Works in any network environment

**For future improvement:**
- Consider adding local QR code generation library
- Or fix the URL encoding in TOTPUtil.java
- Or document that internet access is required

---

**The manual entry method is actually BETTER for demos** because:
1. Shows both QR and manual entry options
2. More reliable (no network dependency)
3. Demonstrates security best practice
4. Works in restricted networks