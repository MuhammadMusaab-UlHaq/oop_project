package com.smartcashpro.db;
import com.smartcashpro.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User authenticate(String username, String password) {
        String sql = "SELECT UserID, PasswordHash, Role, IsActive FROM USER WHERE Username = ?";
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
             System.err.println("Authentication failed: No database connection.");
             return null;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("PasswordHash"); 
                    boolean isActive = rs.getBoolean("IsActive");
                    boolean passwordMatch = storedHash.equals(password); 
                    
                    if (passwordMatch && isActive) {
                         System.out.println("User '" + username + "' authenticated successfully.");
                        return new User(
                                rs.getInt("UserID"),
                                username,
                                rs.getString("Role"),
                                isActive
                        );
                    } else if (!isActive) {
                        System.out.println("Authentication failed: User '" + username + "' is inactive.");
                    } else {
                        System.out.println("Authentication failed: Incorrect password for user '" + username + "'.");
                    }
                } else {
                     System.out.println("Authentication failed: User '" + username + "' not found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error for user '" + username + "': " + e.getMessage());
            e.printStackTrace(); 
        }
        return null; 
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID, Username, Role, IsActive FROM USER ORDER BY Username";
        Connection conn = DatabaseConnector.getConnection();
         if (conn == null) {
             System.err.println("Cannot get users: No database connection.");
             return users; 
         }
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Role"),
                        rs.getBoolean("IsActive")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
    
    
    public boolean saveUser(User user, String plainPassword) {
        Connection conn = DatabaseConnector.getConnection();
         if (conn == null) {
             System.err.println("Cannot save user: No database connection.");
             return false;
         }
        
        boolean isNewUser = (user.getUserId() == 0);
        String sql;
        
        
        String passwordToStore = null;
        if (plainPassword != null && !plainPassword.isEmpty()) {
            
             passwordToStore = plainPassword; 
             System.out.println("WARNING: Storing plain text password for user " + user.getUsername() + " - REPLACE WITH HASHING!");
        }
         

        if (isNewUser) {
            if (passwordToStore == null) {
                System.err.println("Password cannot be empty for new user.");
                return false; 
            }
            sql = "INSERT INTO USER (Username, PasswordHash, Role, IsActive) VALUES (?, ?, ?, ?)";
        } else {
            
            if (passwordToStore != null) {
                
                sql = "UPDATE USER SET Role = ?, IsActive = ?, PasswordHash = ? WHERE UserID = ?";
            } else {
                
                sql = "UPDATE USER SET Role = ?, IsActive = ? WHERE UserID = ?";
            }
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (isNewUser) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, passwordToStore);
                pstmt.setString(3, user.getRole());
                pstmt.setBoolean(4, user.isActive());
            } else {
                pstmt.setString(1, user.getRole());
                pstmt.setBoolean(2, user.isActive());
                if (passwordToStore != null) {
                     pstmt.setString(3, passwordToStore);
                     pstmt.setInt(4, user.getUserId());
                } else {
                    pstmt.setInt(3, user.getUserId());
                }
            }
            int affectedRows = pstmt.executeUpdate();
            System.out.println("User save operation affected " + affectedRows + " rows for UserID/Username: " + (isNewUser ? user.getUsername() : user.getUserId()));
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error saving user '" + user.getUsername() + "': " + e.getMessage());
             if (e.getMessage().contains("Duplicate entry")) { 
                 System.err.println("Username '" + user.getUsername() + "' likely already exists.");
             }
            e.printStackTrace();
            return false;
        }
    }
}