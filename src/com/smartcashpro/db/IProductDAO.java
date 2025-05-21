package com.smartcashpro.db;

import com.smartcashpro.model.Product;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IProductDAO {
    Product findProductBySku(String sku) throws SQLException; // Add throws if methods in concrete class do
    Product findProductById(int productId) throws SQLException;
    List<Product> getAllProducts() throws SQLException;
    boolean saveProduct(Product product) throws SQLException;
    boolean updateStockQuantity(int productId, int quantityChange, Connection conn) throws SQLException;
}