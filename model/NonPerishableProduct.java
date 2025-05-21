package com.smartcashpro.model;

import java.math.BigDecimal;

public class NonPerishableProduct extends Product {

    // Constructor for loading from DB
    public NonPerishableProduct(int productId, String sku, String name, BigDecimal unitPrice,
                                int quantityInStock, BigDecimal currentCostPrice, int reorderLevel) {
        super(productId, sku, name, unitPrice, quantityInStock, currentCostPrice, reorderLevel);
    }

    // Constructor for creating a new non-perishable product
    public NonPerishableProduct(String sku, String name, BigDecimal unitPrice,
                                int quantityInStock, BigDecimal currentCostPrice, int reorderLevel) {
        super(sku, name, unitPrice, quantityInStock, currentCostPrice, reorderLevel);
    }

    @Override
    public String getProductType() {
        return "Non-Perishable";
    }
}