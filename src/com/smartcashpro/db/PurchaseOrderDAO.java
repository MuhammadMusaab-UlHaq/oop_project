package com.smartcashpro.db;

import com.smartcashpro.model.PurchaseOrder;
import com.smartcashpro.model.PurchaseOrderItem;
import com.smartcashpro.model.Product;
import com.smartcashpro.model.Supplier;
import com.smartcashpro.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseOrderDAO {

    // --- findPurchaseOrderById ---
    public PurchaseOrder findPurchaseOrderById(int purchaseOrderId) throws SQLException {
        String sql = "SELECT PurchaseOrderID, PODate, Status, ExpectedDeliveryDate, ActualDeliveryDate, SupplierID, PlacedByUserID, TotalCost " +
                     "FROM PURCHASE_ORDER WHERE PurchaseOrderID = ?";
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            throw new SQLException("Cannot find purchase order: No database connection.");
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchaseOrderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp poTimestamp = rs.getTimestamp("PODate");
                    Date expectedDate = rs.getDate("ExpectedDeliveryDate");
                    Date actualDate = rs.getDate("ActualDeliveryDate");

                    return new PurchaseOrder(
                            rs.getInt("PurchaseOrderID"),
                            (poTimestamp == null) ? null : poTimestamp.toLocalDateTime(),
                            rs.getString("Status"),
                            (expectedDate == null) ? null : expectedDate.toLocalDate(),
                            (actualDate == null) ? null : actualDate.toLocalDate(),
                            rs.getInt("SupplierID"),
                            rs.getInt("PlacedByUserID"),
                            rs.getBigDecimal("TotalCost")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding purchase order by ID " + purchaseOrderId + ": " + e.getMessage());
            throw new SQLException("Error finding purchase order by ID " + purchaseOrderId, e);
        }
        return null;
    }

    // --- getPurchaseOrderItems ---
    public List<PurchaseOrderItem> getPurchaseOrderItems(int purchaseOrderId) throws SQLException {
        List<PurchaseOrderItem> items = new ArrayList<>();
        String sql = "SELECT poi.PurchaseOrderItemID, poi.PurchaseOrderID, poi.ProductID, p.Name AS ProductName, " +
                     "poi.QuantityOrdered, poi.CostPricePerUnit, poi.QuantityReceived " +
                     "FROM PURCHASE_ORDER_ITEM poi " +
                     "JOIN PRODUCT p ON poi.ProductID = p.ProductID " +
                     "WHERE poi.PurchaseOrderID = ?";
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            System.err.println("Cannot get PO Items: No database connection.");
            throw new SQLException("Cannot get PO Items: No database connection.");
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchaseOrderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new PurchaseOrderItem(
                            rs.getInt("PurchaseOrderItemID"),
                            rs.getInt("PurchaseOrderID"),
                            rs.getInt("ProductID"),
                            rs.getString("ProductName"),
                            rs.getInt("QuantityOrdered"),
                            rs.getBigDecimal("CostPricePerUnit"),
                            rs.getInt("QuantityReceived")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching purchase order items for PO ID " + purchaseOrderId + ": " + e.getMessage());
            throw new SQLException("Error fetching purchase order items for PO ID " + purchaseOrderId, e);
        }
        return items;
    }

    // --- receiveStock ---
    public boolean receiveStock(int purchaseOrderId, Map<Integer, Integer> receivedQuantities) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();
        String failureReason = null;

        if (conn == null) throw new SQLException("No database connection.");
        if (receivedQuantities == null || receivedQuantities.isEmpty()) {
            throw new IllegalArgumentException("No quantities provided to receive.");
        }

        ProductDAO productDAO = new ProductDAO();
        String updatePOItemSQL = "UPDATE PURCHASE_ORDER_ITEM SET QuantityReceived = QuantityReceived + ? WHERE PurchaseOrderItemID = ?";
        String updatePOStatusSQL = "UPDATE PURCHASE_ORDER SET Status = ?, ActualDeliveryDate = NOW() WHERE PurchaseOrderID = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtPOItem = conn.prepareStatement(updatePOItemSQL)) {
                for (Map.Entry<Integer, Integer> entry : receivedQuantities.entrySet()) {
                    int poItemId = entry.getKey();
                    int qtyReceivedNow = entry.getValue();

                    if (qtyReceivedNow <= 0) continue;

                    String findProductSql = "SELECT ProductID, QuantityOrdered, QuantityReceived FROM PURCHASE_ORDER_ITEM WHERE PurchaseOrderItemID = ?";
                    int productId = -1;
                    int qtyOrdered = 0;
                    int alreadyReceived = 0;
                    try (PreparedStatement pstmtFindProd = conn.prepareStatement(findProductSql)) {
                        pstmtFindProd.setInt(1, poItemId);
                        try (ResultSet rs = pstmtFindProd.executeQuery()) {
                            if (rs.next()) {
                                productId = rs.getInt("ProductID");
                                qtyOrdered = rs.getInt("QuantityOrdered");
                                alreadyReceived = rs.getInt("QuantityReceived");
                            } else {
                                throw new SQLException("Could not find details for PurchaseOrderItemID: " + poItemId);
                            }
                        }
                    }

                    if (alreadyReceived + qtyReceivedNow > qtyOrdered) {
                        throw new SQLException("Cannot receive " + qtyReceivedNow + " for PO Item ID " + poItemId +
                                ". Only " + (qtyOrdered - alreadyReceived) + " remaining.");
                    }

                    pstmtPOItem.setInt(1, qtyReceivedNow);
                    pstmtPOItem.setInt(2, poItemId);
                    int itemRowsAffected = pstmtPOItem.executeUpdate();
                    if (itemRowsAffected == 0) {
                        throw new SQLException("Failed to update received quantity for POItemID: " + poItemId + ". Item might no longer exist.");
                    }

                    productDAO.updateStockQuantity(productId, qtyReceivedNow, conn);
                    System.out.println("Product ID " + productId + " stock updated by: " + qtyReceivedNow + " for PO Item " + poItemId);
                }
            }

            boolean fullyReceived = checkPOFullyReceived(purchaseOrderId, conn);
            String newStatus = fullyReceived ? "Received" : "PartiallyReceived";
            System.out.println("Updating PO ID " + purchaseOrderId + " status to: " + newStatus);

            try (PreparedStatement pstmtPOStatus = conn.prepareStatement(updatePOStatusSQL)) {
                pstmtPOStatus.setString(1, newStatus);
                pstmtPOStatus.setInt(2, purchaseOrderId);
                int poRowsAffected = pstmtPOStatus.executeUpdate();
                if (poRowsAffected == 0) {
                    System.err.println("Warning: Failed to update status for PurchaseOrderID: " + purchaseOrderId + ". PO might not exist.");
                }
            }

            conn.commit();
            System.out.println("Stock receiving transaction committed successfully for PO ID: " + purchaseOrderId);
            return true;

        } catch (SQLException | IllegalArgumentException e) {
            failureReason = e.getMessage();
            System.err.println("Stock receiving transaction failed for PO ID " + purchaseOrderId + ": " + failureReason);
            try {
                if (conn != null) {
                    System.err.println("Rolling back transaction.");
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error during transaction rollback: " + ex.getMessage());
                e.addSuppressed(ex);
            }

            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new SQLException("Stock receiving failed: " + e.getMessage(), e);
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.err.println("Error resetting auto-commit: " + ex.getMessage());
            }
        }
    }

    // --- checkPOFullyReceived ---
    private boolean checkPOFullyReceived(int purchaseOrderId, Connection conn) throws SQLException {
        if (conn == null) {
            throw new SQLException("Cannot check PO status: No database connection provided.");
        }
        String sql = "SELECT SUM(QuantityOrdered) AS TotalOrdered, SUM(QuantityReceived) AS TotalReceived " +
                     "FROM PURCHASE_ORDER_ITEM WHERE PurchaseOrderID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchaseOrderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalOrdered = rs.getInt("TotalOrdered");
                    if (rs.wasNull()) {
                        System.out.println("PO " + purchaseOrderId + " has no items, considering it received.");
                        return true;
                    }
                    int totalReceived = rs.getInt("TotalReceived");

                    System.out.println("PO " + purchaseOrderId + " Check: Ordered=" + totalOrdered + ", Received=" + totalReceived);
                    return totalReceived >= totalOrdered;
                } else {
                    System.out.println("PO " + purchaseOrderId + " has no items found in item table, considering it received.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not determine if PO " + purchaseOrderId + " is fully received: " + e.getMessage());
            throw new SQLException("Could not determine if PO " + purchaseOrderId + " is fully received.", e);
        }
    }

    // --- receiveFullOrder ---
    public boolean receiveFullOrder(int purchaseOrderId) throws SQLException {
        System.out.println("[DB] receiveFullOrder called for PO " + purchaseOrderId);
        List<PurchaseOrderItem> items = getPurchaseOrderItems(purchaseOrderId);

        if (items.isEmpty()) {
            PurchaseOrder po = null;
            try {
                po = findPurchaseOrderById(purchaseOrderId);
            } catch (SQLException findEx) {
                System.err.println("Error trying to find PO details for empty item list check: " + findEx.getMessage());
            }

            String message = "No items found for Purchase Order ID: " + purchaseOrderId;
            if (po != null) {
                if ("Received".equals(po.getStatus())) {
                    message += ". This PO is already marked as fully received.";
                    System.out.println(message);
                    return true;
                } else {
                    message += ". The PO exists but has no items associated with it.";
                }
            } else {
                message += ". This Purchase Order ID might not exist.";
            }

            throw new SQLException(message);
        }

        Map<Integer, Integer> quantitiesToReceive = new HashMap<>();
        boolean needsReceiving = false;
        for (PurchaseOrderItem item : items) {
            int remaining = item.getQuantityOrdered() - item.getQuantityReceived();
            if (remaining > 0) {
                quantitiesToReceive.put(item.getPurchaseOrderItemId(), remaining);
                needsReceiving = true;
            }
        }

        if (!needsReceiving) {
            System.out.println("PO " + purchaseOrderId + " is already fully received (all items have QuantityReceived >= QuantityOrdered).");

            try (Connection conn = DatabaseConnector.getConnection()) {
                if (conn != null && !checkPOFullyReceived(purchaseOrderId, conn)) {
                    System.out.println("PO " + purchaseOrderId + " items seem received, but main status needs update.");
                    String updatePOStatusSQL = "UPDATE PURCHASE_ORDER SET Status = ?, ActualDeliveryDate = NOW() WHERE PurchaseOrderID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updatePOStatusSQL)) {
                        ps.setString(1, "Received");
                        ps.setInt(2, purchaseOrderId);
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error during final status check/update for already received PO " + purchaseOrderId + ": " + e.getMessage());
            }
            return true;
        }

        return receiveStock(purchaseOrderId, quantitiesToReceive);
    }

    // --- createPurchaseOrder ---
    public int createPurchaseOrder(PurchaseOrder poHeader, List<PurchaseOrderItem> poItems) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) throw new SQLException("No database connection.");
        if (poHeader == null || poItems == null || poItems.isEmpty()) {
            throw new IllegalArgumentException("PO Header and Items cannot be null or empty.");
        }

        String insertPOHeaderSQL = "INSERT INTO PURCHASE_ORDER (PODate, Status, ExpectedDeliveryDate, SupplierID, PlacedByUserID, TotalCost) " +
                                   "VALUES (NOW(), ?, ?, ?, ?, ?)";
        String insertPOItemSQL = "INSERT INTO PURCHASE_ORDER_ITEM (PurchaseOrderID, ProductID, QuantityOrdered, CostPricePerUnit, QuantityReceived) " +
                                 "VALUES (?, ?, ?, ?, 0)";

        long generatedPOId = -1;

        try {
            conn.setAutoCommit(false);

            // 1. Calculate TotalCost for the PO Header
            BigDecimal totalCost = BigDecimal.ZERO;
            for (PurchaseOrderItem item : poItems) {
                if (item.getCostPricePerUnit() == null || item.getQuantityOrdered() <= 0) {
                    throw new IllegalArgumentException("Item cost price and quantity ordered must be valid for PO Item: " + item.getProductName());
                }
                totalCost = totalCost.add(item.getCostPricePerUnit().multiply(new BigDecimal(item.getQuantityOrdered())));
            }

            // 2. Insert PO Header
            try (PreparedStatement pstmtHeader = conn.prepareStatement(insertPOHeaderSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmtHeader.setString(1, poHeader.getStatus());
                if (poHeader.getExpectedDeliveryDate() != null) {
                    pstmtHeader.setDate(2, Date.valueOf(poHeader.getExpectedDeliveryDate()));
                } else {
                    pstmtHeader.setNull(2, Types.DATE);
                }
                pstmtHeader.setInt(3, poHeader.getSupplierId());
                pstmtHeader.setInt(4, poHeader.getPlacedByUserId());
                pstmtHeader.setBigDecimal(5, totalCost);

                int affectedRows = pstmtHeader.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating purchase order header failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmtHeader.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedPOId = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating purchase order header failed, no ID obtained.");
                    }
                }
            }

            // 3. Insert PO Items
            try (PreparedStatement pstmtItems = conn.prepareStatement(insertPOItemSQL)) {
                for (PurchaseOrderItem item : poItems) {
                    pstmtItems.setLong(1, generatedPOId);
                    pstmtItems.setInt(2, item.getProductId());
                    pstmtItems.setInt(3, item.getQuantityOrdered());
                    pstmtItems.setBigDecimal(4, item.getCostPricePerUnit());
                    pstmtItems.addBatch();
                }
                pstmtItems.executeBatch();
            }

            conn.commit();
            System.out.println("Purchase Order #" + generatedPOId + " created successfully.");
            return (int) generatedPOId;

        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error creating Purchase Order: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Transaction rolled back.");
                }
            } catch (SQLException exRollback) {
                exRollback.printStackTrace();
            }
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // --- getAllSuppliers ---
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, SupplierName FROM SUPPLIER ORDER BY SupplierName";
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) return suppliers;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(new Supplier(rs.getInt("SupplierID"), rs.getString("SupplierName")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching suppliers: " + e.getMessage());
            e.printStackTrace();
        }
        return suppliers;
    }

    // --- getAllPurchaseOrders ---
    public List<PurchaseOrder> getAllPurchaseOrders() {
        List<PurchaseOrder> poList = new ArrayList<>();
        String sql = "SELECT po.PurchaseOrderID, po.PODate, po.Status, po.ExpectedDeliveryDate, " +
                     "po.ActualDeliveryDate, po.SupplierID, s.SupplierName, po.PlacedByUserID, u.Username AS PlacedByUser, po.TotalCost " +
                     "FROM PURCHASE_ORDER po " +
                     "JOIN SUPPLIER s ON po.SupplierID = s.SupplierID " +
                     "JOIN USER u ON po.PlacedByUserID = u.UserID " +
                     "ORDER BY po.PODate DESC";

        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            System.err.println("Cannot get PO list: No database connection.");
            return poList;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp poTimestamp = rs.getTimestamp("PODate");
                Date expectedDate = rs.getDate("ExpectedDeliveryDate");
                Date actualDate = rs.getDate("ActualDeliveryDate");

                PurchaseOrder po = new PurchaseOrder(
                        rs.getInt("PurchaseOrderID"),
                        (poTimestamp == null) ? null : poTimestamp.toLocalDateTime(),
                        rs.getString("Status"),
                        (expectedDate == null) ? null : expectedDate.toLocalDate(),
                        (actualDate == null) ? null : actualDate.toLocalDate(),
                        rs.getInt("SupplierID"),
                        rs.getString("SupplierName"),        // New field added
                        rs.getInt("PlacedByUserID"),
                        rs.getString("PlacedByUser"),        // New field added
                        rs.getBigDecimal("TotalCost")
                );
                poList.add(po);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all purchase orders: " + e.getMessage());
            e.printStackTrace();
        }
        return poList;
    }
}
