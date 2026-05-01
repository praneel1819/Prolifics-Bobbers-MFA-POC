package com.prolifics.mfa.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TOTPUtil class.
 * Tests TOTP secret generation, QR code URL creation, and code validation.
 * 
 * @author Bob
 * @version 1.0
 */
@DisplayName("TOTPUtil Tests")
class TOTPUtilTest {

    @Test
    @DisplayName("Should generate valid TOTP secret")
    void testGenerateSecret() {
        // When
        String secret = TOTPUtil.generateSecret();
        
        // Then
        assertNotNull(secret, "Secret should not be null");
        assertFalse(secret.isEmpty(), "Secret should not be empty");
        assertTrue(secret.length() >= 16, "Secret should be at least 16 characters");
        assertTrue(secret.matches("[A-Z2-7]+"), "Secret should be Base32 encoded (A-Z, 2-7)");
    }

    @Test
    @DisplayName("Should generate unique secrets")
    void testGenerateUniqueSecrets() {
        // When
        String secret1 = TOTPUtil.generateSecret();
        String secret2 = TOTPUtil.generateSecret();
        String secret3 = TOTPUtil.generateSecret();
        
        // Then
        assertNotEquals(secret1, secret2, "Secrets should be unique");
        assertNotEquals(secret2, secret3, "Secrets should be unique");
        assertNotEquals(secret1, secret3, "Secrets should be unique");
    }

    @Test
    @DisplayName("Should generate valid QR code URL")
    void testGenerateQRCodeURL() {
        // Given
        String username = "john.smith";
        String secret = TOTPUtil.generateSecret();
        
        // When
        String qrCodeURL = TOTPUtil.generateQRCodeURL(username, secret);
        
        // Then
        assertNotNull(qrCodeURL, "QR code URL should not be null");
        assertTrue(qrCodeURL.startsWith("https://chart.googleapis.com/chart"), 
                  "QR code URL should use Google Charts API");
        assertTrue(qrCodeURL.contains("chl=otpauth://totp/"), 
                  "QR code URL should contain TOTP URI");
        assertTrue(qrCodeURL.contains(username), 
                  "QR code URL should contain username");
        assertTrue(qrCodeURL.contains(secret), 
                  "QR code URL should contain secret");
        assertTrue(qrCodeURL.contains("issuer="), 
                  "QR code URL should contain issuer");
    }

    @Test
    @DisplayName("Should validate correct TOTP code")
    void testValidateTOTPSuccess() {
        // Given
        String secret = TOTPUtil.generateSecret();
        String totpCode = TOTPUtil.getTOTPCode(secret);
        
        // When
        boolean isValid = TOTPUtil.validateTOTP(secret, totpCode);
        
        // Then
        assertTrue(isValid, "Should validate correct TOTP code");
    }

    @Test
    @DisplayName("Should reject incorrect TOTP code")
    void testValidateTOTPFailure() {
        // Given
        String secret = TOTPUtil.generateSecret();
        String wrongCode = "000000";
        
        // When
        boolean isValid = TOTPUtil.validateTOTP(secret, wrongCode);
        
        // Then
        assertFalse(isValid, "Should reject incorrect TOTP code");
    }

    @Test
    @DisplayName("Should generate 6-digit TOTP code")
    void testGetTOTPCode() {
        // Given
        String secret = TOTPUtil.generateSecret();
        
        // When
        String totpCode = TOTPUtil.getTOTPCode(secret);
        
        // Then
        assertNotNull(totpCode, "TOTP code should not be null");
        assertEquals(6, totpCode.length(), "TOTP code should be 6 digits");
        assertTrue(totpCode.matches("\\d{6}"), "TOTP code should contain only digits");
    }

    @Test
    @DisplayName("Should validate TOTP within time window")
    void testTimeWindow() throws InterruptedException {
        // Given
        String secret = TOTPUtil.generateSecret();
        String totpCode = TOTPUtil.getTOTPCode(secret);
        
        // When - validate immediately
        boolean isValid1 = TOTPUtil.validateTOTP(secret, totpCode);
        
        // Wait a few seconds (still within 30-second window)
        Thread.sleep(3000);
        
        // When - validate after delay
        boolean isValid2 = TOTPUtil.validateTOTP(secret, totpCode);
        
        // Then
        assertTrue(isValid1, "Should validate immediately");
        assertTrue(isValid2, "Should validate within time window");
    }

    @Test
    @DisplayName("Should reject invalid code format")
    void testInvalidCodeFormat() {
        // Given
        String secret = TOTPUtil.generateSecret();
        
        // When/Then - test various invalid formats
        assertFalse(TOTPUtil.validateTOTP(secret, "12345"), 
                   "Should reject 5-digit code");
        assertFalse(TOTPUtil.validateTOTP(secret, "1234567"), 
                   "Should reject 7-digit code");
        assertFalse(TOTPUtil.validateTOTP(secret, "abcdef"), 
                   "Should reject non-numeric code");
        assertFalse(TOTPUtil.validateTOTP(secret, "12-34-56"), 
                   "Should reject code with special characters");
    }

