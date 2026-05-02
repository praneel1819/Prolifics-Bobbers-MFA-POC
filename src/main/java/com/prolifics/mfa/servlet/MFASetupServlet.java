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
 * MFASetupServlet handles Multi-Factor Authentication setup for new users.
 * 
 * GET: Generate TOTP secret, create QR code URL, and display setup page
 * POST: Verify initial TOTP code and save secret to user profile
 * 
 * Security:
 * - Requires authenticated session
 * - One-time setup per user
 * - Secret stored temporarily in session until verified
 * 
 * All MFA setup activities are logged to the audit log.
 * 
 * @author Bob
 * @version 1.0
 */
@WebServlet("/mfa-setup")
public class MFASetupServlet extends HttpServlet {
    
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
            throw new ServletException("Failed to initialize MFASetupServlet", e);
        }
    }
    
    /**
     * Display MFA setup page with QR code.
     * 
     * Generates a new TOTP secret, creates QR code URL for Google Authenticator,
     * and stores the secret temporarily in the session.
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
        
        // Check if user already has MFA configured
        if (user.hasMFA()) {
            // User already has MFA - redirect to verification
            auditLogger.log(user.getUsername(), "MFA_SETUP", "FAILED", ipAddress,
                          "User redirected to MFA verify - MFA already configured");
            response.sendRedirect(request.getContextPath() + "/mfa-verify");
            return;
        }
        
        // Log that we're starting MFA setup
        auditLogger.log(user.getUsername(), "MFA_SETUP", "INITIATED", ipAddress,
                      "User accessing MFA setup page");
        
        try {
            // Generate new TOTP secret
            String secret = TOTPUtil.generateSecret();
            
            // Create QR code URL for Google Authenticator
            String qrCodeURL = TOTPUtil.generateQRCodeURL(user.getUsername(), secret);
            
            // Store secret temporarily in session (not saved to CSV yet)
            session.setAttribute("mfaSecret", secret);
            session.setAttribute("qrCodeURL", qrCodeURL);
            
            // Log MFA setup initiation
            auditLogger.log(user.getUsername(), "MFA_SETUP", "SUCCESS", ipAddress, 
                          "QR code generated for MFA setup");
            
            // Forward to setup page
            request.getRequestDispatcher("/mfa-setup.jsp").forward(request, response);
            
        } catch (Exception e) {
            auditLogger.log(user.getUsername(), "MFA_SETUP", "FAILED", ipAddress, 
                          "Error generating QR code: " + e.getMessage());
            
            request.setAttribute("error", "Failed to generate MFA setup. Please try again.");
            request.getRequestDispatcher("/mfa-setup.jsp").forward(request, response);
        }
    }
    
    /**
     * Verify initial TOTP code and complete MFA setup.
     * 
     * Validates the TOTP code entered by the user. If valid, saves the secret
     * to the user's profile and marks MFA as verified.
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
        
        // Validate input
        if (totpCode == null || totpCode.trim().isEmpty()) {
            auditLogger.log(user.getUsername(), "MFA_SETUP", "FAILED", ipAddress, 
                          "Empty TOTP code");
            
            request.setAttribute("error", "Please enter the 6-digit code from Google Authenticator");
            request.getRequestDispatcher("/mfa-setup.jsp").forward(request, response);
            return;
        }
        
        // Get secret from session
        String secret = (String) session.getAttribute("mfaSecret");
        
        if (secret == null) {
            auditLogger.log(user.getUsername(), "MFA_SETUP", "FAILED", ipAddress, 
                          "No secret in session");
            
            request.setAttribute("error", "Session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/mfa-setup");
            return;
        }
        
        try {
            // Validate TOTP code
            if (TOTPUtil.validateTOTP(secret, totpCode.trim())) {
                // Valid code - save secret to user profile
                userRepository.updateMFASecret(user.getUsername(), secret);
                
                // Update user object in session
                user.setMfaSecret(secret);
                session.setAttribute("user", user);
                session.setAttribute("mfaVerified", true);
                
                // Remove temporary session attributes
                session.removeAttribute("mfaSecret");
                session.removeAttribute("qrCodeURL");
                
                // Log successful MFA setup
                auditLogger.log(user.getUsername(), "MFA_SETUP", "SUCCESS", ipAddress, 
                              "MFA setup completed successfully");
                auditLogger.log(user.getUsername(), "MFA_VERIFY_SUCCESS", "SUCCESS", ipAddress, 
                              "Initial TOTP verification successful");
                auditLogger.log(user.getUsername(), "LOGIN_SUCCESS", "SUCCESS", ipAddress, 
                              "User logged in with MFA");
                
                // Redirect to welcome page
                response.sendRedirect(request.getContextPath() + "/welcome.jsp");
                
            } else {
                // Invalid code
                auditLogger.log(user.getUsername(), "MFA_SETUP", "FAILED", ipAddress, 
                              "Invalid TOTP code during setup");
                
                request.setAttribute("error", "Invalid code. Please try again.");
                request.getRequestDispatcher("/mfa-setup.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            auditLogger.log(user.getUsername(), "MFA_SETUP", "FAILED", ipAddress, 
                          "System error: " + e.getMessage());
            
            request.setAttribute("error", "System error occurred. Please try again.");
            request.getRequestDispatcher("/mfa-setup.jsp").forward(request, response);
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
