package com.prolifics.mfa.servlet;

import com.prolifics.mfa.model.User;
import com.prolifics.mfa.repository.CSVUserRepository;
import com.prolifics.mfa.util.AuditLogger;
import com.prolifics.mfa.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LoginServlet handles user authentication.
 * 
 * GET: Display login form
 * POST: Process login credentials, validate user, and route to appropriate page
 * 
 * Routes:
 * - Valid credentials + no MFA -> MFA Setup
 * - Valid credentials + has MFA -> MFA Verify
 * - Invalid credentials -> Login page with error
 * - Disabled account -> Login page with error
 * 
 * All login attempts are logged to the audit log.
 * 
 * @author Bob
 * @version 1.0
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    private CSVUserRepository userRepository;
    private AuditLogger auditLogger;
    
    /**
     * Initialize servlet dependencies.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // Get the real path directly to the CSV files
            String usersPath = getServletContext().getRealPath("/WEB-INF/classes/users.csv");
            String auditPath = getServletContext().getRealPath("/WEB-INF/classes/audit-log.csv");
            
            this.userRepository = new CSVUserRepository(usersPath);
            this.auditLogger = new AuditLogger(auditPath);
        } catch (Exception e) {
            throw new ServletException("Failed to initialize LoginServlet", e);
        }
    }
    
    /**
     * Display login form.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Forward to login page
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    /**
     * Process login credentials.
     * 
     * Validates username and password, checks account status,
     * and routes user to appropriate page based on MFA configuration.
     * 
     * @param request HTTP request containing username and password
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String ipAddress = getClientIpAddress(request);
        
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            auditLogger.log(username != null ? username : "unknown", 
                          "LOGIN_ATTEMPT", "FAILED", ipAddress, 
                          "Empty username or password");
            
            request.setAttribute("error", "Username and password are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        try {
            // Find user
            User user = userRepository.findByUsername(username.trim());
            
            if (user == null) {
                // User not found
                auditLogger.log(username, "LOGIN_ATTEMPT", "FAILED", ipAddress, 
                              "User not found");
                
                request.setAttribute("error", "Invalid username or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Check if account is active
            if (!user.isActive()) {
                auditLogger.log(username, "LOGIN_ATTEMPT", "FAILED", ipAddress, 
                              "Account disabled");
                
                request.setAttribute("error", "Account is disabled. Please contact administrator.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Verify password
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                auditLogger.log(username, "LOGIN_ATTEMPT", "FAILED", ipAddress, 
                              "Invalid password");
                
                request.setAttribute("error", "Invalid username or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            
            // Password is valid - log successful login attempt
            auditLogger.log(username, "LOGIN_ATTEMPT", "SUCCESS", ipAddress, 
                          "Valid credentials");
            
            // Create session and store user
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("authenticated", true);
            session.setAttribute("mfaVerified", false);
            
            // Route based on MFA configuration
            if (user.hasMFA()) {
                // User has MFA configured - go to verification
                response.sendRedirect(request.getContextPath() + "/mfa-verify");
            } else {
                // User needs to set up MFA
                response.sendRedirect(request.getContextPath() + "/mfa-setup");
            }
            
        } catch (Exception e) {
            // Log system error
            auditLogger.log(username, "LOGIN_ATTEMPT", "FAILED", ipAddress, 
                          "System error: " + e.getMessage());
            
            request.setAttribute("error", "System error occurred. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
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
