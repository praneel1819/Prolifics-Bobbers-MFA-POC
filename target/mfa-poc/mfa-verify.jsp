<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.prolifics.mfa.model.User" %>
<%
    // Check if user is authenticated
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    // Get failed attempts counter
    Integer failedAttempts = (Integer) session.getAttribute("mfaFailedAttempts");
    if (failedAttempts == null) {
        failedAttempts = 0;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MFA Verification - MFA POC</title>
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
                <h1>Two-Factor Authentication</h1>
                <p>Welcome back, <strong><%= user.getFullName() %></strong></p>
            </div>

            <!-- Info Message -->
            <div class="alert alert-info">
                <strong>Security Check:</strong> Please enter the 6-digit code from your Google Authenticator app to complete login.
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

            <!-- Failed Attempts Warning -->
            <% if (failedAttempts > 0 && failedAttempts < 5) { %>
                <div class="alert alert-error">
                    <strong>Warning:</strong> <%= failedAttempts %> failed attempt(s). 
                    <%= (5 - failedAttempts) %> attempt(s) remaining before lockout.
                </div>
            <% } %>

            <!-- Verification Form -->
            <form action="<%= request.getContextPath() %>/mfa-verify" method="post">
                <div class="form-group">
                    <label for="totpCode">Authentication Code</label>
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
                        Enter the 6-digit code from Google Authenticator
                    </small>
                </div>

                <button type="submit" class="btn btn-primary">Verify Code</button>
            </form>

            <!-- Help Text -->
            <div class="help-text">
                <p>Don't have access to your authenticator?</p>
                <p style="margin-top: 10px;">
                    <a href="<%= request.getContextPath() %>/logout" class="link">Back to Login</a>
                </p>
            </div>

            <!-- Instructions -->
            <div class="instructions" style="margin-top: 20px;">
                <h3>How to use Google Authenticator</h3>
                <ol>
                    <li>Open the <strong>Google Authenticator</strong> app on your mobile device</li>
                    <li>Find the entry for <strong>MFA-POC</strong> with your username</li>
                    <li>Enter the 6-digit code shown in the app</li>
                    <li>The code refreshes every 30 seconds</li>
                </ol>
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

        // Auto-submit when 6 digits entered (optional)
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

        // Auto-focus on input field
        document.addEventListener('DOMContentLoaded', function() {
            totpInput.focus();
        });

        // Add visual feedback for code expiration (30 seconds)
        let countdown = 30;
        const updateCountdown = () => {
            countdown--;
            if (countdown <= 0) {
                countdown = 30;
            }
            // Optional: Display countdown timer
            // document.getElementById('countdown').textContent = countdown;
        };
        
        // Update every second
        setInterval(updateCountdown, 1000);
    </script>
</body>
</html>