package com.smartcashpro.model;

public class Supplier {
    private int supplierId;
    private String supplierName;
    // Add other fields if needed (contact, address)

    // Constructor for loading from DB or creating
    public Supplier(int id, String name) {
        this.supplierId = id;
        this.supplierName = name;
    }

    // Getters
    public int getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }

    // It's important to override toString for JComboBox display
    @Override
    public String toString() {
        return supplierName; // Just the name is usually fine for a combo box
        // Or: return supplierName + " (ID: " + supplierId + ")";
    }

     // Override equals and hashCode if you put Supplier objects in Sets or as Map keys
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Supplier supplier = (Supplier) o;
         return supplierId == supplier.supplierId;
     }

     @Override
     public int hashCode() {
         return Integer.hashCode(supplierId);
     }
}