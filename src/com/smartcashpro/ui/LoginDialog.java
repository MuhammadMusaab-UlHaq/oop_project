package com.smartcashpro.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.smartcashpro.db.UserDAO; 
import com.smartcashpro.model.User;

public class LoginDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private boolean succeeded;
    private User loggedInUser; 

    public LoginDialog(Frame parent) {
        super(parent, "Login", true); 

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbUsername = new JLabel("Username: ");
        cs.gridx = 0; cs.gridy = 0; cs.gridwidth = 1; panel.add(lbUsername, cs);
        tfUsername = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; cs.gridwidth = 2; panel.add(tfUsername, cs);

        lbPassword = new JLabel("Password: ");
        cs.gridx = 0; cs.gridy = 1; cs.gridwidth = 1; panel.add(lbPassword, cs);
        pfPassword = new JPasswordField();
        cs.gridx = 1; cs.gridy = 1; cs.gridwidth = 2; panel.add(pfPassword, cs);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

        btnLogin = new JButton("Login");
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                UserDAO userDAO = new UserDAO(); 
                loggedInUser = userDAO.authenticate(getUsername(), getPassword());

                if (loggedInUser != null) {
                    succeeded = true;
                    dispose(); 
                } else {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Invalid username or password",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                    
                    pfPassword.setText("");
                    succeeded = false;
                    loggedInUser = null;
                }
            }
        });

        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                succeeded = false;
                loggedInUser = null;
                dispose();
            }
        });

        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent); 
    }

    public String getUsername() {
        return tfUsername.getText().trim();
    }

    public String getPassword() {
        return new String(pfPassword.getPassword());
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}