package com.prolifics.mfa.util;

import com.prolifics.mfa.model.AuditLog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for audit logging operations.
 * Provides thread-safe logging of security events to CSV file.
 * 
 * @author Prolifics MFA POC Team
 * @version 1.0.0
 */
public class AuditLogger {
    
    private final String auditLogFile;
    private static final String CSV_HEADER = "timestamp,username,action,status,ipAddress,details";
    private final Object lock = new Object();
    
    /**
     * Default constructor using default file path
     */
    public AuditLogger() {
        this("src/main/resources/audit-log.csv");
    }
    
    /**
     * Constructor with custom file path
     * 
     * @param filePath Path to the audit log CSV file
     */
    public AuditLogger(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        this.auditLogFile = filePath;
        initializeAuditLog();
    }
    
    /**
     * Initialize audit log file with header if it doesn't exist
     */
    private void initializeAuditLog() {
        File file = new File(auditLogFile);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println(CSV_HEADER);
                }
            } catch (IOException e) {
                System.err.println("Error initializing audit log: " + e.getMessage());
            }
        }
    }
    
    /**
     * Log an audit entry
     * 
     * @param username Username performing the action
     * @param action Action type (LOGIN_ATTEMPT, MFA_SETUP, etc.)
     * @param status Action status (SUCCESS/FAILED)
     * @param ipAddress Client IP address
     * @param details Additional details about the event
     */
    public void log(String username, String action, String status, 
                          String ipAddress, String details) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (ipAddress == null) {
            throw new IllegalArgumentException("IP address cannot be null");
        }
        
        synchronized (lock) {
            try {
                AuditLog auditLog = new AuditLog(username, action, status, ipAddress, details);
                String csvLine = auditLog.toCSVString();
                
                // Append to file
                Files.write(
                    Paths.get(auditLogFile),
                    (csvLine + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
                );
                
                // Also log to console for monitoring
                System.out.println("[AUDIT] " + auditLog);
                
            } catch (IOException e) {
                System.err.println("Error writing audit log: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Log a login attempt
     * 
     * @param username Username attempting to login
     * @param success Whether the login was successful
     * @param ipAddress Client IP address
     */
    public void logLoginAttempt(String username, boolean success, String ipAddress) {
        String action = success ? AuditLog.LOGIN_SUCCESS : AuditLog.LOGIN_FAILED;
        String status = success ? AuditLog.STATUS_SUCCESS : AuditLog.STATUS_FAILED;
        String details = success ? "Valid credentials" : "Invalid credentials";
        
        log(username, action, status, ipAddress, details);
    }
    
    /**
     * Log MFA setup event
     * 
     * @param username Username setting up MFA
     * @param ipAddress Client IP address
     */
    public void logMFASetup(String username, String ipAddress) {
        log(username, AuditLog.MFA_SETUP, AuditLog.STATUS_SUCCESS, 
            ipAddress, "MFA setup initiated - QR code generated");
    }
    
    /**
     * Log MFA setup completion
     * 
     * @param username Username completing MFA setup
     * @param ipAddress Client IP address
     */
    public void logMFASetupComplete(String username, String ipAddress) {
        log(username, AuditLog.MFA_SETUP, AuditLog.STATUS_SUCCESS, 
            ipAddress, "MFA setup completed successfully");
    }
    
    /**
     * Log MFA verification attempt
     * 
     * @param username Username attempting MFA verification
     * @param success Whether the verification was successful
     * @param ipAddress Client IP address
     */
    public void logMFAVerification(String username, boolean success, String ipAddress) {
        String action = success ? AuditLog.MFA_VERIFY_SUCCESS : AuditLog.MFA_VERIFY_FAILED;
        String status = success ? AuditLog.STATUS_SUCCESS : AuditLog.STATUS_FAILED;
        String details = success ? "Valid TOTP code" : "Invalid TOTP code";
        
        log(username, action, status, ipAddress, details);
    }
    
    /**
     * Log MFA verification with attempt count
     * 
     * @param username Username attempting MFA verification
     * @param success Whether the verification was successful
     * @param ipAddress Client IP address
     * @param attemptCount Number of failed attempts
     */
    public void logMFAVerification(String username, boolean success, 
                                         String ipAddress, int attemptCount) {
        String action = success ? AuditLog.MFA_VERIFY_SUCCESS : AuditLog.MFA_VERIFY_FAILED;
        String status = success ? AuditLog.STATUS_SUCCESS : AuditLog.STATUS_FAILED;
        String details = success ? 
            "Valid TOTP code" : 
            "Invalid TOTP code - attempt " + attemptCount;
        
        log(username, action, status, ipAddress, details);
    }
    
    /**
     * Log logout event
     * 
     * @param username Username logging out
     * @param ipAddress Client IP address
     */
    public void logLogout(String username, String ipAddress) {
        log(username, AuditLog.LOGOUT, AuditLog.STATUS_SUCCESS, 
            ipAddress, "User logged out");
    }
    
    /**
     * Log disabled account login attempt
     * 
     * @param username Username of disabled account
     * @param ipAddress Client IP address
     */
    public void logDisabledAccountAttempt(String username, String ipAddress) {
        log(username, AuditLog.LOGIN_FAILED, AuditLog.STATUS_FAILED, 
            ipAddress, "Account disabled");
    }
    
    /**
     * Log session timeout
     * 
     * @param username Username whose session timed out
     * @param ipAddress Client IP address
     */
    public void logSessionTimeout(String username, String ipAddress) {
        log(username, AuditLog.LOGOUT, AuditLog.STATUS_SUCCESS, 
            ipAddress, "Session timeout");
    }
    
    /**
     * Get all audit log entries
     * Useful for testing and reporting
     * 
     * @return List of audit log entries
     */
    public List<AuditLog> getAuditLogs() {
        List<AuditLog> logs = new ArrayList<>();
        
        synchronized (lock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(auditLogFile))) {
                String line;
                boolean firstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    // Skip header
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    
                    AuditLog auditLog = AuditLog.fromCSVString(line);
                    if (auditLog != null) {
                        logs.add(auditLog);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading audit log: " + e.getMessage());
            }
        }
        
        return logs;
    }
    
    /**
     * Get audit logs for a specific user
     * 
     * @param username Username to filter by
     * @return List of audit log entries for the user
     */
    public List<AuditLog> getAuditLogsForUser(String username) {
        List<AuditLog> allLogs = getAuditLogs();
        List<AuditLog> userLogs = new ArrayList<>();
        
        for (AuditLog log : allLogs) {
            if (username.equals(log.getUsername())) {
                userLogs.add(log);
            }
        }
        
        return userLogs;
    }
    
    /**
     * Get audit logs by action type
     * 
     * @param action Action type to filter by
     * @return List of audit log entries for the action
     */
    public List<AuditLog> getAuditLogsByAction(String action) {
        List<AuditLog> allLogs = getAuditLogs();
        List<AuditLog> actionLogs = new ArrayList<>();
        
        for (AuditLog log : allLogs) {
            if (action.equals(log.getAction())) {
                actionLogs.add(log);
            }
        }
        
        return actionLogs;
    }
    
    /**
     * Get count of failed login attempts for a user
     * 
     * @param username Username to check
     * @return Number of failed login attempts
     */
    public int getFailedLoginCount(String username) {
        List<AuditLog> userLogs = getAuditLogsForUser(username);
        int count = 0;
        
        for (AuditLog log : userLogs) {
            if (AuditLog.LOGIN_FAILED.equals(log.getAction())) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Clear audit log file (for testing purposes only)
     * WARNING: This will delete all audit history
     */
    public void clearAuditLog() {
        synchronized (lock) {
            try {
                File file = new File(auditLogFile);
                if (file.exists()) {
                    file.delete();
                }
                initializeAuditLog();
            } catch (Exception e) {
                System.err.println("Error clearing audit log: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get audit log file path
     * 
     * @return Audit log file path
     */
    public String getAuditLogFilePath() {
        return auditLogFile;
    }
}

// Made with Bob
