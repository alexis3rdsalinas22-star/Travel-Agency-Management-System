package ui.customer;

import common.DBConnection;
import common.Screen;
import common.Session;
import common.ValidationUtil;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
        super("My Profile", 700, 600);

        initUI();
        loadProfile();
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("My Profile", 60, 25, 400, 30);
        createLabel("View and update your personal information.", 60, 55, 450, 20);

        // Form Fields
        createLabel("Customer ID:", 60, 95, 120, 25);
        idField = createTextField(190, 95, 430, 30);
        idField.setEditable(false);

        createLabel("Username:", 60, 135, 120, 25);
        usernameField = createTextField(190, 135, 430, 30);
        usernameField.setEditable(false);

        createLabel("First Name *:", 60, 175, 120, 25);
        firstNameField = createTextField(190, 175, 430, 30);

        createLabel("Middle Name:", 60, 215, 120, 25);
        middleNameField = createTextField(190, 215, 430, 30);

        createLabel("Last Name *:", 60, 255, 120, 25);
        lastNameField = createTextField(190, 255, 430, 30);

        createLabel("Email *:", 60, 295, 120, 25);
        emailField = createTextField(190, 295, 430, 30);

        createLabel("Gender *:", 60, 335, 120, 25);
        genderCombo = createComboBox(new String[]{"Male", "Female"}, 190, 335, 430, 30);

        createLabel("Age *:", 60, 375, 120, 25);
        ageField = createTextField(190, 375, 430, 30);

        createLabel("Account Status:", 60, 415, 120, 25);
        statusField = createTextField(190, 415, 430, 30);
        statusField.setEditable(false);

        // Buttons
        JButton saveBtn = createButton("Save Changes", 190, 470, 205, 38);
        saveBtn.addActionListener(e -> handleSaveProfile());

        JButton backBtn = createButton("Back to Dashboard", 415, 470, 205, 38);
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