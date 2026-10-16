package ui;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import ui.customer.CustomerDashboard;

public class CustomerLogin extends Screen {

    public CustomerLogin() {
        super("Account Login", 500, 500);

        getContentPane().setBackground(new Color(245, 247, 250));

        // Header and subtitle
        JLabel header = createHeader("Account Login", 60, 35, 380, 35);
        setFontSize(header, 24);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Please enter your credentials to log in.", 60, 75, 380, 25);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form Fields
        JLabel userLabel = createLabel("Username", 60, 120, 380, 20);
        setFontStyle(userLabel, Font.BOLD);
        setFontSize(userLabel, 13);
        setTextColor(userLabel, new Color(55, 65, 81));

        JTextField username = createTextField(60, 145, 380, 36);
        setFontSize(username, 14);

        JLabel passLabel = createLabel("Password", 60, 195, 380, 20);
        setFontStyle(passLabel, Font.BOLD);
        setFontSize(passLabel, 13);
        setTextColor(passLabel, new Color(55, 65, 81));

        JPasswordField password = createPasswordField(60, 220, 380, 36);
        setFontSize(password, 14);

        // Buttons
        JButton loginBtn = createButton("Sign In to Account", 60, 280, 380, 42);
        setFontStyle(loginBtn, Font.BOLD);
        setFontSize(loginBtn, 14);
        setBackground(loginBtn, new Color(37, 99, 235));
        setTextColor(loginBtn, Color.WHITE);
        loginBtn.setFocusPainted(false);

        // Keyboard ergonomics
        username.addActionListener(e -> password.requestFocusInWindow());
        password.addActionListener(e -> loginBtn.doClick());

        loginBtn.addActionListener(e -> {
            String user = username.getText().trim();
            String pass = new String(password.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                showError("Please enter both username and password.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                CustomerAccountDAO.CustomerLoginResult result = new CustomerAccountDAO().login(con, user, pass);
                if (result != null) {
                    common.Session.setCustomerSession(result.customerId, result.customerAccountId, user);
                    showInfo("Welcome back! Login successful.");
                    navigateTo(new CustomerDashboard(result.customerId));
                } else {
                    showError("Invalid username or password, or account is not active.");
                }
            } catch (SQLException ex) {
                showError("Database connection error: " + ex.getMessage());
            }
        });

        JButton registerBtn = createButton("Don't have an account? Register here", 60, 335, 380, 36);
        setFontSize(registerBtn, 13);
        setBackground(registerBtn, new Color(230, 235, 245));
        setTextColor(registerBtn, new Color(37, 99, 235));
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> navigateTo(new CustomerRegister()));

        JButton backBtn = createButton("Back to Home", 60, 380, 380, 36);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(55, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
    }
}