package com.smartcashpro.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.smartcashpro.model.User;
import com.smartcashpro.model.Product;
import com.smartcashpro.model.PerishableProduct;
import com.smartcashpro.model.NonPerishableProduct;
import com.smartcashpro.model.PurchaseOrderItem;
import com.smartcashpro.model.PurchaseOrder;
import com.smartcashpro.db.IProductDAO; // IMPORT THE INTERFACE
import com.smartcashpro.db.ProductDAO;  // Keep for instantiation
import com.smartcashpro.db.StockAdjustmentDAO;
import com.smartcashpro.db.PurchaseOrderDAO;
import com.smartcashpro.db.ShiftDAO;
import com.smartcashpro.model.Shift;
import com.smartcashpro.ui.ViewPOHistoryDialog;
import java.sql.SQLException;
import java.math.BigDecimal;

public class InventoryPanel extends JPanel {

    private User currentUser;
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private IProductDAO productDAO; // USE THE INTERFACE TYPE
    private StockAdjustmentDAO stockAdjustmentDAO;
    private PurchaseOrderDAO purchaseOrderDAO;
    private ShiftDAO shiftDAO;

    public InventoryPanel(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO(); // INSTANTIATE THE CONCRETE CLASS
        this.stockAdjustmentDAO = new StockAdjustmentDAO();
        this.purchaseOrderDAO = new PurchaseOrderDAO();
        this.shiftDAO = new ShiftDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddProduct = new JButton("Add Product");
        JButton btnEditProduct = new JButton("Edit Selected Product");
        JButton btnCreatePO = new JButton("Create New PO");
        JButton btnViewPOHistory = new JButton("View PO History");
        JButton btnReceiveStock = new JButton("Receive Stock (PO)");
        JButton btnAdjustStock = new JButton("Adjust Stock");
        JButton btnRefresh = new JButton("Refresh List");

        actionPanel.add(btnAddProduct);
        actionPanel.add(btnEditProduct);
        actionPanel.add(btnCreatePO);
        actionPanel.add(btnViewPOHistory);
        actionPanel.add(btnReceiveStock);
        actionPanel.add(btnAdjustStock);
        actionPanel.add(btnRefresh);

        String[] columnNames = {"ID", "SKU", "Name", "Qty", "Unit Price", "Cost Price", "Reorder Lvl", "Type"};
        productTableModel = new DefaultTableModel(columnNames, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(productTableModel);

        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(0).setMaxWidth(80);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        productTable.getColumnModel().getColumn(3).setMaxWidth(80);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(6).setMaxWidth(100);
        productTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        productTable.setDefaultRenderer(Object.class, new LowStockRenderer());

        JScrollPane tableScrollPane = new JScrollPane(productTable);

        add(actionPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadProductData());
        btnAddProduct.addActionListener(e -> displayAddProductDialog());
        btnEditProduct.addActionListener(e -> displayEditProductDialog());
        btnCreatePO.addActionListener(e -> displayCreatePODialog());
        btnViewPOHistory.addActionListener(e -> displayPOHistoryDialog());
        btnAdjustStock.addActionListener(e -> displayAdjustStockDialog());
        btnReceiveStock.addActionListener(e -> displaySelectPODialog());

        loadProductData();
    }

    private void displayPOHistoryDialog() {
        ViewPOHistoryDialog historyDialog = new ViewPOHistoryDialog(SwingUtilities.getWindowAncestor(this));
        historyDialog.setVisible(true);
    }

    private void loadProductData() {
        try {
            productTableModel.setRowCount(0);
            List<Product> products = productDAO.getAllProducts(); // Uses IProductDAO
            if (products == null) { // Should not happen if DAO returns empty list on DB error, but good check
                JOptionPane.showMessageDialog(this, "Error loading products: Product list is null.", "Load Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (Product p : products) {
                productTableModel.addRow(new Object[]{
                        p.getProductId(), p.getSku(), p.getName(),
                        p.getQuantityInStock(), p.getUnitPrice(), p.getCurrentCostPrice(),
                        p.getReorderLevel(),
                        p.getProductType()
                });
            }
        } catch (Exception ex) { // Catching general Exception as getAllProducts() might not throw SQLException directly
            JOptionPane.showMessageDialog(this, "An unexpected error occurred while loading products:\n" + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void displayAddProductDialog() {
        ProductDialog addDialog = new ProductDialog(SwingUtilities.getWindowAncestor(this), "Add New Product", null);
        addDialog.setVisible(true);
        if (addDialog.isSaved()) {
            loadProductData();
        }
    }

    private void displayEditProductDialog() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int productId = (Integer) productTableModel.getValueAt(selectedRow, 0);
            Product productToEdit = productDAO.findProductById(productId); // Uses IProductDAO
            if (productToEdit != null) {
                ProductDialog editDialog = new ProductDialog(SwingUtilities.getWindowAncestor(this), "Edit Product", productToEdit);
                editDialog.setVisible(true);
                if (editDialog.isSaved()) {
                    loadProductData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not retrieve product details for editing (ID: " + productId + "). It might have been deleted or an error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
                loadProductData(); // Refresh to reflect potential deletion
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error preparing edit dialog:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void displayCreatePODialog() {
        CreatePODialog createPODialog = new CreatePODialog(SwingUtilities.getWindowAncestor(this), currentUser);
        createPODialog.setVisible(true);
    }

    private void displayAdjustStockDialog() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to adjust stock.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Shift currentShift = shiftDAO.findOpenShift();
        if (currentShift == null) {
            JOptionPane.showMessageDialog(this, "Cannot adjust stock: No shift is currently active.\nPlease start a shift first.", "Shift Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int shiftId = currentShift.getShiftId();

        try {
            int productId = (Integer) productTableModel.getValueAt(selectedRow, 0);
            String productName = (String) productTableModel.getValueAt(selectedRow, 2);

            StockAdjustmentDialog adjustDialog = new StockAdjustmentDialog(
                SwingUtilities.getWindowAncestor(this),
                "Adjust Stock for " + productName,
                productId,
                shiftId,
                currentUser.getUserId()
            );
            adjustDialog.setVisible(true);

            if (adjustDialog.isStockAdjusted()) {
                loadProductData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening adjustment dialog:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void displaySelectPODialog() {
        String poIdStr = JOptionPane.showInputDialog(this, "Enter Purchase Order ID to receive:");
        if (poIdStr == null || poIdStr.trim().isEmpty()) {
            return;
        }
        int poId;
        try {
            poId = Integer.parseInt(poIdStr.trim());
            PurchaseOrder po = purchaseOrderDAO.findPurchaseOrderById(poId);
            if (po == null) {
                JOptionPane.showMessageDialog(this, "Purchase Order ID " + poId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if ("Received".equals(po.getStatus()) || "Cancelled".equals(po.getStatus())) {
                 String message = "Purchase Order ID " + poId + " is already '" + po.getStatus() + "' and cannot be received against.";
                JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            displayReceiveStockDialog(poId);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Purchase Order ID format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException sqlEx) {
             JOptionPane.showMessageDialog(this, "Database error checking PO status:\n" + sqlEx.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
             sqlEx.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error checking Purchase Order status:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void displayReceiveStockDialog(int poId) {
        try {
            List<PurchaseOrderItem> items = purchaseOrderDAO.getPurchaseOrderItems(poId);
            if (items == null || items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No receivable items found for PO ID: " + poId, "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JDialog receiveDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Receive Items for PO " + poId,
                    Dialog.ModalityType.APPLICATION_MODAL);
            receiveDialog.setLayout(new BorderLayout(10, 10));
            receiveDialog.setSize(700, 450);
            receiveDialog.setLocationRelativeTo(this);

            JPanel itemsPanel = new JPanel();
            itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
            itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            Map<Integer, JTextField> inputFields = new HashMap<>();
            boolean itemsNeedReceiving = false;

            for (PurchaseOrderItem item : items) {
                int remainingQty = item.getQuantityOrdered() - item.getQuantityReceived();
                if (remainingQty > 0) {
                    itemsNeedReceiving = true;
                    JPanel itemRowPanel = new JPanel(new GridBagLayout());
                    itemRowPanel.setBorder(BorderFactory.createEtchedBorder());
                    GridBagConstraints gbcItem = new GridBagConstraints();
                    gbcItem.insets = new Insets(3,5,3,5);
                    gbcItem.anchor = GridBagConstraints.WEST;

                    gbcItem.gridx = 0; gbcItem.weightx = 0.6; itemRowPanel.add(new JLabel(String.format("%s (ID: %d)", item.getProductName(), item.getPurchaseOrderItemId())), gbcItem);
                    gbcItem.gridx = 1; gbcItem.weightx = 0.1; itemRowPanel.add(new JLabel(String.format("Ord: %d", item.getQuantityOrdered())), gbcItem);
                    gbcItem.gridx = 2; gbcItem.weightx = 0.1; itemRowPanel.add(new JLabel(String.format("Rcvd: %d", item.getQuantityReceived())), gbcItem);
                    gbcItem.gridx = 3; gbcItem.weightx = 0.05; itemRowPanel.add(new JLabel("Now:"), gbcItem);
                    gbcItem.gridx = 4; gbcItem.weightx = 0.15; gbcItem.fill = GridBagConstraints.HORIZONTAL;
                    JTextField qtyInput = new JTextField(String.valueOf(remainingQty), 5);
                    itemRowPanel.add(qtyInput, gbcItem);

                    inputFields.put(item.getPurchaseOrderItemId(), qtyInput);
                    itemsPanel.add(itemRowPanel);
                    itemsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }

            if (!itemsNeedReceiving) {
                JOptionPane.showMessageDialog(this, "All items on PO " + poId + " have already been fully received.", "PO Complete", JOptionPane.INFORMATION_MESSAGE);
                try {
                    PurchaseOrder po = purchaseOrderDAO.findPurchaseOrderById(poId);
                    if (po != null && !"Received".equals(po.getStatus())) {
                       purchaseOrderDAO.receiveFullOrder(poId);
                       JOptionPane.showMessageDialog(this, "PO status updated to 'Received'.", "Status Update", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException exStatus) {
                    System.err.println("Error trying to update status for already received PO: " + exStatus.getMessage());
                }
                receiveDialog.dispose();
                return;
            }

            JButton confirmButton = new JButton("Confirm Received Quantities");
            confirmButton.addActionListener(e_confirm -> {
                Map<Integer, Integer> quantitiesToReceive = new HashMap<>();
                boolean inputValid = true;
                String firstError = null;
                try {
                    for (Map.Entry<Integer, JTextField> entry : inputFields.entrySet()) {
                        int poItemId = entry.getKey();
                        String qtyStr = entry.getValue().getText().trim();
                        int qty = qtyStr.isEmpty() ? 0 : Integer.parseInt(qtyStr);
                        if (qty < 0) {
                            firstError = "Quantity cannot be negative (Item ID: " + poItemId + ").";
                            throw new NumberFormatException(firstError);
                        }
                        if (qty > 0) {
                            quantitiesToReceive.put(poItemId, qty);
                        }
                    }
                } catch (NumberFormatException ex_format) {
                    inputValid = false;
                    String errorMessage = (firstError != null) ? firstError : "Invalid quantity entered. Please enter whole non-negative numbers.";
                    JOptionPane.showMessageDialog(receiveDialog, errorMessage, "Input Error", JOptionPane.ERROR_MESSAGE);
                }

                if (inputValid) {
                    if (quantitiesToReceive.isEmpty()) {
                        JOptionPane.showMessageDialog(receiveDialog, "No quantities entered to receive.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        try {
                            boolean success = purchaseOrderDAO.receiveStock(poId, quantitiesToReceive);
                            if (success) {
                                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                                                              "Stock received successfully for PO: " + poId);
                                loadProductData();
                                receiveDialog.dispose();
                            }
                        } catch (SQLException sqlEx) {
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                                                          "Error receiving stock for PO " + poId + ":\n" + sqlEx.getMessage(),
                                                          "Database Error", JOptionPane.ERROR_MESSAGE);
                            sqlEx.printStackTrace();
                        } catch (IllegalArgumentException iae) {
                             JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                                                          "Input error for PO " + poId + ":\n" + iae.getMessage(),
                                                          "Input Error", JOptionPane.ERROR_MESSAGE);
                             iae.printStackTrace();
                        } catch (Exception generalEx) {
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                                                          "An unexpected error occurred during stock receiving:\n" + generalEx.getMessage(),
                                                          "Critical Error", JOptionPane.ERROR_MESSAGE);
                            generalEx.printStackTrace();
                        }
                    }
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e_cancel -> receiveDialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(cancelButton);
            buttonPanel.add(confirmButton);

            receiveDialog.add(new JScrollPane(itemsPanel), BorderLayout.CENTER);
            receiveDialog.add(buttonPanel, BorderLayout.SOUTH);
            receiveDialog.setVisible(true);

        } catch (SQLException sqlEx_main) {
             JOptionPane.showMessageDialog(this, "Database error opening receiving dialog for PO " + poId + ":\n" + sqlEx_main.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            sqlEx_main.printStackTrace();
        } catch (Exception ex_main) {
            JOptionPane.showMessageDialog(this, "Error opening receiving dialog for PO " + poId + ":\n" + ex_main.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex_main.printStackTrace();
        }
    }

    class LowStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try {
                if (table.getModel().getColumnCount() > 6 && row < table.getModel().getRowCount()) {
                    Object qtyValue = table.getModel().getValueAt(row, 3);
                    Object reorderValue = table.getModel().getValueAt(row, 6);

                    int qty = (qtyValue instanceof Integer) ? (Integer) qtyValue : Integer.parseInt(qtyValue.toString());
                    int reorderLvl = (reorderValue instanceof Integer) ? (Integer) reorderValue : Integer.parseInt(reorderValue.toString());

                    if (reorderLvl > 0 && qty <= reorderLvl) {
                        c.setBackground(Color.ORANGE);
                        c.setForeground(Color.BLACK);
                    } else {
                        setDefaultColors(c, isSelected, table);
                    }
                } else {
                    setDefaultColors(c, isSelected, table);
                }
            } catch (Exception e_render) {
                setDefaultColors(c, isSelected, table);
            }
            return c;
        }
        private void setDefaultColors(Component c, boolean isSelected, JTable table) {
            c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        }
    }

    class ProductDialog extends JDialog {
        private JTextField nameField, skuField, priceField, costField, qtyField, reorderField;
        private JTextField storageTempField;
        private JLabel storageTempLabel;
        private JComboBox<String> productTypeCombo;
        private JLabel productTypeLabel;

        private JButton saveButton, cancelButton;
        private Product productToEdit; // This will be IProductDAO after further refactoring
        private boolean saved = false;

        public ProductDialog(Window owner, String title, Product product) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            this.productToEdit = product;
            initComponents();
            populateFields();
            // Pack only once after all initial component visibility is set
            if(!this.isShowing()){
                 pack();
            }
            setMinimumSize(new Dimension(450, 350));
            setLocationRelativeTo(owner);
        }

        private void initComponents() {
            nameField = new JTextField(25);
            skuField = new JTextField(15);
            priceField = new JTextField(10);
            costField = new JTextField(10);
            qtyField = new JTextField(5);
            reorderField = new JTextField(5);

            productTypeLabel = new JLabel("Product Type:");
            productTypeCombo = new JComboBox<>(new String[]{"Non-Perishable", "Perishable"});

            storageTempLabel = new JLabel("Storage Temp (°C):");
            storageTempField = new JTextField(10);

            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            int y_pos = 0;
            gbc.gridx = 0; gbc.gridy = y_pos; panel.add(new JLabel("Name:"), gbc);
            gbc.gridx = 1; gbc.gridy = y_pos; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(nameField, gbc);
            gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

            y_pos++;
            gbc.gridx = 0; gbc.gridy = y_pos; panel.add(new JLabel("SKU:"), gbc);
            gbc.gridx = 1; gbc.gridy = y_pos; panel.add(skuField, gbc);

            y_pos++;
            gbc.gridx = 0; gbc.gridy = y_pos; panel.add(new JLabel("Price:"), gbc);
            gbc.gridx = 1; gbc.gridy = y_pos; panel.add(priceField, gbc);
            gbc.gridx = 2; gbc.gridy = y_pos; panel.add(new JLabel("Cost:"), gbc);
            gbc.gridx = 3; gbc.gridy = y_pos; panel.add(costField, gbc);

            y_pos++;
            gbc.gridx = 0; gbc.gridy = y_pos; panel.add(new JLabel(productToEdit == null ? "Initial Qty:" : "Current Qty:"), gbc);
            gbc.gridx = 1; gbc.gridy = y_pos; panel.add(qtyField, gbc);
            gbc.gridx = 2; gbc.gridy = y_pos; panel.add(new JLabel("Reorder Lvl:"), gbc);
            gbc.gridx = 3; gbc.gridy = y_pos; panel.add(reorderField, gbc);

            y_pos++;
            gbc.gridx = 0; gbc.gridy = y_pos; panel.add(productTypeLabel, gbc);
            gbc.gridx = 1; gbc.gridy = y_pos; gbc.gridwidth = 3; panel.add(productTypeCombo, gbc);
            gbc.gridwidth = 1;

            y_pos++;
            gbc.gridx = 0; gbc.gridy = y_pos; panel.add(storageTempLabel, gbc);
            gbc.gridx = 1; gbc.gridy = y_pos; gbc.gridwidth = 3; panel.add(storageTempField, gbc);
            gbc.gridwidth = 1;

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);

            setLayout(new BorderLayout(10, 10));
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            saveButton.addActionListener(e_save -> saveProductAction());
            cancelButton.addActionListener(e_cancel -> dispose());

            productTypeCombo.addActionListener(e_type -> {
                boolean isPerishableSelected = "Perishable".equals(productTypeCombo.getSelectedItem());
                storageTempLabel.setVisible(isPerishableSelected);
                storageTempField.setVisible(isPerishableSelected);
                storageTempField.setEditable(isPerishableSelected);

                if (this.isShowing()) { 
                     this.pack();
                }
            });
        }

        private void populateFields() {
            if (productToEdit != null) {
                nameField.setText(productToEdit.getName());
                skuField.setText(productToEdit.getSku());
                skuField.setEditable(false);
                priceField.setText(productToEdit.getUnitPrice().toPlainString());
                costField.setText(productToEdit.getCurrentCostPrice().toPlainString());
                qtyField.setText(String.valueOf(productToEdit.getQuantityInStock()));
                qtyField.setEditable(false);
                reorderField.setText(String.valueOf(productToEdit.getReorderLevel()));

                productTypeCombo.setEnabled(false);
                productTypeLabel.setText("Product Type (Fixed):");

                if (productToEdit instanceof PerishableProduct) {
                    productTypeCombo.setSelectedItem("Perishable");
                    PerishableProduct pp = (PerishableProduct) productToEdit;
                    storageTempField.setText(pp.getStorageTempRequirement() != null ? pp.getStorageTempRequirement() : "");
                    storageTempLabel.setVisible(true);
                    storageTempField.setVisible(true);
                    storageTempField.setEditable(true);
                } else {
                    productTypeCombo.setSelectedItem("Non-Perishable");
                    storageTempLabel.setVisible(false);
                    storageTempField.setVisible(false);
                    storageTempField.setText("");
                    storageTempField.setEditable(false);
                }
            } else {
                qtyField.setText("0");
                reorderField.setText("0");
                skuField.setEditable(true);
                qtyField.setEditable(true);
                productTypeCombo.setEnabled(true);
                productTypeLabel.setText("Product Type:");

                boolean isPerishableSelected = "Perishable".equals(productTypeCombo.getSelectedItem());
                storageTempLabel.setVisible(isPerishableSelected);
                storageTempField.setVisible(isPerishableSelected);
                storageTempField.setEditable(isPerishableSelected);
            }
        }

        private void saveProductAction() {
            String name = nameField.getText().trim();
            String sku = skuField.getText().trim();
            String priceStr = priceField.getText().trim();
            String costStr = costField.getText().trim();
            String qtyStr = qtyField.getText().trim();
            String reorderStr = reorderField.getText().trim();
            String selectedType = (String) productTypeCombo.getSelectedItem();
            String storageTemp = storageTempField.getText().trim();

            if (name.isEmpty() || sku.isEmpty() || priceStr.isEmpty() || costStr.isEmpty() || qtyStr.isEmpty() || reorderStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, SKU, Price, Cost, Quantity, and Reorder Level are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BigDecimal price = new BigDecimal(priceStr);
                BigDecimal cost = new BigDecimal(costStr);
                int qty = Integer.parseInt(qtyStr);
                int reorderLvl = Integer.parseInt(reorderStr);

                if (price.compareTo(BigDecimal.ZERO) < 0 || cost.compareTo(BigDecimal.ZERO) < 0 ||
                    (productToEdit == null && qty < 0) || reorderLvl < 0) {
                    JOptionPane.showMessageDialog(this, "Price, Cost, and Reorder Level cannot be negative. Initial Quantity cannot be negative.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Product productToSave;

                if (productToEdit != null) {
                    productToSave = productToEdit;
                    productToSave.setName(name);
                    // productToSave.setSku(sku); // SKU is not editable for existing products
                    productToSave.setUnitPrice(price);
                    productToSave.setCurrentCostPrice(cost);
                    productToSave.setReorderLevel(reorderLvl);
                    if (productToSave instanceof PerishableProduct) {
                        ((PerishableProduct) productToSave).setStorageTempRequirement(storageTemp.isEmpty() ? null : storageTemp);
                    }
                } else {
                    if ("Perishable".equals(selectedType)) {
                        productToSave = new PerishableProduct(sku, name, price, qty, cost, reorderLvl, storageTemp.isEmpty() ? null : storageTemp);
                    } else {
                        productToSave = new NonPerishableProduct(sku, name, price, qty, cost, reorderLvl);
                    }
                }

                boolean success = InventoryPanel.this.productDAO.saveProduct(productToSave); // Use InventoryPanel.this.productDAO

                if (success) {
                    saved = true;
                    JOptionPane.showMessageDialog(this, "Product " + (productToEdit == null ? "added" : "updated") + " successfully!");
                    dispose();
                } else {
                    String errorMessage = "Failed to save product.";
                     Product existingSkuProductCheck = InventoryPanel.this.productDAO.findProductBySku(sku);
                     if (productToEdit == null && existingSkuProductCheck != null) { 
                         errorMessage += "\nReason: SKU '" + sku + "' already exists.";
                     } else if (productToEdit != null && existingSkuProductCheck != null && existingSkuProductCheck.getProductId() != productToEdit.getProductId()) {
                         // This case is unlikely if SKU is not editable for existing products
                         errorMessage += "\nReason: SKU '" + sku + "' already exists for another product.";
                     }
                    JOptionPane.showMessageDialog(this, errorMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex_num) {
                JOptionPane.showMessageDialog(this, "Invalid number format for Price, Cost, Qty, or Reorder Level.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex_gen) { // Catch general Exception to include potential SQLExceptions from DAO
                JOptionPane.showMessageDialog(this, "Error saving product:\n" + ex_gen.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex_gen.printStackTrace();
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }

    class StockAdjustmentDialog extends JDialog {
        private int productId;
        private int shiftId;
        private int userId;
        private JTextField qtyChangeField;
        private JComboBox<String> reasonCombo;
        private JTextArea notesArea;
        private JButton adjustButton, cancelButton;
        private boolean stockAdjusted = false;
        private StockAdjustmentDAO stockAdjustmentDAO_dialog;

        public StockAdjustmentDialog(Window owner, String title, int productId, int shiftId, int userId) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            this.productId = productId;
            this.shiftId = shiftId;
            this.userId = userId;
            this.stockAdjustmentDAO_dialog = new StockAdjustmentDAO();
            initComponents();
            pack();
            setLocationRelativeTo(owner);
        }

        private void initComponents() {
            qtyChangeField = new JTextField(5);
            String[] reasons = {"Damage", "Spoilage", "Count Correction", "Theft", "Promotion", "Other"};
            reasonCombo = new JComboBox<>(reasons);
            notesArea = new JTextArea(3, 25);
            adjustButton = new JButton("Adjust Stock");
            cancelButton = new JButton("Cancel");

            JPanel panel = new JPanel(new BorderLayout(10,10));
            JPanel inputGrid = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,5,5,5); gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx=0; gbc.gridy=0; inputGrid.add(new JLabel("Quantity Change (+/-):"), gbc);
            gbc.gridx=1; gbc.gridy=0; inputGrid.add(qtyChangeField, gbc);
            gbc.gridx=0; gbc.gridy=1; inputGrid.add(new JLabel("Reason:"), gbc);
            gbc.gridx=1; gbc.gridy=1; inputGrid.add(reasonCombo, gbc);

            notesArea.setBorder(BorderFactory.createTitledBorder("Notes (Optional)"));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(cancelButton);
            buttonPanel.add(adjustButton);

            panel.add(inputGrid, BorderLayout.NORTH);
            panel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
            panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            setLayout(new BorderLayout());
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            adjustButton.addActionListener(e -> adjustStockAction());
            cancelButton.addActionListener(e -> dispose());
        }

        private void adjustStockAction() {
            String qtyStr = qtyChangeField.getText().trim();
            if (qtyStr.isEmpty()){
                JOptionPane.showMessageDialog(this, "Quantity change cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int quantityChange = Integer.parseInt(qtyStr);
                String reason = (String) reasonCombo.getSelectedItem();
                String notes = notesArea.getText().trim();

                if (quantityChange == 0) {
                    JOptionPane.showMessageDialog(this, "Quantity change cannot be zero.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                stockAdjustmentDAO_dialog.adjustStock(productId, this.userId, this.shiftId, quantityChange, reason, notes);

                stockAdjusted = true;
                JOptionPane.showMessageDialog(this, "Stock adjusted successfully!");
                dispose();

            } catch (NumberFormatException ex_num_adj) {
                JOptionPane.showMessageDialog(this, "Invalid number format for Quantity Change.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException | IllegalArgumentException ex_sql_arg_adj) {
                JOptionPane.showMessageDialog(this, "Failed to adjust stock:\n" + ex_sql_arg_adj.getMessage(), "Adjustment Error", JOptionPane.ERROR_MESSAGE);
                ex_sql_arg_adj.printStackTrace();
            } catch (Exception ex_adj) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred:\n" + ex_adj.getMessage(), "Critical Error", JOptionPane.ERROR_MESSAGE);
                ex_adj.printStackTrace();
            }
        }

        public boolean isStockAdjusted() {
            return stockAdjusted;
        }
    }
}