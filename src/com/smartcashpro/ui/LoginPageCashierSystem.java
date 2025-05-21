package com.smartcashpro.ui; // Changed package to fit existing structure

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
// Remove java.sql.* imports as DAO will handle it
// import java.sql.*;

// Import necessary classes from your existing project
import com.smartcashpro.db.UserDAO;
import com.smartcashpro.model.User;
import com.smartcashpro.ui.MainFrame; // Assuming MainFrame is in com.smartcashpro
                                    // Or adjust if it's in com.smartcashpro.ui.MainFrame

public class LoginPageCashierSystem extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JCheckBox showPasswordCheck;
    private JButton loginButton, clearButton, exitButton; // Removed social, forgot, register for now
    // private JComboBox<String> roleComboBox; // We'll get role from UserDAO
    private int loginAttempts = 0;

    private UserDAO userDAO; // DAO for authentication

    public LoginPageCashierSystem() {
        this.userDAO = new UserDAO(); // Initialize your DAO

        setTitle("SmartCash Pro - Login"); // Updated title
        setSize(600, 400); // Adjusted size slightly
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Or WindowConstants.EXIT_ON_CLOSE
        setLocationRelativeTo(null);
        setResizable(false);

        // Background Image - Consider using ClassLoader for portability
        // For now, assuming path is accessible or remove if problematic
        // JLabel backgroundLabel = new JLabel(new ImageIcon("C:\\Users\\ICT\\Desktop\\lgin.jpg\\"));
        // setContentPane(backgroundLabel);
        // backgroundLabel.setLayout(new BorderLayout());
        // If no background image, use a standard JPanel
        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);


        JLabel titleLabel = new JLabel("Welcome to SmartCash Pro", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 24)); // Adjusted font size
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(51, 102, 204));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0)); // Padding
        contentPane.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout()); // Using GridBagLayout for more control
        // centerPanel.setOpaque(false); // If using background image
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        Font customFont = new Font("Verdana", Font.PLAIN, 14); // Adjusted font size

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Username:")).setFont(customFont);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(30);
        usernameField.setFont(new Font("Verdana", Font.PLAIN, 11));
        // Set preferred, minimum, and maximum size for username field (extremely compact)
        Dimension ultraCompactFieldSize = new Dimension(140, 22);
        usernameField.setPreferredSize(ultraCompactFieldSize);
        usernameField.setMinimumSize(ultraCompactFieldSize);
        usernameField.setMaximumSize(new Dimension(140, 22));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            usernameField.getBorder(),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        centerPanel.add(usernameField);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Password:")).setFont(customFont);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(30);
        passwordField.setFont(new Font("Verdana", Font.PLAIN, 11));
        passwordField.setPreferredSize(ultraCompactFieldSize);
        passwordField.setMinimumSize(ultraCompactFieldSize);
        passwordField.setMaximumSize(new Dimension(140, 22));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            passwordField.getBorder(),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        centerPanel.add(passwordField);

        // Role ComboBox removed - role will be determined by UserDAO
        // gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        // centerPanel.add(new JLabel("Role:")).setFont(customFont);
        // gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        // roleComboBox = new JComboBox<>(new String[]{"Admin", "Cashier"}); // Reflect your actual roles
        // roleComboBox.setFont(customFont);
        // centerPanel.add(roleComboBox);

        gbc.gridx = 1; gbc.gridy = 2; // Adjusted gridy due to roleComboBox removal
        showPasswordCheck = new JCheckBox("Show Password");
        // showPasswordCheck.setOpaque(false); // If using background
        showPasswordCheck.setForeground(Color.blue);
        showPasswordCheck.setFont(customFont);
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '•');
        });
        centerPanel.add(showPasswordCheck);

        // Add 'Forgot Password?' button below password field and show password checkbox
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        JButton forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setFont(new Font("Verdana", Font.PLAIN, 12));
        forgotPasswordButton.setForeground(Color.BLUE);
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setFocusPainted(false);
        forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Forgot Password functionality is not implemented yet.", "Forgot Password", JOptionPane.INFORMATION_MESSAGE);
        });
        centerPanel.add(forgotPasswordButton, gbc);

        messageLabel = new JLabel(" ", SwingConstants.CENTER); // Space for non-error state
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("Verdana", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(messageLabel, gbc);


        contentPane.add(centerPanel, BorderLayout.CENTER);
        // contentPane.add(messageLabel, BorderLayout.SOUTH); // Message label moved into center panel

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        // buttonPanel.setOpaque(false); // If using background

        loginButton = createStyledButton("Login", new Color(0, 120, 215));
        loginButton.addActionListener(this); // 'this' implements ActionListener

        clearButton = createStyledButton("Clear", new Color(204, 102, 0));
        clearButton.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            messageLabel.setText(" ");
        });

        exitButton = createStyledButton("Exit", Color.RED);
        exitButton.addActionListener(e -> {
            com.smartcashpro.db.DatabaseConnector.closeConnection(); // Close DB before exit
            System.exit(0);
        });

        // --- Temporarily disable extra buttons or implement later ---
        // forgotButton = createStyledButton("Forgot Password", new Color(102, 153, 255));
        // forgotButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Forgot Password - Not Implemented"));
        // registerButton = createStyledButton("Register", new Color(0, 120, 215));
        // registerButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Register - Not Implemented"));
        // googleButton = createStyledButton("Login vis Gmail", new Color(66, 133, 244));
        // googleButton.setEnabled(false);
        // facebookButton = createStyledButton("Login via Facebook", new Color(59, 89, 152));
        // facebookButton.setEnabled(false);


        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);
        // buttonPanel.add(forgotButton);
        // buttonPanel.add(registerButton);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        // Add KeyListener for Enter key on password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
             @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocusInWindow(); // Move focus to password
                }
            }
        });


        // setVisible(true); // Main application will make it visible
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Verdana", Font.BOLD, 12)); // Adjusted font size
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15) // Padding
        ));
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) { // This is for the loginButton
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        // String role = (String) roleComboBox.getSelectedItem(); // Role not used for UserDAO auth

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and Password are required.");
            return;
        }

        // Use your existing UserDAO
        User authenticatedUser = userDAO.authenticate(username, password);

        if (authenticatedUser != null) {
            JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + authenticatedUser.getUsername() +
                                                " (" + authenticatedUser.getRole() + ")",
                                          "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close the login window

            // --- Launch your existing MainFrame ---
            // Ensure MainFrame constructor can accept a User object or has a method to set it
            SwingUtilities.invokeLater(() -> {
                MainFrame mainAppFrame = new MainFrame(authenticatedUser); // Pass the user
                mainAppFrame.setVisible(true);
            });

        } else {
            loginAttempts++;
            if (loginAttempts >= 3) {
                JOptionPane.showMessageDialog(this, "Too many failed login attempts. Exiting application.", "Login Error", JOptionPane.ERROR_MESSAGE);
                com.smartcashpro.db.DatabaseConnector.closeConnection(); // Close DB before exit
                System.exit(0);
            } else {
                messageLabel.setText("Invalid username or password. Attempts: " + loginAttempts + "/3");
            }
        }
    }

    // main method is removed from here, it will be in SmartCashProApp.java
    // public static void main(String[] args) { ... }
}

// RegistrationWindow and ForgotPasswordWindow classes should be separate files
// and refactored to use your UserDAO and MySQL database if you implement them.
// For now, they are commented out or removed from immediate integration.