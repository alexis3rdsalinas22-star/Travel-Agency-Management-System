package ui.customer;

import common.DBConnection;
import common.Screen;
import common.Session;
import common.ValidationUtil;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class CustomerProfile extends Screen {

    private final CustomerDAO customerDAO = new CustomerDAO();

    private JTextField idField;
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JComboBox<String> genderCombo;
    private JTextField ageField;
    private JTextField statusField;

    public CustomerProfile() {
        super("My Profile", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
        loadProfile();
    }

    private void initUI() {
        // Header & Subtitle
        JLabel header = createHeader("My Profile", 200, 25, 600, 30);
        setFontSize(header, 24);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("View and update your personal account information.", 200, 56, 600, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form Fields
        JLabel idLbl = createLabel("Customer ID:", 200, 95, 130, 25);
        setFontStyle(idLbl, Font.BOLD);
        setFontSize(idLbl, 13);
        setTextColor(idLbl, new Color(55, 65, 81));
        idField = createTextField(340, 95, 460, 30);
        idField.setEditable(false);

        JLabel userLbl = createLabel("Username:", 200, 131, 130, 25);
        setFontStyle(userLbl, Font.BOLD);
        setFontSize(userLbl, 13);
        setTextColor(userLbl, new Color(55, 65, 81));
        usernameField = createTextField(340, 131, 460, 30);
        usernameField.setEditable(false);

        JLabel firstLbl = createLabel("First Name *:", 200, 167, 130, 25);
        setFontStyle(firstLbl, Font.BOLD);
        setFontSize(firstLbl, 13);
        setTextColor(firstLbl, new Color(55, 65, 81));
        firstNameField = createTextField(340, 167, 460, 30);
        setFontSize(firstNameField, 13);

        JLabel midLbl = createLabel("Middle Name:", 200, 203, 130, 25);
        setFontSize(midLbl, 13);
        setTextColor(midLbl, new Color(55, 65, 81));
        middleNameField = createTextField(340, 203, 460, 30);
        setFontSize(middleNameField, 13);

        JLabel lastLbl = createLabel("Last Name *:", 200, 239, 130, 25);
        setFontStyle(lastLbl, Font.BOLD);
        setFontSize(lastLbl, 13);
        setTextColor(lastLbl, new Color(55, 65, 81));
        lastNameField = createTextField(340, 239, 460, 30);
        setFontSize(lastNameField, 13);

        JLabel emailLbl = createLabel("Email *:", 200, 275, 130, 25);
        setFontStyle(emailLbl, Font.BOLD);
        setFontSize(emailLbl, 13);
        setTextColor(emailLbl, new Color(55, 65, 81));
        emailField = createTextField(340, 275, 460, 30);
        setFontSize(emailField, 13);

        JLabel genderLbl = createLabel("Gender *:", 200, 311, 130, 25);
        setFontStyle(genderLbl, Font.BOLD);
        setFontSize(genderLbl, 13);
        setTextColor(genderLbl, new Color(55, 65, 81));
        genderCombo = createComboBox(new String[]{"Male", "Female"}, 340, 311, 460, 30);
        setFontSize(genderCombo, 13);

        JLabel ageLbl = createLabel("Age *:", 200, 347, 130, 25);
        setFontStyle(ageLbl, Font.BOLD);
        setFontSize(ageLbl, 13);
        setTextColor(ageLbl, new Color(55, 65, 81));
        ageField = createTextField(340, 347, 460, 30);
        setFontSize(ageField, 13);

        JLabel statusLbl = createLabel("Account Status:", 200, 383, 130, 25);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 13);
        setTextColor(statusLbl, new Color(55, 65, 81));
        statusField = createTextField(340, 383, 460, 30);
        statusField.setEditable(false);

        // Buttons
        JButton saveBtn = createButton("Save Changes", 340, 435, 225, 40);
        setFontStyle(saveBtn, Font.BOLD);
        setFontSize(saveBtn, 14);
        setBackground(saveBtn, new Color(16, 149, 93));
        setTextColor(saveBtn, Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> handleSaveProfile());

        JButton backBtn = createButton("Back to Dashboard", 575, 435, 225, 40);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(55, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new CustomerDashboard()));
    }

    private void loadProfile() {
        int customerId = Session.getCustomerId();
        if (customerId <= 0) {
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            CustomerRecord record = customerDAO.getCustomerById(con, customerId);
            if (record != null) {
                idField.setText(String.valueOf(record.customerID));
                usernameField.setText(record.username != null ? record.username : "");
                firstNameField.setText(record.firstName != null ? record.firstName : "");
                middleNameField.setText(record.middleName != null ? record.middleName : "");
                lastNameField.setText(record.lastName != null ? record.lastName : "");
                emailField.setText(record.email != null ? record.email : "");
                if (record.sex != null) {
                    genderCombo.setSelectedItem(record.sex);
                }
                ageField.setText(String.valueOf(record.age));
                statusField.setText(record.status != null ? record.status : "Active");
            }
        } catch (SQLException ex) {
            showError("Database error loading profile: " + ex.getMessage());
        }
    }

    private void handleSaveProfile() {
        int customerId = Session.getCustomerId();
        if (customerId <= 0) {
            showError("No active customer session found.");
            return;
        }

        String first = firstNameField.getText().trim();
        String middle = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String sex = (String) genderCombo.getSelectedItem();
        String ageStr = ageField.getText().trim();

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || ageStr.isEmpty()) {
            showError("Please fill in all required fields (*).");
            return;
        }

        Integer ageValue = ValidationUtil.parsePositiveInt(ageStr);
        if (ageValue == null || ageValue > 130) {
            showError("Please enter a valid age.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (customerDAO.emailExistsForOther(con, email, customerId)) {
                showError("The email address '" + email + "' is already in use by another account.");
                return;
            }

            boolean updated = customerDAO.updateCustomer(
                    con, customerId, first, middle.isEmpty() ? null : middle, last, email, sex, ageValue);

            if (updated) {
                showInfo("Profile updated successfully!");
                loadProfile();
            } else {
                showError("Update failed: customer not found.");
            }
        } catch (SQLException ex) {
            showError("Failed to update profile: " + ex.getMessage());
        }
    }
}