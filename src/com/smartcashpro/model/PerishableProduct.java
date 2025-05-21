package com.smartcashpro.model;

import java.math.BigDecimal;

public class PerishableProduct extends Product {
    private String storageTempRequirement; // Specific to PerishableProduct

    // Constructor for loading from DB
    public PerishableProduct(int productId, String sku, String name, BigDecimal unitPrice,
                             int quantityInStock, BigDecimal currentCostPrice, int reorderLevel,
                             String storageTempRequirement) {
        super(productId, sku, name, unitPrice, quantityInStock, currentCostPrice, reorderLevel);
        this.storageTempRequirement = storageTempRequirement;
    }

    // Constructor for creating a new perishable product
    public PerishableProduct(String sku, String name, BigDecimal unitPrice,
                             int quantityInStock, BigDecimal currentCostPrice, int reorderLevel,
                             String storageTempRequirement) {
        super(sku, name, unitPrice, quantityInStock, currentCostPrice, reorderLevel);
        this.storageTempRequirement = storageTempRequirement;
    }

    public String getStorageTempRequirement() {
        return storageTempRequirement;
    }

    public void setStorageTempRequirement(String storageTempRequirement) {
        this.storageTempRequirement = storageTempRequirement;
    }

    @Override
    public String getProductType() {
        return "Perishable";
    }
}