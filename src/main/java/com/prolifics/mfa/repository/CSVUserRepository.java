package com.prolifics.mfa.repository;

import com.prolifics.mfa.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing user data in CSV file.
 * Provides thread-safe CRUD operations for user management.
 * 
 * @author Prolifics MFA POC Team
 * @version 1.0.0
 */
public class CSVUserRepository {
    
    private final String usersFile;
    private static final String CSV_HEADER = "username,password,fullName,email,status,role,mfaSecret";
    private final Object lock = new Object();
    
    /**
     * Default constructor using default file path
     */
    public CSVUserRepository() {
        this("src/main/resources/users.csv");
    }
    
    /**
     * Constructor with custom file path
     *
     * @param filePath Path to the users CSV file
     */
    public CSVUserRepository(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        this.usersFile = filePath;
        
        // Check if file exists
        File file = new File(this.usersFile);
        if (!file.exists()) {
            throw new RuntimeException("CSV file does not exist: " + file.getAbsolutePath());
        }
        
        // Verify file is readable
        if (!file.canRead()) {
            throw new RuntimeException("CSV file is not readable: " + file.getAbsolutePath());
        }
    }
    
    /**
     * Load all users from CSV file
     * 
     * @return List of all users
     */
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        
        synchronized (lock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
                String line;
                boolean firstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    // Skip header
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    
                    User user = parseUserFromCSV(line);
                    if (user != null) {
                        users.add(user);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading users: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return users;
    }
    
    /**
     * Save all users to CSV file
     * 
     * @param users List of users to save
     */
    public void saveUsers(List<User> users) {
        synchronized (lock) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(usersFile))) {
                // Write header
                writer.println(CSV_HEADER);
                
                // Write users
                for (User user : users) {
                    writer.println(userToCSV(user));
                }
                
                writer.flush();
            } catch (IOException e) {
                System.err.println("Error saving users: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Find user by username
     * 
     * @param username Username to search for
     * @return User object if found, null otherwise
     */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        List<User> users = loadUsers();
        for (User user : users) {
            if (username.equals(user.getUsername())) {  // Changed to case-sensitive
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Update user's MFA secret
     * 
     * @param username Username of the user
     * @param mfaSecret New MFA secret
     * @return true if updated successfully, false otherwise
     */
    public boolean updateMFASecret(String username, String mfaSecret) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (mfaSecret == null || mfaSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("MFA secret cannot be null or empty");
        }
        
        List<User> users = loadUsers();
        boolean updated = false;
        
        for (User user : users) {
            if (username.equals(user.getUsername())) {  // Changed to case-sensitive
                user.setMfaSecret(mfaSecret);
                updated = true;
                break;
            }
        }
        
        if (!updated) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        
        saveUsers(users);
        return updated;
    }
    
    /**
     * Update user information
     * 
     * @param updatedUser User object with updated information
     * @return true if updated successfully, false otherwise
     */
    public boolean updateUser(User updatedUser) {
        if (updatedUser == null || updatedUser.getUsername() == null) {
            return false;
        }
        
        List<User> users = loadUsers();
        boolean updated = false;
        
        for (int i = 0; i < users.size(); i++) {
            if (updatedUser.getUsername().equals(users.get(i).getUsername())) {  // Changed to case-sensitive
                users.set(i, updatedUser);
                updated = true;
                break;
            }
        }
        
        if (updated) {
            saveUsers(users);
        }
        
        return updated;
    }
    
    /**
     * Add a new user
     * 
     * @param user User to add
     * @return true if added successfully, false if user already exists
     */
    public boolean addUser(User user) {
        if (user == null || user.getUsername() == null) {
            return false;
        }
        
        // Check if user already exists
        if (findByUsername(user.getUsername()) != null) {
            return false;
        }
        
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
        
        return true;
    }
    
    /**
     * Delete a user
     * 
     * @param username Username of user to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        List<User> users = loadUsers();
        boolean removed = users.removeIf(user ->
            username.equals(user.getUsername()));  // Changed to case-sensitive
        
        if (removed) {
            saveUsers(users);
        }
        
        return removed;
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return loadUsers();
    }
    
    /**
     * Get count of users
     * 
     * @return Number of users
     */
    public int getUserCount() {
        return loadUsers().size();
    }
    
    /**
     * Get count of active users
     * 
     * @return Number of active users
     */
    public int getActiveUserCount() {
        List<User> users = loadUsers();
        int count = 0;
        for (User user : users) {
            if (user.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Parse user from CSV line
     * 
     * @param csvLine CSV line to parse
     * @return User object or null if parsing fails
     */
    private User parseUserFromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] parts = csvLine.split(",", -1);
            if (parts.length < 7) {
                return null;
            }
            
            String username = parts[0].trim();
            String password = parts[1].trim();
            String fullName = parts[2].trim();
            String email = parts[3].trim();
            String status = parts[4].trim();
            String role = parts[5].trim();
            String mfaSecret = parts[6].trim();
            
            // Handle empty mfaSecret
            if (mfaSecret.isEmpty()) {
                mfaSecret = null;
            }
            
            return new User(username, password, fullName, email, status, role, mfaSecret);
        } catch (Exception e) {
            System.err.println("Error parsing user from CSV: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert user to CSV format
     * 
     * @param user User to convert
     * @return CSV formatted string
     */
    private String userToCSV(User user) {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                user.getUsername(),
                user.getPassword(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus(),
                user.getRole(),
                user.getMfaSecret() != null ? user.getMfaSecret() : "");
    }
    
    /**
     * Check if CSV file exists and is readable
     * 
     * @return true if file exists and is readable
     */
    public boolean isFileAccessible() {
        File file = new File(usersFile);
        return file.exists() && file.canRead();
    }
    
    /**
     * Get CSV file path
     *
     * @return CSV file path
     */
    public String getFilePath() {
        return usersFile;
    }
}

// Made with Bob
