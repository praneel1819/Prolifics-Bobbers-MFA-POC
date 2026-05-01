package com.prolifics.mfa.util;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * Utility class for TOTP (Time-based One-Time Password) operations.
 * Implements RFC 6238 for Google Authenticator compatibility.
 * 
 * @author Prolifics MFA POC Team
 * @version 1.0.0
 */
public class TOTPUtil {
    
    private static final String ALGORITHM = "HmacSHA1";
    private static final int SECRET_SIZE = 20; // 160 bits
    private static final Duration TIME_STEP = Duration.ofSeconds(30);
    private static final int CODE_DIGITS = 6;
    private static final String ISSUER = "Prolifics-MFA-POC";
    
    /**
     * Private constructor to prevent instantiation
     */
    private TOTPUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Generate a new random secret key for TOTP
     * 
     * @return Base32 encoded secret key
     */
    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }
    
    /**
     * Generate QR code URL for Google Authenticator
     * 
     * @param username Username for the account
     * @param secret Base32 encoded secret key
     * @return QR code URL for Google Authenticator
     */
    public static String generateQRCodeURL(String username, String secret) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null or empty");
        }
        if (!isValidSecret(secret.trim())) {
            throw new IllegalArgumentException("Invalid Base32 secret format");
        }
        
        try {
            String accountName = URLEncoder.encode(username.trim(), StandardCharsets.UTF_8.toString());
            String issuerEncoded = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8.toString());
            
            String otpauthURL = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuerEncoded,
                accountName,
                secret.trim(),
                issuerEncoded,
                CODE_DIGITS,
                TIME_STEP.getSeconds()
            );
            
            // Return Google Charts API URL - encode only the otpauth URL for the chl parameter
            return String.format(
                "https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=%s",
                otpauthURL
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
    
    /**
     * Validate a TOTP code against a secret
     * Allows for time drift by checking adjacent time windows
     * 
     * @param secret Base32 encoded secret key
     * @param code 6-digit TOTP code to validate
     * @return true if code is valid, false otherwise
     */
    public static boolean validateTOTP(String secret, String code) {
        if (secret == null) {
            throw new IllegalArgumentException("Secret cannot be null");
        }
        if (code == null) {
            throw new IllegalArgumentException("TOTP code cannot be null");
        }
        
        // Handle empty strings - return false instead of throwing
        if (secret.trim().isEmpty() || code.trim().isEmpty()) {
            return false;
        }
        
        // Trim whitespace from code
        String trimmedCode = code.trim();
        
        if (trimmedCode.length() != CODE_DIGITS) {
            return false;
        }
        
        // Validate secret format
        if (!isValidSecret(secret.trim())) {
            return false;
        }
        
        try {
            Base32 base32 = new Base32();
            byte[] decodedKey = base32.decode(secret.trim());
            
            // Check current time window and adjacent windows (±1) for clock drift
            long currentTimeStep = Instant.now().getEpochSecond() / TIME_STEP.getSeconds();
            
            for (int i = -1; i <= 1; i++) {
                String generatedCode = generateTOTPCode(decodedKey, currentTimeStep + i);
                if (trimmedCode.equals(generatedCode)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error validating TOTP: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate TOTP code for current time
     * Useful for testing
     * 
     * @param secret Base32 encoded secret key
     * @return 6-digit TOTP code
     */
    public static String getTOTPCode(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null or empty");
        }
        if (!isValidSecret(secret.trim())) {
            throw new IllegalArgumentException("Invalid Base32 secret format");
        }
        
        try {
            Base32 base32 = new Base32();
            byte[] decodedKey = base32.decode(secret.trim());
            long currentTimeStep = Instant.now().getEpochSecond() / TIME_STEP.getSeconds();
            
            return generateTOTPCode(decodedKey, currentTimeStep);
        } catch (Exception e) {
            System.err.println("Error generating TOTP code: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate TOTP code for a specific time step
     * 
     * @param key Secret key bytes
     * @param timeStep Time step value
     * @return 6-digit TOTP code
     * @throws NoSuchAlgorithmException if HMAC algorithm not available
     * @throws InvalidKeyException if key is invalid
     */
    private static String generateTOTPCode(byte[] key, long timeStep) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        
        // Convert time step to byte array
        byte[] data = new byte[8];
        long value = timeStep;
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        
        // Generate HMAC-SHA1 hash
        SecretKeySpec signKey = new SecretKeySpec(key, ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        
        // Dynamic truncation (RFC 4226)
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                     ((hash[offset + 1] & 0xFF) << 16) |
                     ((hash[offset + 2] & 0xFF) << 8) |
                     (hash[offset + 3] & 0xFF);
        
        int otp = binary % (int) Math.pow(10, CODE_DIGITS);
        
        // Pad with leading zeros if necessary
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }
    
    /**
     * Get the remaining time in seconds until the current TOTP code expires
     * 
     * @return Seconds remaining in current time window
     */
    public static long getRemainingSeconds() {
        long currentTime = Instant.now().getEpochSecond();
        long timeStep = TIME_STEP.getSeconds();
        return timeStep - (currentTime % timeStep);
    }
    
    /**
     * Validate secret key format
     * 
     * @param secret Secret key to validate
     * @return true if secret is valid Base32, false otherwise
     */
    public static boolean isValidSecret(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = secret.trim();
        
        // Base32 alphabet: A-Z and 2-7 (case insensitive)
        // Invalid characters: 0, 1, 8, 9
        if (!trimmed.matches("^[A-Z2-7]+$")) {
            return false;
        }
        
        try {
            Base32 base32 = new Base32();
            byte[] decoded = base32.decode(trimmed);
            return decoded != null && decoded.length > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get TOTP configuration details
     * 
     * @return String describing TOTP configuration
     */
    public static String getTOTPConfiguration() {
        return String.format(
            "TOTP Configuration:\n" +
            "- Algorithm: %s\n" +
            "- Time Step: %d seconds\n" +
            "- Code Digits: %d\n" +
            "- Issuer: %s",
            ALGORITHM,
            TIME_STEP.getSeconds(),
            CODE_DIGITS,
            ISSUER
        );
    }
    
    /**
     * Generate a QR code data URL for embedding in HTML
     * Uses Google Charts API for QR code generation
     * 
     * @param username Username for the account
     * @param secret Base32 encoded secret key
     * @return QR code image URL
     */
    public static String generateQRCodeImageURL(String username, String secret) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null or empty");
        }
        
        String otpauthURL = generateQRCodeURL(username, secret);
        try {
            String encodedURL = URLEncoder.encode(otpauthURL, StandardCharsets.UTF_8.toString());
            return String.format(
                "https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=%s",
                encodedURL
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
}

// Made with Bob
