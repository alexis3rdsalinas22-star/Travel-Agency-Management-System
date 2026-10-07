package ui;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.*;

public class UserRegister extends Screen {
    public UserRegister() {
        super("Account Register", 600, 800);

        createHeader("ACCOUNT REGISTER", 125, 50, 400, 50);

        createLabel("First Name: ", 50, 100);
        JTextField firstName = createTextField(200, 100);
  
        createLabel("Middle Name: ", 50, 150);
        JTextField middleName = createTextField(200, 150);

        createLabel("Last Name: ", 50, 200);
        JTextField lastName = createTextField(200, 200);

        createLabel("Email: ", 50, 250);
        JTextField email = createTextField(200, 250);

        createLabel("Age: ", 50, 300);
        JTextField age = createTextField(200, 300);

        createLabel("Username: ", 50, 400);
        JTextField username = createTextField(200, 400);

        createLabel("Password: ", 50, 450);
        JPasswordField password = createPasswordField(200, 450);

        createButton("Register", 125, 550).addActionListener(e -> {
            String first = firstName.getText().trim();
            String middle = middleName.getText().trim();
            String last = lastName.getText().trim();
            String mail = email.getText().trim();
            String ageStr = age.getText().trim();
            String user = username.getText().trim();
            String pass = new String(password.getPassword());

            if (first.isEmpty() || last.isEmpty() || mail.isEmpty() || ageStr.isEmpty()
                    || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }

            int ageValue;
            try {
                ageValue = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age must be a number.");
                return;
            }

       

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    CustomerAccountDAO accountDAO = new CustomerAccountDAO();
                    if (accountDAO.usernameExists(con, user)) {
                        JOptionPane.showMessageDialog(this, "Username already exists.");
                        return;
                    }
                    int customerId = new CustomerDAO()
                            .insertCustomer(con, first, middle, last, mail, ageValue);
                    accountDAO.insertAccount(con, customerId, user, pass);
                    con.commit();

                    JOptionPane.showMessageDialog(this, "Registration successful!");
                    navigateTo(new Homepage());
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                } 
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        });

        createButton("Go Back", 50, 700).addActionListener(e -> {
            navigateTo(new Homepage());
        });
    }
}