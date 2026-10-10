package ui;

import common.DBConnection;
import common.Screen;
import common.Session;
import dao.AdminAccountDAO;
import model.AdminAccount;
import ui.admin.AdminDashboard;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AdminLogin extends Screen {

    public AdminLogin() {
        super("Administrator Login", 500, 450);

        // Header and subtitle
        createHeader("Administrator Login", 60, 35, 380, 35);
        createLabel("Please enter your admin credentials.", 60, 75, 380, 25);

        // Form fields
        createLabel("Username", 60, 120, 380, 20);
        JTextField username = createTextField(60, 145, 380, 35);

        createLabel("Password", 60, 195, 380, 20);
        JPasswordField password = createPasswordField(60, 220, 380, 35);

        // Action buttons
        JButton loginBtn = createButton("Login", 60, 280, 380, 35);
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

        JButton backBtn = createButton("Back to Home", 60, 330, 380, 35);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
    }
}
