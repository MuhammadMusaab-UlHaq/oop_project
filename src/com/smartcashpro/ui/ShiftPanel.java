package com.smartcashpro.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import com.smartcashpro.model.User;
import com.smartcashpro.model.Shift; 
import com.smartcashpro.db.ShiftDAO;

import java.math.BigDecimal; 
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; 

public class ShiftPanel extends JPanel {
    
    private User currentUser;
    private JLabel statusLabel;
    private JLabel shiftTimerLabel;
    private JButton startShiftButton;
    private JButton endShiftButton;
    private JTextArea reconciliationArea;
    private ShiftDAO shiftDAO;
    private Shift activeShift = null; 
    private Timer shiftTimer;
    private JProgressBar animatedBar;
    private JLabel shiftStatusIcon;
    private Color activeColor = new Color(76, 175, 80);
    private Color inactiveColor = new Color(255, 87, 34);
    private JPanel cashierInfoPanel;
    
    // Formatter for displaying currency
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    // Formatter for displaying date and time
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public ShiftPanel(User user) {
        this.currentUser = user;
        this.shiftDAO = new ShiftDAO(); 
        setLayout(new BorderLayout(10, 10)); 
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 240, 245));

        // Create header panel with gradient background
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create center panel with main content
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Initialize shift timer
        shiftTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateShiftTimer();
            }
        });

        // Load current shift status
        loadCurrentShiftStatus();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185),
                                                    w, h, new Color(109, 213, 237));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        
        headerPanel.setLayout(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        
        // Add shift status section
        JPanel statusPanel = new JPanel(new BorderLayout(5, 0));
        statusPanel.setOpaque(false);
        
        // Create status icon
        shiftStatusIcon = new JLabel(createCircleIcon(20, inactiveColor));
        statusPanel.add(shiftStatusIcon, BorderLayout.WEST);
        
        // Create status labels
        JPanel labelsPanel = new JPanel(new GridLayout(2, 1));
        labelsPanel.setOpaque(false);
        
        statusLabel = new JLabel("No Active Shift");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        shiftTimerLabel = new JLabel("00:00:00");
        shiftTimerLabel.setForeground(Color.WHITE);
        shiftTimerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        labelsPanel.add(statusLabel);
        labelsPanel.add(shiftTimerLabel);
        statusPanel.add(labelsPanel, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);
        
        startShiftButton = createStyledButton("Start Shift", new Color(46, 204, 113));
        startShiftButton.setIcon(createStartIcon());
        startShiftButton.addActionListener(e -> startShift());
        
        endShiftButton = createStyledButton("End Shift", new Color(231, 76, 60));
        endShiftButton.setIcon(createEndIcon());
        endShiftButton.addActionListener(e -> endShift());

        buttonsPanel.add(startShiftButton);
        buttonsPanel.add(endShiftButton);
        
        headerPanel.add(statusPanel, BorderLayout.WEST);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        
        // Create cashier info panel
        cashierInfoPanel = new JPanel();
        cashierInfoPanel.setLayout(new BoxLayout(cashierInfoPanel, BoxLayout.Y_AXIS));
        cashierInfoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Shift Information"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        cashierInfoPanel.setBackground(Color.WHITE);
        
        // Add info to panel
        addInfoRow(cashierInfoPanel, "Cashier:", currentUser.getUsername());
        addInfoRow(cashierInfoPanel, "Status:", "No Active Shift");
        addInfoRow(cashierInfoPanel, "Start Time:", "--");
        addInfoRow(cashierInfoPanel, "Duration:", "00:00:00");
        addInfoRow(cashierInfoPanel, "Starting Cash:", "--");
        
        // Create animated progress bar to indicate shift activity
        animatedBar = new JProgressBar();
        animatedBar.setIndeterminate(false);
        animatedBar.setStringPainted(false);
        animatedBar.setForeground(activeColor);
        animatedBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        cashierInfoPanel.add(Box.createVerticalStrut(10));
        cashierInfoPanel.add(animatedBar);
        
        // Create shift summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Shift Summary & Reconciliation"));
        summaryPanel.setBackground(Color.WHITE);
        
        reconciliationArea = new JTextArea(12, 50);
        reconciliationArea.setEditable(false);
        reconciliationArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        reconciliationArea.setBackground(new Color(252, 252, 252));
        reconciliationArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(reconciliationArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        summaryPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create left panel to hold cashier info
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(cashierInfoPanel, BorderLayout.NORTH);
        
        centerPanel.add(leftPanel, BorderLayout.WEST);
        centerPanel.add(summaryPanel, BorderLayout.CENTER);
        
        return centerPanel;
    }
    
    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(300, 25));
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        
        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.CENTER);
        
        panel.add(row);
        panel.add(Box.createVerticalStrut(5));
    }
    
    private void updateInfoRow(String label, String value) {
        for (Component comp : cashierInfoPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel row = (JPanel) comp;
                Component[] components = row.getComponents();
                if (components.length >= 2 && components[0] instanceof JLabel) {
                    JLabel labelComp = (JLabel) components[0];
                    if (labelComp.getText().equals(label) && components[1] instanceof JLabel) {
                        JLabel valueComp = (JLabel) components[1];
                        valueComp.setText(value);
                        break;
                    }
                }
            }
        }
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void loadCurrentShiftStatus() {
        try {
            activeShift = shiftDAO.findOpenShift(); 
            updateUIState(); 
        } catch (Exception ex) {
            statusLabel.setText("Error: Can't load shift status");
            startShiftButton.setEnabled(false);
            endShiftButton.setEnabled(false);
            reconciliationArea.setText("⚠️ Error connecting to database:\n" + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error connecting to database to check shift status:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); 
        }
    }
    
    private void updateUIState() {
        boolean isShiftCurrentlyActive = (activeShift != null);

        if (isShiftCurrentlyActive) {
            // Update status label
            String startTimeFormatted = activeShift.getStartTime().format(dateFormatter) + " " +
                                      activeShift.getStartTime().format(timeFormatter);
            statusLabel.setText("Active Shift: #" + activeShift.getShiftId());
            shiftStatusIcon.setIcon(createCircleIcon(20, activeColor));
            
            // Update info panel
            updateInfoRow("Status:", "ACTIVE ✓");
            updateInfoRow("Start Time:", startTimeFormatted);
            updateInfoRow("Starting Cash:", currencyFormat.format(activeShift.getStartingFloat()));
            
            // Start animated progress bar
            animatedBar.setIndeterminate(true);
            
            // Start shift timer
            if (!shiftTimer.isRunning()) {
                shiftTimer.start();
            }
        } else {
            statusLabel.setText("No Active Shift");
            shiftStatusIcon.setIcon(createCircleIcon(20, inactiveColor));
            shiftTimerLabel.setText("00:00:00");
            
            // Update info panel
            updateInfoRow("Status:", "INACTIVE");
            updateInfoRow("Start Time:", "--");
            updateInfoRow("Duration:", "00:00:00");
            updateInfoRow("Starting Cash:", "--");
            
            // Stop animated progress bar
            animatedBar.setIndeterminate(false);
            
            // Stop shift timer
            shiftTimer.stop();
            
            reconciliationArea.setText("");
        }

        startShiftButton.setEnabled(!isShiftCurrentlyActive); 
        endShiftButton.setEnabled(isShiftCurrentlyActive);   
    }

    private void updateShiftTimer() {
        if (activeShift != null) {
            long seconds = ChronoUnit.SECONDS.between(activeShift.getStartTime(), LocalDateTime.now());
            Duration duration = Duration.ofSeconds(seconds);
            
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long secs = duration.toSecondsPart();
            
            String timeText = String.format("%02d:%02d:%02d", hours, minutes, secs);
            shiftTimerLabel.setText(timeText);
            updateInfoRow("Duration:", timeText);
        }
    }

    private void startShift() {
        if (activeShift != null) {
            JOptionPane.showMessageDialog(this, "Cannot start new shift, one is already active (ID: " + activeShift.getShiftId() + ").", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a custom panel with a spinner control for cash amount
        JPanel inputPanel = new JPanel(new BorderLayout(5, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Remove image loading that's causing errors and replace with a text label
        JLabel icon = new JLabel("💰");
        icon.setFont(new Font("Dialog", Font.PLAIN, 36));
        icon.setForeground(new Color(46, 204, 113));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 15));
        
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add title
        JLabel title = new JLabel("Starting Cash in Drawer");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(title, gbc);
        
        // Add spinner for cash amount
        gbc.gridy++;
        JSpinner cashSpinner = new JSpinner(new SpinnerNumberModel(100.00, 0.00, 10000.00, 10.00));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(cashSpinner, "$,##0.00");
        cashSpinner.setEditor(editor);
        cashSpinner.setPreferredSize(new Dimension(150, 25));
        controlPanel.add(cashSpinner, gbc);
        
        // Add help text
        gbc.gridy++;
        JLabel helpText = new JLabel("Enter the amount of cash you're starting with");
        helpText.setFont(new Font("Arial", Font.ITALIC, 11));
        helpText.setForeground(Color.GRAY);
        controlPanel.add(helpText, gbc);
        
        inputPanel.add(icon, BorderLayout.WEST);
        inputPanel.add(controlPanel, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Start New Shift", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                BigDecimal startingFloat = new BigDecimal(cashSpinner.getValue().toString());
                
                // Show loading animation
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            int shiftId = shiftDAO.startShift(currentUser.getUserId(), startingFloat);

                // Reset cursor
                setCursor(Cursor.getDefaultCursor());
            
            if (shiftId > 0) {
                    // Show success animation
                    showSuccessToast("Shift #" + shiftId + " started!");
                    
                    // Play success sound
                    Toolkit.getDefaultToolkit().beep();
                    
                loadCurrentShiftStatus(); 
            } else {
                    if (shiftDAO.findOpenShift() == null) {
                        JOptionPane.showMessageDialog(this, "Failed to start shift. Please check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid starting amount. Please enter a valid number (e.g., 100.00).", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Error starting shift:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); 
            }
        }
    }

    private void endShift() {
        if (activeShift == null) {
            JOptionPane.showMessageDialog(this, "No active shift to end.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int shiftIdToEnd = activeShift.getShiftId(); 

        // Create a stylish custom dialog panel
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Replace image loading with a text label
        JLabel iconLabel = new JLabel("🏦");
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 36));
        iconLabel.setForeground(new Color(231, 76, 60));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 15));
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("End of Shift Cash Reconciliation");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(title, gbc);
        
        // Add fields
        gbc.gridwidth = 1; gbc.gridy++;
        JLabel endingCashLabel = new JLabel("Cash Count:");
        endingCashLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(endingCashLabel, gbc);
        
        gbc.gridx = 1;
        JSpinner endingFloatField = new JSpinner(new SpinnerNumberModel(100.00, 0.00, 10000.00, 10.00));
        JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(endingFloatField, "$,##0.00");
        endingFloatField.setEditor(editor1);
        endingFloatField.setPreferredSize(new Dimension(150, 25));
        formPanel.add(endingFloatField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        JLabel cashRemovedLabel = new JLabel("Cash Removed:");
        cashRemovedLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(cashRemovedLabel, gbc);
        
        gbc.gridx = 1;
        JSpinner cashRemovedField = new JSpinner(new SpinnerNumberModel(0.00, 0.00, 10000.00, 10.00));
        JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(cashRemovedField, "$,##0.00");
        cashRemovedField.setEditor(editor2);
        cashRemovedField.setPreferredSize(new Dimension(150, 25));
        formPanel.add(cashRemovedField, gbc);
        
        // Add help text
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel helpText = new JLabel("Count all cash in drawer and enter the total amount");
        helpText.setFont(new Font("Arial", Font.ITALIC, 11));
        helpText.setForeground(Color.GRAY);
        formPanel.add(helpText, gbc);
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(formPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "End Shift #" + shiftIdToEnd,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BigDecimal endingFloat = new BigDecimal(endingFloatField.getValue().toString());
                BigDecimal cashRemoved = new BigDecimal(cashRemovedField.getValue().toString());
                
                // Show loading animation
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                ShiftDAO.ReconciliationResult summary = shiftDAO.endShift(shiftIdToEnd, currentUser.getUserId(), endingFloat, cashRemoved);

                // Reset cursor
                setCursor(Cursor.getDefaultCursor());
                
                if (summary != null) {
                    // Format summary text with emoji and colors
                    String formattedSummary = formatReconciliationSummary(summary);
                    reconciliationArea.setText(formattedSummary);
                    
                    showSuccessToast("Shift ended successfully!");
                    Toolkit.getDefaultToolkit().beep();
                    
                    loadCurrentShiftStatus(); 
                } else {
                     JOptionPane.showMessageDialog(this, "Failed to end shift or reconcile. Please check application logs.", "Error", JOptionPane.ERROR_MESSAGE);
                     loadCurrentShiftStatus();
                }
            } catch (NumberFormatException ex) {
                 JOptionPane.showMessageDialog(this, "Invalid number format for amounts. Please enter valid numbers (e.g., 550.75).", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(this, "Error ending shift:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); 
            }
        }
    }
    
    private String formatReconciliationSummary(ShiftDAO.ReconciliationResult summary) {
        // This would normally use the actual summary data, but since we don't have its structure
        // we'll just return the summary text with some formatting
        return "📊 SHIFT RECONCILIATION REPORT 📊\n" +
               "===================================\n\n" +
               summary.getSummaryText() + "\n\n" +
               "===================================\n" +
               "Report generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
               "🙏 Thank you for your hard work today!";
    }
    
    private void showSuccessToast(String message) {
        // Create a toast-like popup that automatically disappears
        JWindow toast = new JWindow();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        panel.setBackground(new Color(240, 255, 240));
        
        JLabel iconLabel = new JLabel("✓");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 16));
        iconLabel.setForeground(new Color(76, 175, 80));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(msgLabel, BorderLayout.CENTER);
        
        toast.add(panel);
        toast.pack();
        
        // Center on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension toastSize = toast.getSize();
        int x = (screenSize.width - toastSize.width) / 2;
        int y = screenSize.height - toastSize.height - 100;
        toast.setLocation(x, y);
        
        toast.setOpacity(0.9f);
        toast.setVisible(true);
        
        // Fade out after delay
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toast.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    // Helper method to create a colored circle icon for status indication
    private Icon createCircleIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(x, y, size, size);
                g2d.setColor(color.darker());
                g2d.drawOval(x, y, size, size);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }
    
    // Helper method to create a start icon
    private Icon createStartIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                int[] xPoints = {x, x + 12, x};
                int[] yPoints = {y, y + 6, y + 12};
                g2d.fillPolygon(xPoints, yPoints, 3);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }
    
    // Helper method to create an end icon
    private Icon createEndIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x, y, 4, 12);
                g2d.fillRect(x + 8, y, 4, 12);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }
} 