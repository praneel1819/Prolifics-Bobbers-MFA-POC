package com.prolifics.mfa.servlet;

import com.prolifics.mfa.model.User;
import com.prolifics.mfa.util.AuditLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LogoutServlet handles user logout and session cleanup.
 * 
 * GET/POST: Invalidate session, clear attributes, and redirect to login
 * 
 * Security:
 * - Prevents session fixation attacks
 * - Clears all sensitive session data
 * - Logs logout events for audit trail
 * 
 * All logout events are logged to the audit log.
 * 
 * @author Bob
 * @version 1.0
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    private AuditLogger auditLogger;
    
    /**
     * Initialize servlet dependencies.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            String auditPath = getServletContext().getRealPath("/WEB-INF/classes/audit-log.csv");
            this.auditLogger = new AuditLogger(auditPath);
        } catch (Exception e) {
            throw new ServletException("Failed to initialize LogoutServlet", e);
        }
    }
    
    /**
     * Handle logout via GET request.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    /**
     * Handle logout via POST request.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleLogout(request, response);
    }
    
    /**
     * Process logout request.
     * 
     * Invalidates the session, clears all session attributes, logs the logout event,
     * and redirects to the login page.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String username = "unknown";
        String ipAddress = getClientIpAddress(request);
        
        // Get username before invalidating session
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                username = user.getUsername();
            }
            
            try {
                // Log logout event
                auditLogger.log(username, "LOGOUT", "SUCCESS", ipAddress, 
                              "User logged out successfully");
            } catch (Exception e) {
                // Log error but continue with logout
                System.err.println("Failed to log logout event: " + e.getMessage());
            }
            
            // Invalidate session to clear all attributes and prevent session fixation
            session.invalidate();
        }
        
        // Redirect to login page with logout message
        response.sendRedirect(request.getContextPath() + "/login?logout=true");
    }
    
    /**
     * Get client IP address from request.
     * Handles proxy headers (X-Forwarded-For) for accurate IP capture.
     * 
     * @param request HTTP request
     * @return client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // If multiple IPs in X-Forwarded-For, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress != null ? ipAddress : "unknown";
    }
}

// Made with Bob
