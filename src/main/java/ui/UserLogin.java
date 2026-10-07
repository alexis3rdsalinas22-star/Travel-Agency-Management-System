package ui;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.*;

public class UserLogin extends Screen {
    public UserLogin() {
        super("Account Login", 600, 800);

        createHeader("ACCOUNT LOGIN", 150, 100, 200, 50);

        createLabel("Username: ", 50, 200);
        JTextField username = createTextField(200, 200);

        createLabel("Password: ", 50, 250);
        JPasswordField password = createPasswordField(200, 250);

        createButton("Login", 125, 350).addActionListener(e -> {
            String user = username.getText().trim();
            String pass = new String(password.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter your username and password.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                int customerId = new CustomerAccountDAO().login(con, user, pass);
                if (customerId != -1) {
                    JOptionPane.showMessageDialog(this, "Welcome!");
                    
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid username or password, or account not active.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        });

        createButton("Go Back", 50, 500).addActionListener(e -> {
            navigateTo(new Homepage());
        });
    }
}