package ui;

import common.DBConnection;
import common.Screen;
import common.ValidationUtil;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class CustomerRegister extends Screen {

    public CustomerRegister() {
        super("Traveler Registration", 580, 660);

        getContentPane().setBackground(COLOR_BG);

        initUI();
    }

    private void initUI() {
        JPanel card = createCard(40, 20, 500, 580);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, COLOR_SUCCESS),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
            )
        ));

        // Top Brand Logo & Pill Tag
        ImageIcon logo = getLogoIcon(38, 38);
        if (logo != null) {
            JLabel logoLbl = new JLabel(logo);
            logoLbl.setBounds(500 - 24 - 38, 14, 38, 38);
            card.add(logoLbl);
        }

        JLabel tag = new JLabel("  TRAVELER REGISTRATION  •  NEW ACCOUNT  ");
        ImageIcon custIcon = loadIcon("/images/icons/customers.png", 14, 14);
        if (custIcon != null) {
            tag.setIcon(custIcon);
        }
        tag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tag.setForeground(COLOR_SUCCESS);
        tag.setOpaque(true);
        tag.setBackground(new Color(236, 253, 245));
        tag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(167, 243, 208), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        tag.setBounds(24, 16, 390, 22);
        card.add(tag);

        // Header and subtitle
        JLabel header = createHeader(card, "Create an Account", 24, 42, 390, 28);
        setFontSize(header, 20);
        setTextColor(header, COLOR_TEXT_MAIN);

        JLabel subheader = createLabel(card, "Fill in your profile details to start booking trips.", 24, 70, 452, 18);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        // Form Fields (y starting at 96, gap 34)
        JTextField firstName = addFormField(card, "First Name *:", 96, true);
        JTextField middleName = addFormField(card, "Middle Name:", 130, true);
        JTextField lastName = addFormField(card, "Last Name *:", 164, true);

        JLabel genderLbl = createLabel(card, "Gender *:", 16, 198, 95, 28);
        setFontStyle(genderLbl, Font.BOLD);
        setFontSize(genderLbl, 11);
        setTextColor(genderLbl, new Color(71, 85, 105));
        JComboBox<String> genderCombo = createComboBox(card, new String[]{"Male", "Female"}, 115, 198, 361, 28);

        JTextField age = addFormField(card, "Age *:", 232, true);
        JTextField email = addFormField(card, "Email *:", 266, true);
        JTextField username = addFormField(card, "Username *:", 300, true);

        JLabel passLbl = createLabel(card, "Password *:", 16, 334, 95, 28);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 11);
        setTextColor(passLbl, new Color(71, 85, 105));
        JPasswordField password = createPasswordField(card, 115, 334, 361, 28);

        JLabel confirmLbl = createLabel(card, "Confirm Pass *:", 16, 368, 95, 28);
        setFontStyle(confirmLbl, Font.BOLD);
        setFontSize(confirmLbl, 11);
        setTextColor(confirmLbl, new Color(71, 85, 105));
        JPasswordField confirmPassword = createPasswordField(card, 115, 368, 361, 28);

        // Submit Button
        JButton registerBtn = createSuccessButton(card, "Register Account", 24, 412, 452, 40, null);
        setFontStyle(registerBtn, Font.BOLD);
        setFontSize(registerBtn, 14);
        registerBtn.setIcon(loadIcon("/images/icons/add.png", 18, 18));

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

            if (!ValidationUtil.isValidName(first)) {
                showError("First name can only contain letters, spaces, hyphens, and periods.");
                return;
            }
            if (!middle.isEmpty() && !ValidationUtil.isValidName(middle)) {
                showError("Middle name can only contain letters, spaces, hyphens, and periods.");
                return;
            }
            if (!ValidationUtil.isValidName(last)) {
                showError("Last name can only contain letters, spaces, hyphens, and periods.");
                return;
            }
            if (!ValidationUtil.isValidEmail(mail)) {
                showError("Please enter a valid email address (e.g. user@example.com).");
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

        // Already have an account link
        JButton loginLinkBtn = createStyledButton(card, "Already have an account? Login here", 24, 460, 452, 34,
            new Color(239, 246, 255), COLOR_PRIMARY, new Color(191, 219, 254), 12, () -> navigateTo(new CustomerLogin()));
        setFontStyle(loginLinkBtn, Font.BOLD);
        loginLinkBtn.setIcon(loadIcon("/images/icons/user.png", 16, 16));

        // Back button
        JButton backBtn = createSecondaryButton(card, "← Back to Home", 24, 502, 452, 34, () -> navigateTo(new Homepage()));
        setFontSize(backBtn, 12);
    }
}