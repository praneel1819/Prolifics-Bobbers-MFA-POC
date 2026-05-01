package com.prolifics.mfa.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLog model class representing an audit log entry.
 * Captures security-related events for compliance and monitoring.
 * 
 * @author Prolifics MFA POC Team
 * @version 1.0.0
 */
public class AuditLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // Action types
    public static final String LOGIN_ATTEMPT = "LOGIN_ATTEMPT";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String MFA_SETUP = "MFA_SETUP";
    public static final String MFA_VERIFY_SUCCESS = "MFA_VERIFY_SUCCESS";
    public static final String MFA_VERIFY_FAILED = "MFA_VERIFY_FAILED";
    public static final String LOGOUT = "LOGOUT";
    
    // Status types
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    
    private LocalDateTime timestamp;
    private String username;
    private String action;
    private String status;
    private String ipAddress;
    private String details;
    
    /**
     * Default constructor
     */
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor with all fields
     * 
     * @param timestamp Event timestamp
     * @param username Username performing the action
     * @param action Action type (LOGIN_ATTEMPT, MFA_SETUP, etc.)
     * @param status Action status (SUCCESS/FAILED)
     * @param ipAddress Client IP address
     * @param details Additional details about the event
     */
    public AuditLog(LocalDateTime timestamp, String username, String action, 
                    String status, String ipAddress, String details) {
        this.timestamp = timestamp;
        this.username = username;
        this.action = action;
        this.status = status;
        this.ipAddress = ipAddress;
        this.details = details;
    }
    
    /**
     * Constructor with current timestamp
     * 
     * @param username Username performing the action
     * @param action Action type
     * @param status Action status
     * @param ipAddress Client IP address
     * @param details Additional details
     */
    public AuditLog(String username, String action, String status, 
                    String ipAddress, String details) {
        this(LocalDateTime.now(), username, action, status, ipAddress, details);
    }
    
    // Getters and Setters
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    /**
     * Convert audit log to CSV string format
     * 
     * @return CSV formatted string
     */
    public String toCSVString() {
        return String.format("%s,%s,%s,%s,%s,\"%s\"",
                timestamp.format(FORMATTER),
                escapeCsv(username),
                escapeCsv(action),
                escapeCsv(status),
                escapeCsv(ipAddress),
                escapeCsv(details));
    }
    
    /**
     * Parse audit log from CSV string
     * 
     * @param csvLine CSV line to parse
     * @return AuditLog object
     */
    public static AuditLog fromCSVString(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = parseCsvLine(csvLine);
        if (parts.length < 6) {
            return null;
        }
        
        try {
            LocalDateTime timestamp = LocalDateTime.parse(parts[0], FORMATTER);
            return new AuditLog(timestamp, parts[1], parts[2], parts[3], parts[4], parts[5]);
        } catch (Exception e) {
            System.err.println("Error parsing audit log: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Escape special characters for CSV format
     * 
     * @param value String to escape
     * @return Escaped string
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Replace quotes with double quotes and handle commas
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Parse CSV line handling quoted fields
     * 
     * @param line CSV line to parse
     * @return Array of field values
     */
    private static String[] parseCsvLine(String line) {
        String[] result = new String[6];
        int fieldIndex = 0;
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length() && fieldIndex < 6; i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result[fieldIndex++] = currentField.toString();
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        if (fieldIndex < 6) {
            result[fieldIndex] = currentField.toString();
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        return "AuditLog{" +
                "timestamp=" + timestamp +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", status='" + status + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}

// Made with Bob
