package com.smartcashpro.db;

import java.sql.*;
import com.smartcashpro.db.DatabaseConnector;
import com.smartcashpro.db.ProductDAO; 



public class StockAdjustmentDAO {

    
    public boolean adjustStock(int productId, int userId, int shiftId, int quantityChange, String reason, String notes) throws SQLException { 
         Connection conn = DatabaseConnector.getConnection();
         if (conn == null) throw new SQLException("No database connection.");
         if (quantityChange == 0) {
              throw new IllegalArgumentException("Stock adjustment quantity cannot be zero.");
         }


         String insertLogSQL = "INSERT INTO STOCK_ADJUSTMENT (AdjustmentDate, ProductID, UserID, ShiftID, QuantityChange, Reason, Notes) VALUES (NOW(), ?, ?, ?, ?, ?, ?)";
         ProductDAO productDAO = new ProductDAO(); 

        try {
             conn.setAutoCommit(false); 

             
             productDAO.updateStockQuantity(productId, quantityChange, conn);
             System.out.println("Product stock update successful for ID: " + productId + " by " + quantityChange);


             
             try(PreparedStatement pstmtLog = conn.prepareStatement(insertLogSQL)) {
                 pstmtLog.setInt(1, productId);
                 pstmtLog.setInt(2, userId);
                 pstmtLog.setInt(3, shiftId);
                 pstmtLog.setInt(4, quantityChange);
                 pstmtLog.setString(5, reason);
                 if (notes != null && !notes.trim().isEmpty()) {
                     pstmtLog.setString(6, notes);
                 } else {
                      pstmtLog.setNull(6, Types.VARCHAR);
                 }
                 int logRows = pstmtLog.executeUpdate();
                  if (logRows == 0) {
                      throw new SQLException("Failed to insert stock adjustment log.");
                  }
                  System.out.println("Stock adjustment log inserted successfully.");
             }

             conn.commit(); 
             System.out.println("Stock adjustment transaction committed successfully.");
             return true;

         } catch (SQLException | IllegalArgumentException e) { 
            System.err.println("Stock adjustment transaction failed: " + e.getMessage());
             try { if (conn != null) conn.rollback(); System.err.println("Transaction rolled back."); } catch (SQLException ex) { ex.printStackTrace(); }
             
             throw e instanceof SQLException ? (SQLException)e : new SQLException(e.getMessage(), e);
         } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
         }
    }
}