package com.smartcashpro.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import com.smartcashpro.model.User;
import com.smartcashpro.model.Product;
import com.smartcashpro.model.OrderItem;
import com.smartcashpro.model.Customer; 
import com.smartcashpro.model.Shift; 
import com.smartcashpro.db.ProductDAO;
import com.smartcashpro.db.OrderDAO;
import com.smartcashpro.db.ShiftDAO;
import com.smartcashpro.db.CustomerDAO;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.sql.SQLException; 

public class POSPanel extends JPanel {
    
    private User currentUser;
    private JTextField skuInput;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JButton payCashButton, payCardButton, returnButton, customerButton, addItemButton; 
    private JLabel customerLabel;
    private JPanel cartPanel;
    private JPanel actionPanel;
    
    // Color scheme
    private Color primaryColor = new Color(41, 128, 185);
    private Color secondaryColor = new Color(52, 152, 219);
    private Color successColor = new Color(46, 204, 113);
    private Color dangerColor = new Color(231, 76, 60);
    private Color warningColor = new Color(241, 196, 15);
    private Color lightGrayColor = new Color(245, 245, 245);
    
    // Currency formatter
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private ShiftDAO shiftDAO;
    private CustomerDAO customerDAO;
    
    private List<OrderItem> currentSaleOrderItems = new ArrayList<>();
    private Customer currentCustomer = null; 
    
    public POSPanel(User user) {
        this.currentUser = user;
        
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.shiftDAO = new ShiftDAO(); 
        this.customerDAO = new CustomerDAO();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Create top panel with product search
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Create center panel (cart)
        cartPanel = createCartPanel();
        add(cartPanel, BorderLayout.CENTER);
        
        // Create right panel (totals and actions)
        actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.EAST);

        // Register event handlers
        registerEventHandlers();
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
        
        // Left side - SKU input
        JPanel skuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        skuPanel.setBackground(Color.WHITE);
        
        JLabel skuLabel = new JLabel("Scan/Enter SKU:");
        skuLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        skuInput = new JTextField(15);
        skuInput.setFont(new Font("Arial", Font.PLAIN, 14));
        skuInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        addItemButton = createStyledButton("Add Item", primaryColor, "+");
        
        skuPanel.add(skuLabel);
        skuPanel.add(skuInput);
        skuPanel.add(addItemButton);
        
        // Right side - Customer info
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        customerPanel.setBackground(Color.WHITE);
        
        customerLabel = new JLabel("Customer: Not Selected");
        customerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        customerLabel.setIcon(createTextIcon("O", new Font("Arial", Font.PLAIN, 16)));
        
        customerButton = createStyledButton("Find/Add Customer", secondaryColor, "?");
        
        customerPanel.add(customerLabel);
        customerPanel.add(customerButton);

        topPanel.add(skuPanel, BorderLayout.WEST);
        topPanel.add(customerPanel, BorderLayout.EAST);

