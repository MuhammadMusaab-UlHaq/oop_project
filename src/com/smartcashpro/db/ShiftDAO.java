package com.smartcashpro.db;

import com.smartcashpro.model.Shift; 
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

import javax.swing.JOptionPane;



public class ShiftDAO {

    
    public static class ReconciliationResult {
        private String summaryText;
        public ReconciliationResult(String summary) { this.summaryText = summary; }
        public String getSummaryText() { return summaryText; }

        
        public static String buildSummaryText(int shiftId, BigDecimal startingFloat, BigDecimal cashSales,
                                            BigDecimal cardSales, BigDecimal otherSales, BigDecimal cashRemoved,
                                            BigDecimal expectedCash, BigDecimal endingFloat, BigDecimal discrepancy) {
             
             BigDecimal zero = BigDecimal.ZERO;
             return String.format(
                 "Shift %d Reconciled:\n" +
                 "------------------------------------\n" +
                 " Starting Cash Drawer:  $ %8.2f\n" +
                 "+ Calculated Cash Sales: $ %8.2f\n" +
                 "- Total Cash Removed:    $ %8.2f\n" +
                 "------------------------------------\n" +
                 "= Expected Cash in Drawer:$ %8.2f\n" +
                 "  Actual Ending Count:   $ %8.2f\n" +
                 "------------------------------------\n" +
                 "  CASH DISCREPANCY:      $ %8.2f %s\n" + 
                 "====================================\n" +
                 "  Card Sales Total:      $ %8.2f\n" +
                 "  Other Sales Total:     $ %8.2f\n" +
                 "====================================",
                 shiftId,
                 startingFloat != null ? startingFloat : zero,
                 cashSales != null ? cashSales : zero,
                 cashRemoved != null ? cashRemoved : zero,
                 expectedCash != null ? expectedCash : zero,
                 endingFloat != null ? endingFloat : zero,
                 discrepancy != null ? discrepancy.abs() : zero, 
                 (discrepancy != null && discrepancy.compareTo(BigDecimal.ZERO) < 0) ? "(SHORT)" :
                 ((discrepancy != null && discrepancy.compareTo(BigDecimal.ZERO) > 0) ? "(OVER)" : ""), 
                 cardSales != null ? cardSales : zero,
                 otherSales != null ? otherSales : zero
             );
        }
    }

    
     public Shift findOpenShift() {
         
         String sql = "SELECT ShiftID, StartTime, EndTime, Status, StartUserID, EndUserID, StartingFloat FROM SHIFT WHERE Status = 'Open' ORDER BY StartTime DESC LIMIT 1";
         
         try (Connection conn = DatabaseConnector.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql);
              ResultSet rs = pstmt.executeQuery()) {

             if (conn == null) {
                 System.err.println("Error finding open shift: Database connection failed.");
                 return null;
             }

             if (rs.next()) {
                  Timestamp startTimestamp = rs.getTimestamp("StartTime");
                  Timestamp endTimeStamp = rs.getTimestamp("EndTime"); 
                  Integer endUserId = rs.getObject("EndUserID", Integer.class); 
                  BigDecimal startingFloat = rs.getBigDecimal("StartingFloat");

                  
                  return new Shift(
                      rs.getInt("ShiftID"),
                      
                      (startTimestamp == null) ? null : startTimestamp.toLocalDateTime(),
                      (endTimeStamp == null) ? null : endTimeStamp.toLocalDateTime(),
                      rs.getString("Status"),
                      rs.getInt("StartUserID"),
                      endUserId, 
                      startingFloat 
                  );
             }
         } catch (SQLException e) {
             System.err.println("Error finding open shift: " + e.getMessage());
             e.printStackTrace();
         }
         return null; 
      }

    
    public int startShift(int userId, BigDecimal startingFloat) {
        
         Shift openShift = findOpenShift(); 
         if (openShift != null) {
             System.err.println("Cannot start new shift: Shift ID " + openShift.getShiftId() + " is already open.");
              
              
              JOptionPane.showMessageDialog(null,
                  "Cannot start new shift:\nAn existing shift (ID: " + openShift.getShiftId() + ") is already open.\nPlease end the current shift first.",
                  "Shift Error", JOptionPane.WARNING_MESSAGE);
             return -1; 
         }

        String sql = "INSERT INTO SHIFT (StartTime, Status, StartUserID, StartingFloat) VALUES (NOW(), 'Open', ?, ?)";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

             if (conn == null) {
                 System.err.println("Error starting shift: Database connection failed.");
                 return -1; 
             }

            
            pstmt.setInt(1, userId);
            pstmt.setBigDecimal(2, (startingFloat != null ? startingFloat : BigDecimal.ZERO));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); 
                    } else {
                        System.err.println("Error starting shift: Failed to retrieve generated key.");
                    }
                }
            } else {
                 System.err.println("Error starting shift: No rows affected.");
            }
        } catch (SQLException e) {
            System.err.println("Error starting shift: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; 
    }


    
    public ReconciliationResult endShift(int shiftId, int endUserId, BigDecimal endingFloat, BigDecimal cashRemoved) {
        
        BigDecimal cashSales = BigDecimal.ZERO;
        BigDecimal cardSales = BigDecimal.ZERO;
        BigDecimal otherSales = BigDecimal.ZERO;
        BigDecimal startingFloat = BigDecimal.ZERO; 
        BigDecimal expectedCash = BigDecimal.ZERO;
        BigDecimal discrepancy = BigDecimal.ZERO;

        
        if (endingFloat == null) {
             System.err.println("Error ending shift ID " + shiftId + ": Ending float cannot be null.");
             
             return new ReconciliationResult("Error: Ending float not provided.");
        }
         if (cashRemoved == null) {
             System.err.println("Error ending shift ID " + shiftId + ": Cash removed cannot be null. Use 0 if none was removed.");
             
             cashRemoved = BigDecimal.ZERO;
             
         }


        String salesSQL = "SELECT p.PaymentID, p.Amount, " +
                          "CASE WHEN cp.PaymentID IS NOT NULL THEN 'Cash' " +
                          "     WHEN cdp.PaymentID IS NOT NULL THEN 'Card' " +
                          "     WHEN op.PaymentID IS NOT NULL THEN 'Other' " + 
                          "     ELSE 'Unknown' END AS PaymentType " +
                          "FROM PAYMENT p " +
                          "JOIN `ORDER` o ON p.OrderID = o.OrderID " +
                          "LEFT JOIN CASH_PAYMENT cp ON p.PaymentID = cp.PaymentID " +
                          "LEFT JOIN CARD_PAYMENT cdp ON p.PaymentID = cdp.PaymentID " +
                          "LEFT JOIN OTHER_PAYMENT op ON p.PaymentID = op.PaymentID " +
                          "WHERE o.ShiftID = ? AND o.OrderStatus = 'Completed'"; 

        String startFloatSQL = "SELECT StartingFloat FROM SHIFT WHERE ShiftID = ?";

        String updateShiftSQL = "UPDATE SHIFT SET EndTime = NOW(), Status = 'Reconciled', EndUserID = ?, " +
                                "CashSalesAmount = ?, CardSalesAmount = ?, OtherSalesAmount = ?, " +
                                "CashRemoved = ?, EndingFloat = ?, CashDiscrepancy = ? " +
                                "WHERE ShiftID = ? AND Status = 'Open'"; 

        
        try (Connection conn = DatabaseConnector.getConnection()) {

             if (conn == null) {
                 System.err.println("Error ending shift ID " + shiftId + ": Database connection failed.");
                 return new ReconciliationResult("Error: Database connection failed."); 
             }

             
             try (PreparedStatement pstmtStart = conn.prepareStatement(startFloatSQL)) {
                 pstmtStart.setInt(1, shiftId);
                 try (ResultSet rsStart = pstmtStart.executeQuery()) {
                     if (rsStart.next()) {
                         startingFloat = rsStart.getBigDecimal("StartingFloat");
                         if(rsStart.wasNull() || startingFloat == null) startingFloat = BigDecimal.ZERO; 
                     } else {
                         
                         System.err.println("Error ending shift: Cannot find starting float for Shift ID: " + shiftId);
                         return new ReconciliationResult("Error: Shift ID " + shiftId + " not found."); 
                     }
                 }
             }

             
             try (PreparedStatement pstmtSales = conn.prepareStatement(salesSQL)) {
                 pstmtSales.setInt(1, shiftId);
                 try (ResultSet rsSales = pstmtSales.executeQuery()) {
                     while (rsSales.next()) {
                         BigDecimal amount = rsSales.getBigDecimal("Amount");
                         if(amount == null) amount = BigDecimal.ZERO; 
                         String type = rsSales.getString("PaymentType");

                         
                         if ("Cash".equals(type)) cashSales = cashSales.add(amount);
                         else if ("Card".equals(type)) cardSales = cardSales.add(amount);
                         else if ("Other".equals(type)) otherSales = otherSales.add(amount);
                         
                     }
                 }
             }

             
             
             expectedCash = (startingFloat != null ? startingFloat : BigDecimal.ZERO)
                             .add(cashSales != null ? cashSales : BigDecimal.ZERO)
                             .subtract(cashRemoved != null ? cashRemoved : BigDecimal.ZERO);
             discrepancy = (endingFloat != null ? endingFloat : BigDecimal.ZERO)
                             .subtract(expectedCash);


             
             try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateShiftSQL)) {
                 pstmtUpdate.setInt(1, endUserId);
                 pstmtUpdate.setBigDecimal(2, cashSales);
                 pstmtUpdate.setBigDecimal(3, cardSales);
                 pstmtUpdate.setBigDecimal(4, otherSales);
                 pstmtUpdate.setBigDecimal(5, cashRemoved);
                 pstmtUpdate.setBigDecimal(6, endingFloat);
                 pstmtUpdate.setBigDecimal(7, discrepancy);
                 pstmtUpdate.setInt(8, shiftId);

                 int affectedRows = pstmtUpdate.executeUpdate();
                 if (affectedRows == 0) {
                      
                      System.err.println("Failed to update shift record for Shift ID " + shiftId + ". Maybe it wasn't 'Open' or the ID is wrong.");
                      
                      
                       return new ReconciliationResult("Error: Failed to close Shift ID " + shiftId + ". It might already be closed or reconciled.");
                 }
             }

             
             String summary = ReconciliationResult.buildSummaryText(
                 shiftId, startingFloat, cashSales, cardSales, otherSales,
                 cashRemoved, expectedCash, endingFloat, discrepancy
             );
             return new ReconciliationResult(summary);

        } catch (SQLException e) {
            System.err.println("SQL Error ending shift ID " + shiftId + ": " + e.getMessage());
            e.printStackTrace();
             
            return new ReconciliationResult("Error reconciling Shift ID " + shiftId + ". See logs for details.");
        } catch (Exception e) {
            
            System.err.println("Unexpected Error ending shift ID " + shiftId + ": " + e.getMessage());
            e.printStackTrace();
            return new ReconciliationResult("Unexpected error reconciling Shift ID " + shiftId + ".");
        }
    }
}