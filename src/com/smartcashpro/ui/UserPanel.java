package com.smartcashpro.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*; 
import java.util.List;
import com.smartcashpro.model.User;
import com.smartcashpro.db.UserDAO;
import java.sql.SQLException; 

public class UserPanel extends JPanel {
    private User currentUser; 
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private UserDAO userDAO;
    
    // UI components
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton refreshButton;
    private JTextField searchField;
    
    // Colors
    private Color primaryColor = new Color(41, 128, 185);
    private Color secondaryColor = new Color(52, 152, 219);
    private Color accentColor = new Color(46, 204, 113);
    private Color warningColor = new Color(230, 126, 34);

    public UserPanel(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO(); 

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create center panel with user table
        JPanel centerPanel = createTablePanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Load initial data
        loadUserData();
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
        
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel subtitleLabel = new JLabel("Add, edit and manage user accounts");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);
        
        // Search field
        searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search users...");
        
        // Create buttons
        addUserButton = createStyledButton("Add New User", accentColor, "O+");
        editUserButton = createStyledButton("Edit User", primaryColor, "E");
        refreshButton = createStyledButton("Refresh", secondaryColor, "R");
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.add(searchField);
        buttonsPanel.add(addUserButton);
        buttonsPanel.add(editUserButton);
        buttonsPanel.add(refreshButton);
        
