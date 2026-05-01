package com.prolifics.mfa.repository;

import com.prolifics.mfa.model.User;
import com.prolifics.mfa.util.PasswordUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CSVUserRepository class.
 * Tests CSV file operations, user management, and thread safety.
 * 
 * @author Bob
 * @version 1.0
 */
@DisplayName("CSVUserRepository Tests")
class CSVUserRepositoryTest {

    @TempDir
    Path tempDir;
    
    private File testCsvFile;
    private CSVUserRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary CSV file for testing
        testCsvFile = tempDir.resolve("test-users.csv").toFile();
        
        // Write test data
        try (FileWriter writer = new FileWriter(testCsvFile)) {
            writer.write("username,password,fullName,email,status,role,mfaSecret\n");
            writer.write("john.smith," + PasswordUtil.hashPassword("SecurePass123!") + ",John Smith,john@example.com,ACTIVE,USER,\n");
            writer.write("jane.doe," + PasswordUtil.hashPassword("Welcome2024!") + ",Jane Doe,jane@example.com,ACTIVE,USER,JBSWY3DPEHPK3PXP\n");
            writer.write("admin.user," + PasswordUtil.hashPassword("Admin@2024") + ",Admin User,admin@example.com,ACTIVE,ADMIN,JBSWY3DPEHPK3PXQ\n");
            writer.write("disabled.user," + PasswordUtil.hashPassword("Disabled123!") + ",Disabled User,disabled@example.com,DISABLED,USER,\n");
        }
        
