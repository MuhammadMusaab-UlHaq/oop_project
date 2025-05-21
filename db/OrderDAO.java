// src/com/smartcashpro/db/OrderDAO.java

package com.smartcashpro.db; // Assuming this is the correct package

import com.smartcashpro.model.OrderItem;
import com.smartcashpro.model.Order;
// import com.smartcashpro.model.Product; // Not directly used in this version of OrderDAO, but ProductDAO is
// import com.smartcashpro.model.User; // Not directly used

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class OrderDAO {

    /**
     * Saves a new order, associated items, payment details, and updates stock within a transaction.
     *
     * @param items          List of items in the order.
     * @param customerId     ID of the customer (can be null).
     * @param userId         ID of the user processing the order.
     * @param shiftId        ID of the current shift.
     * @param paymentType    Type of payment ("Cash", "Card", etc.).
     * @param amountTendered Amount tendered (especially for cash payments).
     * @return true if the order was saved successfully.
     * @throws SQLException             If any database error occurs during the transaction.
     * @throws IllegalArgumentException If input parameters are invalid.
     */
    public boolean saveOrder(List<OrderItem> items, Integer customerId, int userId, int shiftId, String paymentType, BigDecimal amountTendered) throws SQLException, IllegalArgumentException {
        // --- Pre-condition validations ---
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty.");
        }
        for (OrderItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Order list contains a null OrderItem.");
            }
            if (item.getLineTotal() == null) {
                // You might want to include item.getProductId() or some identifier if available and safe
                throw new IllegalArgumentException("OrderItem has a null line total. Cannot calculate total amount.");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("OrderItem has non-positive quantity: " + item.getQuantity());
            }
            if (item.getUnitPriceAtSale() == null || item.getCostPriceAtSale() == null) {
                throw new IllegalArgumentException("OrderItem has null price(s) at sale.");
            }
        }
        if (paymentType == null || paymentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment type cannot be empty.");
        }
        if ("Cash".equalsIgnoreCase(paymentType) && amountTendered == null) {
            throw new IllegalArgumentException("Amount tendered cannot be null for cash payment.");
        }
        // --- End Pre-condition validations ---

        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            throw new SQLException("No database connection available.");
        }

        String insertOrderSQL = "INSERT INTO `ORDER` (OrderDate, TotalAmount, CustomerID, UserID, ShiftID, OrderStatus) VALUES (NOW(), ?, ?, ?, ?, 'Completed')";
        String insertOrderItemSQL = "INSERT INTO ORDER_ITEM (OrderID, ProductID, Quantity, UnitPriceAtSale, CostPriceAtSale) VALUES (?, ?, ?, ?, ?)";
        String insertPaymentSQL = "INSERT INTO PAYMENT (PaymentDate, Amount, OrderID) VALUES (NOW(), ?, ?)";
        String insertCashPaymentSQL = "INSERT INTO CASH_PAYMENT (PaymentID, AmountTendered, ChangeGiven) VALUES (?, ?, ?)";
        String insertCardPaymentSQL = "INSERT INTO CARD_PAYMENT (PaymentID) VALUES (?)"; // Simplified, add more fields as needed

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            totalAmount = totalAmount.add(item.getLineTotal());
        }

        long orderId = -1;
        long paymentId = -1;

        try {
            conn.setAutoCommit(false);

            // 1. Insert into ORDER
            try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmtOrder.setBigDecimal(1, totalAmount);
                if (customerId != null && customerId > 0) {
                    pstmtOrder.setInt(2, customerId);
                } else {
                    pstmtOrder.setNull(2, Types.INTEGER);
                }
                pstmtOrder.setInt(3, userId);
                pstmtOrder.setInt(4, shiftId);
                pstmtOrder.executeUpdate();

                try (ResultSet generatedKeys = pstmtOrder.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
            }

            // 2. Insert into ORDER_ITEM (Loop)
            try (PreparedStatement pstmtOrderItem = conn.prepareStatement(insertOrderItemSQL)) {
                for (OrderItem item : items) {
                    pstmtOrderItem.setLong(1, orderId);
                    pstmtOrderItem.setInt(2, item.getProductId());
                    pstmtOrderItem.setInt(3, item.getQuantity());
                    pstmtOrderItem.setBigDecimal(4, item.getUnitPriceAtSale());
                    pstmtOrderItem.setBigDecimal(5, item.getCostPriceAtSale());
                    pstmtOrderItem.addBatch();
                }
                pstmtOrderItem.executeBatch();
            }

            // 3. Insert into PAYMENT
            try (PreparedStatement pstmtPayment = conn.prepareStatement(insertPaymentSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmtPayment.setBigDecimal(1, totalAmount);
                pstmtPayment.setLong(2, orderId);
                pstmtPayment.executeUpdate();

                try (ResultSet generatedKeys = pstmtPayment.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        paymentId = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating payment failed, no ID obtained.");
                    }
                }
            }

            // 4. Insert into PAYMENT subclass
            if ("Cash".equalsIgnoreCase(paymentType)) {
                try (PreparedStatement pstmtCash = conn.prepareStatement(insertCashPaymentSQL)) {
                    BigDecimal change = amountTendered.subtract(totalAmount);
                    if (change.compareTo(BigDecimal.ZERO) < 0) {
                        // Business decision: Log warning and proceed with 0 change, or throw error.
                        // Old code logged warning and set change to 0.
                        // Consider throwing new IllegalArgumentException("Amount tendered is less than total amount.");
                        System.err.println("Warning: Amount tendered (" + amountTendered + ") was less than total amount (" + totalAmount + "). Setting change to 0.");
                        change = BigDecimal.ZERO;
                    }
                    pstmtCash.setLong(1, paymentId);
                    pstmtCash.setBigDecimal(2, amountTendered);
                    pstmtCash.setBigDecimal(3, change);
                    pstmtCash.executeUpdate();
                }
            } else if ("Card".equalsIgnoreCase(paymentType)) {
                try (PreparedStatement pstmtCard = conn.prepareStatement(insertCardPaymentSQL)) {
                    pstmtCard.setLong(1, paymentId);
                    // Add other card details here if needed (e.g., CardType, Last4Digits)
                    // pstmtCard.setString(2, cardType);
                    pstmtCard.executeUpdate();
                }
            } else {
                // Unsupported payment type that wasn't caught by pre-condition checks (if any added for specific types)
                throw new SQLException("Unsupported payment type: " + paymentType + ". Transaction will be rolled back.");
            }

            // 5. Update Stock (Loop)
            ProductDAO productDAO = new ProductDAO(); // Assumes ProductDAO is set up for this
            for (OrderItem item : items) {
                // updateStockQuantity in ProductDAO should throw SQLException on failure
                // and handle its own connection or use the passed one appropriately.
                productDAO.updateStockQuantity(item.getProductId(), -item.getQuantity(), conn);
            }

            conn.commit();
            System.out.println("Order " + orderId + " saved successfully.");
            return true; // Success

        } catch (SQLException e) {
            System.err.println("Order saving transaction failed in DAO: " + e.getMessage());
            try {
                if (conn != null) {
                    System.err.println("Rolling back transaction in DAO.");
                    conn.rollback();
                }
            } catch (SQLException exRollback) {
                System.err.println("Rollback failed in DAO: " + exRollback.getMessage());
                e.addSuppressed(exRollback); // Add rollback failure as suppressed exception
            }
            throw e; // Re-throw the original SQLException (or the one from payment type)
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Restore default behavior
                    // No need to close connection here if DatabaseConnector.getConnection() provides
                    // connections that are managed (e.g., from a pool, or closed by caller)
                    // If this method is responsible for closing, it should be conn.close()
                }
            } catch (SQLException e) {
                System.err.println("Error restoring auto-commit in OrderDAO: " + e.getMessage());
                // This error is less critical than the main transaction failure
            }
        }
        // No 'return false' needed here, as all failure paths should throw an exception.
    }


    /**
     * Processes a return for a specific item from an original order.
     * Updates stock if restockFlag is true. Handles partial returns correctly.
     *
     * @param originalOrderItemId ID of the specific item being returned from the ORDER_ITEM table.
     * @param quantityToReturn    The number of units being returned for this item.
     * @param restockFlag         If true, the returned quantity will be added back to stock.
     * @param reason              Reason for the return (optional).
     * @param processedByUserId   ID of the user processing the return.
     * @param shiftId             ID of the shift during which the return is processed.
     * @return true if the return was processed successfully.
     * @throws SQLException             If any database error occurs or validation fails (e.g., item not found, return quantity invalid).
     * @throws IllegalArgumentException If quantityToReturn is not positive.
     */
    public boolean processReturn(int originalOrderItemId, int quantityToReturn, boolean restockFlag, String reason, int processedByUserId, int shiftId) throws SQLException, IllegalArgumentException {
        if (quantityToReturn <= 0) {
            throw new IllegalArgumentException("Quantity to return must be positive.");
        }

        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            throw new SQLException("No database connection available for processing return.");
        }

        String checkExistingReturnSQL = "SELECT SUM(QuantityReturned) AS TotalReturned FROM SALES_RETURN WHERE OriginalOrderItemID = ?";
        String getOrderItemDetailsSQL = "SELECT ProductID, Quantity, UnitPriceAtSale FROM ORDER_ITEM WHERE OrderItemID = ?";
        String insertReturnSQL = "INSERT INTO SALES_RETURN (ReturnDate, OriginalOrderItemID, QuantityReturned, Reason, RestockFlag, RefundAmount, ProcessedByUserID, ShiftID) VALUES (NOW(), ?, ?, ?, ?, ?, ?, ?)";
        ProductDAO productDAO = new ProductDAO();
        int totalPreviouslyReturned = 0;

        try {
            conn.setAutoCommit(false);

            int productId = -1;
            int originalQuantitySold = -1;
            BigDecimal unitPrice = BigDecimal.ZERO;

            // 1. Get original order item details
            try (PreparedStatement pstmtDetails = conn.prepareStatement(getOrderItemDetailsSQL)) {
                pstmtDetails.setInt(1, originalOrderItemId);
                try (ResultSet rsDetails = pstmtDetails.executeQuery()) {
                    if (rsDetails.next()) {
                        productId = rsDetails.getInt("ProductID");
                        originalQuantitySold = rsDetails.getInt("Quantity");
                        unitPrice = rsDetails.getBigDecimal("UnitPriceAtSale");
                        if (unitPrice == null) {
                            // This is a data integrity issue.
                            // For now, warn and use 0 as per old code, but this could lead to incorrect refunds.
                            // Consider throwing an SQLException or using a default/configurable value.
                            System.err.println("Warning: UnitPriceAtSale is NULL for OrderItemID " + originalOrderItemId + ". Assuming 0.00 for refund calculation.");
                            unitPrice = BigDecimal.ZERO;
                        }
                    } else {
                        throw new SQLException("Original order item (ID: " + originalOrderItemId + ") not found.");
                    }
                }
            }

            // 2. Check for existing returns for this item
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkExistingReturnSQL)) {
                pstmtCheck.setInt(1, originalOrderItemId);
                try (ResultSet rsCheck = pstmtCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        totalPreviouslyReturned = rsCheck.getInt("TotalReturned"); // Default 0 if NULL
                    }
                }
            }

            // 3. Validate return quantity
            int maxAllowedReturn = originalQuantitySold - totalPreviouslyReturned;
            if (quantityToReturn > maxAllowedReturn) {
                throw new SQLException("Cannot return quantity (" + quantityToReturn + "). " +
                        "Max allowed for this item (OrderItemID: " + originalOrderItemId + "): " + maxAllowedReturn +
                        " (Sold: " + originalQuantitySold + ", Already Returned: " + totalPreviouslyReturned + ").");
            }

            // 4. Calculate refund amount
            BigDecimal refundAmount = unitPrice.multiply(new BigDecimal(quantityToReturn));

            // 5. Insert into SALES_RETURN
            try (PreparedStatement pstmtReturn = conn.prepareStatement(insertReturnSQL)) {
                pstmtReturn.setInt(1, originalOrderItemId);
                pstmtReturn.setInt(2, quantityToReturn);
                pstmtReturn.setString(3, (reason == null || reason.trim().isEmpty()) ? null : reason.trim());
                pstmtReturn.setBoolean(4, restockFlag);
                pstmtReturn.setBigDecimal(5, refundAmount);
                pstmtReturn.setInt(6, processedByUserId);
                pstmtReturn.setInt(7, shiftId);
                int rowsAffected = pstmtReturn.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to insert sales return record for OrderItemID " + originalOrderItemId + ". No rows affected.");
                }
            }

            // 6. Update stock if restockFlag is true
            if (restockFlag && productId > 0) { // productId must be valid
                // Assuming productDAO.updateStockQuantity throws SQLException on failure
                productDAO.updateStockQuantity(productId, quantityToReturn, conn); // Positive quantity to add back
            }

            conn.commit();
            System.out.println("Return processed successfully for OrderItemID: " + originalOrderItemId + ", Quantity: " + quantityToReturn);
            return true;

        } catch (SQLException | IllegalArgumentException e) { // Catch IAE also if productDAO or other parts might throw it and it should abort
            System.err.println("Return processing transaction failed for OrderItemID " + originalOrderItemId + ": " + e.getMessage());
            try {
                if (conn != null) {
                    System.err.println("Rolling back transaction for return processing.");
                    conn.rollback();
                }
            } catch (SQLException exRollback) {
                System.err.println("Rollback failed during return processing: " + exRollback.getMessage());
                e.addSuppressed(exRollback);
            }
            throw e; // Re-throw the original exception (SQLException or IllegalArgumentException)
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                     // As with saveOrder, manage connection closure based on DatabaseConnector's design
                }
            } catch (SQLException ex) {
                System.err.println("Error restoring auto-commit in processReturn: " + ex.getMessage());
            }
        }
    }

    /**
     * Retrieves all orders with basic information.
     * 
     * @return List of Order objects with basic info
     * @throws SQLException If database operation fails
     */
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        
        String sql = "SELECT o.OrderID, o.OrderDate, o.TotalAmount, o.CustomerID, " +
                    "c.Name AS CustomerName, o.UserID, u.Username AS UserName, " +
                    "o.ShiftID, o.OrderStatus, " +
                    "CASE WHEN cp.PaymentID IS NOT NULL THEN 'Cash' " +
                    "     WHEN cdp.PaymentID IS NOT NULL THEN 'Card' " +
                    "     ELSE 'Unknown' END AS PaymentType " +
                    "FROM `ORDER` o " +
                    "LEFT JOIN CUSTOMER c ON o.CustomerID = c.CustomerID " +
                    "LEFT JOIN USER u ON o.UserID = u.UserID " +
                    "LEFT JOIN PAYMENT p ON o.OrderID = p.OrderID " +
                    "LEFT JOIN CASH_PAYMENT cp ON p.PaymentID = cp.PaymentID " +
                    "LEFT JOIN CARD_PAYMENT cdp ON p.PaymentID = cdp.PaymentID " +
                    "ORDER BY o.OrderDate DESC";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("OrderID"),
                    rs.getTimestamp("OrderDate"),
                    rs.getBigDecimal("TotalAmount"),
                    rs.getObject("CustomerID") != null ? rs.getInt("CustomerID") : null,
                    rs.getString("CustomerName"),
                    rs.getInt("UserID"),
                    rs.getString("UserName"),
                    rs.getInt("ShiftID"),
                    rs.getString("OrderStatus"),
                    rs.getString("PaymentType")
                );
                orders.add(order);
            }
        }
        
        return orders;
    }
    
    /**
     * Searches orders by date range, customer, or payment type.
     * 
     * @param startDate Start date for search, can be null
     * @param endDate End date for search, can be null
     * @param customerId Customer ID to filter by, can be null
     * @param paymentType Payment type to filter by, can be null
     * @return List of filtered Order objects
     * @throws SQLException If database operation fails
     */
    public List<Order> searchOrders(
            Date startDate, Date endDate, 
            Integer customerId, String paymentType) throws SQLException {
        
        List<Order> orders = new ArrayList<>();
        
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT o.OrderID, o.OrderDate, o.TotalAmount, o.CustomerID, " +
            "c.Name AS CustomerName, o.UserID, u.Username AS UserName, " +
            "o.ShiftID, o.OrderStatus, " +
            "CASE WHEN cp.PaymentID IS NOT NULL THEN 'Cash' " +
            "     WHEN cdp.PaymentID IS NOT NULL THEN 'Card' " +
            "     ELSE 'Unknown' END AS PaymentType " +
            "FROM `ORDER` o " +
            "LEFT JOIN CUSTOMER c ON o.CustomerID = c.CustomerID " +
            "LEFT JOIN USER u ON o.UserID = u.UserID " +
            "LEFT JOIN PAYMENT p ON o.OrderID = p.OrderID " +
            "LEFT JOIN CASH_PAYMENT cp ON p.PaymentID = cp.PaymentID " +
            "LEFT JOIN CARD_PAYMENT cdp ON p.PaymentID = cdp.PaymentID " +
            "WHERE 1=1 "
        );
        
        List<Object> params = new ArrayList<>();
        
        if (startDate != null) {
            sqlBuilder.append("AND o.OrderDate >= ? ");
            params.add(new java.sql.Timestamp(startDate.getTime()));
        }
        
        if (endDate != null) {
            sqlBuilder.append("AND o.OrderDate <= ? ");
            params.add(new java.sql.Timestamp(endDate.getTime()));
        }
        
        if (customerId != null) {
            sqlBuilder.append("AND o.CustomerID = ? ");
            params.add(customerId);
        }
        
        if (paymentType != null && !paymentType.isEmpty()) {
            if ("Cash".equalsIgnoreCase(paymentType)) {
                sqlBuilder.append("AND cp.PaymentID IS NOT NULL ");
            } else if ("Card".equalsIgnoreCase(paymentType)) {
                sqlBuilder.append("AND cdp.PaymentID IS NOT NULL ");
            }
        }
        
        sqlBuilder.append("ORDER BY o.OrderDate DESC");
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("OrderID"),
                        rs.getTimestamp("OrderDate"),
                        rs.getBigDecimal("TotalAmount"),
                        rs.getObject("CustomerID") != null ? rs.getInt("CustomerID") : null,
                        rs.getString("CustomerName"),
                        rs.getInt("UserID"),
                        rs.getString("UserName"),
                        rs.getInt("ShiftID"),
                        rs.getString("OrderStatus"),
                        rs.getString("PaymentType")
                    );
                    orders.add(order);
                }
            }
        }
        
        return orders;
    }
    
    /**
     * Gets order details including all items for a specific order.
     * 
     * @param orderId ID of the order to retrieve
     * @return Order object with items, or null if not found
     * @throws SQLException If database operation fails
     */
    public Order getOrderDetails(int orderId) throws SQLException {
        Order order = null;
        
        // First get order header
        String orderSql = "SELECT o.OrderID, o.OrderDate, o.TotalAmount, o.CustomerID, " +
                        "c.Name AS CustomerName, o.UserID, u.Username AS UserName, " +
                        "o.ShiftID, o.OrderStatus, " +
                        "CASE WHEN cp.PaymentID IS NOT NULL THEN 'Cash' " +
                        "     WHEN cdp.PaymentID IS NOT NULL THEN 'Card' " +
                        "     ELSE 'Unknown' END AS PaymentType " +
                        "FROM `ORDER` o " +
                        "LEFT JOIN CUSTOMER c ON o.CustomerID = c.CustomerID " +
                        "LEFT JOIN USER u ON o.UserID = u.UserID " +
                        "LEFT JOIN PAYMENT p ON o.OrderID = p.OrderID " +
                        "LEFT JOIN CASH_PAYMENT cp ON p.PaymentID = cp.PaymentID " +
                        "LEFT JOIN CARD_PAYMENT cdp ON p.PaymentID = cdp.PaymentID " +
                        "WHERE o.OrderID = ?";
        
        // Then get order items
        String itemsSql = "SELECT oi.OrderItemID, oi.ProductID, p.Name AS ProductName, " +
                        "oi.Quantity, oi.UnitPriceAtSale, oi.CostPriceAtSale " +
                        "FROM ORDER_ITEM oi " +
                        "LEFT JOIN PRODUCT p ON oi.ProductID = p.ProductID " +
                        "WHERE oi.OrderID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            
            orderStmt.setInt(1, orderId);
            
            try (ResultSet rs = orderStmt.executeQuery()) {
                if (rs.next()) {
                    order = new Order(
                        rs.getInt("OrderID"),
                        rs.getTimestamp("OrderDate"),
                        rs.getBigDecimal("TotalAmount"),
                        rs.getObject("CustomerID") != null ? rs.getInt("CustomerID") : null,
                        rs.getString("CustomerName"),
                        rs.getInt("UserID"),
                        rs.getString("UserName"),
                        rs.getInt("ShiftID"),
                        rs.getString("OrderStatus"),
                        rs.getString("PaymentType")
                    );
                }
            }
            
            // If order was found, get its items
            if (order != null) {
                List<OrderItem> items = new ArrayList<>();
                
                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                    itemsStmt.setInt(1, orderId);
                    
                    try (ResultSet rs = itemsStmt.executeQuery()) {
                        while (rs.next()) {
                            OrderItem item = new OrderItem(
                                rs.getInt("ProductID"),
                                rs.getString("ProductName"),
                                rs.getInt("Quantity"),
                                rs.getBigDecimal("UnitPriceAtSale"),
                                rs.getBigDecimal("CostPriceAtSale")
                            );
                            item.setOrderItemId(rs.getInt("OrderItemID"));
                            items.add(item);
                        }
                    }
                }
                
                order.setOrderItems(items);
            }
        }
        
        return order;
    }
}