        // Add action listeners
        addUserButton.addActionListener(e -> displayAddUserDialog());
        editUserButton.addActionListener(e -> displayEditUserDialog());
        refreshButton.addActionListener(e -> loadUserData());
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Create the table model
        String[] columnNames = {"ID", "Username", "Role", "Status"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3) return Boolean.class;
                return Object.class;
            }
        };
        
        // Create and configure the table
        userTable = new JTable(userTableModel);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        userTable.setRowHeight(30);
        userTable.setShowGrid(false);
        userTable.setIntercellSpacing(new Dimension(0, 0));
        userTable.setFillsViewportHeight(true);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        userTable.getColumnModel().getColumn(0).setMaxWidth(60);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(3).setMaxWidth(80);
        
        // Add header styling
        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(240, 240, 240));
        header.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Add custom renderer for boolean values
        userTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Add double-click listener
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    displayEditUserDialog();
                }
            }
        });
        
        // Add selection listener to enable/disable edit button
        userTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = userTable.getSelectedRow() != -1;
            editUserButton.setEnabled(hasSelection);
            if (!hasSelection) {
                editUserButton.setBackground(new Color(180, 180, 180));
            } else {
                editUserButton.setBackground(primaryColor);
            }
        });
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add help text
        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        helpPanel.setBackground(Color.WHITE);
        JLabel helpLabel = new JLabel("Double-click a user to edit");
        helpLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        helpLabel.setForeground(Color.GRAY);
        helpPanel.add(helpLabel);
        
        tablePanel.add(helpPanel, BorderLayout.SOUTH);
        
        // Initial state for edit button
        editUserButton.setEnabled(false);
        editUserButton.setBackground(new Color(180, 180, 180));
        
        return tablePanel;
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
    
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Use default renderer as a base
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            
            // Format based on the active status
            boolean isActive = (Boolean) value;
            
            if (isActive) {
                label.setText("Active");
                label.setForeground(new Color(46, 204, 113));
            } else {
                label.setText("Inactive");
                label.setForeground(new Color(231, 76, 60));
            }
            
            label.setHorizontalAlignment(JLabel.CENTER);
            
            return label;
        }
    }

     private void loadUserData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
         try {
            userTableModel.setRowCount(0);
            List<User> users = userDAO.getAllUsers();
            
             if (users == null) { 
                JOptionPane.showMessageDialog(this, 
                    "Error loading users: Could not connect to database.", 
                    "Load Error", 
                    JOptionPane.ERROR_MESSAGE);
                 return;
             }
            
            for (User u : users) {
                userTableModel.addRow(new Object[]{
                    u.getUserId(), 
                    u.getUsername(), 
                    u.getRole(), 
                    u.isActive()
                });
            }
            
            if (users.isEmpty()) {
                // Show a message if no users found
                JOptionPane.showMessageDialog(this, 
                    "No users found in the database.", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
         } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "An unexpected error occurred while loading users:\n" + ex.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
              ex.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
         }
    }
    
    private void displayAddUserDialog() {
        // Create a styled dialog
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
         JTextField usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
         JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Role combo
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));
         JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Cashier", "Manager"}); 
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Add components to form
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(roleCombo, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(220, 220, 220));
        cancelButton.setForeground(Color.BLACK);
        
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setBackground(accentColor);
        saveButton.setForeground(Color.WHITE);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Add action listeners
        cancelButton.addActionListener(e -> dialog.dispose());
        
        saveButton.addActionListener(e -> {
              String username = usernameField.getText().trim();
              String password = new String(passwordField.getPassword());
              String role = (String) roleCombo.getSelectedItem();
              
              if (username.isEmpty() || password.isEmpty()){
                JOptionPane.showMessageDialog(dialog, 
                    "Username and password cannot be empty.", 
                    "Input Error", 
                    JOptionPane.WARNING_MESSAGE);
                  return;
              }
            
              if (username.length() > 50){ 
                JOptionPane.showMessageDialog(dialog, 
                    "Username cannot be longer than 50 characters.", 
                    "Input Error", 
                    JOptionPane.WARNING_MESSAGE);
                  return;
              }
              
              User newUser = new User(0, username, role, true); 

              try {
                  boolean success = userDAO.saveUser(newUser, password);

                 if (success) {
                    JOptionPane.showMessageDialog(dialog, 
                        "User added successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                      loadUserData(); 
                    dialog.dispose();
                  } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Failed to add user (Username might already exist).", 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                  }
              } catch (Exception ex) { 
                JOptionPane.showMessageDialog(dialog, 
                    "Error saving user:\n" + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                  ex.printStackTrace();
              }
        });
        
        // Add components to dialog
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        
        // Add title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(primaryColor);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Add New User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        dialog.add(titlePanel, BorderLayout.NORTH);
        
        dialog.setVisible(true);
    }
     
     private void displayEditUserDialog() {
          int selectedRow = userTable.getSelectedRow();
         if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a user to edit.", 
                "Selection Required", 
                JOptionPane.WARNING_MESSAGE);
             return;
         }
          
          int userId = (Integer) userTableModel.getValueAt(selectedRow, 0);
          String username = (String) userTableModel.getValueAt(selectedRow, 1);
          String currentRole = (String) userTableModel.getValueAt(selectedRow, 2);
          boolean isActive = (Boolean) userTableModel.getValueAt(selectedRow, 3);

        // Create a styled dialog
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit User", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField usernameField = new JTextField(username, 15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
           usernameField.setEditable(false); 
        usernameField.setBackground(new Color(240, 240, 240));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Password field
        JLabel passwordLabel = new JLabel("New Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
          JPasswordField passwordField = new JPasswordField(15); 
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JLabel passwordHintLabel = new JLabel("Leave blank to keep current password");
        passwordHintLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        passwordHintLabel.setForeground(Color.GRAY);
        
        // Role combo
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));
          JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Cashier", "Manager"});
           roleCombo.setSelectedItem(currentRole);
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Status checkbox
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JCheckBox activeCheck = new JCheckBox("Active", isActive);
        activeCheck.setFont(new Font("Arial", Font.PLAIN, 14));
        activeCheck.setBackground(Color.WHITE);
        
        // Add components to form
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(passwordField, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(passwordHintLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(roleCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        formPanel.add(statusLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(activeCheck, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(220, 220, 220));
        cancelButton.setForeground(Color.BLACK);
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setBackground(primaryColor);
        saveButton.setForeground(Color.WHITE);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        // Add action listeners
        cancelButton.addActionListener(e -> dialog.dispose());
        
        saveButton.addActionListener(e -> {
              String password = new String(passwordField.getPassword());
              String role = (String) roleCombo.getSelectedItem();
              boolean makeActive = activeCheck.isSelected();
              
              User updatedUser = new User(userId, username, role, makeActive);

              try {
                  boolean success = userDAO.saveUser(updatedUser, password.isEmpty() ? null : password);

                  if (success) {
                    JOptionPane.showMessageDialog(dialog, 
                        "User updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                      loadUserData(); 
                    dialog.dispose();
                  } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Failed to update user.", 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                  }
              } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error updating user:\n" + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                  ex.printStackTrace();
              }
        });
        
        // Add components to dialog
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        
        // Add title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(primaryColor);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Edit User (ID: " + userId + ")");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        dialog.add(titlePanel, BorderLayout.NORTH);
        
        dialog.setVisible(true);
     }
}