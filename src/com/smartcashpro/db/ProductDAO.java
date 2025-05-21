package com.smartcashpro.db;

import com.smartcashpro.model.Product;
import com.smartcashpro.model.PerishableProduct;
import com.smartcashpro.model.NonPerishableProduct;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements IProductDAO { // IMPLEMENTS IProductDAO

    @Override
    public Product findProductBySku(String sku) {
        String sql = "SELECT p.ProductID, p.SKU, p.Name, p.UnitPrice, p.QuantityInStock, p.CurrentCostPrice, p.ReorderLevel, " +
                     "pp.ProductID AS PerishableProductID, pp.StorageTempRequirement, " +
                     "np.ProductID AS NonPerishableProductID " +
                     "FROM PRODUCT p " +
                     "LEFT JOIN PERISHABLE_PRODUCT pp ON p.ProductID = pp.ProductID " +
                     "LEFT JOIN NONPERISHABLE_PRODUCT np ON p.ProductID = np.ProductID " +
                     "WHERE p.SKU = ?";
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            System.err.println("DB Error: No connection for findProductBySku");
            return null;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sku);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding product by SKU '" + sku + "': " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Product findProductById(int productId) {
        String sql = "SELECT p.ProductID, p.SKU, p.Name, p.UnitPrice, p.QuantityInStock, p.CurrentCostPrice, p.ReorderLevel, " +
                     "pp.ProductID AS PerishableProductID, pp.StorageTempRequirement, " +
                     "np.ProductID AS NonPerishableProductID " +
                     "FROM PRODUCT p " +
                     "LEFT JOIN PERISHABLE_PRODUCT pp ON p.ProductID = pp.ProductID " +
                     "LEFT JOIN NONPERISHABLE_PRODUCT np ON p.ProductID = np.ProductID " +
                     "WHERE p.ProductID = ?";
        Connection conn = DatabaseConnector.getConnection();
         if (conn == null) {
            System.err.println("DB Error: No connection for findProductById");
            return null;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding product by ID " + productId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.ProductID, p.SKU, p.Name, p.UnitPrice, p.QuantityInStock, p.CurrentCostPrice, p.ReorderLevel, " +
                     "pp.ProductID AS PerishableProductID, pp.StorageTempRequirement, " +
                     "np.ProductID AS NonPerishableProductID " +
                     "FROM PRODUCT p " +
                     "LEFT JOIN PERISHABLE_PRODUCT pp ON p.ProductID = pp.ProductID " +
                     "LEFT JOIN NONPERISHABLE_PRODUCT np ON p.ProductID = np.ProductID " +
                     "ORDER BY p.Name";
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            System.err.println("DB Error: No connection for getAllProducts");
            return products; // Return empty list
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public boolean saveProduct(Product product) {
        Connection conn = DatabaseConnector.getConnection();
        if (conn == null) {
            System.err.println("DB Error: No connection for saveProduct");
            return false;
        }

        boolean isNew = product.getProductId() == 0;
        String productSQL = isNew
                ? "INSERT INTO PRODUCT (SKU, Name, UnitPrice, QuantityInStock, CurrentCostPrice, ReorderLevel) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE PRODUCT SET SKU = ?, Name = ?, UnitPrice = ?, CurrentCostPrice = ?, ReorderLevel = ? WHERE ProductID = ?";

        String perishableSQL = "INSERT INTO PERISHABLE_PRODUCT (ProductID, StorageTempRequirement) VALUES (?, ?)";
        String nonPerishableSQL = "INSERT INTO NONPERISHABLE_PRODUCT (ProductID) VALUES (?)";

        try {
            conn.setAutoCommit(false);

            int generatedProductId = product.getProductId();
            try (PreparedStatement pstmtProd = conn.prepareStatement(productSQL, isNew ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {

                pstmtProd.setString(1, product.getSku());
                pstmtProd.setString(2, product.getName());
                pstmtProd.setBigDecimal(3, product.getUnitPrice());

                if (isNew) {
                    pstmtProd.setInt(4, product.getQuantityInStock());
                    pstmtProd.setBigDecimal(5, product.getCurrentCostPrice());
                    pstmtProd.setInt(6, product.getReorderLevel());
                } else {
                    pstmtProd.setBigDecimal(4, product.getCurrentCostPrice());
                    pstmtProd.setInt(5, product.getReorderLevel());
                    pstmtProd.setInt(6, product.getProductId());
                }

                int affectedRows = pstmtProd.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Saving product base failed, no rows affected for SKU: " + product.getSku());
                }

                if (isNew) {
                    try (ResultSet generatedKeys = pstmtProd.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            generatedProductId = generatedKeys.getInt(1);
                            product.setProductId(generatedProductId);
                        } else {
                            throw new SQLException("Creating product failed, no ID obtained for SKU: " + product.getSku());
                        }
                    }
                }
            } 

            if (isNew && generatedProductId > 0) {
                if (product instanceof PerishableProduct) {
                    try (PreparedStatement pstmtSub = conn.prepareStatement(perishableSQL)) {
                        pstmtSub.setInt(1, generatedProductId);
                        PerishableProduct pp = (PerishableProduct) product;
                        if (pp.getStorageTempRequirement() != null && !pp.getStorageTempRequirement().trim().isEmpty()) {
                            pstmtSub.setString(2, pp.getStorageTempRequirement());
                        } else {
                            pstmtSub.setNull(2, Types.VARCHAR);
                        }
                        pstmtSub.executeUpdate();
                    }
                } else if (product instanceof NonPerishableProduct) {
                    try (PreparedStatement pstmtSub = conn.prepareStatement(nonPerishableSQL)) {
                        pstmtSub.setInt(1, generatedProductId);
                        pstmtSub.executeUpdate();
                    }
                }
            }
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving product (SKU: " + product.getSku() + "): " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("duplicate entry") && e.getMessage().toLowerCase().contains("sku")) {
                 System.err.println("Detail: SKU '" + product.getSku() + "' likely already exists.");
            }
            try { if (conn != null) conn.rollback(); } catch (SQLException exRollback) {
                System.err.println("Rollback failed: " + exRollback.getMessage());
            }
            e.printStackTrace(); 
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException exFinally) {
                 System.err.println("Error resetting autocommit: " + exFinally.getMessage());
            }
        }
    }

    @Override
    public boolean updateStockQuantity(int productId, int quantityChange, Connection conn) throws SQLException {
        String sql = "UPDATE PRODUCT SET QuantityInStock = QuantityInStock + ? WHERE ProductID = ?";

        if (conn == null) {
            throw new SQLException("Database connection is null in updateStockQuantity.");
        }

        if (quantityChange < 0) {
            String checkSql = "SELECT QuantityInStock FROM PRODUCT WHERE ProductID = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
                 pstmtCheck.setInt(1, productId);
                 try(ResultSet rs = pstmtCheck.executeQuery()) {
                     if (rs.next()) {
                         if (rs.getInt("QuantityInStock") < Math.abs(quantityChange)) {
                             throw new SQLException("Insufficient stock for ProductID " + productId +
                                                    ". Current: " + rs.getInt("QuantityInStock") +
                                                    ", Requested Change: " + quantityChange);
                         }
                     } else {
                         throw new SQLException("Product not found for stock check, ID: " + productId);
                     }
                 }
            }
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityChange);
            pstmt.setInt(2, productId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0 && quantityChange != 0) {
                throw new SQLException("Product not found for stock update, ID: " + productId + ". No rows affected.");
            }
            return affectedRows > 0 || quantityChange == 0;
        }
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        int productId = rs.getInt("p.ProductID");
        String sku = rs.getString("p.SKU");
        String name = rs.getString("p.Name");
        BigDecimal unitPrice = rs.getBigDecimal("p.UnitPrice");
        int quantityInStock = rs.getInt("p.QuantityInStock");
        BigDecimal currentCostPrice = rs.getBigDecimal("p.CurrentCostPrice");
        int reorderLevel = rs.getInt("p.ReorderLevel");

        if (rs.getObject("PerishableProductID") != null) {
            String storageTemp = rs.getString("StorageTempRequirement");
            return new PerishableProduct(
                    productId, sku, name, unitPrice, quantityInStock,
                    currentCostPrice, reorderLevel, storageTemp
            );
        } else {
            return new NonPerishableProduct(
                    productId, sku, name, unitPrice, quantityInStock,
                    currentCostPrice, reorderLevel
            );
        }
    }
}