package ui;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class UserRegister extends Screen {

    public UserRegister() {
        super("Account Registration", 560, 720);

        getContentPane().setBackground(new Color(245, 247, 250));

        // Header and subtitle
        JLabel header = createHeader("Create an Account", 50, 25, 450, 32);
        setFont(header, "Segoe UI", Font.BOLD, 22);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Please fill in your details to register as a new traveler.", 50, 58, 450, 20);
        setFont(subheader, "Segoe UI", Font.PLAIN, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form Fields
        JLabel firstLbl = createLabel("First Name *", 50, 90, 130, 30);
        setFont(firstLbl, "Segoe UI", Font.BOLD, 13);
        JTextField firstName = createTextField(180, 90, 320, 30);
        setFont(firstName, "Segoe UI", Font.PLAIN, 13);

        JLabel midLbl = createLabel("Middle Name", 50, 132, 130, 30);
        setFont(midLbl, "Segoe UI", Font.PLAIN, 13);
        JTextField middleName = createTextField(180, 132, 320, 30);
        setFont(middleName, "Segoe UI", Font.PLAIN, 13);

        JLabel lastLbl = createLabel("Last Name *", 50, 174, 130, 30);
        setFont(lastLbl, "Segoe UI", Font.BOLD, 13);
        JTextField lastName = createTextField(180, 174, 320, 30);
        setFont(lastName, "Segoe UI", Font.PLAIN, 13);

        JLabel genderLbl = createLabel("Gender *", 50, 216, 130, 30);
        setFont(genderLbl, "Segoe UI", Font.BOLD, 13);
        JComboBox<String> genderCombo = createComboBox(new String[]{"Male", "Female"}, 180, 216, 320, 30);
        setFont(genderCombo, "Segoe UI", Font.PLAIN, 13);

        JLabel ageLbl = createLabel("Age *", 50, 258, 130, 30);
        setFont(ageLbl, "Segoe UI", Font.BOLD, 13);
        JTextField age = createTextField(180, 258, 320, 30);
        setFont(age, "Segoe UI", Font.PLAIN, 13);

        JLabel emailLbl = createLabel("Email *", 50, 300, 130, 30);
        setFont(emailLbl, "Segoe UI", Font.BOLD, 13);
        JTextField email = createTextField(180, 300, 320, 30);
        setFont(email, "Segoe UI", Font.PLAIN, 13);

        JLabel userLbl = createLabel("Username *", 50, 342, 130, 30);
        setFont(userLbl, "Segoe UI", Font.BOLD, 13);
        JTextField username = createTextField(180, 342, 320, 30);
        setFont(username, "Segoe UI", Font.PLAIN, 13);

        JLabel passLbl = createLabel("Password *", 50, 384, 130, 30);
        setFont(passLbl, "Segoe UI", Font.BOLD, 13);
        JPasswordField password = createPasswordField(180, 384, 320, 30);
        setFont(password, "Segoe UI", Font.PLAIN, 13);

        JLabel confirmLbl = createLabel("Confirm Pass *", 50, 426, 130, 30);
        setFont(confirmLbl, "Segoe UI", Font.BOLD, 13);
        JPasswordField confirmPassword = createPasswordField(180, 426, 320, 30);
        setFont(confirmPassword, "Segoe UI", Font.PLAIN, 13);

        // Buttons
        JButton registerBtn = createButton("Register Account", 50, 480, 450, 40);
        setFont(registerBtn, "Segoe UI", Font.BOLD, 14);
        setBackground(registerBtn, new Color(16, 149, 93));
        setTextColor(registerBtn, Color.WHITE);
        registerBtn.setFocusPainted(false);

        registerBtn.addActionListener(e -> {
            String first = firstName.getText().trim();
            String middle = middleName.getText().trim();
            String last = lastName.getText().trim();
            String sex = (String) genderCombo.getSelectedItem();
            String mail = email.getText().trim();
            String ageStr = age.getText().trim();
            String user = username.getText().trim();
            String pass = new String(password.getPassword());
            String confirmPass = new String(confirmPassword.getPassword());

            if (first.isEmpty() || last.isEmpty() || mail.isEmpty() || ageStr.isEmpty()
                    || user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                showError("Please fill in all required fields (*).");
                return;
            }

            if (!pass.equals(confirmPass)) {
                showError("Passwords do not match. Please try again.");
                return;
            }

            int ageValue;
            try {
                ageValue = Integer.parseInt(ageStr);
                if (ageValue <= 0 || ageValue > 130) {
                    showError("Please enter a valid age.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Age must be a valid number.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    CustomerAccountDAO accountDAO = new CustomerAccountDAO();
                    if (accountDAO.usernameExists(con, user)) {
                        showError("Username already exists. Please choose a different username.");
                        return;
                    }

                    int customerId = new CustomerDAO().insertCustomer(
                            con, first, middle.isEmpty() ? null : middle, last, mail, sex, ageValue);

                    accountDAO.insertAccount(con, customerId, user, pass);
                    con.commit();

                    showInfo("Registration successful! You may now log in.");
                    navigateTo(new UserLogin());
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            }
        });

        JButton loginLinkBtn = createButton("Already have an account? Login here", 50, 530, 450, 35);
        setFont(loginLinkBtn, "Segoe UI", Font.PLAIN, 13);
        setBackground(loginLinkBtn, new Color(230, 235, 245));
        setTextColor(loginLinkBtn, new Color(37, 99, 235));
        loginLinkBtn.setFocusPainted(false);
        loginLinkBtn.addActionListener(e -> navigateTo(new UserLogin()));

        JButton backBtn = createButton("Back to Home", 50, 575, 450, 35);
        setFont(backBtn, "Segoe UI", Font.PLAIN, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(55, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
    }
}