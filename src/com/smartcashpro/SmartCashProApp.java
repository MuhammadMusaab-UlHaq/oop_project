package com.smartcashpro; 

import javax.swing.SwingUtilities;
import com.smartcashpro.ui.LoginPageCashierSystem;

public class SmartCashProApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginPageCashierSystem loginPage = new LoginPageCashierSystem();
                loginPage.setVisible(true);
            }
        });
    }
}