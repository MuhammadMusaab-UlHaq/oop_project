package com.smartcashpro.model;

public class Customer {
    private int customerId;
    private String name;
    private String contactInfo;
    private int loyaltyPoints;

    
    public Customer(int id, String name, String contact, int points) {
        this.customerId = id;
        this.name = name;
        this.contactInfo = contact;
        this.loyaltyPoints = points;
    }
    
    public Customer(String name, String contact, int points) {
        this(0, name, contact, points);
    }


    
    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getContactInfo() { return contactInfo; }
    public int getLoyaltyPoints() { return loyaltyPoints; }

    
     public void setCustomerId(int customerId) { this.customerId = customerId; } 
     public void setName(String name) { this.name = name; }
     public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
     public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    @Override
    
    public String toString() { return name + (contactInfo != null && !contactInfo.isEmpty() ? " (" + contactInfo + ")" : ""); }
}