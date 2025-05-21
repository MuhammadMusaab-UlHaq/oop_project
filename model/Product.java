package com.smartcashpro.model;

import java.math.BigDecimal;

public abstract class Product { // Made abstract
    private int productId;
    private String sku;
    private String name;
    private BigDecimal unitPrice;
    private int quantityInStock;
    private BigDecimal currentCostPrice;
    // private boolean isPerishable; // REMOVED - this will be determined by subclass type
    private int reorderLevel;

    // Constructor for subclasses to use
    public Product(int productId, String sku, String name, BigDecimal unitPrice,
                   int quantityInStock, BigDecimal currentCostPrice, int reorderLevel) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantityInStock = quantityInStock;
        this.currentCostPrice = currentCostPrice;
        this.reorderLevel = reorderLevel;
    }

    // Constructor for creating new products (ID will be 0)
    public Product(String sku, String name, BigDecimal unitPrice,
                   int quantityInStock, BigDecimal currentCostPrice, int reorderLevel) {
        this(0, sku, name, unitPrice, quantityInStock, currentCostPrice, reorderLevel);
    }

    // Getters
    public int getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantityInStock() { return quantityInStock; }
    public BigDecimal getCurrentCostPrice() { return currentCostPrice; }
    public int getReorderLevel() { return reorderLevel; }

    // Setters
    public void setProductId(int productId) { this.productId = productId; }
    public void setSku(String sku) { this.sku = sku; } // SKU might be set by DAO
    public void setName(String name) { this.name = name; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setCurrentCostPrice(BigDecimal currentCostPrice) { this.currentCostPrice = currentCostPrice; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    // Abstract method to indicate type, could be used for display or logic
    public abstract String getProductType();

    // Example of another abstract method that subclasses might implement differently
    // public abstract String getStorageDetails(); // For example

    @Override
    public String toString() {
        // For JComboBox display, often just the name is preferred.
        // If more detail is needed elsewhere, specific methods can provide it.
        // return name + " (SKU: " + sku + ") - Type: " + getProductType();
        return name + " (SKU: " + sku + ")"; // Simpler for JComboBox
    }
}