package com.prolifics.mfa.servlet;

import com.prolifics.mfa.model.User;
import com.prolifics.mfa.repository.CSVUserRepository;
import com.prolifics.mfa.util.AuditLogger;
import com.prolifics.mfa.util.TOTPUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * MFAVerifyServlet handles TOTP verification for users with configured MFA.
 * 
 * GET: Display TOTP input form
 * POST: Verify TOTP code and complete login
 * 
 * Security:
 * - Requires authenticated session
 * - Rate limiting (max 5 failed attempts)
 * - Time-based validation (30-second window)
 * 
 * All verification attempts are logged to the audit log.
 * 
 * @author Bob
 * @version 1.0
 */
@WebServlet("/mfa-verify")
public class MFAVerifyServlet extends HttpServlet {
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
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
            throw new ServletException("Failed to initialize MFAVerifyServlet", e);
        }
    }
    
    /**
     * Display TOTP verification form.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Check if user is authenticated
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        String ipAddress = getClientIpAddress(request);
        
        // Check if user has MFA configured
        if (!user.hasMFA()) {
            // User needs to set up MFA first
            auditLogger.log(user.getUsername(), "MFA_VERIFY", "FAILED", ipAddress,
                          "User redirected to MFA setup - no MFA configured");
            response.sendRedirect(request.getContextPath() + "/mfa-setup");
            return;
        }
        
        // Check if already verified
        Boolean mfaVerified = (Boolean) session.getAttribute("mfaVerified");
        if (mfaVerified != null && mfaVerified) {
            // Already verified - go to welcome page
            response.sendRedirect(request.getContextPath() + "/welcome.jsp");
            return;
        }
        
        // Initialize failed attempts counter if not present
        if (session.getAttribute("mfaFailedAttempts") == null) {
            session.setAttribute("mfaFailedAttempts", 0);
        }
        
        // Forward to verification page
        request.getRequestDispatcher("/mfa-verify.jsp").forward(request, response);
    }
    
    /**
     * Verify TOTP code and complete login.
     * 
     * Validates the TOTP code against the user's secret. Tracks failed attempts
     * and locks out after maximum attempts exceeded.
     * 
     * @param request HTTP request containing TOTP code
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Check if user is authenticated
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        String ipAddress = getClientIpAddress(request);
        String totpCode = request.getParameter("totpCode");
        
        // Get failed attempts counter
        Integer failedAttempts = (Integer) session.getAttribute("mfaFailedAttempts");
        if (failedAttempts == null) {
            failedAttempts = 0;
        }
        
        // Check if max attempts exceeded
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            auditLogger.log(user.getUsername(), "MFA_VERIFY_FAILED", "FAILED", ipAddress, 
                          "Maximum verification attempts exceeded");
            
            // Invalidate session and redirect to login
            session.invalidate();
            request.setAttribute("error", "Maximum verification attempts exceeded. Please login again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // Validate input
        if (totpCode == null || totpCode.trim().isEmpty()) {
            auditLogger.log(user.getUsername(), "MFA_VERIFY_FAILED", "FAILED", ipAddress, 
                          "Empty TOTP code");
            
            request.setAttribute("error", "Please enter the 6-digit code from Google Authenticator");
            request.getRequestDispatcher("/mfa-verify.jsp").forward(request, response);
            return;
        }
        
        // Validate TOTP code format (6 digits)
        if (!totpCode.trim().matches("\\d{6}")) {
            auditLogger.log(user.getUsername(), "MFA_VERIFY_FAILED", "FAILED", ipAddress, 
                          "Invalid TOTP code format");
            
            request.setAttribute("error", "Code must be 6 digits");
            request.getRequestDispatcher("/mfa-verify.jsp").forward(request, response);
            return;
        }
        
        try {
            // Validate TOTP code
            if (TOTPUtil.validateTOTP(user.getMfaSecret(), totpCode.trim())) {
                // Valid code - mark as verified
                session.setAttribute("mfaVerified", true);
                session.removeAttribute("mfaFailedAttempts");
                
                // Log successful verification
                auditLogger.log(user.getUsername(), "MFA_VERIFY_SUCCESS", "SUCCESS", ipAddress, 
                              "TOTP verification successful");
                auditLogger.log(user.getUsername(), "LOGIN_SUCCESS", "SUCCESS", ipAddress, 
                              "User logged in with MFA");
                
                // Redirect to welcome page
                response.sendRedirect(request.getContextPath() + "/welcome.jsp");
                
            } else {
                // Invalid code - increment failed attempts
                failedAttempts++;
                session.setAttribute("mfaFailedAttempts", failedAttempts);
                
                auditLogger.log(user.getUsername(), "MFA_VERIFY_FAILED", "FAILED", ipAddress, 
                              "Invalid TOTP code (attempt " + failedAttempts + " of " + MAX_FAILED_ATTEMPTS + ")");
                
                int remainingAttempts = MAX_FAILED_ATTEMPTS - failedAttempts;
                
                if (remainingAttempts > 0) {
                    request.setAttribute("error", 
                        "Invalid code. " + remainingAttempts + " attempt(s) remaining.");
                } else {
                    // Max attempts reached - invalidate session
                    session.invalidate();
                    request.setAttribute("error", 
                        "Maximum verification attempts exceeded. Please login again.");
                    request.getRequestDispatcher("/login.jsp").forward(request, response);
                    return;
                }
                
                request.getRequestDispatcher("/mfa-verify.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            auditLogger.log(user.getUsername(), "MFA_VERIFY_FAILED", "FAILED", ipAddress, 
                          "System error: " + e.getMessage());
            
            request.setAttribute("error", "System error occurred. Please try again.");
            request.getRequestDispatcher("/mfa-verify.jsp").forward(request, response);
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
