package com.smartcashpro.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.sql.SQLException;

import com.smartcashpro.model.User;
import com.smartcashpro.model.Order;
import com.smartcashpro.model.OrderItem;
import com.smartcashpro.db.OrderDAO;
import com.smartcashpro.db.CustomerDAO;

public class OrderHistoryPanel extends JPanel {
    
    private User currentUser;
    private OrderDAO orderDAO;
    private CustomerDAO customerDAO;
    
    // UI Components
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JButton refreshButton;
    private JButton viewDetailsButton;
    private JButton printReceiptButton;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JComboBox<String> paymentTypeCombo;
    private JTextField customerSearchField;
    
    // Colors
    private Color primaryColor = new Color(41, 128, 185);
    private Color secondaryColor = new Color(52, 152, 219);
    private Color accentColor = new Color(46, 204, 113);
    
    // Date formatter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    // Currency formatter
    private final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    public OrderHistoryPanel(User user) {
        this.currentUser = user;
        this.orderDAO = new OrderDAO();
        this.customerDAO = new CustomerDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create search panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.WEST);
        
        // Create orders table panel
        JPanel ordersPanel = createOrdersPanel();
        add(ordersPanel, BorderLayout.CENTER);
        
        // Load initial data
        loadOrders();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
        
        // Title and description
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Order History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel subtitleLabel = new JLabel("View and search past orders");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);
        
        refreshButton = createStyledButton("Refresh", secondaryColor, "R");
        viewDetailsButton = createStyledButton("View Details", primaryColor, "V");
        printReceiptButton = createStyledButton("Print Receipt", accentColor, "P");
        
        // Disable buttons initially
        viewDetailsButton.setEnabled(false);
        printReceiptButton.setEnabled(false);
        
        actionPanel.add(refreshButton);
        actionPanel.add(viewDetailsButton);
        actionPanel.add(printReceiptButton);
        
        // Add action listeners
        refreshButton.addActionListener(e -> loadOrders());
        viewDetailsButton.addActionListener(e -> viewOrderDetails());
        printReceiptButton.addActionListener(e -> printReceipt());
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 0, 10, 15)
        ));
        searchPanel.setPreferredSize(new Dimension(250, getHeight()));
        
        // Search title
        JLabel searchTitle = new JLabel("Search Orders");
        searchTitle.setFont(new Font("Arial", Font.BOLD, 16));
        searchTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Date range panel
        JPanel dateRangePanel = new JPanel(new GridLayout(4, 1, 0, 5));
        dateRangePanel.setBackground(Color.WHITE);
        dateRangePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        dateRangePanel.setMaximumSize(new Dimension(250, 150));
        dateRangePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel dateRangeLabel = new JLabel("Date Range:");
        dateRangeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateChooser = new JDateChooser();
        startDateChooser.setDate(getDefaultStartDate());
        
        JLabel endDateLabel = new JLabel("End Date:");
        endDateChooser = new JDateChooser();
        endDateChooser.setDate(new Date());
        
        dateRangePanel.add(dateRangeLabel);
        dateRangePanel.add(startDateLabel);
        dateRangePanel.add(startDateChooser);
        dateRangePanel.add(endDateLabel);
        dateRangePanel.add(endDateChooser);
        
        // Payment type panel
        JPanel paymentTypePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        paymentTypePanel.setBackground(Color.WHITE);
        paymentTypePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        paymentTypePanel.setMaximumSize(new Dimension(250, 80));
        paymentTypePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel paymentTypeLabel = new JLabel("Payment Type:");
        paymentTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        String[] paymentTypes = {"All", "Cash", "Card"};
        paymentTypeCombo = new JComboBox<>(paymentTypes);
        paymentTypeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        
        paymentTypePanel.add(paymentTypeLabel);
        paymentTypePanel.add(paymentTypeCombo);
        
        // Customer search panel
        JPanel customerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        customerPanel.setBackground(Color.WHITE);
        customerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        customerPanel.setMaximumSize(new Dimension(250, 80));
        customerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel customerLabel = new JLabel("Customer:");
        customerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        customerSearchField = new JTextField();
        customerSearchField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        customerPanel.add(customerLabel);
        customerPanel.add(customerSearchField);
        
        // Search button
        JButton searchButton = new JButton("Search Orders");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(primaryColor);
        searchButton.setForeground(Color.BLACK);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButton.addActionListener(e -> searchOrders());
        
        // Reset button
        JButton resetButton = new JButton("Reset Filters");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 14));
        resetButton.setBackground(new Color(220, 220, 220));
        resetButton.setForeground(Color.BLACK);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        resetButton.addActionListener(e -> resetFilters());
        
        // Add components to search panel
        searchPanel.add(searchTitle);
        searchPanel.add(dateRangePanel);
        searchPanel.add(paymentTypePanel);
        searchPanel.add(customerPanel);
        searchPanel.add(Box.createVerticalStrut(10));
        searchPanel.add(searchButton);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(resetButton);
        searchPanel.add(Box.createVerticalGlue());
        
        return searchPanel;
    }
    
    private JPanel createOrdersPanel() {
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBackground(Color.WHITE);
        ordersPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 0));
        
        // Create the table model
        String[] columnNames = {"Order ID", "Date", "Customer", "Cashier", "Total", "Payment Type", "Status"};
        ordersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create and configure the table
        ordersTable = new JTable(ordersTableModel);
        ordersTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ordersTable.setRowHeight(30);
        ordersTable.setShowGrid(false);
        ordersTable.setIntercellSpacing(new Dimension(0, 0));
        ordersTable.setFillsViewportHeight(true);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // Order ID
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(140); // Date
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Customer
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Cashier
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Total
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Payment Type
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status
        
        // Add header styling
        JTableHeader header = ordersTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(240, 240, 240));
        header.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Add selection listener
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = ordersTable.getSelectedRow() != -1;
            viewDetailsButton.setEnabled(hasSelection);
            printReceiptButton.setEnabled(hasSelection);
        });
        
        // Add double-click listener
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewOrderDetails();
                }
            }
        });
        
        ordersPanel.add(scrollPane, BorderLayout.CENTER);
        
        return ordersPanel;
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
            button.setIconTextGap(8);
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
    
    private Date getDefaultStartDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Default to 1 month ago
        return cal.getTime();
    }
    
    private void loadOrders() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ordersTableModel.setRowCount(0);
        
        try {
            List<Order> orders = orderDAO.getAllOrders();
            displayOrders(orders);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading orders: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void searchOrders() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ordersTableModel.setRowCount(0);
        
        try {
            // Get selected dates
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();
            
            // Get payment type
            String paymentType = null;
            if (paymentTypeCombo.getSelectedIndex() > 0) {
                paymentType = (String) paymentTypeCombo.getSelectedItem();
            }
            
            // Get customer info (placeholder for now, in a real app you'd search by name)
            Integer customerId = null;
            String customerText = customerSearchField.getText().trim();
            if (!customerText.isEmpty()) {
                // For simplicity, we'll assume customer search is handled separately
                // and just display a message for now
                JOptionPane.showMessageDialog(this,
                    "Customer search not implemented in this demo",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            List<Order> orders = orderDAO.searchOrders(startDate, endDate, customerId, paymentType);
            displayOrders(orders);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error searching orders: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void displayOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No orders found matching your criteria.",
                "No Results",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        for (Order order : orders) {
            ordersTableModel.addRow(new Object[]{
                order.getOrderId(),
                dateFormat.format(order.getOrderDate()),
                order.getCustomerName() != null ? order.getCustomerName() : "Guest",
                order.getUserName(),
                currencyFormat.format(order.getTotalAmount()),
                order.getPaymentType(),
                order.getOrderStatus()
            });
        }
    }
    
    private void resetFilters() {
        startDateChooser.setDate(getDefaultStartDate());
        endDateChooser.setDate(new Date());
        paymentTypeCombo.setSelectedIndex(0);
        customerSearchField.setText("");
        
        // Reload all orders
        loadOrders();
    }
    
    private void viewOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to view details.",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        
        try {
            Order order = orderDAO.getOrderDetails(orderId);
            if (order != null) {
                displayOrderDetailsDialog(order);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Order details could not be loaded. The order may have been deleted.",
                    "Order Not Found",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading order details: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void displayOrderDetailsDialog(Order order) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Order Details", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Header with order info
        JPanel headerPanel = new JPanel(new GridLayout(5, 2, 10, 5));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));
        
        headerPanel.add(new JLabel("Order ID:"));
        headerPanel.add(new JLabel("#" + order.getOrderId()));
        
        headerPanel.add(new JLabel("Date:"));
        headerPanel.add(new JLabel(dateFormat.format(order.getOrderDate())));
        
        headerPanel.add(new JLabel("Customer:"));
        headerPanel.add(new JLabel(order.getCustomerName() != null ? order.getCustomerName() : "Guest"));
        
        headerPanel.add(new JLabel("Cashier:"));
        headerPanel.add(new JLabel(order.getUserName()));
        
        headerPanel.add(new JLabel("Payment:"));
        headerPanel.add(new JLabel(order.getPaymentType()));
        
        // Items table
        String[] columnNames = {"Item", "Quantity", "Unit Price", "Total"};
        DefaultTableModel itemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable itemsTable = new JTable(itemsTableModel);
        itemsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        itemsTable.setRowHeight(25);
        itemsTable.setFillsViewportHeight(true);
        
        // Add order items to table
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                BigDecimal lineTotal = item.getUnitPriceAtSale().multiply(new BigDecimal(item.getQuantity()));
                
                itemsTableModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    currencyFormat.format(item.getUnitPriceAtSale()),
                    currencyFormat.format(lineTotal)
                });
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(Color.WHITE);
        
        JLabel totalLabel = new JLabel("TOTAL: " + currencyFormat.format(order.getTotalAmount()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalPanel.add(totalLabel);
        
        // Add components to the dialog
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(totalPanel, BorderLayout.SOUTH);
        
        // Add title bar
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(primaryColor);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Order Details - #" + order.getOrderId());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        dialog.add(titlePanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Add button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.addActionListener(e -> dialog.dispose());
        
        JButton printButton = new JButton("Print Receipt");
        printButton.setFont(new Font("Arial", Font.BOLD, 14));
        printButton.setBackground(primaryColor);
        printButton.setForeground(Color.WHITE);
        printButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, 
                "Print functionality would be implemented here",
                "Print Receipt",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void printReceipt() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        
        // In a real application, this would connect to a printer
        // For this demo, we'll just show a message
        JOptionPane.showMessageDialog(this,
            "Print functionality would be implemented here\nOrder ID: " + orderId,
            "Print Receipt",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // JDateChooser - Simplified implementation for the demo
    // In a real application, use a third-party date chooser like JCalendar
    private class JDateChooser extends JPanel {
        private JSpinner dateSpinner;
        private Date date;
        
        public JDateChooser() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            
            SpinnerDateModel model = new SpinnerDateModel();
            dateSpinner = new JSpinner(model);
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            dateSpinner.addChangeListener(e -> date = (Date) dateSpinner.getValue());
            
            add(dateSpinner, BorderLayout.CENTER);
        }
        
        public Date getDate() {
            return (Date) dateSpinner.getValue();
        }
        
        public void setDate(Date date) {
            this.date = date;
            dateSpinner.setValue(date);
        }
    }
} 