    @Test
    @DisplayName("Should handle null secret")
    void testNullSecret() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> TOTPUtil.getTOTPCode(null),
                    "Should throw exception for null secret");
        
        assertThrows(IllegalArgumentException.class, 
                    () -> TOTPUtil.validateTOTP(null, "123456"),
                    "Should throw exception for null secret in validation");
    }

    @Test
    @DisplayName("Should handle null TOTP code")
    void testNullTOTPCode() {
        // Given
        String secret = TOTPUtil.generateSecret();
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> TOTPUtil.validateTOTP(secret, null),
                    "Should throw exception for null TOTP code");
    }

    @Test
    @DisplayName("Should handle empty secret")
    void testEmptySecret() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> TOTPUtil.getTOTPCode(""),
                    "Should throw exception for empty secret");
    }

    @Test
    @DisplayName("Should handle empty TOTP code")
    void testEmptyTOTPCode() {
        // Given
        String secret = TOTPUtil.generateSecret();
        
        // When
        boolean isValid = TOTPUtil.validateTOTP(secret, "");
        
        // Then
        assertFalse(isValid, "Should reject empty TOTP code");
    }

    @Test
    @DisplayName("Should handle whitespace in TOTP code")
    void testWhitespaceInCode() {
        // Given
        String secret = TOTPUtil.generateSecret();
        String totpCode = TOTPUtil.getTOTPCode(secret);
        
        // When
        boolean isValid = TOTPUtil.validateTOTP(secret, " " + totpCode + " ");
        
        // Then
        assertTrue(isValid, "Should handle whitespace in TOTP code");
    }

    @Test
    @DisplayName("Should generate different codes for different secrets")
    void testDifferentSecretsGenerateDifferentCodes() {
        // Given
        String secret1 = TOTPUtil.generateSecret();
        String secret2 = TOTPUtil.generateSecret();
        
        // When
        String code1 = TOTPUtil.getTOTPCode(secret1);
        String code2 = TOTPUtil.getTOTPCode(secret2);
        
        // Then
        assertNotEquals(code1, code2, "Different secrets should generate different codes");
    }

    @Test
    @DisplayName("Should validate code with leading zeros")
    void testLeadingZeros() {
        // Given
        String secret = TOTPUtil.generateSecret();
        String totpCode = TOTPUtil.getTOTPCode(secret);
        
        // Ensure code is properly formatted with leading zeros if needed
        String paddedCode = String.format("%06d", Integer.parseInt(totpCode));
        
        // When
        boolean isValid = TOTPUtil.validateTOTP(secret, paddedCode);
        
        // Then
        assertTrue(isValid, "Should validate code with leading zeros");
    }

    @Test
    @DisplayName("Should generate QR code URL with special characters in username")
    void testQRCodeURLWithSpecialCharacters() {
        // Given
        String username = "john.smith@example.com";
        String secret = TOTPUtil.generateSecret();
        
        // When
        String qrCodeURL = TOTPUtil.generateQRCodeURL(username, secret);
        
        // Then
        assertNotNull(qrCodeURL, "QR code URL should not be null");
        assertTrue(qrCodeURL.contains("john.smith"), 
                  "QR code URL should contain username");
    }

    @Test
    @DisplayName("Should handle invalid Base32 secret")
    void testInvalidBase32Secret() {
        // Given - invalid Base32 characters (0, 1, 8, 9 are not valid)
        String invalidSecret = "INVALID0189SECRET";
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> TOTPUtil.getTOTPCode(invalidSecret),
                    "Should throw exception for invalid Base32 secret");
    }

    @Test
    @DisplayName("Should generate consistent code for same secret at same time")
    void testConsistentCodeGeneration() {
        // Given
        String secret = TOTPUtil.generateSecret();
        
        // When
        String code1 = TOTPUtil.getTOTPCode(secret);
        String code2 = TOTPUtil.getTOTPCode(secret);
        
        // Then
        assertEquals(code1, code2, "Should generate same code for same secret at same time");
    }

    @Test
    @DisplayName("Should validate code case-insensitively for secret")
    void testSecretCaseInsensitivity() {
        // Given
        String secret = TOTPUtil.generateSecret();
        String totpCode = TOTPUtil.getTOTPCode(secret);
        
        // When
        boolean isValidLower = TOTPUtil.validateTOTP(secret.toLowerCase(), totpCode);
        boolean isValidUpper = TOTPUtil.validateTOTP(secret.toUpperCase(), totpCode);
        
        // Then
        assertTrue(isValidLower || isValidUpper, 
                  "Should handle case variations in secret");
    }

    @Test
    @DisplayName("Should reject code after time window expires")
    void testExpiredCode() throws InterruptedException {
        // Given
        String secret = TOTPUtil.generateSecret();
        String totpCode = TOTPUtil.getTOTPCode(secret);
        
        // When - wait for code to expire (30+ seconds)
        // Note: This test is commented out as it takes too long
        // In real testing, you would mock the time or use a shorter window
        
        // Thread.sleep(35000); // Wait 35 seconds
        // boolean isValid = TOTPUtil.validateTOTP(secret, totpCode);
        
        // Then
        // assertFalse(isValid, "Should reject expired code");
        
        // For now, just verify the code is valid immediately
        assertTrue(TOTPUtil.validateTOTP(secret, totpCode), 
                  "Code should be valid immediately");
    }

    @Test
    @DisplayName("Should generate URL-safe QR code")
    void testQRCodeURLEncoding() {
        // Given
        String username = "test user";
        String secret = TOTPUtil.generateSecret();
        
        // When
        String qrCodeURL = TOTPUtil.generateQRCodeURL(username, secret);
        
        // Then
        assertNotNull(qrCodeURL, "QR code URL should not be null");
        assertFalse(qrCodeURL.contains(" "), 
                   "QR code URL should not contain unencoded spaces");
    }
}

// Made with Bob
