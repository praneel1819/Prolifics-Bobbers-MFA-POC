<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.prolifics.mfa.model.User" %>
<%
    // Check if user is authenticated
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    String qrCodeURL = (String) session.getAttribute("qrCodeURL");
    String mfaSecret = (String) session.getAttribute("mfaSecret");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MFA Setup - MFA POC</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <!-- Header with Logo -->
    <div class="header">
        <img src="<%= request.getContextPath() %>/images/prolifics-logo.png" alt="Prolifics Logo" class="logo" onerror="this.style.display='none'">
    </div>

    <!-- Main Container -->
    <div class="container">
        <div class="card">
            <div class="card-header">
                <h1>Set Up Multi-Factor Authentication</h1>
                <p>Secure your account with Google Authenticator</p>
            </div>

            <!-- Error Message -->
            <% 
                String error = (String) request.getAttribute("error");
                if (error != null && !error.isEmpty()) {
            %>
                <div class="alert alert-error">
                    <%= error %>
                </div>
            <% } %>

            <!-- Instructions -->
            <div class="instructions">
                <h3>Setup Instructions</h3>
                <ol>
                    <li>Download <strong>Google Authenticator</strong> on your mobile device:
                        <ul style="margin-top: 5px; margin-left: 20px;">
                            <li><a href="https://apps.apple.com/app/google-authenticator/id388497605" target="_blank" class="link">iOS App Store</a></li>
                            <li><a href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2" target="_blank" class="link">Google Play Store</a></li>
                        </ul>
                    </li>
                    <li>Open the app and tap the <strong>+</strong> button</li>
                    <li>Select <strong>"Scan a QR code"</strong></li>
                    <li>Scan the QR code below with your device</li>
                    <li>Enter the 6-digit code from the app to verify</li>
                </ol>
            </div>

            <!-- QR Code Display -->
            <% if (qrCodeURL != null && !qrCodeURL.isEmpty()) { %>
                <div class="qr-code-container">
                    <img src="<%= qrCodeURL %>" alt="QR Code for MFA Setup">
                    
                    <% if (mfaSecret != null && !mfaSecret.isEmpty()) { %>
                        <div class="secret-key">
                            <strong>Manual Entry Key:</strong><br>
                            <%= mfaSecret %>
                        </div>
                        <p style="font-size: 12px; color: #666; margin-top: 10px;">
                            Can't scan? Enter this key manually in Google Authenticator
                        </p>
                    <% } %>
                </div>
            <% } %>

            <!-- Verification Form -->
            <form action="<%= request.getContextPath() %>/mfa-setup" method="post">
                <div class="form-group">
                    <label for="totpCode">Enter 6-Digit Code</label>
                    <input 
                        type="text" 
                        id="totpCode" 
                        name="totpCode" 
                        class="form-control totp-input" 
                        placeholder="000000"
                        maxlength="6"
                        pattern="\d{6}"
                        required
                        autofocus
                        autocomplete="off"
                    >
                    <small style="display: block; margin-top: 5px; color: #666;">
                        Enter the code shown in Google Authenticator
                    </small>
                </div>

                <button type="submit" class="btn btn-primary">Verify and Continue</button>
            </form>

            <!-- Help Text -->
            <div class="help-text">
                <p>Having trouble? <a href="#" class="link" onclick="location.reload(); return false;">Refresh to generate a new QR code</a></p>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <div class="footer">
        <p>&copy; 2024 Prolifics. All rights reserved. | MFA POC Application</p>
    </div>

    <script>
        // Auto-format TOTP input (digits only)
        const totpInput = document.getElementById('totpCode');
        
        totpInput.addEventListener('input', function(e) {
            // Remove non-digit characters
            this.value = this.value.replace(/\D/g, '');
            
            // Limit to 6 digits
            if (this.value.length > 6) {
                this.value = this.value.slice(0, 6);
            }
        });

        // Auto-submit when 6 digits entered
        totpInput.addEventListener('input', function(e) {
            if (this.value.length === 6) {
                // Optional: auto-submit after a short delay
                // setTimeout(() => this.form.submit(), 500);
            }
        });

        // Prevent paste of non-numeric content
        totpInput.addEventListener('paste', function(e) {
            e.preventDefault();
            const pastedText = (e.clipboardData || window.clipboardData).getData('text');
            const digitsOnly = pastedText.replace(/\D/g, '').slice(0, 6);
            this.value = digitsOnly;
        });

        // Clear error message on input
        totpInput.addEventListener('input', function() {
            const alert = document.querySelector('.alert-error');
            if (alert) {
                alert.style.display = 'none';
            }
        });
    </script>
</body>
</html>