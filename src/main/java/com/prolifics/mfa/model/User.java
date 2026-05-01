package com.prolifics.mfa.model;

import java.io.Serializable;

/**
 * User model class representing a user in the system.
 * Contains user credentials, profile information, and MFA configuration.
 * 
 * @author Prolifics MFA POC Team
 * @version 1.0.0
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;  // BCrypt hashed
    private String fullName;
    private String email;
    private String status;    // ACTIVE or DISABLED
    private String role;      // USER or ADMIN
    private String mfaSecret; // TOTP secret (Base32 encoded), nullable
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Constructor with all fields
     * 
     * @param username User's unique username
     * @param password User's hashed password
     * @param fullName User's full name
     * @param email User's email address
     * @param status User's account status (ACTIVE/DISABLED)
     * @param role User's role (USER/ADMIN)
     * @param mfaSecret User's MFA secret key (nullable)
     */
    public User(String username, String password, String fullName, String email, 
                String status, String role, String mfaSecret) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.role = role;
        this.mfaSecret = mfaSecret;
    }
    
    // Getters and Setters
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getMfaSecret() {
        return mfaSecret;
    }
    
    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }
    
    // Utility methods
    
    /**
     * Check if the user account is active
     * 
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
    
    /**
     * Check if the user has MFA configured
     * 
     * @return true if mfaSecret is not null and not empty, false otherwise
     */
    public boolean hasMFA() {
        return mfaSecret != null && !mfaSecret.trim().isEmpty();
    }
    
    /**
     * Check if the user has admin role
     * 
     * @return true if role is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", role='" + role + '\'' +
                ", hasMFA=" + hasMFA() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username != null && username.equals(user.username);
    }
    
    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}

// Made with Bob
