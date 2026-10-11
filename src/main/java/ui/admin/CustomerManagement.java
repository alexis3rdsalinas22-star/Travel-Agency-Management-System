package ui.admin;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class CustomerManagement extends Screen {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final CustomerAccountDAO accountDAO = new CustomerAccountDAO();

    // Form inputs
    private JTextField idField;
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JComboBox<String> genderCombo;
    private JTextField ageField;
    private JComboBox<String> statusCombo;
    private JPasswordField passwordField;

    // Search input
    private JTextField searchField;

    // Table
    private DefaultTableModel tableModel;
    private JTable customerTable;

    // Memory cache for currently displayed customer records
    private List<CustomerRecord> currentRecords = new ArrayList<>();

    public CustomerManagement() {
        super("Customer Management", 1040, 620);

        initUI();
        loadCustomers(null);
    }

    private void initUI() {
        // Header and subtitle
        createHeader("Customer Management", 40, 15, 340, 30);
        createLabel("View, search, and manage registered customers.", 40, 45, 340, 20);

        // Form Fields
        createLabel("Customer ID:", 40, 75, 110, 25);
        idField = createTextField(150, 75, 210, 30);
        idField.setEditable(false);

        createLabel("Username *:", 40, 110, 110, 25);
        usernameField = createTextField(150, 110, 210, 30);

        createLabel("First Name *:", 40, 145, 110, 25);
        firstNameField = createTextField(150, 145, 210, 30);

        createLabel("Middle Name:", 40, 180, 110, 25);
        middleNameField = createTextField(150, 180, 210, 30);

        createLabel("Last Name *:", 40, 215, 110, 25);
        lastNameField = createTextField(150, 215, 210, 30);

        createLabel("Email *:", 40, 250, 110, 25);
        emailField = createTextField(150, 250, 210, 30);

        createLabel("Gender *:", 40, 285, 110, 25);
        genderCombo = createComboBox(new String[]{"Male", "Female"}, 150, 285, 210, 30);

        createLabel("Age *:", 40, 320, 110, 25);
        ageField = createTextField(150, 320, 210, 30);

        createLabel("Status *:", 40, 355, 110, 25);
        statusCombo = createComboBox(new String[]{"Active", "Inactive", "Suspended"}, 150, 355, 210, 30);

        createLabel("Password:", 40, 390, 110, 25);
        passwordField = createPasswordField(150, 390, 210, 30);

        JLabel passNote = createLabel("(Leave blank to keep unchanged)", 150, 420, 210, 18);
        setFontSize(passNote, 11);

        // Action Buttons
        JButton saveBtn = createButton("Save", 40, 445, 100, 32);
        saveBtn.addActionListener(e -> handleSave());

        JButton updateBtn = createButton("Update", 150, 445, 100, 32);
        updateBtn.addActionListener(e -> handleUpdate());

        JButton deleteBtn = createButton("Delete", 260, 445, 100, 32);
        deleteBtn.addActionListener(e -> handleDelete());

        JButton clearBtn = createButton("Clear Form", 40, 485, 320, 32);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 525, 320, 32);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section
        searchField = createSearchBar(380, 18, 620, 30, this::handleSearch, () -> loadCustomers(null));

        // Table
        String[] columnNames = {"ID", "Username", "First Name", "Last Name", "Email", "Gender", "Age", "Status"};
        tableModel = createReadOnlyTableModel(columnNames);
        customerTable = createTable(tableModel, 380, 55, 620, 500);
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadCustomers(String keyword) {
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                currentRecords = customerDAO.getAllCustomers(con);
            } else {
                currentRecords = customerDAO.searchCustomer(con, keyword);
            }

            for (CustomerRecord c : currentRecords) {
                tableModel.addRow(new Object[]{
                    c.customerID,
                    c.username != null ? c.username : "(None)",
                    c.firstName,
                    c.lastName,
                    c.email,
                    c.sex != null ? c.sex : "",
                    c.age,
                    c.status != null ? c.status : "Active"
                });
            }
        } catch (SQLException ex) {
            showError("Database error loading customers: " + ex.getMessage());
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentRecords.size()) {
            CustomerRecord c = currentRecords.get(selectedRow);
            idField.setText(String.valueOf(c.customerID));
            usernameField.setText(c.username != null ? c.username : "");
            firstNameField.setText(c.firstName != null ? c.firstName : "");
            middleNameField.setText(c.middleName != null ? c.middleName : "");
            lastNameField.setText(c.lastName != null ? c.lastName : "");
            emailField.setText(c.email != null ? c.email : "");
            if (c.sex != null) {
                genderCombo.setSelectedItem(c.sex);
            }
            ageField.setText(String.valueOf(c.age));
            if (c.status != null) {
                statusCombo.setSelectedItem(c.status);
            }
            passwordField.setText("");
        }
    }

    private void handleSave() {
        String username = usernameField.getText().trim();
        String first = firstNameField.getText().trim();
        String middle = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String sex = (String) genderCombo.getSelectedItem();
        String ageStr = ageField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || first.isEmpty() || last.isEmpty() || email.isEmpty() || ageStr.isEmpty()) {
            showError("Please fill in all required fields (*).");
            return;
        }

        if (password.isEmpty()) {
            showError("Please specify a password when creating a new customer account.");
            return;
        }

        Integer ageValue = common.ValidationUtil.parsePositiveInt(ageStr);
        if (ageValue == null || ageValue > 130) {
            showError("Please enter a valid age.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (accountDAO.usernameExists(con, username)) {
                    showError("Username '" + username + "' is already taken. Please choose another.");
                    return;
                }

                int customerId = customerDAO.insertCustomer(
                        con, first, middle.isEmpty() ? null : middle, last, email, sex, ageValue);

                accountDAO.insertAccount(con, customerId, username, password);

                if (!"Active".equals(status)) {
                    // Update status if saved as Inactive or Suspended
                    CustomerRecord rec = customerDAO.getCustomerById(con, customerId);
                    if (rec != null && rec.customerAccountID > 0) {
                        accountDAO.updateStatus(con, rec.customerAccountID, status);
                    }
                }

                con.commit();
                showInfo("Customer '" + first + " " + last + "' created successfully (ID: " + customerId + ").");
                clearForm();
                loadCustomers(null);
            } catch (SQLException ex) {
                con.rollback();
                showError("Failed to create customer: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            showError("Database connection error: " + ex.getMessage());
        }
    }

    private void handleUpdate() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please select a customer from the table to update.");
            return;
        }

        int customerId = Integer.parseInt(idText);
        String username = usernameField.getText().trim();
        String first = firstNameField.getText().trim();
        String middle = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String sex = (String) genderCombo.getSelectedItem();
        String ageStr = ageField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        String password = new String(passwordField.getPassword());

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || ageStr.isEmpty()) {
            showError("Please fill in all required customer details (*).");
            return;
        }

        Integer ageValue = common.ValidationUtil.parsePositiveInt(ageStr);
        if (ageValue == null || ageValue > 130) {
            showError("Please enter a valid age.");
            return;
        }

        int selectedRow = customerTable.getSelectedRow();
        CustomerRecord existing = (selectedRow >= 0 && selectedRow < currentRecords.size())
                ? currentRecords.get(selectedRow) : null;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                customerDAO.updateCustomer(con, customerId, first, middle.isEmpty() ? null : middle, last, email, sex, ageValue);

                if (existing != null && existing.customerAccountID > 0) {
                    if (!username.isEmpty() && !username.equals(existing.username)) {
                        boolean edited = accountDAO.editUsername(con, existing.customerAccountID, username);
                        if (!edited) {
                            showError("Username '" + username + "' is already in use by another account.");
                            con.rollback();
                            return;
                        }
                    }

                    accountDAO.updateStatus(con, existing.customerAccountID, status);

                    if (!password.isEmpty()) {
                        accountDAO.resetPassword(con, existing.customerAccountID, password);
                    }
                } else if (!username.isEmpty() && !password.isEmpty()) {
                    accountDAO.insertAccount(con, customerId, username, password);
                    if (!"Active".equals(status)) {
                        CustomerRecord rec = customerDAO.getCustomerById(con, customerId);
                        if (rec != null && rec.customerAccountID > 0) {
                            accountDAO.updateStatus(con, rec.customerAccountID, status);
                        }
                    }
                }

                con.commit();
                showInfo("Customer information updated successfully.");
                clearForm();
                loadCustomers(null);
            } catch (SQLException ex) {
                con.rollback();
                showError("Failed to update customer: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            showError("Database connection error: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please select a customer from the table to delete.");
            return;
        }

        int customerId = Integer.parseInt(idText);
        String name = firstNameField.getText().trim() + " " + lastNameField.getText().trim();

        if (confirm("Are you sure you want to permanently delete customer '" + name + "' (ID: " + customerId + ")?\nThis will also remove their login account and bookings.")) {
            try (Connection con = DBConnection.getConnection()) {
                boolean deleted = customerDAO.deleteCustomer(con, customerId);
                if (deleted) {
                    showInfo("Customer '" + name + "' was deleted successfully.");
                    clearForm();
                    loadCustomers(null);
                } else {
                    showError("Could not delete customer. Record not found.");
                }
            } catch (SQLException ex) {
                showError("Failed to delete customer: " + ex.getMessage());
            }
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadCustomers(null);
            return;
        }
        loadCustomers(keyword);
    }

    private void clearForm() {
        idField.setText("");
        usernameField.setText("");
        firstNameField.setText("");
        middleNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        genderCombo.setSelectedIndex(0);
        ageField.setText("");
        statusCombo.setSelectedIndex(0);
        passwordField.setText("");
        customerTable.clearSelection();
    }
}