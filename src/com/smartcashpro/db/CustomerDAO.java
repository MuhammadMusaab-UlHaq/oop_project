package com.smartcashpro.db;

import com.smartcashpro.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    
    public List<Customer> findCustomers(String searchTerm) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT CustomerID, Name, ContactInfo, LoyaltyPoints FROM CUSTOMER " +
                     "WHERE Name LIKE ? OR ContactInfo LIKE ? ORDER BY Name LIMIT 50";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Error finding customers: No database connection.");
                return customers; 
            }

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(
                            rs.getInt("CustomerID"),
                            rs.getString("Name"),
                            rs.getString("ContactInfo"),
                            rs.getInt("LoyaltyPoints")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding customers: " + e.getMessage());
            e.printStackTrace();
            
            
        }
        return customers;
    }

    
    
    public boolean saveCustomer(Customer customer) {
        try {
            Customer savedCustomer = saveCustomerAndRetrieve(customer);
            return savedCustomer != null; 
        } catch (SQLException e) {
            
            System.err.println("saveCustomer (boolean) failed for customer '" + customer.getName() + "': " + e.getMessage());
            
            return false; 
        }
    }


    
    
    public Customer saveCustomerAndRetrieve(Customer customer) throws SQLException {
        boolean isNew = (customer.getCustomerId() == 0);
        String sql = isNew ? "INSERT INTO CUSTOMER (Name, ContactInfo, LoyaltyPoints) VALUES (?, ?, ?)"
                           : "UPDATE CUSTOMER SET Name = ?, ContactInfo = ?, LoyaltyPoints = ? WHERE CustomerID = ?";

        
        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                throw new SQLException("No database connection.");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql,
                                         isNew ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {

                pstmt.setString(1, customer.getName());
                pstmt.setString(2, customer.getContactInfo());
                pstmt.setInt(3, customer.getLoyaltyPoints());

                if (!isNew) {
                    pstmt.setInt(4, customer.getCustomerId());
                }

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                     throw new SQLException("Saving customer failed, no rows affected. Customer ID: " + customer.getCustomerId() + ", Name: " + customer.getName());
                }

                if (isNew) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            customer.setCustomerId(generatedKeys.getInt(1)); 
                        } else {
                            throw new SQLException("Creating customer failed, no ID obtained for: " + customer.getName());
                        }
                    }
                }
                System.out.println("Customer " + (isNew ? "added with ID " + customer.getCustomerId() : "updated with ID " + customer.getCustomerId()) + " successfully.");
                return customer; 
            }
        } catch (SQLException e) {
            System.err.println("Error saving customer '" + customer.getName() + "' (ID: " + customer.getCustomerId() + "): " + e.getMessage());
            e.printStackTrace(); 
            
            throw e;
        }
    }
}