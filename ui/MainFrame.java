package com.smartcashpro.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import com.smartcashpro.model.User;
import com.smartcashpro.db.DatabaseConnector;
import com.smartcashpro.SmartCashProApp;

public class MainFrame extends JFrame {

    private User currentUser;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private JLabel statusLabel;
    private Color accentColor = new Color(41, 128, 185);
    private Color sidebarColor = new Color(52, 73, 94);
    private Color selectedButtonColor = new Color(44, 62, 80);

    // Panel Names (Constants remain the same)
    private static final String POS_PANEL = "POSPanel";
    private static final String INVENTORY_PANEL = "InventoryPanel";
    private static final String SHIFT_PANEL = "ShiftPanel";
    private static final String USER_PANEL = "UserPanel";
    private static final String ORDER_HISTORY_PANEL = "OrderHistoryPanel";
    
    // Sidebar buttons
    private JButton posButton, shiftButton, inventoryButton, userButton, orderHistoryButton;

    public MainFrame(User authenticatedUser) {
        super("SmartCash Pro");
        this.currentUser = authenticatedUser;

        if (this.currentUser == null) {
            System.err.println("MainFrame received null user. Exiting.");
            JOptionPane.showMessageDialog(null, "Login failed unexpectedly. Application will exit.", "Critical Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        initializeUI();
    }

    private void initializeUI() {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create main container with border layout
        JPanel mainContainer = new JPanel(new BorderLayout());
        
        // Create sidebar
        sidebarPanel = createSidebar();
        mainContainer.add(sidebarPanel, BorderLayout.WEST);
        
        // Create header
        JPanel headerPanel = createHeader();
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        
        // Create content panel with card layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create main content panels
        POSPanel posPanel = new POSPanel(currentUser);
        ShiftPanel shiftPanel = new ShiftPanel(currentUser);
        OrderHistoryPanel orderHistoryPanel = new OrderHistoryPanel(currentUser);
        
        cardPanel.add(posPanel, POS_PANEL);
        cardPanel.add(shiftPanel, SHIFT_PANEL);
        cardPanel.add(orderHistoryPanel, ORDER_HISTORY_PANEL);

        // Conditionally add panels based on user role
        if (isManagerOrAdmin()) {
             InventoryPanel inventoryPanel = new InventoryPanel(currentUser);
             UserPanel userPanel = new UserPanel(currentUser);
             cardPanel.add(inventoryPanel, INVENTORY_PANEL);
             cardPanel.add(userPanel, USER_PANEL);
             
             // Enable manager-only buttons
             inventoryButton.setEnabled(true);
             userButton.setEnabled(true);
        } else {
             // Disable manager-only buttons for regular users
             inventoryButton.setEnabled(false);
             userButton.setEnabled(false);
        }
        
        mainContainer.add(cardPanel, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        mainContainer.add(statusBar, BorderLayout.SOUTH);
        
        setContentPane(mainContainer);
        
        // Show the default panel (POS)
        cardLayout.show(cardPanel, POS_PANEL);
        updateActiveButton(posButton);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Logo/app name
        JLabel logo = new JLabel("SmartCash Pro");
        logo.setFont(new Font("Arial", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        sidebar.add(logo);
        
        // Navigation buttons
        posButton = createSidebarButton("$ POS", e -> {
            cardLayout.show(cardPanel, POS_PANEL);
            updateActiveButton(posButton);
        });
        sidebar.add(posButton);
        
        shiftButton = createSidebarButton("T Shifts", e -> {
            cardLayout.show(cardPanel, SHIFT_PANEL);
            updateActiveButton(shiftButton);
        });
        sidebar.add(shiftButton);
        
        orderHistoryButton = createSidebarButton("O Order History", e -> {
            cardLayout.show(cardPanel, ORDER_HISTORY_PANEL);
            updateActiveButton(orderHistoryButton);
        });
        sidebar.add(orderHistoryButton);
        
        if (isManagerOrAdmin()) {
            sidebar.add(Box.createVerticalStrut(20));
            
            JLabel adminSection = new JLabel("MANAGEMENT");
            adminSection.setFont(new Font("Arial", Font.BOLD, 12));
            adminSection.setForeground(new Color(149, 165, 166));
            adminSection.setAlignmentX(Component.CENTER_ALIGNMENT);
            adminSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            sidebar.add(adminSection);
        }
        
        inventoryButton = createSidebarButton("# Inventory", e -> {
            cardLayout.show(cardPanel, INVENTORY_PANEL);
            updateActiveButton(inventoryButton);
        });
        sidebar.add(inventoryButton);
        
        userButton = createSidebarButton("U Users", e -> {
            cardLayout.show(cardPanel, USER_PANEL);
            updateActiveButton(userButton);
        });
        sidebar.add(userButton);
        
        // Logout button at bottom
        sidebar.add(Box.createVerticalGlue());
        
        JButton logoutButton = createSidebarButton("X Logout", e -> logout());
        sidebar.add(logoutButton);
        
        return sidebar;
    }
    
    private JButton createSidebarButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(sidebarColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(200, 40));
        button.setPreferredSize(new Dimension(200, 40));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 8));
        button.addActionListener(action);
        
        return button;
    }
    
    private void updateActiveButton(JButton activeButton) {
        // Reset all buttons
        posButton.setBackground(sidebarColor);
        shiftButton.setBackground(sidebarColor);
        orderHistoryButton.setBackground(sidebarColor);
        inventoryButton.setBackground(sidebarColor);
        userButton.setBackground(sidebarColor);
        
        // Highlight active button
        activeButton.setBackground(selectedButtonColor);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // User info section
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfo.setOpaque(false);
        
        JLabel avatarLabel = new JLabel("O");
        avatarLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        avatarLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        JLabel nameLabel = new JLabel(currentUser.getUsername());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel roleLabel = new JLabel(" (" + currentUser.getRole() + ")");
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(100, 100, 100));
        
        userInfo.add(avatarLabel);
        userInfo.add(nameLabel);
        userInfo.add(roleLabel);
        
        header.add(userInfo, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(245, 245, 245));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        return statusBar;
    }

    private boolean isManagerOrAdmin() {
        return currentUser != null && ("Manager".equalsIgnoreCase(currentUser.getRole()) || "Admin".equalsIgnoreCase(currentUser.getRole()));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            statusLabel.setText("Logging out...");
            this.dispose();
            DatabaseConnector.closeConnection();
            SmartCashProApp.main(null);
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit SmartCash Pro?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            statusLabel.setText("Shutting down...");
            DatabaseConnector.closeConnection();
            System.exit(0);
        }
    }
}