        // Initialize repository
        repository = new CSVUserRepository(testCsvFile.getAbsolutePath());
    }

    @Test
    @DisplayName("Should load users from CSV file")
    void testLoadUsers() {
        // When
        List<User> users = repository.getAllUsers();
        
        // Then
        assertNotNull(users, "Users list should not be null");
        assertEquals(4, users.size(), "Should load 4 users");
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        // When
        User user = repository.findByUsername("john.smith");
        
        // Then
        assertNotNull(user, "User should be found");
        assertEquals("john.smith", user.getUsername());
        assertEquals("John Smith", user.getFullName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("ACTIVE", user.getStatus());
        assertEquals("USER", user.getRole());
        assertNull(user.getMfaSecret(), "MFA secret should be null");
    }

    @Test
    @DisplayName("Should return null for non-existent user")
    void testFindNonExistentUser() {
        // When
        User user = repository.findByUsername("nonexistent.user");
        
        // Then
        assertNull(user, "Should return null for non-existent user");
    }

    @Test
    @DisplayName("Should find user with MFA configured")
    void testFindUserWithMFA() {
        // When
        User user = repository.findByUsername("jane.doe");
        
        // Then
        assertNotNull(user, "User should be found");
        assertTrue(user.hasMFA(), "User should have MFA configured");
        assertEquals("JBSWY3DPEHPK3PXP", user.getMfaSecret());
    }

    @Test
    @DisplayName("Should update MFA secret")
    void testUpdateMFASecret() {
        // Given
        String username = "john.smith";
        String newSecret = "NEWSECRETKEY1234";
        
        // When
        repository.updateMFASecret(username, newSecret);
        
        // Then
        User updatedUser = repository.findByUsername(username);
        assertNotNull(updatedUser, "User should still exist");
        assertEquals(newSecret, updatedUser.getMfaSecret(), "MFA secret should be updated");
        assertTrue(updatedUser.hasMFA(), "User should have MFA configured");
    }

    @Test
    @DisplayName("Should persist MFA secret update to file")
    void testPersistMFASecret() throws IOException {
        // Given
        String username = "john.smith";
        String newSecret = "NEWSECRETKEY1234";
        
        // When
        repository.updateMFASecret(username, newSecret);
        
        // Create new repository instance to reload from file
        CSVUserRepository newRepository = new CSVUserRepository(testCsvFile.getAbsolutePath());
        
        // Then
        User user = newRepository.findByUsername(username);
        assertNotNull(user, "User should exist in reloaded repository");
        assertEquals(newSecret, user.getMfaSecret(), "MFA secret should persist");
    }

    @Test
    @DisplayName("Should handle case-sensitive username lookup")
    void testCaseSensitiveUsername() {
        // When
        User user1 = repository.findByUsername("john.smith");
        User user2 = repository.findByUsername("JOHN.SMITH");
        User user3 = repository.findByUsername("John.Smith");
        
        // Then
        assertNotNull(user1, "Should find lowercase username");
        assertNull(user2, "Should not find uppercase username");
        assertNull(user3, "Should not find mixed case username");
    }

    @Test
    @DisplayName("Should load all user properties correctly")
    void testUserProperties() {
        // When
        User user = repository.findByUsername("admin.user");
        
        // Then
        assertNotNull(user);
        assertEquals("admin.user", user.getUsername());
        assertEquals("Admin User", user.getFullName());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals("ACTIVE", user.getStatus());
        assertEquals("ADMIN", user.getRole());
        assertEquals("JBSWY3DPEHPK3PXQ", user.getMfaSecret());
        assertTrue(user.isActive());
        assertTrue(user.hasMFA());
    }

    @Test
    @DisplayName("Should handle disabled user")
    void testDisabledUser() {
        // When
        User user = repository.findByUsername("disabled.user");
        
        // Then
        assertNotNull(user);
        assertEquals("DISABLED", user.getStatus());
        assertFalse(user.isActive(), "User should not be active");
    }

    @Test
    @DisplayName("Should handle empty CSV file")
    void testEmptyCSVFile() throws IOException {
        // Given - create empty CSV file
        File emptyFile = tempDir.resolve("empty-users.csv").toFile();
        try (FileWriter writer = new FileWriter(emptyFile)) {
            writer.write("username,password,fullName,email,status,role,mfaSecret\n");
        }
        
        // When
        CSVUserRepository emptyRepository = new CSVUserRepository(emptyFile.getAbsolutePath());
        List<User> users = emptyRepository.getAllUsers();
        
        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty(), "Should return empty list for empty CSV");
    }

    @Test
    @DisplayName("Should handle missing CSV file")
    void testMissingCSVFile() {
        // Given
        String nonExistentPath = tempDir.resolve("nonexistent.csv").toString();
        
        // When/Then
        assertThrows(RuntimeException.class, 
                    () -> new CSVUserRepository(nonExistentPath),
                    "Should throw exception for missing file");
    }

    @Test
    @DisplayName("Should handle malformed CSV line")
    void testMalformedCSVLine() throws IOException {
        // Given - create CSV with malformed line
        File malformedFile = tempDir.resolve("malformed-users.csv").toFile();
        try (FileWriter writer = new FileWriter(malformedFile)) {
            writer.write("username,password,fullName,email,status,role,mfaSecret\n");
            writer.write("john.smith," + PasswordUtil.hashPassword("test") + ",John Smith,john@example.com,ACTIVE,USER,\n");
            writer.write("incomplete.line,password\n"); // Malformed line
            writer.write("jane.doe," + PasswordUtil.hashPassword("test") + ",Jane Doe,jane@example.com,ACTIVE,USER,\n");
        }
        
        // When
        CSVUserRepository malformedRepository = new CSVUserRepository(malformedFile.getAbsolutePath());
        List<User> users = malformedRepository.getAllUsers();
        
        // Then
        assertNotNull(users);
        assertEquals(2, users.size(), "Should skip malformed line and load valid users");
    }

    @Test
    @DisplayName("Should handle special characters in user data")
    void testSpecialCharacters() throws IOException {
        // Given
        File specialFile = tempDir.resolve("special-users.csv").toFile();
        try (FileWriter writer = new FileWriter(specialFile)) {
            writer.write("username,password,fullName,email,status,role,mfaSecret\n");
            writer.write("test.user," + PasswordUtil.hashPassword("test") + ",Test O'Brien,test@example.com,ACTIVE,USER,\n");
        }
        
        // When
        CSVUserRepository specialRepository = new CSVUserRepository(specialFile.getAbsolutePath());
        User user = specialRepository.findByUsername("test.user");
        
        // Then
        assertNotNull(user);
        assertEquals("Test O'Brien", user.getFullName(), "Should handle apostrophe in name");
    }

    @Test
    @DisplayName("Should update existing user MFA secret")
    void testUpdateExistingMFASecret() {
        // Given
        String username = "jane.doe";
        String oldSecret = "JBSWY3DPEHPK3PXP";
        String newSecret = "NEWSECRETKEY5678";
        
        User userBefore = repository.findByUsername(username);
        assertEquals(oldSecret, userBefore.getMfaSecret());
        
        // When
        repository.updateMFASecret(username, newSecret);
        
        // Then
        User userAfter = repository.findByUsername(username);
        assertEquals(newSecret, userAfter.getMfaSecret(), "Should update existing MFA secret");
    }

    @Test
    @DisplayName("Should handle null MFA secret update")
    void testNullMFASecretUpdate() {
        // Given
        String username = "john.smith";
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> repository.updateMFASecret(username, null),
                    "Should throw exception for null MFA secret");
    }

    @Test
    @DisplayName("Should handle empty MFA secret update")
    void testEmptyMFASecretUpdate() {
        // Given
        String username = "john.smith";
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> repository.updateMFASecret(username, ""),
                    "Should throw exception for empty MFA secret");
    }

    @Test
    @DisplayName("Should handle update for non-existent user")
    void testUpdateNonExistentUser() {
        // Given
        String username = "nonexistent.user";
        String secret = "NEWSECRETKEY1234";
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                    () -> repository.updateMFASecret(username, secret),
                    "Should throw exception for non-existent user");
    }

    @Test
    @DisplayName("Should maintain user count after update")
    void testUserCountAfterUpdate() {
        // Given
        int initialCount = repository.getAllUsers().size();
        
        // When
        repository.updateMFASecret("john.smith", "NEWSECRET123");
        
        // Then
        int finalCount = repository.getAllUsers().size();
        assertEquals(initialCount, finalCount, "User count should remain the same");
    }

    @Test
    @DisplayName("Should preserve other user data during MFA update")
    void testPreserveUserDataDuringUpdate() {
        // Given
        String username = "john.smith";
        User userBefore = repository.findByUsername(username);
        
        // When
        repository.updateMFASecret(username, "NEWSECRET123");
        
        // Then
        User userAfter = repository.findByUsername(username);
        assertEquals(userBefore.getUsername(), userAfter.getUsername());
        assertEquals(userBefore.getFullName(), userAfter.getFullName());
        assertEquals(userBefore.getEmail(), userAfter.getEmail());
        assertEquals(userBefore.getStatus(), userAfter.getStatus());
        assertEquals(userBefore.getRole(), userAfter.getRole());
        assertEquals(userBefore.getPassword(), userAfter.getPassword());
    }

    @Test
    @DisplayName("Should handle concurrent reads")
    void testConcurrentReads() throws InterruptedException {
        // Given
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final boolean[] results = new boolean[threadCount];
        
        // When - multiple threads reading simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                User user = repository.findByUsername("john.smith");
                results[index] = (user != null);
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - all reads should succeed
        for (boolean result : results) {
            assertTrue(result, "All concurrent reads should succeed");
        }
    }

    @Test
    @DisplayName("Should handle whitespace in username")
    void testWhitespaceInUsername() {
        // When
        User user1 = repository.findByUsername(" john.smith ");
        User user2 = repository.findByUsername("john.smith");
        
        // Then
        assertNull(user1, "Should not find user with whitespace");
        assertNotNull(user2, "Should find user without whitespace");
    }

    @Test
    @DisplayName("Should load users in order")
    void testUserOrder() {
        // When
        List<User> users = repository.getAllUsers();
        
        // Then
        assertEquals("john.smith", users.get(0).getUsername());
        assertEquals("jane.doe", users.get(1).getUsername());
        assertEquals("admin.user", users.get(2).getUsername());
        assertEquals("disabled.user", users.get(3).getUsername());
    }
}

// Made with Bob
