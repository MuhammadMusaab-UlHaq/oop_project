package com.smartcashpro.model;

import java.math.BigDecimal;


public class PurchaseOrderItem {
    private int purchaseOrderItemId;
    private int purchaseOrderId;
    private int productId;
    private int quantityOrdered;
    private BigDecimal costPricePerUnit;
    private int quantityReceived;
    private String productName; 

    
    public PurchaseOrderItem(int id, int poId, int prodId, String prodName, int qtyOrd, BigDecimal cost, int qtyRcvd) {
        this.purchaseOrderItemId = id;
        this.purchaseOrderId = poId;
        this.productId = prodId;
        this.productName = prodName;
        this.quantityOrdered = qtyOrd;
        this.costPricePerUnit = cost;
        this.quantityReceived = qtyRcvd;
    }

    
    public int getPurchaseOrderItemId() { return purchaseOrderItemId; }
    public int getPurchaseOrderId() { return purchaseOrderId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantityOrdered() { return quantityOrdered; }
    public BigDecimal getCostPricePerUnit() { return costPricePerUnit; }
    public int getQuantityReceived() { return quantityReceived; }

    
    

     @Override
     public String toString() {
         
         return String.format("%s (ID:%d Ord:%d Rcvd:%d)", productName, purchaseOrderItemId, quantityOrdered, quantityReceived);
     }
}