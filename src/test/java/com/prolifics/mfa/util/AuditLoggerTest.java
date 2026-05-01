package com.prolifics.mfa.util;

import com.prolifics.mfa.model.AuditLog;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditLogger class.
 * Tests audit log writing, reading, and thread safety.
 * 
 * @author Bob
 * @version 1.0
 */
@DisplayName("AuditLogger Tests")
class AuditLoggerTest {

    @TempDir
    Path tempDir;
    
    private File testAuditFile;
    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary audit log file for testing
        testAuditFile = tempDir.resolve("test-audit-log.csv").toFile();
        auditLogger = new AuditLogger(testAuditFile.getAbsolutePath());
    }

    @Test
    @DisplayName("Should create audit log file with header")
    void testCreateAuditLogFile() throws IOException {
        // Then
        assertTrue(testAuditFile.exists(), "Audit log file should be created");
        
        // Read first line
        try (BufferedReader reader = new BufferedReader(new FileReader(testAuditFile))) {
            String header = reader.readLine();
            assertEquals("timestamp,username,action,status,ipAddress,details", header,
                        "Should have correct CSV header");
        }
    }

    @Test
    @DisplayName("Should log entry successfully")
    void testLogEntry() throws IOException {
        // When
        auditLogger.log("john.smith", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", "Valid credentials");
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(2, lines.size(), "Should have header + 1 entry");
        
        String logEntry = lines.get(1);
        assertTrue(logEntry.contains("john.smith"), "Should contain username");
        assertTrue(logEntry.contains("LOGIN_ATTEMPT"), "Should contain action");
        assertTrue(logEntry.contains("SUCCESS"), "Should contain status");
        assertTrue(logEntry.contains("192.168.1.100"), "Should contain IP address");
        assertTrue(logEntry.contains("Valid credentials"), "Should contain details");
    }

    @Test
    @DisplayName("Should log login attempt")
    void testLogLoginAttempt() throws IOException {
        // When
        auditLogger.logLoginAttempt("john.smith", true, "192.168.1.100");
        
        // Then
        List<String> lines = readAllLines();
        String logEntry = lines.get(1);
        assertTrue(logEntry.contains("LOGIN_SUCCESS"), "Should log LOGIN_SUCCESS");
        assertTrue(logEntry.contains("SUCCESS"), "Should have SUCCESS status");
    }

    @Test
    @DisplayName("Should log failed login attempt")
    void testLogFailedLoginAttempt() throws IOException {
        // When
        auditLogger.logLoginAttempt("john.smith", false, "192.168.1.100");
        
        // Then
        List<String> lines = readAllLines();
        String logEntry = lines.get(1);
        assertTrue(logEntry.contains("LOGIN_FAILED"), "Should log LOGIN_FAILED");
        assertTrue(logEntry.contains("FAILED"), "Should have FAILED status");
    }

    @Test
    @DisplayName("Should log MFA setup")
    void testLogMFASetup() throws IOException {
        // When
        auditLogger.logMFASetup("john.smith", "192.168.1.100");
        
        // Then
        List<String> lines = readAllLines();
        String logEntry = lines.get(1);
        assertTrue(logEntry.contains("MFA_SETUP"), "Should log MFA_SETUP");
        assertTrue(logEntry.contains("SUCCESS"), "Should have SUCCESS status");
    }

    @Test
    @DisplayName("Should log MFA verification success")
    void testLogMFAVerificationSuccess() throws IOException {
        // When
        auditLogger.logMFAVerification("john.smith", true, "192.168.1.100");
        
        // Then
        List<String> lines = readAllLines();
        String logEntry = lines.get(1);
        assertTrue(logEntry.contains("MFA_VERIFY_SUCCESS"), "Should log MFA_VERIFY_SUCCESS");
        assertTrue(logEntry.contains("SUCCESS"), "Should have SUCCESS status");
    }

    @Test
    @DisplayName("Should log MFA verification failure")
    void testLogMFAVerificationFailure() throws IOException {
        // When
        auditLogger.logMFAVerification("john.smith", false, "192.168.1.100");
        
        // Then
        List<String> lines = readAllLines();
        String logEntry = lines.get(1);
        assertTrue(logEntry.contains("MFA_VERIFY_FAILED"), "Should log MFA_VERIFY_FAILED");
        assertTrue(logEntry.contains("FAILED"), "Should have FAILED status");
    }

    @Test
    @DisplayName("Should log logout")
    void testLogLogout() throws IOException {
        // When
        auditLogger.logLogout("john.smith", "192.168.1.100");
        
        // Then
        List<String> lines = readAllLines();
        String logEntry = lines.get(1);
        assertTrue(logEntry.contains("LOGOUT"), "Should log LOGOUT");
        assertTrue(logEntry.contains("SUCCESS"), "Should have SUCCESS status");
    }

    @Test
    @DisplayName("Should append multiple entries")
    void testMultipleEntries() throws IOException {
        // When
        auditLogger.log("user1", "ACTION1", "SUCCESS", "192.168.1.1", "Details 1");
        auditLogger.log("user2", "ACTION2", "FAILED", "192.168.1.2", "Details 2");
        auditLogger.log("user3", "ACTION3", "SUCCESS", "192.168.1.3", "Details 3");
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(4, lines.size(), "Should have header + 3 entries");
    }

    @Test
    @DisplayName("Should format timestamp correctly")
    void testTimestampFormat() throws IOException {
        // When
        auditLogger.log("john.smith", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", "Test");
        
        // Then
        List<String> lines = readAllLines();
        String logEntry = lines.get(1);
        String timestamp = logEntry.split(",")[0];
        
        // Verify ISO 8601 format
        assertDoesNotThrow(() -> LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                          "Timestamp should be in ISO 8601 format");
    }

    @Test
    @DisplayName("Should handle special characters in details")
    void testSpecialCharactersInDetails() throws IOException {
        // When
        auditLogger.log("john.smith", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", 
                       "Details with, comma and \"quotes\"");
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(2, lines.size(), "Should log entry with special characters");
    }

    @Test
    @DisplayName("Should handle empty details")
    void testEmptyDetails() throws IOException {
        // When
        auditLogger.log("john.smith", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", "");
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(2, lines.size(), "Should log entry with empty details");
    }

    @Test
    @DisplayName("Should handle null details")
    void testNullDetails() throws IOException {
        // When
        auditLogger.log("john.smith", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", null);
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(2, lines.size(), "Should log entry with null details");
    }

    @Test
    @DisplayName("Should get audit logs")
    void testGetAuditLogs() {
        // Given
        auditLogger.log("user1", "ACTION1", "SUCCESS", "192.168.1.1", "Details 1");
        auditLogger.log("user2", "ACTION2", "FAILED", "192.168.1.2", "Details 2");
        
        // When
        List<AuditLog> logs = auditLogger.getAuditLogs();
        
        // Then
        assertNotNull(logs, "Audit logs should not be null");
        assertEquals(2, logs.size(), "Should return 2 audit logs");
        
        AuditLog log1 = logs.get(0);
        assertEquals("user1", log1.getUsername());
        assertEquals("ACTION1", log1.getAction());
        assertEquals("SUCCESS", log1.getStatus());
        
        AuditLog log2 = logs.get(1);
        assertEquals("user2", log2.getUsername());
        assertEquals("ACTION2", log2.getAction());
        assertEquals("FAILED", log2.getStatus());
    }

    @Test
    @DisplayName("Should parse CSV correctly")
    void testCSVParsing() {
        // Given
        auditLogger.log("john.smith", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", "Valid credentials");
        
        // When
        List<AuditLog> logs = auditLogger.getAuditLogs();
        
        // Then
        assertEquals(1, logs.size());
        AuditLog log = logs.get(0);
        
        assertNotNull(log.getTimestamp());
        assertEquals("john.smith", log.getUsername());
        assertEquals("LOGIN_ATTEMPT", log.getAction());
        assertEquals("SUCCESS", log.getStatus());
        assertEquals("192.168.1.100", log.getIpAddress());
        assertEquals("Valid credentials", log.getDetails());
    }

    @Test
    @DisplayName("Should handle concurrent writes")
    void testConcurrentWrites() throws InterruptedException {
        // Given
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        // When - multiple threads writing simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                auditLogger.log("user" + index, "ACTION", "SUCCESS", "192.168.1." + index, "Details " + index);
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - all writes should succeed
        List<AuditLog> logs = auditLogger.getAuditLogs();
        assertEquals(threadCount, logs.size(), "All concurrent writes should succeed");
    }

    @Test
    @DisplayName("Should maintain chronological order")
    void testChronologicalOrder() throws InterruptedException {
        // When
        auditLogger.log("user1", "ACTION1", "SUCCESS", "192.168.1.1", "First");
        Thread.sleep(10); // Small delay to ensure different timestamps
        auditLogger.log("user2", "ACTION2", "SUCCESS", "192.168.1.2", "Second");
        Thread.sleep(10);
        auditLogger.log("user3", "ACTION3", "SUCCESS", "192.168.1.3", "Third");
        
        // Then
        List<AuditLog> logs = auditLogger.getAuditLogs();
        assertEquals(3, logs.size());
        
        // Verify chronological order
        assertTrue(logs.get(0).getTimestamp().isBefore(logs.get(1).getTimestamp()) ||
                  logs.get(0).getTimestamp().isEqual(logs.get(1).getTimestamp()),
                  "First entry should be before or equal to second");
        assertTrue(logs.get(1).getTimestamp().isBefore(logs.get(2).getTimestamp()) ||
                  logs.get(1).getTimestamp().isEqual(logs.get(2).getTimestamp()),
                  "Second entry should be before or equal to third");
    }

    @Test
    @DisplayName("Should handle long details field")
    void testLongDetails() throws IOException {
        // Given
        String longDetails = "A".repeat(500);
        
        // When
        auditLogger.log("john.smith", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", longDetails);
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(2, lines.size(), "Should log entry with long details");
    }

    @Test
    @DisplayName("Should handle Unicode characters")
    void testUnicodeCharacters() throws IOException {
        // When
        auditLogger.log("用户", "LOGIN_ATTEMPT", "SUCCESS", "192.168.1.100", "详细信息");
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(2, lines.size(), "Should log entry with Unicode characters");
    }

    @Test
    @DisplayName("Should handle IP address formats")
    void testIPAddressFormats() throws IOException {
        // When
        auditLogger.log("user1", "ACTION", "SUCCESS", "192.168.1.100", "IPv4");
        auditLogger.log("user2", "ACTION", "SUCCESS", "2001:0db8:85a3:0000:0000:8a2e:0370:7334", "IPv6");
        auditLogger.log("user3", "ACTION", "SUCCESS", "unknown", "Unknown IP");
        
        // Then
        List<String> lines = readAllLines();
        assertEquals(4, lines.size(), "Should log entries with different IP formats");
    }

    @Test
    @DisplayName("Should create file if not exists")
    void testCreateFileIfNotExists() throws IOException {
        // Given
        File newFile = tempDir.resolve("new-audit-log.csv").toFile();
        assertFalse(newFile.exists(), "File should not exist initially");
        
        // When
        AuditLogger newLogger = new AuditLogger(newFile.getAbsolutePath());
        newLogger.log("test", "ACTION", "SUCCESS", "192.168.1.1", "Test");
        
        // Then
        assertTrue(newFile.exists(), "File should be created");
    }

    @Test
    @DisplayName("Should handle null username")
    void testNullUsername() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                    () -> auditLogger.log(null, "ACTION", "SUCCESS", "192.168.1.1", "Test"),
                    "Should throw exception for null username");
    }

    @Test
    @DisplayName("Should handle null action")
    void testNullAction() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                    () -> auditLogger.log("user", null, "SUCCESS", "192.168.1.1", "Test"),
                    "Should throw exception for null action");
    }

    @Test
    @DisplayName("Should handle null status")
    void testNullStatus() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                    () -> auditLogger.log("user", "ACTION", null, "192.168.1.1", "Test"),
                    "Should throw exception for null status");
    }

    @Test
    @DisplayName("Should handle null IP address")
    void testNullIPAddress() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                    () -> auditLogger.log("user", "ACTION", "SUCCESS", null, "Test"),
                    "Should throw exception for null IP address");
    }

    @Test
    @DisplayName("Should return empty list for empty audit log")
    void testEmptyAuditLog() {
        // When
        List<AuditLog> logs = auditLogger.getAuditLogs();
        
        // Then
        assertNotNull(logs);
        assertTrue(logs.isEmpty(), "Should return empty list for empty audit log");
    }

    /**
     * Helper method to read all lines from the audit log file.
     */
    private List<String> readAllLines() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(testAuditFile))) {
            return reader.lines().collect(Collectors.toList());
        }
    }
}

// Made with Bob
