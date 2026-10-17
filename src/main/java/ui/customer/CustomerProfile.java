package ui.customer;

import common.DBConnection;
import common.Screen;
import common.Session;
import common.ValidationUtil;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;

import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
        super("My Profile");
        initUI();
        loadProfile();
    }

    private void initUI() {
      
        createTopHeader(
                "CUSTOMER",
                "My Profile",
                "View and update your personal account information.",
                () -> navigateTo(new CustomerDashboard())
        );

        
        JPanel accountCard = createCard(45, 105, 345, 215);
        createCardTag(accountCard, "ACCOUNT");
        idField       = addFormField(accountCard, "Customer ID", 48,  false);
        usernameField = addFormField(accountCard, "Username",    100, false);
        statusField   = addFormField(accountCard, "Status",      152, false);

    
        JPanel nameCard = createCard(427, 105, 345, 215);
        createCardTag(nameCard, "NAME");
        firstNameField  = addFormField(nameCard, "First Name *", 48,  true);
        middleNameField = addFormField(nameCard, "Middle Name",  100, true);
        lastNameField   = addFormField(nameCard, "Last Name *",  152, true);

 
        JPanel detailsCard = createCard(810, 105, 345, 215);
        createCardTag(detailsCard, "CONTACT & DETAILS");
        emailField  = addFormField(detailsCard, "Email *", 48, true);
        genderCombo = addComboRow(detailsCard, "Sex *", new String[]{"Male", "Female"}, 100);
        ageField    = addFormField(detailsCard, "Age *", 152, true);

    
        createSuccessButton("Save Changes", 940, 345, 215, 38, this::handleSaveProfile);
    }

   
    private JComboBox<String> addComboRow(JPanel parent, String labelText, String[] items, int y) {
        JLabel lbl = createLabel(parent, labelText, 16, y, 95, 26);
        setFontStyle(lbl, Font.BOLD);
        setFontSize(lbl, 11);
        setTextColor(lbl, COLOR_TEXT_MAIN);

        JComboBox<String> combo = createComboBox(parent, items, 115, y, 214, 26);
        setFontSize(combo, 12);
        return combo;
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