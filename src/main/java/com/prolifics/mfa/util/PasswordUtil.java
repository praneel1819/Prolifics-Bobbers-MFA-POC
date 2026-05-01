package com.prolifics.mfa.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and validation using BCrypt.
 * Implements security best practices for password management.
 * 
 * @author Prolifics MFA POC Team
 * @version 1.0.0
 */
public class PasswordUtil {
    
    // BCrypt work factor (log2 rounds) - higher is more secure but slower
    private static final int BCRYPT_ROUNDS = 12;
    
    // Password policy constants
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    /**
     * Private constructor to prevent instantiation
     */
    private PasswordUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Hash a password using BCrypt with salt
     * 
     * @param plainPassword Plain text password to hash
     * @return BCrypt hashed password
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Verify a password against a BCrypt hash
     *
     * @param plainPassword Plain text password to verify
     * @param hashedPassword BCrypt hashed password to compare against
     * @return true if password matches, false otherwise
     * @throws IllegalArgumentException if plainPassword or hashedPassword is null, or if hash format is invalid
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (hashedPassword == null) {
            throw new IllegalArgumentException("Hashed password cannot be null");
        }
        
        // Validate BCrypt hash format
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$") && !hashedPassword.startsWith("$2y$")) {
            throw new IllegalArgumentException("Invalid BCrypt hash format");
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // BCrypt throws IllegalArgumentException for invalid hash format
            throw new IllegalArgumentException("Invalid BCrypt hash format: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate password against security policy
     * Password must:
     * - Be at least 8 characters long
     * - Contain at least one uppercase letter
     * - Contain at least one lowercase letter
     * - Contain at least one digit
     * - Contain at least one special character (@$!%*?&)
     * 
     * @param password Password to validate
     * @return true if password meets policy requirements, false otherwise
     */
    public static boolean validatePasswordPolicy(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        return password.matches(PASSWORD_PATTERN);
    }
    
    /**
     * Get password policy requirements as a human-readable string
     * 
     * @return Password policy description
     */
    public static String getPasswordPolicyDescription() {
        return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long and contain:\n" +
               "- At least one uppercase letter (A-Z)\n" +
               "- At least one lowercase letter (a-z)\n" +
               "- At least one digit (0-9)\n" +
               "- At least one special character (@$!%*?&)";
    }
    
    /**
     * Generate a secure random password that meets policy requirements
     * Useful for testing and temporary passwords
     * 
     * @return Randomly generated secure password
     */
    public static String generateSecurePassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String allChars = uppercase + lowercase + digits + special;
        
        StringBuilder password = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        
        // Ensure at least one character from each required category
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        // Fill remaining characters randomly
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Check if a password needs to be rehashed (e.g., if work factor has changed)
     * 
     * @param hashedPassword BCrypt hashed password to check
     * @return true if password should be rehashed, false otherwise
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return true;
        }
        
        try {
            // Extract work factor from hash
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 3) {
                return true;
            }
            
            int currentRounds = Integer.parseInt(parts[2]);
            return currentRounds < BCRYPT_ROUNDS;
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Get detailed validation errors for a password
     * 
     * @param password Password to validate
     * @return String describing validation errors, or empty string if valid
     */
    public static String getPasswordValidationErrors(String password) {
        if (password == null) {
            return "Password cannot be null";
        }
        
        StringBuilder errors = new StringBuilder();
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.append("Password must be at least ").append(MIN_PASSWORD_LENGTH)
                  .append(" characters long. ");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            errors.append("Password must contain at least one uppercase letter. ");
        }
        
        if (!password.matches(".*[a-z].*")) {
            errors.append("Password must contain at least one lowercase letter. ");
        }
        
        if (!password.matches(".*\\d.*")) {
            errors.append("Password must contain at least one digit. ");
        }
        
        if (!password.matches(".*[@$!%*?&].*")) {
            errors.append("Password must contain at least one special character (@$!%*?&). ");
        }
        
        return errors.toString().trim();
    }
}

// Made with Bob
