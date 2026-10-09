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

public class UserLogin extends Screen {

    public UserLogin() {
        super("Account Login", 500, 500);

        getContentPane().setBackground(new Color(245, 247, 250));

        // Header and subtitle
        JLabel header = createHeader("Account Login", 60, 35, 380, 35);
        setFont(header, "Segoe UI", Font.BOLD, 24);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Please enter your credentials to log in.", 60, 75, 380, 25);
        setFont(subheader, "Segoe UI", Font.PLAIN, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form Fields
        JLabel userLabel = createLabel("Username", 60, 120, 380, 20);
        setFont(userLabel, "Segoe UI", Font.BOLD, 13);
        setTextColor(userLabel, new Color(55, 65, 81));

        JTextField username = createTextField(60, 145, 380, 35);
        setFont(username, "Segoe UI", Font.PLAIN, 14);

        JLabel passLabel = createLabel("Password", 60, 195, 380, 20);
        setFont(passLabel, "Segoe UI", Font.BOLD, 13);
        setTextColor(passLabel, new Color(55, 65, 81));

        JPasswordField password = createPasswordField(60, 220, 380, 35);
        setFont(password, "Segoe UI", Font.PLAIN, 14);

        // Buttons
        JButton loginBtn = createButton("Login", 60, 280, 380, 40);
        setFont(loginBtn, "Segoe UI", Font.BOLD, 14);
        setBackground(loginBtn, new Color(37, 99, 235));
        setTextColor(loginBtn, Color.WHITE);
        loginBtn.setFocusPainted(false);

        loginBtn.addActionListener(e -> {
            String user = username.getText().trim();
            String pass = new String(password.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                showError("Please enter both username and password.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                int customerId = new CustomerAccountDAO().login(con, user, pass);
                if (customerId != -1) {
                    showInfo("Welcome back! Login successful.");
                    // Reserved: navigate to Customer Dashboard when implemented
                } else {
                    showError("Invalid username or password, or account is not active.");
                }
            } catch (SQLException ex) {
                showError("Database connection error: " + ex.getMessage());
            }
        });

        JButton registerBtn = createButton("Don't have an account? Register here", 60, 335, 380, 35);
        setFont(registerBtn, "Segoe UI", Font.PLAIN, 13);
        setBackground(registerBtn, new Color(230, 235, 245));
        setTextColor(registerBtn, new Color(37, 99, 235));
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> navigateTo(new UserRegister()));

        JButton backBtn = createButton("Back to Home", 60, 380, 380, 35);
        setFont(backBtn, "Segoe UI", Font.PLAIN, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(55, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
    }
}