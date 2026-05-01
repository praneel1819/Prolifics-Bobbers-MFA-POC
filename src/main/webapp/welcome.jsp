<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.prolifics.mfa.model.User" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    // Check if user is authenticated and MFA verified
    User user = (User) session.getAttribute("user");
    Boolean authenticated = (Boolean) session.getAttribute("authenticated");
    Boolean mfaVerified = (Boolean) session.getAttribute("mfaVerified");
    
    if (user == null || authenticated == null || !authenticated || mfaVerified == null || !mfaVerified) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    // Format current time
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
    String currentTime = now.format(formatter);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome - MFA POC</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <!-- Header with Logo -->
    <div class="header">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <img src="<%= request.getContextPath() %>/images/prolifics-logo.png" alt="Prolifics Logo" class="logo" onerror="this.style.display='none'">
            <form action="<%= request.getContextPath() %>/logout" method="post" style="margin: 0;">
                <button type="submit" class="btn btn-logout">Logout</button>
            </form>
        </div>
    </div>

    <!-- Main Container -->
    <div class="container">
        <div class="welcome-container">
            <!-- Welcome Header -->
            <div class="welcome-header">
                <h1>Welcome, <%= user.getFullName() %>! 🎉</h1>
                <p>You have successfully logged in with Multi-Factor Authentication</p>
            </div>

            <!-- User Information Card -->
            <div class="user-info-card">
                <h2>Account Information</h2>
                
                <div class="info-row">
                    <div class="info-label">Full Name:</div>
                    <div class="info-value"><%= user.getFullName() %></div>
                </div>
                
                <div class="info-row">
                    <div class="info-label">Username:</div>
                    <div class="info-value"><%= user.getUsername() %></div>
                </div>
                
                <div class="info-row">
                    <div class="info-label">Email:</div>
                    <div class="info-value"><%= user.getEmail() %></div>
                </div>
                
                <div class="info-row">
                    <div class="info-label">Role:</div>
                    <div class="info-value">
                        <span class="badge <%= user.getRole().equals("ADMIN") ? "badge-warning" : "badge-primary" %>">
                            <%= user.getRole() %>
                        </span>
                    </div>
                </div>
                
                <div class="info-row">
                    <div class="info-label">Account Status:</div>
                    <div class="info-value">
                        <span class="badge badge-success">ACTIVE</span>
                    </div>
                </div>
                
                <div class="info-row">
                    <div class="info-label">MFA Status:</div>
                    <div class="info-value">
                        <span class="badge badge-success">✓ ENABLED</span>
                    </div>
                </div>
                
                <div class="info-row">
                    <div class="info-label">Login Time:</div>
                    <div class="info-value"><%= currentTime %></div>
                </div>
                
                <div class="info-row">
                    <div class="info-label">Session ID:</div>
                    <div class="info-value" style="font-family: monospace; font-size: 12px; color: #999;">
                        <%= session.getId().substring(0, 16) %>...
                    </div>
                </div>
            </div>

            <!-- Quick Actions Card -->
            <div class="actions-card">
                <h2>Quick Actions</h2>
                
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-top: 20px;">
                    <div style="padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center;">
                        <div style="font-size: 32px; margin-bottom: 10px;">🔐</div>
                        <h3 style="font-size: 16px; color: #667eea; margin-bottom: 5px;">Security</h3>
                        <p style="font-size: 13px; color: #666;">Your account is protected with MFA</p>
                    </div>
                    
                    <div style="padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center;">
                        <div style="font-size: 32px; margin-bottom: 10px;">📱</div>
                        <h3 style="font-size: 16px; color: #667eea; margin-bottom: 5px;">Authenticator</h3>
                        <p style="font-size: 13px; color: #666;">Google Authenticator configured</p>
                    </div>
                    
                    <div style="padding: 20px; background: #f8f9fa; border-radius: 8px; text-align: center;">
                        <div style="font-size: 32px; margin-bottom: 10px;">✅</div>
                        <h3 style="font-size: 16px; color: #667eea; margin-bottom: 5px;">Verified</h3>
                        <p style="font-size: 13px; color: #666;">Identity confirmed successfully</p>
                    </div>
                </div>

                <!-- Logout Button -->
                <div style="margin-top: 30px;">
                    <form action="<%= request.getContextPath() %>/logout" method="post">
                        <button type="submit" class="btn btn-logout" style="width: auto; padding: 12px 40px;">
                            Logout Securely
                        </button>
                    </form>
                </div>
            </div>

            <!-- Information Box -->
            <div class="alert alert-info" style="margin-top: 20px;">
                <strong>🛡️ Security Notice:</strong> This is a proof-of-concept application demonstrating 
                Multi-Factor Authentication using Google Authenticator. All login attempts and activities 
                are logged for security audit purposes.
            </div>

            <!-- Features List -->
            <div class="user-info-card" style="margin-top: 20px;">
                <h2>Application Features</h2>
                <ul style="margin-left: 20px; color: #555; line-height: 2;">
                    <li>✓ Secure password authentication with BCrypt hashing</li>
                    <li>✓ Time-based One-Time Password (TOTP) verification</li>
                    <li>✓ Google Authenticator integration</li>
                    <li>✓ Comprehensive audit logging</li>
                    <li>✓ Session management and security</li>
                    <li>✓ Rate limiting on failed attempts</li>
                    <li>✓ Professional user interface</li>
                    <li>✓ Responsive design for mobile devices</li>
                </ul>
            </div>

            <!-- Technology Stack -->
            <div class="user-info-card" style="margin-top: 20px;">
                <h2>Technology Stack</h2>
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 10px; margin-top: 15px;">
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">Java 11</strong>
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">J2EE Servlets</strong>
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">JSP</strong>
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">Maven</strong>
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">Tomcat</strong>
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">BCrypt</strong>
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">TOTP</strong>
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-radius: 6px; text-align: center;">
                        <strong style="color: #667eea;">Google Auth</strong>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <div class="footer">
        <p>&copy; 2024 Prolifics. All rights reserved. | MFA POC Application | Session Active</p>
    </div>

    <script>
        // Session timeout warning (optional)
        let sessionTimeout = 30 * 60 * 1000; // 30 minutes
        let warningTime = 5 * 60 * 1000; // 5 minutes before timeout
        
        setTimeout(() => {
            if (confirm('Your session will expire in 5 minutes. Do you want to stay logged in?')) {
                // Refresh the page to keep session alive
                location.reload();
            }
        }, sessionTimeout - warningTime);

        // Prevent back button after logout
        window.history.pushState(null, "", window.location.href);
        window.onpopstate = function() {
            window.history.pushState(null, "", window.location.href);
        };
    </script>
</body>
</html>