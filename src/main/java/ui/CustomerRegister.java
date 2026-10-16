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

public class CustomerRegister extends Screen {

    public CustomerRegister() {
        super("Account Registration", 560, 680);

        getContentPane().setBackground(new Color(245, 247, 250));

        // Header and subtitle
        JLabel header = createHeader("Create an Account", 50, 25, 450, 32);
        setFontSize(header, 22);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Please fill in your details to register as a new traveler.", 50, 58, 450, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form Labels & Fields
        JLabel firstLbl = createLabel("First Name *", 50, 90, 130, 30);
        setFontStyle(firstLbl, Font.BOLD);
        setFontSize(firstLbl, 13);
        setTextColor(firstLbl, new Color(55, 65, 81));
        JTextField firstName = createTextField(180, 90, 320, 30);
        setFontSize(firstName, 13);

        JLabel midLbl = createLabel("Middle Name", 50, 132, 130, 30);
        setFontSize(midLbl, 13);
        setTextColor(midLbl, new Color(55, 65, 81));
        JTextField middleName = createTextField(180, 132, 320, 30);
        setFontSize(middleName, 13);

        JLabel lastLbl = createLabel("Last Name *", 50, 174, 130, 30);
        setFontStyle(lastLbl, Font.BOLD);
        setFontSize(lastLbl, 13);
        setTextColor(lastLbl, new Color(55, 65, 81));
        JTextField lastName = createTextField(180, 174, 320, 30);
        setFontSize(lastName, 13);

        JLabel genderLbl = createLabel("Gender *", 50, 216, 130, 30);
        setFontStyle(genderLbl, Font.BOLD);
        setFontSize(genderLbl, 13);
        setTextColor(genderLbl, new Color(55, 65, 81));
        JComboBox<String> genderCombo = createComboBox(new String[]{"Male", "Female"}, 180, 216, 320, 30);
        setFontSize(genderCombo, 13);

        JLabel ageLbl = createLabel("Age *", 50, 258, 130, 30);
        setFontStyle(ageLbl, Font.BOLD);
        setFontSize(ageLbl, 13);
        setTextColor(ageLbl, new Color(55, 65, 81));
        JTextField age = createTextField(180, 258, 320, 30);
        setFontSize(age, 13);

        JLabel emailLbl = createLabel("Email *", 50, 300, 130, 30);
        setFontStyle(emailLbl, Font.BOLD);
        setFontSize(emailLbl, 13);
        setTextColor(emailLbl, new Color(55, 65, 81));
        JTextField email = createTextField(180, 300, 320, 30);
        setFontSize(email, 13);

        JLabel userLbl = createLabel("Username *", 50, 342, 130, 30);
        setFontStyle(userLbl, Font.BOLD);
        setFontSize(userLbl, 13);
        setTextColor(userLbl, new Color(55, 65, 81));
        JTextField username = createTextField(180, 342, 320, 30);
        setFontSize(username, 13);

        JLabel passLbl = createLabel("Password *", 50, 384, 130, 30);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 13);
        setTextColor(passLbl, new Color(55, 65, 81));
        JPasswordField password = createPasswordField(180, 384, 320, 30);
        setFontSize(password, 13);

        JLabel confirmLbl = createLabel("Confirm Pass *", 50, 426, 130, 30);
        setFontStyle(confirmLbl, Font.BOLD);
        setFontSize(confirmLbl, 13);
        setTextColor(confirmLbl, new Color(55, 65, 81));
        JPasswordField confirmPassword = createPasswordField(180, 426, 320, 30);
        setFontSize(confirmPassword, 13);

        // Buttons
        JButton registerBtn = createButton("Register Account", 50, 480, 450, 42);
        setFontStyle(registerBtn, Font.BOLD);
        setFontSize(registerBtn, 14);
        setBackground(registerBtn, new Color(16, 149, 93));
        setTextColor(registerBtn, Color.WHITE);
        registerBtn.setFocusPainted(false);

        confirmPassword.addActionListener(e -> registerBtn.doClick());

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
                    CustomerDAO customerDAO = new CustomerDAO();
                    if (customerDAO.emailExists(con, mail)) {
                        showError("The email address '" + mail + "' is already registered. Please log in or use another email.");
                        return;
                    }

                    CustomerAccountDAO accountDAO = new CustomerAccountDAO();
                    if (accountDAO.usernameExists(con, user)) {
                        showError("Username already exists. Please choose a different username.");
                        return;
                    }

                    int customerId = customerDAO.insertCustomer(
                            con, first, middle.isEmpty() ? null : middle, last, mail, sex, ageValue);

                    accountDAO.insertAccount(con, customerId, user, pass);
                    con.commit();

                    showInfo("Registration successful! You may now log in.");
                    navigateTo(new CustomerLogin());
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            }
        });

        JButton loginLinkBtn = createButton("Already have an account? Login here", 50, 530, 450, 35);
        setFontSize(loginLinkBtn, 13);
        setBackground(loginLinkBtn, new Color(230, 235, 245));
        setTextColor(loginLinkBtn, new Color(37, 99, 235));
        loginLinkBtn.setFocusPainted(false);
        loginLinkBtn.addActionListener(e -> navigateTo(new CustomerLogin()));

        JButton backBtn = createButton("Back to Home", 50, 575, 450, 35);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(55, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
    }
}