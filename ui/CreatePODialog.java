package com.smartcashpro.ui;

import com.smartcashpro.db.ProductDAO;
import com.smartcashpro.db.PurchaseOrderDAO;
import com.smartcashpro.model.Product;
import com.smartcashpro.model.NonPerishableProduct; // Import for placeholder
import com.smartcashpro.model.PurchaseOrder;
import com.smartcashpro.model.PurchaseOrderItem;
import com.smartcashpro.model.Supplier;
import com.smartcashpro.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreatePODialog extends JDialog {

    private JComboBox<Supplier> supplierComboBox;
    private JComboBox<Product> productComboBox;
    private JTextField quantityField;
    private JTextField costPriceField;
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton savePOButton;
    private JButton cancelButton;
    private JTable poItemsTable;
    private DefaultTableModel poItemsTableModel;
    private JLabel totalCostLabel;

    private PurchaseOrderDAO poDAO;
    private ProductDAO productDAO;

    private List<PurchaseOrderItem> currentPOItems;
    private User currentUser;

    public CreatePODialog(Window owner, User currentUser) {
        super(owner, "Create New Purchase Order", ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.poDAO = new PurchaseOrderDAO();
        this.productDAO = new ProductDAO();
        this.currentPOItems = new ArrayList<>();

        initComponents();
        loadInitialData();
        pack();
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; topPanel.add(new JLabel("Supplier:"), gbc);
        supplierComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(supplierComboBox, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 1; topPanel.add(new JLabel("Product:"), gbc);
        productComboBox = new JComboBox<>();
        productComboBox.setPrototypeDisplayValue(
            new NonPerishableProduct("PROTOTYPE_SKU", "Long Product Name To Set Width For ComboBox Display",
                                     BigDecimal.ZERO, 0, BigDecimal.ZERO, 0)
        );
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(productComboBox, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 2; topPanel.add(new JLabel("Quantity:"), gbc);
        quantityField = new JTextField(5);
        gbc.gridx = 1; gbc.gridy = 2; topPanel.add(quantityField, gbc);

        gbc.gridx = 2; gbc.gridy = 2; topPanel.add(new JLabel("Cost Price/Unit:"), gbc);
        costPriceField = new JTextField(7);
        gbc.gridx = 3; gbc.gridy = 2; topPanel.add(costPriceField, gbc);

        addItemButton = new JButton("Add Item to PO");
        gbc.gridx = 4; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(addItemButton, gbc);
        gbc.anchor = GridBagConstraints.WEST;

        productComboBox.addActionListener(e -> {
            Product selectedProduct = (Product) productComboBox.getSelectedItem();
            if (selectedProduct != null && selectedProduct.getProductId() != 0 && // Check not a placeholder
                !"NO_PRODUCTS".equals(selectedProduct.getSku()) &&
                !"PROTOTYPE_SKU".equals(selectedProduct.getSku())) {
                costPriceField.setText(selectedProduct.getCurrentCostPrice().toPlainString());
                quantityField.setText("1");
                quantityField.requestFocus();
            } else {
                costPriceField.setText("");
                quantityField.setText("");
            }
        });

        String[] columnNames = {"Product ID", "Product Name", "Quantity", "Cost/Unit", "Line Total"};
        poItemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        poItemsTable = new JTable(poItemsTableModel);
        JScrollPane tableScrollPane = new JScrollPane(poItemsTable);

        JPanel bottomPanel = new JPanel(new BorderLayout(10,5));
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removeItemButton = new JButton("Remove Selected Item");
        actionButtonPanel.add(removeItemButton);

        totalCostLabel = new JLabel("Total PO Cost: $0.00");
        totalCostLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(totalCostLabel);

        bottomPanel.add(actionButtonPanel, BorderLayout.WEST);
        bottomPanel.add(totalPanel, BorderLayout.EAST);

        JPanel southButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        savePOButton = new JButton("Save Purchase Order");
        cancelButton = new JButton("Cancel");
        southButtonPanel.add(cancelButton);
        southButtonPanel.add(savePOButton);

        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(southButtonPanel, BorderLayout.PAGE_END);

        addItemButton.addActionListener(this::addItemToPOTable);
        removeItemButton.addActionListener(this::removeItemFromPOTable);
        savePOButton.addActionListener(this::savePurchaseOrder);
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadInitialData() {
        List<Supplier> suppliers = poDAO.getAllSuppliers();
        supplierComboBox.removeAllItems(); 
        if (suppliers == null || suppliers.isEmpty()) { 
             supplierComboBox.addItem(new Supplier(0, "No Suppliers Found - Add in Mngmt"));
        } else {
            for (Supplier s : suppliers) {
                supplierComboBox.addItem(s);
            }
        }

        List<Product> products = productDAO.getAllProducts();
        productComboBox.removeAllItems(); 
        if (products == null || products.isEmpty()) { 
             productComboBox.addItem(
                 new NonPerishableProduct("NO_PRODUCTS", "No Products Available - Add in Inventory", BigDecimal.ZERO, 0, BigDecimal.ZERO, 0)
             );
        } else {
            for (Product p : products) {
                productComboBox.addItem(p);
            }
            if (productComboBox.getItemCount() > 0) {
                productComboBox.setSelectedIndex(0);
            }
        }

        boolean validSupplierSelected = false;
        if (supplierComboBox.getItemCount() > 0 && supplierComboBox.getItemAt(0) != null) {
            Supplier firstSupplier = (Supplier) supplierComboBox.getItemAt(0);
            if (firstSupplier.getSupplierId() != 0) { // Not the "No Suppliers Found" placeholder
                validSupplierSelected = true;
            }
        }

        boolean validProductAvailable = false;
        if (productComboBox.getItemCount() > 0 && productComboBox.getItemAt(0) != null) {
            Product firstProduct = (Product) productComboBox.getItemAt(0);
            if (!"NO_PRODUCTS".equals(firstProduct.getSku())) { // Not the "No Products Available" placeholder
                validProductAvailable = true;
            }
        }
        
        addItemButton.setEnabled(validSupplierSelected && validProductAvailable);
        savePOButton.setEnabled(validSupplierSelected); 
    }

    private void addItemToPOTable(ActionEvent e) {
        Product selectedProduct = (Product) productComboBox.getSelectedItem();
        Supplier selectedSupplier = (Supplier) supplierComboBox.getSelectedItem();

        if (selectedSupplier == null || selectedSupplier.getSupplierId() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a valid supplier.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedProduct == null || selectedProduct.getProductId() == 0 || // Check for actual product ID
            "NO_PRODUCTS".equals(selectedProduct.getSku()) || 
            "PROTOTYPE_SKU".equals(selectedProduct.getSku())) {
            JOptionPane.showMessageDialog(this, "Please select a valid product.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            BigDecimal costPrice = new BigDecimal(costPriceField.getText().trim());

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (costPrice.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Cost price cannot be negative.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (PurchaseOrderItem existingItem : currentPOItems) {
                if (existingItem.getProductId() == selectedProduct.getProductId()) {
                    JOptionPane.showMessageDialog(this, "Product already added to this PO. Remove and re-add to change quantity/cost.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }

            PurchaseOrderItem newItem = new PurchaseOrderItem(0, 0,
                    selectedProduct.getProductId(), selectedProduct.getName(), quantity, costPrice, 0);
            currentPOItems.add(newItem);
            updatePOTableAndTotal();

            quantityField.setText("1");
            productComboBox.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity or cost price format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeItemFromPOTable(ActionEvent e) {
        int selectedRow = poItemsTable.getSelectedRow();
        if (selectedRow != -1) {
            currentPOItems.remove(selectedRow);
            updatePOTableAndTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updatePOTableAndTotal() {
        poItemsTableModel.setRowCount(0);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (PurchaseOrderItem item : currentPOItems) {
            BigDecimal lineTotal = item.getCostPricePerUnit().multiply(new BigDecimal(item.getQuantityOrdered()));
            poItemsTableModel.addRow(new Object[]{
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantityOrdered(),
                    item.getCostPricePerUnit(),
                    lineTotal
            });
            totalCost = totalCost.add(lineTotal);
        }
        totalCostLabel.setText(String.format("Total PO Cost: $%.2f", totalCost));
    }

    private void savePurchaseOrder(ActionEvent e) {
        if (currentPOItems.isEmpty()) {
            int confirmEmpty = JOptionPane.showConfirmDialog(this,
                "Purchase order has no items. Do you want to save an empty PO (e.g., for services or future items)?",
                "Empty Purchase Order", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmEmpty == JOptionPane.NO_OPTION) {
                return;
            }
        }
        Supplier selectedSupplier = (Supplier) supplierComboBox.getSelectedItem();
        if (selectedSupplier == null || selectedSupplier.getSupplierId() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a valid supplier.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = JOptionPane.showInputDialog(this, "Enter Expected Delivery Date (YYYY-MM-DD, optional):");
        LocalDate expectedDate = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                expectedDate = LocalDate.parse(dateStr.trim());
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        PurchaseOrder poHeader = new PurchaseOrder(selectedSupplier.getSupplierId(), currentUser.getUserId());
        poHeader.setExpectedDeliveryDate(expectedDate);
        poHeader.setStatus("Ordered");

        try {
            int newPOId = poDAO.createPurchaseOrder(poHeader, currentPOItems);
            JOptionPane.showMessageDialog(this, "Purchase Order #" + newPOId + " created successfully!", "PO Saved", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save Purchase Order:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception exGen){
            JOptionPane.showMessageDialog(this, "An unexpected error occurred:\n" + exGen.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            exGen.printStackTrace();
        }
    }
}