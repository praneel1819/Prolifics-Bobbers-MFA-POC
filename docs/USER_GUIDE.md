# MFA POC - User Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [First-Time Login](#first-time-login)
4. [Setting Up Multi-Factor Authentication](#setting-up-multi-factor-authentication)
5. [Logging In with MFA](#logging-in-with-mfa)
6. [Using the Application](#using-the-application)
7. [Logging Out](#logging-out)
8. [Troubleshooting](#troubleshooting)
9. [Frequently Asked Questions](#frequently-asked-questions)
10. [Security Best Practices](#security-best-practices)

## Introduction

### What is MFA POC?
MFA POC (Multi-Factor Authentication Proof of Concept) is a secure web application that demonstrates two-factor authentication using Google Authenticator. It adds an extra layer of security to your account by requiring both your password and a time-based code from your mobile device.

### Why Use Multi-Factor Authentication?
Multi-Factor Authentication (MFA) significantly enhances your account security by requiring two forms of verification:
1. **Something you know**: Your password
2. **Something you have**: Your mobile device with Google Authenticator

Even if someone obtains your password, they cannot access your account without the time-based code from your authenticator app.

### System Requirements
- **Web Browser**: Chrome, Firefox, Safari, or Edge (latest version)
- **Mobile Device**: iOS or Android smartphone
- **Internet Connection**: Required for initial setup and login

## Getting Started

### Prerequisites

#### 1. Install Google Authenticator
Download and install Google Authenticator on your mobile device:

**For iOS**:
1. Open the App Store
2. Search for "Google Authenticator"
3. Tap "Get" to download and install
4. Open the app

**For Android**:
1. Open Google Play Store
2. Search for "Google Authenticator"
3. Tap "Install"
4. Open the app

#### 2. Obtain Your Credentials
You should have received:
- **Username**: Your unique username (e.g., john.smith)
- **Password**: Your initial password

If you haven't received credentials, contact your administrator.

### Accessing the Application

1. Open your web browser
2. Navigate to: `http://localhost:8080/mfa-poc/login`
   - Or use the URL provided by your administrator
3. You should see the login page

## First-Time Login

### Step 1: Enter Your Credentials

![Login Page](screenshots/login-page.png)

1. Enter your **username** in the first field
2. Enter your **password** in the second field
3. Click the **Login** button

**Example**:
```
Username: john.smith
Password: SecurePass123!
```

### Step 2: Automatic Redirect
After successful authentication, you will be automatically redirected to the MFA setup page.

## Setting Up Multi-Factor Authentication

### Overview
First-time users must set up MFA before accessing the application. This is a one-time process that takes about 2-3 minutes.

### Step 1: View the QR Code

![MFA Setup Page](screenshots/mfa-setup-page.png)

You will see:
- A large QR code
- Step-by-step instructions
- A manual entry code (if needed)
- An input field for verification

### Step 2: Scan the QR Code

1. Open **Google Authenticator** on your mobile device
2. Tap the **+** (plus) button
3. Select **Scan a QR code**
4. Point your camera at the QR code on the screen
5. Wait for the app to recognize and add the account

**What you'll see in Google Authenticator**:
```
MFA-POC (john.smith)
123 456
```
The 6-digit code refreshes every 30 seconds.

### Step 3: Manual Entry (Alternative)

If you cannot scan the QR code:

1. In Google Authenticator, tap **+** (plus)
2. Select **Enter a setup key**
3. Enter the following:
   - **Account name**: MFA-POC (john.smith)
   - **Your key**: Copy the code shown below the QR code
   - **Type of key**: Time based
4. Tap **Add**

**Example Manual Entry**:
```
Account: MFA-POC (john.smith)
Key: JBSWY3DPEHPK3PXP
Type: Time based
```

### Step 4: Verify Your Setup

1. Look at your Google Authenticator app
2. Find the 6-digit code for "MFA-POC (john.smith)"
3. Enter this code in the verification field on the web page
4. Click **Continue**

**Important**: 
- The code changes every 30 seconds
- Enter the current code before it expires
- If the code expires, wait for the next one

### Step 5: Setup Complete

![Welcome Page](screenshots/welcome-page.png)

After successful verification:
- You'll be redirected to the welcome page
- Your MFA setup is complete
- Future logins will require the authenticator code

## Logging In with MFA

### For Returning Users

Once MFA is set up, follow these steps for subsequent logins:

### Step 1: Enter Credentials

1. Navigate to the login page
2. Enter your **username**
3. Enter your **password**
4. Click **Login**

### Step 2: Enter TOTP Code

![MFA Verify Page](screenshots/mfa-verify-page.png)

1. You'll be redirected to the verification page
2. Open **Google Authenticator** on your mobile device
3. Find the code for "MFA-POC (your-username)"
4. Enter the 6-digit code
5. Click **Verify**

**Tips**:
- Wait for a fresh code if the current one is about to expire
- The code refreshes every 30 seconds
- You have 5 attempts before being locked out

### Step 3: Access Granted

After successful verification, you'll be taken to the welcome page where you can use the application.

## Using the Application

### Welcome Page

![Welcome Page Details](screenshots/welcome-details.png)

The welcome page displays:
- **Personalized greeting**: "Welcome, [Your Name]!"
- **User information card**:
  - Full Name
  - Email Address
  - Role (USER or ADMIN)
  - Account Status
  - MFA Status

### Navigation

**Logout Button**: Located in the top-right corner
- Click to end your session
- You'll be redirected to the login page

### Session Management

**Session Timeout**: 30 minutes of inactivity
- Your session will automatically expire after 30 minutes
- You'll need to log in again
- Any unsaved work will be lost

**Active Session**:
- Your session remains active while you're using the application
- Each interaction resets the timeout timer

## Logging Out

### How to Logout

1. Click the **Logout** button (top-right corner)
2. You'll be automatically redirected to the login page
3. Your session is completely terminated

### Why Logout is Important

Always logout when:
- You're finished using the application
- You're leaving your computer unattended
- You're using a shared or public computer
- You're switching users

**Security Note**: Logging out ensures no one else can access your account from your computer.

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: "Invalid username or password"

**Possible Causes**:
- Incorrect username or password
- Account has been disabled
- Caps Lock is on

**Solutions**:
1. Verify your username (case-sensitive)
2. Check your password (case-sensitive)
3. Ensure Caps Lock is off
4. Contact administrator if account is disabled

---

#### Issue 2: "Invalid verification code"

**Possible Causes**:
- Code has expired (30-second window)
- Wrong code entered
- Time synchronization issue

**Solutions**:
1. Wait for a fresh code and try again
2. Verify you're looking at the correct account in Google Authenticator
3. Check your device's time settings (should be automatic)
4. Ensure you're entering all 6 digits

**Time Synchronization (Android)**:
1. Open Google Authenticator
2. Tap the three dots (menu)
3. Select "Settings"
4. Tap "Time correction for codes"
5. Tap "Sync now"

**Time Synchronization (iOS)**:
1. Go to Settings > General > Date & Time
2. Enable "Set Automatically"

---

#### Issue 3: QR Code Not Displaying

**Possible Causes**:
- Browser compatibility issue
- Network connection problem
- Page didn't load completely

**Solutions**:
1. Refresh the page (F5 or Ctrl+R)
2. Try a different browser
3. Use manual entry method instead
4. Clear browser cache and try again

---

#### Issue 4: "Too many failed attempts"

**What Happened**:
- You entered an incorrect code 5 times
- Your session has been terminated for security

**Solution**:
1. Return to the login page
2. Log in again with your username and password
3. Be more careful when entering the TOTP code
4. Wait for a fresh code before entering

---

#### Issue 5: Lost Access to Authenticator App

**Scenarios**:
- Lost or replaced mobile device
- Uninstalled Google Authenticator
- Phone was reset

**Solution**:
Contact your administrator immediately. They can:
- Reset your MFA configuration
- Allow you to set up MFA again
- Provide temporary access

**Prevention**:
- Keep backup codes (if provided)
- Set up authenticator on multiple devices
- Note down your secret key during setup

---

#### Issue 6: Session Expired

**What Happened**:
- You were inactive for more than 30 minutes
- Your session automatically expired

**Solution**:
1. You'll be redirected to the login page
2. Log in again with your credentials
3. Complete MFA verification
4. Continue working

**Prevention**:
- Save your work frequently
- Stay active in the application
- Plan for the 30-minute timeout

---

#### Issue 7: Browser Compatibility Issues

**Symptoms**:
- Pages not loading correctly
- Buttons not working
- Layout appears broken

**Solutions**:
1. Update your browser to the latest version
2. Try a different browser:
   - Google Chrome (recommended)
   - Mozilla Firefox
   - Microsoft Edge
   - Safari (macOS)
3. Disable browser extensions temporarily
4. Clear browser cache and cookies

---

## Frequently Asked Questions

### General Questions

**Q: Do I need to set up MFA every time I log in?**  
A: No, MFA setup is a one-time process. After initial setup, you only need to enter the 6-digit code from your authenticator app.

**Q: Can I use MFA on multiple devices?**  
A: Yes, you can scan the same QR code with multiple devices during initial setup. Each device will generate the same codes.

**Q: What if I don't have a smartphone?**  
A: Contact your administrator. They may provide alternative authentication methods or a hardware token.

**Q: How long does the TOTP code last?**  
A: Each code is valid for 30 seconds. A new code is generated every 30 seconds.

**Q: Can I disable MFA?**  
A: No, MFA is required for all users. Contact your administrator if you have concerns.

### Security Questions

**Q: Is my password stored securely?**  
A: Yes, passwords are hashed using BCrypt with a high work factor. Plain text passwords are never stored.

**Q: What happens if someone steals my password?**  
A: They still cannot access your account without the TOTP code from your authenticator app.

**Q: Are my login attempts logged?**  
A: Yes, all authentication attempts (successful and failed) are logged for security auditing.

**Q: What if I suspect unauthorized access?**  
A: Contact your administrator immediately. They can:
- Review audit logs
- Reset your password
- Reset your MFA configuration

### Technical Questions

**Q: Why does the code keep changing?**  
A: TOTP codes are time-based and change every 30 seconds for security. This prevents replay attacks.

**Q: Can I use a different authenticator app?**  
A: Yes, any TOTP-compatible app works (Microsoft Authenticator, Authy, etc.). Google Authenticator is recommended.

**Q: What if my phone's time is wrong?**  
A: TOTP codes depend on accurate time. Enable automatic time synchronization on your device.

**Q: Can I access the application from multiple browsers?**  
A: Yes, but each browser session is independent. You'll need to log in separately in each browser.

## Security Best Practices

### Password Security

1. **Use a Strong Password**:
   - Minimum 8 characters
   - Mix of uppercase and lowercase letters
   - Include numbers and special characters
   - Avoid common words or patterns

2. **Keep Your Password Secret**:
   - Never share your password
   - Don't write it down
   - Don't save it in unsecured locations
   - Use a password manager if needed

3. **Change Your Password Regularly**:
   - Change every 90 days (if required)
   - Change immediately if compromised
   - Don't reuse old passwords

### Authenticator Security

1. **Protect Your Mobile Device**:
   - Use a device lock (PIN, pattern, biometric)
   - Keep your device with you
   - Don't leave it unattended

2. **Backup Your Authenticator**:
   - Set up on multiple devices if possible
   - Save the secret key securely
   - Keep backup codes (if provided)

3. **Report Lost Devices**:
   - Contact administrator immediately
   - Request MFA reset
   - Set up on new device

### Session Security

1. **Always Logout**:
   - When finished working
   - Before leaving your computer
   - On shared computers

2. **Don't Share Sessions**:
   - Each user should have their own account
   - Don't let others use your logged-in session
   - Don't share your screen while logged in

3. **Be Aware of Timeouts**:
   - Sessions expire after 30 minutes
   - Save work frequently
   - Plan for automatic logout

### General Security

1. **Use Secure Networks**:
   - Prefer trusted networks
   - Avoid public Wi-Fi when possible
   - Use VPN on untrusted networks

2. **Keep Software Updated**:
   - Update your browser regularly
   - Update Google Authenticator
   - Update your operating system

3. **Be Vigilant**:
   - Watch for phishing attempts
   - Verify the URL before logging in
   - Report suspicious activity

4. **Physical Security**:
   - Lock your computer when away
   - Don't leave devices unattended
   - Be aware of shoulder surfing

## Getting Help

### Contact Information

**Technical Support**:
- Email: support@prolifics.com
- Phone: 1-800-XXX-XXXX
- Hours: Monday-Friday, 9 AM - 5 PM EST

**Administrator**:
- For account issues
- For MFA resets
- For access problems

### What to Include When Reporting Issues

1. **Your Information**:
   - Username (never password)
   - Email address
   - Phone number

2. **Issue Details**:
   - What you were trying to do
   - What happened instead
   - Error messages (exact text)
   - When it occurred

3. **Environment**:
   - Browser and version
   - Operating system
   - Mobile device (for MFA issues)

### Self-Service Resources

- **User Guide**: This document
- **FAQ Section**: See above
- **Troubleshooting Guide**: See above
- **Video Tutorials**: (if available)

## Appendix

### Glossary

**MFA (Multi-Factor Authentication)**: Security method requiring multiple forms of verification

**TOTP (Time-based One-Time Password)**: Temporary code that changes every 30 seconds

**QR Code**: Square barcode scanned by your authenticator app

**Session**: Your active login period (expires after 30 minutes)

**Authenticator App**: Mobile application that generates TOTP codes

**BCrypt**: Secure password hashing algorithm

**Audit Log**: Record of all security events and login attempts

### Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Tab | Move to next field |
| Shift+Tab | Move to previous field |
| Enter | Submit form |
| Ctrl+R / F5 | Refresh page |

### Browser Recommendations

**Recommended Browsers** (latest versions):
- ✅ Google Chrome
- ✅ Mozilla Firefox
- ✅ Microsoft Edge
- ✅ Safari (macOS)

**Not Recommended**:
- ❌ Internet Explorer (any version)
- ❌ Outdated browser versions

---

**Document Version**: 1.0  
**Last Updated**: 2024-01-15  
**Author**: User Experience Team  
**Status**: Final

**Need Help?** Contact support@prolifics.com