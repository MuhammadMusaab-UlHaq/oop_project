package com.smartcashpro.model;


public class User {
    private int userId;
    private String username;
    
    private String role; 
    private boolean isActive;

    
    public User(int userId, String username, String role, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.isActive = isActive;
    }

    
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isActive() { return isActive; }

     
     public void setActive(boolean active) { this.isActive = active; }
     
     public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", username=" + username + ", role=" + role + "]";
    }
}