        return topPanel;
    }
    
    private JPanel createCartPanel() {
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(Color.WHITE);
        cartPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 15));
        
        // Create table header
        JLabel cartHeader = new JLabel("Shopping Cart");
        cartHeader.setFont(new Font("Arial", Font.BOLD, 16));
        cartHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create items table
        String[] columnNames = {"SKU", "Name", "Qty", "Unit Price", "Line Total"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3 || column == 4) return BigDecimal.class;
                if (column == 2) return Integer.class;
                return String.class;
            }
        };
        
        itemsTable = new JTable(tableModel);
        itemsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        itemsTable.setRowHeight(30);
        itemsTable.setShowGrid(false);
        itemsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Set column widths
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(80); 
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(250); 
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(40); 
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(80); 
        itemsTable.getColumnModel().getColumn(4).setPreferredWidth(90); 
        
        // Custom cell renderer for currency
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        // Add empty cart message as placeholder
        JPanel emptyCartPanel = new JPanel(new GridBagLayout());
        emptyCartPanel.setBackground(Color.WHITE);
        
        JLabel emptyCartLabel = new JLabel("Cart is empty. Scan items to add them.");
        emptyCartLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        emptyCartLabel.setForeground(Color.GRAY);
        
        emptyCartPanel.add(emptyCartLabel);
        
        // Add components to cart panel
        cartPanel.add(cartHeader, BorderLayout.NORTH);
        cartPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        helpPanel.setBackground(Color.WHITE);
        JLabel helpLabel = new JLabel("Double-click an item to remove it");
        helpLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        helpLabel.setForeground(Color.GRAY);
        helpPanel.add(helpLabel);
        
        cartPanel.add(helpPanel, BorderLayout.SOUTH);
        
        return cartPanel;
    }
    
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 15, 5, 5)
        ));
        actionPanel.setPreferredSize(new Dimension(250, getHeight()));
        
        // Create total display
        JPanel totalPanel = new JPanel(new BorderLayout(0, 5));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 0, 15, 0)
        ));
        
        JLabel totalHeaderLabel = new JLabel("TOTAL");
        totalHeaderLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 28));
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setForeground(primaryColor);
        
        totalPanel.add(totalHeaderLabel, BorderLayout.NORTH);
        totalPanel.add(totalLabel, BorderLayout.CENTER);
        
        // Create payment buttons
        JPanel paymentPanel = new JPanel();
        paymentPanel.setBackground(Color.WHITE);
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel paymentLabel = new JLabel("PAYMENT OPTIONS");
        paymentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        paymentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        paymentLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        payCashButton = createPaymentButton("Pay with Cash", successColor, "$");
        payCardButton = createPaymentButton("Pay with Card", primaryColor, "C");
        
        paymentPanel.add(paymentLabel);
        paymentPanel.add(payCashButton);
        paymentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        paymentPanel.add(payCardButton);
        
        // Create other actions panel
        JPanel otherActionsPanel = new JPanel();
        otherActionsPanel.setBackground(Color.WHITE);
        otherActionsPanel.setLayout(new BoxLayout(otherActionsPanel, BoxLayout.Y_AXIS));
        otherActionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        otherActionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JLabel otherLabel = new JLabel("OTHER ACTIONS");
        otherLabel.setFont(new Font("Arial", Font.BOLD, 14));
        otherLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        otherLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        returnButton = createPaymentButton("Process Return", warningColor, "<");
        
        otherActionsPanel.add(otherLabel);
        otherActionsPanel.add(returnButton);
        
        // Add all components to action panel
        actionPanel.add(totalPanel);
        actionPanel.add(paymentPanel);
        actionPanel.add(Box.createVerticalGlue()); 
        actionPanel.add(otherActionsPanel);
        
        return actionPanel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor, String iconText) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        if (iconText != null) {
            button.setIcon(createTextIcon(iconText, new Font("Dialog", Font.PLAIN, 14)));
        }
        
        return button;
    }
    
    private JButton createPaymentButton(String text, Color backgroundColor, String iconText) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 40));
        button.setPreferredSize(new Dimension(220, 40));
        
        if (iconText != null) {
            button.setIcon(createTextIcon(iconText, new Font("Dialog", Font.PLAIN, 18)));
            button.setIconTextGap(10);
        }
        
        return button;
    }
    
    private Icon createTextIcon(final String text, final Font font) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(font);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g2.drawString(text, x, y + textHeight - fm.getDescent());
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }
    
    private void registerEventHandlers() {
        addItemButton.addActionListener(e -> addItem());
        skuInput.addActionListener(e -> addItem()); 
        customerButton.addActionListener(e -> findCustomer());
        payCashButton.addActionListener(e -> processPayment("Cash")); 
        payCardButton.addActionListener(e -> processPayment("Card")); 
        returnButton.addActionListener(e -> handleReturnAction());    
        
        itemsTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    removeItem(table.getSelectedRow());
                }
            }
        });
    }

    private void addItem() {
        String sku = skuInput.getText().trim();
        if (sku.isEmpty()) {
            skuInput.requestFocus();
            return;
        }

        try {
            Product product = productDAO.findProductBySku(sku);

            if (product != null) {
                 Product currentProductState = productDAO.findProductById(product.getProductId()); 
                 if (currentProductState == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Error retrieving current product state for stock check.",
                        "Stock Check Error", 
                        JOptionPane.ERROR_MESSAGE);
                      return;
                 }

                boolean found = false;
                for (int i = 0; i < currentSaleOrderItems.size(); i++) {
                    OrderItem currentItem = currentSaleOrderItems.get(i);
                    if (currentItem.getProductId() == product.getProductId()) {
                        if (currentProductState.getQuantityInStock() > currentItem.getQuantity()) {
                            currentItem.setQuantity(currentItem.getQuantity() + 1);
                            found = true;
                            break;
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "<html><b>Cannot add more of '" + product.getName() + "'.</b><br>Only " 
                                + currentProductState.getQuantityInStock() + " in stock total.</html>",
                                "Stock Limit", 
                                JOptionPane.WARNING_MESSAGE);
                            skuInput.setText("");
                            skuInput.requestFocus();
                            return; 
                        }
                    }
                }
                
                if (!found) {
                    if (currentProductState.getQuantityInStock() > 0) {
                         OrderItem newItem = new OrderItem(
                            product.getProductId(), product.getName(), 1,
                            product.getUnitPrice(), product.getCurrentCostPrice()
                        );
                        currentSaleOrderItems.add(newItem);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "<html><b>Product '" + product.getName() + "' is out of stock!</b></html>",
                            "Stock Alert", 
                            JOptionPane.WARNING_MESSAGE);
                         skuInput.setText("");
                         skuInput.requestFocus();
                         return; 
                    }
                }
                
                updateTableAndTotal();

                // Flash effect for success
                showSuccessFlash();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Product with SKU '" + sku + "' not found!",
                    "Product Not Found", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, 
                "Error adding product:\n" + ex.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

            skuInput.setText(""); 
            skuInput.requestFocus();
        }

    private void showSuccessFlash() {
        // Create a flash effect to indicate item was added
        JPanel flashPanel = new JPanel();
        flashPanel.setBackground(new Color(46, 204, 113, 100));
        flashPanel.setBounds(0, 0, cartPanel.getWidth(), cartPanel.getHeight());
        cartPanel.add(flashPanel, BorderLayout.CENTER);
        cartPanel.revalidate();
        
        // Remove the flash panel after a short delay
        Timer timer = new Timer(300, e -> {
            cartPanel.remove(flashPanel);
            cartPanel.revalidate();
            cartPanel.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void removeItem(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < currentSaleOrderItems.size()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Remove " + currentSaleOrderItems.get(rowIndex).getProductName() + " from cart?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                currentSaleOrderItems.remove(rowIndex);
                updateTableAndTotal();
            }
        }
    }

    private void updateTableAndTotal() {
        // Update table
        tableModel.setRowCount(0); 
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : currentSaleOrderItems) {
            BigDecimal lineTotal = item.getUnitPriceAtSale().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(lineTotal);
            
            tableModel.addRow(new Object[]{
                "SKU" + item.getProductId(), // Simplified SKU display
                    item.getProductName(),
                    item.getQuantity(),
                currencyFormat.format(item.getUnitPriceAtSale()),
                currencyFormat.format(lineTotal)
            });
        }
        
        // Update total display
        totalLabel.setText(currencyFormat.format(total));
        
        // Enable/disable payment buttons based on cart status
        boolean hasItems = !currentSaleOrderItems.isEmpty();
        payCashButton.setEnabled(hasItems);
        payCardButton.setEnabled(hasItems);
        
        if (!hasItems) {
            payCashButton.setBackground(new Color(200, 200, 200));
            payCardButton.setBackground(new Color(200, 200, 200));
        } else {
            payCashButton.setBackground(successColor);
            payCardButton.setBackground(primaryColor);
        }
    }

    private void findCustomer() {
         String customerSearch = JOptionPane.showInputDialog(this, "Enter Customer Name or Contact Info to search:");
         if (customerSearch == null || customerSearch.trim().isEmpty()){
              currentCustomer = null; 
            customerLabel.setText("Customer: Not Selected");
            customerLabel.setIcon(createTextIcon("O", new Font("Arial", Font.PLAIN, 16)));
              return;
         }

         try {
              List<Customer> customers = customerDAO.findCustomers(customerSearch.trim());
              if (customers == null || customers.isEmpty()) {
                  int addResult = JOptionPane.showConfirmDialog(this, "No customer found. Add new customer?", "Add Customer?", JOptionPane.YES_NO_OPTION);
                  if (addResult == JOptionPane.YES_OPTION){
                      addCustomer(customerSearch.trim()); 
                  } else {
                       currentCustomer = null;
                    customerLabel.setText("Customer: Not Selected");
                    customerLabel.setIcon(createTextIcon("O", new Font("Arial", Font.PLAIN, 16)));
                  }
              } else if (customers.size() == 1) {
                  currentCustomer = customers.get(0);
                  customerLabel.setText("Customer: " + currentCustomer.getName() + " (ID: " + currentCustomer.getCustomerId() + ")");
                customerLabel.setIcon(createTextIcon("O", new Font("Arial", Font.PLAIN, 16)));
              } else {
                  Customer selected = (Customer) JOptionPane.showInputDialog(
                          this,
                          "Multiple customers found. Please select one:",
                          "Select Customer",
                          JOptionPane.QUESTION_MESSAGE,
                          null, 
                          customers.toArray(), 
                          customers.get(0) 
                  );
                  currentCustomer = selected; 
                customerLabel.setText(currentCustomer != null ? "Customer: " + currentCustomer.getName() + " (ID: " + currentCustomer.getCustomerId() + ")" : "Customer: Not Selected");
                customerLabel.setIcon(createTextIcon("O", new Font("Arial", Font.PLAIN, 16)));
              }
         } catch (Exception ex) {
               JOptionPane.showMessageDialog(this, "Error searching for customer:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
               ex.printStackTrace();
         }
     }

    private void addCustomer(String initialName) {
          JTextField nameField = new JTextField(initialName);
          JTextField contactField = new JTextField();

          JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
          panel.add(new JLabel("Name:")); panel.add(nameField);
          panel.add(new JLabel("Contact Info (Phone/Email):")); panel.add(contactField);

          int result = JOptionPane.showConfirmDialog(this, panel, "Add New Customer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
          if (result == JOptionPane.OK_OPTION) {
               String name = nameField.getText().trim();
               String contact = contactField.getText().trim();
               if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Customer name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
               }
               Customer newCustomer = new Customer(name, contact, 0); 
               try {
                   Customer savedCustomer = customerDAO.saveCustomerAndRetrieve(newCustomer); 
                    if (savedCustomer != null) {
                        JOptionPane.showMessageDialog(this, "Customer added successfully.");
                        currentCustomer = savedCustomer; 
                        customerLabel.setText("Customer: " + currentCustomer.getName() + " (ID: " + currentCustomer.getCustomerId() + ")");
                    customerLabel.setIcon(createTextIcon("O", new Font("Arial", Font.PLAIN, 16)));
                    } else {
                         JOptionPane.showMessageDialog(this, "Failed to add customer (check logs).", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
               } catch (SQLException sqle) {
                   if (sqle.getMessage().contains("UNIQUE constraint failed")) { 
                        JOptionPane.showMessageDialog(this, "Failed to add customer. A customer with similar details might already exist.", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                   } else {
                        JOptionPane.showMessageDialog(this, "Error adding customer:\n" + sqle.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                   }
                   sqle.printStackTrace();
               } catch (Exception ex) { 
                   JOptionPane.showMessageDialog(this, "An unexpected error occurred while adding customer:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                   ex.printStackTrace();
               }
          }
     }
    
    private void processPayment(String type) {
        if (currentSaleOrderItems.isEmpty()){
            JOptionPane.showMessageDialog(this, "No items in the sale.", "Payment Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Shift currentShift = shiftDAO.findOpenShift();
        if (currentShift == null) {
              JOptionPane.showMessageDialog(this, "Cannot process sale: No shift is currently active.\nPlease start a shift first.", "Shift Error", JOptionPane.ERROR_MESSAGE);
              return;
        }
        int shiftId = currentShift.getShiftId();

        Integer customerId = (currentCustomer != null) ? currentCustomer.getCustomerId() : null;
        boolean paymentSuccess = false;
        BigDecimal total = calculateCurrentTotal();
        BigDecimal amountTendered = BigDecimal.ZERO; 
        
         if ("Cash".equals(type)) {
              String tenderedStr = JOptionPane.showInputDialog(this, String.format("Total Due: $%.2f\nEnter Amount Tendered:", total));
              if(tenderedStr == null) return; 
              try {
                  amountTendered = new BigDecimal(tenderedStr.trim());
                   if (amountTendered.compareTo(total) < 0) {
                       JOptionPane.showMessageDialog(this, "Insufficient amount tendered.", "Payment Error", JOptionPane.ERROR_MESSAGE);
                       return; 
                  }
                  
                  BigDecimal change = amountTendered.subtract(total);
                  JOptionPane.showMessageDialog(this, String.format("Change Due: $%.2f", change));
                  paymentSuccess = true; 

              } catch (NumberFormatException ex) {
                   JOptionPane.showMessageDialog(this, "Invalid amount entered. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                   return; 
              }
         } else if ("Card".equals(type)) {
             int result = JOptionPane.showConfirmDialog(this, String.format("Process Card Payment for $%.2f?\n(Simulating terminal interaction)", total), "Card Payment", JOptionPane.YES_NO_OPTION);
              if (result == JOptionPane.YES_OPTION) {
                  int approvedResult = JOptionPane.showConfirmDialog(this, "Payment Approved by Terminal?", "Card Payment Status", JOptionPane.YES_NO_OPTION);
                  paymentSuccess = (approvedResult == JOptionPane.YES_OPTION);
                  if (!paymentSuccess){
                      JOptionPane.showMessageDialog(this, "Card payment declined or cancelled by user.", "Payment Error", JOptionPane.WARNING_MESSAGE);
                  }
              }
              
              if (paymentSuccess) {
                   amountTendered = total;
              }
         }
        
        if (paymentSuccess) {
            try {
                boolean saved = orderDAO.saveOrder(
                    currentSaleOrderItems,
                    customerId,
                    currentUser.getUserId(),
                    shiftId, 
                    type,
                    amountTendered 
                );

                if (saved){
                    JOptionPane.showMessageDialog(this, "Sale Completed Successfully!");
                    clearSale(); 
                } else {
                     JOptionPane.showMessageDialog(this, "Failed to save the order!\nPossible insufficient stock or database error.\nPlease check application logs.", "Order Save Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException sqle) { 
                 JOptionPane.showMessageDialog(this, "Database Error saving order:\n" + sqle.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
                 sqle.printStackTrace();
            } catch (Exception ex) { 
                 JOptionPane.showMessageDialog(this, "Critical Error saving order:\n" + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
            }
        }
    }

    private BigDecimal calculateCurrentTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : currentSaleOrderItems) {
           total = total.add(item.getLineTotal());
       }
       return total;
    }

    private void clearSale() {
        currentSaleOrderItems.clear();
        tableModel.setRowCount(0);
        totalLabel.setText("$0.00");
        customerLabel.setText("Customer: Not Selected");
        customerLabel.setIcon(createTextIcon("O", new Font("Arial", Font.PLAIN, 16)));
        currentCustomer = null; 
        skuInput.requestFocus(); 
    }
    
    private void handleReturnAction() {
         Shift currentShift = shiftDAO.findOpenShift();
         if (currentShift == null) {
              JOptionPane.showMessageDialog(this, "Cannot process return: No shift is currently active.\nPlease start a shift first.", "Shift Error", JOptionPane.ERROR_MESSAGE);
              return;
         }
         int shiftId = currentShift.getShiftId();
        
        int originalOrderItemId = -1;
        while (originalOrderItemId <= 0) {
            String orderItemIdStr = JOptionPane.showInputDialog(this, "Enter original ORDER ITEM ID for return (found on receipt or order history):");
            if (orderItemIdStr == null) return; 
            try {
                originalOrderItemId = Integer.parseInt(orderItemIdStr.trim());
                if (originalOrderItemId <= 0) {
                    JOptionPane.showMessageDialog(this, "Order Item ID must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    originalOrderItemId = -1; 
                }
            } catch (NumberFormatException ex) {
                 JOptionPane.showMessageDialog(this, "Invalid Order Item ID. Please enter a whole number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        int quantityToReturn = -1;
         while (quantityToReturn <= 0) {
             String qtyStr = JOptionPane.showInputDialog(this, "Enter Quantity to return for Item ID " + originalOrderItemId + ":", "1"); 
             if (qtyStr == null) return; 
              try {
                 quantityToReturn = Integer.parseInt(qtyStr.trim());
                 if (quantityToReturn <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    quantityToReturn = -1; 
                 }
             } catch (NumberFormatException ex) {
                  JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a whole number.", "Input Error", JOptionPane.WARNING_MESSAGE);
             }
         }

         String reason = JOptionPane.showInputDialog(this, "Enter Reason for return (optional):");
         reason = (reason == null) ? "" : reason.trim(); 

         int restockResult = JOptionPane.showConfirmDialog(this, "Should the returned item(s) be restocked into inventory?", "Restock Item?", JOptionPane.YES_NO_OPTION);
         boolean restockFlag = (restockResult == JOptionPane.YES_OPTION);
         
         try {
             boolean success = orderDAO.processReturn(
                     originalOrderItemId,
                     quantityToReturn,
                     restockFlag,
                     reason,
                     currentUser.getUserId(), 
                     shiftId                
             );

             if (success) {
                 JOptionPane.showMessageDialog(this, "Return processed successfully.");
             }
         } catch (SQLException | IllegalArgumentException ex) { 
              JOptionPane.showMessageDialog(this, "Failed to process return:\n" + ex.getMessage(), "Return Error", JOptionPane.ERROR_MESSAGE);
              ex.printStackTrace(); 
         } catch (Exception ex) { 
             JOptionPane.showMessageDialog(this, "An unexpected error occurred during return processing:\n" + ex.getMessage(), "Critical Error", JOptionPane.ERROR_MESSAGE);
             ex.printStackTrace();
         }
    }
} 