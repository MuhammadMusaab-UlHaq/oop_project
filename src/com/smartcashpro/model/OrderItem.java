package com.smartcashpro.model;

import java.math.BigDecimal;


public class OrderItem {
    private int orderItemId; 
    private int orderId;     
    private int productId;
    private int quantity;
    private BigDecimal unitPriceAtSale;
    private BigDecimal costPriceAtSale;
    private String productName; 

    
    public OrderItem(int productId, String productName, int quantity, BigDecimal unitPriceAtSale, BigDecimal costPriceAtSale) {
        this.orderItemId = 0; 
        this.orderId = 0;     
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPriceAtSale = unitPriceAtSale;
        this.costPriceAtSale = costPriceAtSale;
    }

    
    public int getOrderItemId() { return orderItemId; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPriceAtSale() { return unitPriceAtSale; }
    public BigDecimal getCostPriceAtSale() { return costPriceAtSale; }

    
    public BigDecimal getLineTotal() {
        if (unitPriceAtSale == null) return BigDecimal.ZERO;
        return unitPriceAtSale.multiply(new BigDecimal(quantity));
    }

    
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    
    public void setQuantity(int quantity) { this.quantity = quantity; }
}