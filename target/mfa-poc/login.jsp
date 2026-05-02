<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - MFA POC</title>
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
                <h1>Welcome Back</h1>
                <p>Sign in to access your account</p>
            </div>

            <!-- Logout Success Message -->
            <% if (request.getParameter("logout") != null) { %>
                <div class="alert alert-success">
                    You have been successfully logged out.
                </div>
            <% } %>

            <!-- Error Message -->
            <% 
                String error = (String) request.getAttribute("error");
                if (error != null && !error.isEmpty()) {
            %>
                <div class="alert alert-error">
                    <%= error %>
                </div>
            <% } %>

            <!-- Login Form -->
            <form action="<%= request.getContextPath() %>/login" method="post">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input 
                        type="text" 
                        id="username" 
                        name="username" 
                        class="form-control" 
                        placeholder="Enter your username"
                        required
                        autofocus
                        value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>"
                    >
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input 
                        type="password" 
                        id="password" 
                        name="password" 
                        class="form-control" 
                        placeholder="Enter your password"
                        required
                    >
                </div>

                <button type="submit" class="btn btn-primary">Sign In</button>
            </form>

            <!-- Help Text -->
            <div class="help-text">
                <p>Test Users:</p>
                <p style="font-size: 12px; color: #999; margin-top: 10px;">
                    john.smith / SecurePass123!<br>
                    jane.doe / Welcome2024!<br>
                    admin.user / Admin@2024
                </p>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <div class="footer">
        <p>&copy; 2024 Prolifics. All rights reserved. | MFA POC Application</p>
    </div>

    <script>
        // Auto-focus on username field
        document.addEventListener('DOMContentLoaded', function() {
            const usernameField = document.getElementById('username');
            if (usernameField && !usernameField.value) {
                usernameField.focus();
            }
        });

        // Clear error message on input
        const inputs = document.querySelectorAll('.form-control');
        inputs.forEach(input => {
            input.addEventListener('input', function() {
                const alert = document.querySelector('.alert-error');
                if (alert) {
                    alert.style.display = 'none';
                }
            });
        });
    </script>
</body>
</html>