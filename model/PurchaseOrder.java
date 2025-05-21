package com.smartcashpro.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseOrder {
    private int purchaseOrderId;
    private LocalDateTime poDate;
    private String status;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private int supplierId;
    private String supplierName; // Added for display
    private int placedByUserId;
    private String placedByUserName; // Added for display
    private BigDecimal totalCost;

    // Enhanced constructor for loading with names
    public PurchaseOrder(int id, LocalDateTime date, String status, LocalDate expected, LocalDate actual,
                         int supId, String supName, int userId, String userName, BigDecimal cost) {
        this.purchaseOrderId = id;
        this.poDate = date;
        this.status = status;
        this.expectedDeliveryDate = expected;
        this.actualDeliveryDate = actual;
        this.supplierId = supId;
        this.supplierName = supName;
        this.placedByUserId = userId;
        this.placedByUserName = userName;
        this.totalCost = cost;
    }

    // Original constructor (can be kept for cases where names aren't fetched)
    public PurchaseOrder(int id, LocalDateTime date, String status, LocalDate expected, LocalDate actual,
                         int supId, int userId, BigDecimal cost) {
        this(id, date, status, expected, actual, supId, "N/A", userId, "N/A", cost);
    }

    // Constructor for creating a new one (names will be "N/A" initially)
    public PurchaseOrder(int supplierId, int placedByUserId) {
        this.purchaseOrderId = 0;
        this.poDate = LocalDateTime.now();
        this.status = "Pending";
        this.supplierId = supplierId;
        this.placedByUserId = placedByUserId;
        this.totalCost = BigDecimal.ZERO;
        this.supplierName = "N/A";
        this.placedByUserName = "N/A";
    }

    // Getters
    public int getPurchaseOrderId() { return purchaseOrderId; }
    public LocalDateTime getPoDate() { return poDate; }
    public String getStatus() { return status; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public LocalDate getActualDeliveryDate() { return actualDeliveryDate; }
    public int getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public int getPlacedByUserId() { return placedByUserId; }
    public String getPlacedByUserName() { return placedByUserName; }
    public BigDecimal getTotalCost() { return totalCost; }

    // Setters
    public void setPurchaseOrderId(int purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public void setStatus(String status) { this.status = status; }
    public void setActualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setPlacedByUserName(String placedByUserName) { this.placedByUserName = placedByUserName; }

    // Updated toString to include supplier name
    @Override
    public String toString() {
        return "PO #" + purchaseOrderId + " (" + status + ") - " + supplierName;
    }
}
