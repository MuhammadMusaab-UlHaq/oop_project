package com.smartcashpro.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Order {
    private int orderId;
    private Date orderDate;
    private BigDecimal totalAmount;
    private Integer customerId;
    private String customerName; // For display purposes
    private int userId;
    private String userName; // For display purposes
    private int shiftId;
    private String orderStatus;
    private String paymentType;
    private List<OrderItem> orderItems;
    
    // Constructor for new orders
    public Order(BigDecimal totalAmount, Integer customerId, int userId, int shiftId, String orderStatus) {
        this.orderDate = new Date(); // Current date
        this.totalAmount = totalAmount;
        this.customerId = customerId;
        this.userId = userId;
        this.shiftId = shiftId;
        this.orderStatus = orderStatus;
    }
    
    // Constructor for retrieved orders from database
    public Order(int orderId, Date orderDate, BigDecimal totalAmount, Integer customerId, 
                String customerName, int userId, String userName, int shiftId, 
                String orderStatus, String paymentType) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.customerId = customerId;
        this.customerName = customerName;
        this.userId = userId;
        this.userName = userName;
        this.shiftId = shiftId;
        this.orderStatus = orderStatus;
        this.paymentType = paymentType;
    }
    
    // Getters and setters
    public int getOrderId() { return orderId; }
    public Date getOrderDate() { return orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Integer getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getShiftId() { return shiftId; }
    public String getOrderStatus() { return orderStatus; }
    public String getPaymentType() { return paymentType; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    
    @Override
    public String toString() {
        return "Order #" + orderId + " - " + orderDate + " ($" + totalAmount + ")";
    }
} 