package ui;

import common.DBConnection;
import common.Screen;
import common.Session;
import dao.AdminAccountDAO;
import model.AdminAccount;
import ui.admin.AdminDashboard;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AdminLogin extends Screen {

    public AdminLogin() {
        super("Administrator Login", 500, 500);

        getContentPane().setBackground(new Color(245, 247, 250));

        // Header and subtitle
        JLabel header = createHeader("Administrator Login", 60, 40, 380, 35);
        setFontSize(header, 24);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Please enter your admin credentials to continue.", 60, 80, 380, 25);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form fields
        JLabel userLabel = createLabel("Username", 60, 125, 380, 20);
        setFontStyle(userLabel, Font.BOLD);
        setFontSize(userLabel, 13);
        setTextColor(userLabel, new Color(55, 65, 81));

        JTextField username = createTextField(60, 150, 380, 36);
        setFontSize(username, 14);

        JLabel passLabel = createLabel("Password", 60, 205, 380, 20);
        setFontStyle(passLabel, Font.BOLD);
        setFontSize(passLabel, 13);
        setTextColor(passLabel, new Color(55, 65, 81));

        JPasswordField password = createPasswordField(60, 230, 380, 36);
        setFontSize(password, 14);

        // Action buttons
        JButton loginBtn = createButton("Sign In as Administrator", 60, 295, 380, 42);
        setFontStyle(loginBtn, Font.BOLD);
        setFontSize(loginBtn, 14);
        setBackground(loginBtn, new Color(51, 65, 85));
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
                AdminAccount admin = new AdminAccountDAO().login(con, user, pass);
                if (admin != null) {
                    Session.setAdminSession(admin);
                    showInfo("Welcome, " + admin.getusername() + "! Login successful.");
                    navigateTo(new AdminDashboard());
                } else {
                    showError("Invalid administrator credentials.");
                }
            } catch (SQLException ex) {
                showError("Database connection error: " + ex.getMessage());
            }
        });

        JButton backBtn = createButton("Back to Home", 60, 350, 380, 38);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(55, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
    }
}
