package com.smartcashpro.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnector {

    
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SmartCashPro";
    private static final String DB_USER = "m";       
    private static final String DB_PASSWORD = "mmmm"; 

    private static Connection connection = null;

    
    public static Connection getConnection() {
        try {
            
            if (connection == null || connection.isClosed()) {
                try {
                    
                    connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    
                } catch (SQLException e) {
                    System.err.println("FATAL: Failed to connect to the database!");
                    e.printStackTrace();
                     
                    javax.swing.JOptionPane.showMessageDialog(null, "Database Connection Failed:\n" + e.getMessage() + "\nPlease check connection settings and database status.", "Database Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    System.exit(1); 
                    return null; 
                }
            }
        } catch (SQLException e) {
            
             System.err.println("Error checking database connection status!");
             e.printStackTrace();
             connection = null; 
             return null;
        }
        return connection;
    }

    
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("Failed to close the database connection!");
                e.printStackTrace();
            } finally {
                connection = null; 
            }
        }
    }
}