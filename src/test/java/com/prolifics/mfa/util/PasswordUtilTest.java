package com.prolifics.mfa.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil class.
 * Tests BCrypt password hashing and verification functionality.
 * 
 * @author Bob
 * @version 1.0
 */
@DisplayName("PasswordUtil Tests")
class PasswordUtilTest {

    @Test
    @DisplayName("Should hash password successfully")
    void testHashPassword() {
        // Given
        String plainPassword = "SecurePass123!";
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        
        // Then
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(plainPassword, hashedPassword, "Hashed password should differ from plain password");
        assertTrue(hashedPassword.startsWith("$2a$"), "BCrypt hash should start with $2a$");
        assertTrue(hashedPassword.length() >= 60, "BCrypt hash should be at least 60 characters");
    }

    @Test
    @DisplayName("Should verify correct password")
    void testVerifyPasswordSuccess() {
        // Given
        String plainPassword = "SecurePass123!";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        
        // When
        boolean isValid = PasswordUtil.verifyPassword(plainPassword, hashedPassword);
        
        // Then
        assertTrue(isValid, "Password verification should succeed for correct password");
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testVerifyPasswordFailure() {
        // Given
        String plainPassword = "SecurePass123!";
        String wrongPassword = "WrongPassword456!";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        
        // When
        boolean isValid = PasswordUtil.verifyPassword(wrongPassword, hashedPassword);
        
        // Then
        assertFalse(isValid, "Password verification should fail for incorrect password");
    }

    @Test
    @DisplayName("Should generate unique hashes for same password")
    void testHashUniqueness() {
        // Given
        String plainPassword = "SecurePass123!";
        
        // When
        String hash1 = PasswordUtil.hashPassword(plainPassword);
        String hash2 = PasswordUtil.hashPassword(plainPassword);
        
        // Then
        assertNotEquals(hash1, hash2, "BCrypt should generate unique hashes due to random salt");
        
        // But both should verify correctly
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hash1), "First hash should verify");
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hash2), "Second hash should verify");
    }

    @Test
    @DisplayName("Should handle empty password")
    void testEmptyPassword() {
        // Given
        String emptyPassword = "";
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(emptyPassword);
        
        // Then
        assertNotNull(hashedPassword, "Should hash empty password");
        assertTrue(PasswordUtil.verifyPassword(emptyPassword, hashedPassword), 
                  "Should verify empty password");
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testSpecialCharacters() {
        // Given
        String specialPassword = "P@$$w0rd!#$%^&*()_+-=[]{}|;:',.<>?/~`";
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(specialPassword);
        
        // Then
        assertNotNull(hashedPassword, "Should hash password with special characters");
        assertTrue(PasswordUtil.verifyPassword(specialPassword, hashedPassword), 
                  "Should verify password with special characters");
    }

    @Test
    @DisplayName("Should handle long password")
    void testLongPassword() {
        // Given
        String longPassword = "A".repeat(100) + "1!";
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(longPassword);
        
        // Then
        assertNotNull(hashedPassword, "Should hash long password");
        assertTrue(PasswordUtil.verifyPassword(longPassword, hashedPassword), 
                  "Should verify long password");
    }

    @Test
    @DisplayName("Should handle Unicode characters")
    void testUnicodePassword() {
        // Given
        String unicodePassword = "Pässwörd123!你好";
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(unicodePassword);
        
        // Then
        assertNotNull(hashedPassword, "Should hash password with Unicode characters");
        assertTrue(PasswordUtil.verifyPassword(unicodePassword, hashedPassword), 
                  "Should verify password with Unicode characters");
    }

    @Test
    @DisplayName("Should reject null password for hashing")
    void testHashNullPassword() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> PasswordUtil.hashPassword(null),
                    "Should throw exception for null password");
    }

    @Test
    @DisplayName("Should reject null password for verification")
    void testVerifyNullPassword() {
        // Given
        String hashedPassword = PasswordUtil.hashPassword("test");
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> PasswordUtil.verifyPassword(null, hashedPassword),
                    "Should throw exception for null password");
    }

    @Test
    @DisplayName("Should reject null hash for verification")
    void testVerifyNullHash() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> PasswordUtil.verifyPassword("test", null),
                    "Should throw exception for null hash");
    }

    @Test
    @DisplayName("Should reject invalid hash format")
    void testVerifyInvalidHash() {
        // Given
        String invalidHash = "not-a-valid-bcrypt-hash";
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> PasswordUtil.verifyPassword("test", invalidHash),
                    "Should throw exception for invalid hash format");
    }

    @Test
    @DisplayName("Should be case-sensitive")
    void testCaseSensitivity() {
        // Given
        String password = "SecurePass123!";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // When
        boolean lowerCase = PasswordUtil.verifyPassword("securepass123!", hashedPassword);
        boolean upperCase = PasswordUtil.verifyPassword("SECUREPASS123!", hashedPassword);
        
        // Then
        assertFalse(lowerCase, "Password verification should be case-sensitive (lowercase)");
        assertFalse(upperCase, "Password verification should be case-sensitive (uppercase)");
    }

    @Test
    @DisplayName("Should handle whitespace in password")
    void testWhitespacePassword() {
        // Given
        String passwordWithSpaces = "  Secure Pass 123!  ";
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(passwordWithSpaces);
        
        // Then
        assertTrue(PasswordUtil.verifyPassword(passwordWithSpaces, hashedPassword), 
                  "Should preserve whitespace in password");
        assertFalse(PasswordUtil.verifyPassword(passwordWithSpaces.trim(), hashedPassword), 
                   "Trimmed password should not match");
    }

    @Test
    @DisplayName("Should generate secure random password")
    void testGenerateSecurePassword() {
        // When
        String password1 = PasswordUtil.generateSecurePassword();
        String password2 = PasswordUtil.generateSecurePassword();
        
        // Then
        assertNotNull(password1, "Generated password should not be null");
        assertNotNull(password2, "Generated password should not be null");
        assertNotEquals(password1, password2, "Generated passwords should be unique");
        assertTrue(password1.length() >= 12, "Generated password should be at least 12 characters");
        assertTrue(password2.length() >= 12, "Generated password should be at least 12 characters");
        
        // Verify it contains required character types
        assertTrue(password1.matches(".*[A-Z].*"), "Should contain uppercase letter");
        assertTrue(password1.matches(".*[a-z].*"), "Should contain lowercase letter");
        assertTrue(password1.matches(".*[0-9].*"), "Should contain digit");
        assertTrue(password1.matches(".*[!@#$%^&*].*"), "Should contain special character");
    }

    @Test
    @DisplayName("Should hash and verify generated password")
    void testHashGeneratedPassword() {
        // Given
        String generatedPassword = PasswordUtil.generateSecurePassword();
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(generatedPassword);
        
        // Then
        assertTrue(PasswordUtil.verifyPassword(generatedPassword, hashedPassword), 
                  "Should verify generated password");
    }
}

// Made with Bob
