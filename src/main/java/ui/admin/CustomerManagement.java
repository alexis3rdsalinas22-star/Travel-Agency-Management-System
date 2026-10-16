package ui.admin;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;

import java.awt.Color;
import java.awt.Font;
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
        super("Customer Management", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
        loadCustomers(null);
    }

    private void initUI() {
        // Header and subtitle
        JLabel header = createHeader("Customer Management", 40, 15, 320, 28);
        setFontSize(header, 22);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("View, search, and manage registered customers.", 40, 44, 320, 18);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form Fields
        JLabel idLbl = createLabel("Customer ID:", 40, 68, 105, 26);
        setFontStyle(idLbl, Font.BOLD);
        setFontSize(idLbl, 13);
        setTextColor(idLbl, new Color(55, 65, 81));
        idField = createTextField(150, 68, 200, 28);
        idField.setEditable(false);

        JLabel userLbl = createLabel("Username *:", 40, 100, 105, 26);
        setFontStyle(userLbl, Font.BOLD);
        setFontSize(userLbl, 13);
        setTextColor(userLbl, new Color(55, 65, 81));
        usernameField = createTextField(150, 100, 200, 28);

        JLabel firstLbl = createLabel("First Name *:", 40, 132, 105, 26);
        setFontStyle(firstLbl, Font.BOLD);
        setFontSize(firstLbl, 13);
        setTextColor(firstLbl, new Color(55, 65, 81));
        firstNameField = createTextField(150, 132, 200, 28);

        JLabel midLbl = createLabel("Middle Name:", 40, 164, 105, 26);
        setFontSize(midLbl, 13);
        setTextColor(midLbl, new Color(55, 65, 81));
        middleNameField = createTextField(150, 164, 200, 28);

        JLabel lastLbl = createLabel("Last Name *:", 40, 196, 105, 26);
        setFontStyle(lastLbl, Font.BOLD);
        setFontSize(lastLbl, 13);
        setTextColor(lastLbl, new Color(55, 65, 81));
        lastNameField = createTextField(150, 196, 200, 28);

        JLabel emailLbl = createLabel("Email *:", 40, 228, 105, 26);
        setFontStyle(emailLbl, Font.BOLD);
        setFontSize(emailLbl, 13);
        setTextColor(emailLbl, new Color(55, 65, 81));
        emailField = createTextField(150, 228, 200, 28);

        JLabel genderLbl = createLabel("Gender *:", 40, 260, 105, 26);
        setFontStyle(genderLbl, Font.BOLD);
        setFontSize(genderLbl, 13);
        setTextColor(genderLbl, new Color(55, 65, 81));
        genderCombo = createComboBox(new String[]{"Male", "Female"}, 150, 260, 200, 28);

        JLabel ageLbl = createLabel("Age *:", 40, 292, 105, 26);
        setFontStyle(ageLbl, Font.BOLD);
        setFontSize(ageLbl, 13);
        setTextColor(ageLbl, new Color(55, 65, 81));
        ageField = createTextField(150, 292, 200, 28);

        JLabel statusLbl = createLabel("Status *:", 40, 324, 105, 26);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 13);
        setTextColor(statusLbl, new Color(55, 65, 81));
        statusCombo = createComboBox(new String[]{"Active", "Inactive", "Suspended"}, 150, 324, 200, 28);

        JLabel passLbl = createLabel("Password:", 40, 356, 105, 26);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 13);
        setTextColor(passLbl, new Color(55, 65, 81));
        passwordField = createPasswordField(150, 356, 200, 28);

        JLabel passNote = createLabel("(Leave blank to keep unchanged)", 150, 384, 200, 16);
        setFontSize(passNote, 11);
        setTextColor(passNote, new Color(100, 116, 139));

        // Action Buttons
        JButton saveBtn = createButton("Save", 40, 408, 98, 32);
        setFontStyle(saveBtn, Font.BOLD);
        setBackground(saveBtn, new Color(16, 149, 93));
        setTextColor(saveBtn, Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> handleSave());

        JButton updateBtn = createButton("Update", 146, 408, 98, 32);
        setFontStyle(updateBtn, Font.BOLD);
        setBackground(updateBtn, new Color(37, 99, 235));
        setTextColor(updateBtn, Color.WHITE);
        updateBtn.setFocusPainted(false);
        updateBtn.addActionListener(e -> handleUpdate());

        JButton deleteBtn = createButton("Delete", 252, 408, 98, 32);
        setFontStyle(deleteBtn, Font.BOLD);
        setBackground(deleteBtn, new Color(220, 53, 69));
        setTextColor(deleteBtn, Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> handleDelete());

        JButton clearBtn = createButton("Clear Form", 40, 448, 310, 32);
        setFontSize(clearBtn, 13);
        setBackground(clearBtn, new Color(229, 231, 235));
        setTextColor(clearBtn, new Color(55, 65, 81));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 488, 310, 32);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(220, 225, 235));
        setTextColor(backBtn, new Color(51, 65, 85));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section
        searchField = createSearchBar(370, 18, 590, 30, this::handleSearch, () -> loadCustomers(null));
        searchField.addActionListener(e -> handleSearch());

        // Table
        String[] columnNames = {"ID", "Username", "First Name", "Last Name", "Email", "Gender", "Age", "Status"};
        tableModel = createReadOnlyTableModel(columnNames);
        customerTable = createTable(tableModel, 370, 52, 590, 468);
        customerTable.setRowHeight(24);
        if (customerTable.getColumnModel().getColumnCount() >= 8) {
            customerTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            customerTable.getColumnModel().getColumn(0).setMaxWidth(55);
            customerTable.getColumnModel().getColumn(1).setPreferredWidth(80);
            customerTable.getColumnModel().getColumn(2).setPreferredWidth(75);
            customerTable.getColumnModel().getColumn(3).setPreferredWidth(75);
            customerTable.getColumnModel().getColumn(4).setPreferredWidth(140);
            customerTable.getColumnModel().getColumn(5).setPreferredWidth(65);
            customerTable.getColumnModel().getColumn(6).setPreferredWidth(45);
            customerTable.getColumnModel().getColumn(7).setPreferredWidth(70);
        }
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
                if (customerDAO.emailExists(con, email)) {
                    showError("The email address '" + email + "' is already in use by another customer.");
                    return;
                }

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

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                CustomerRecord existing = customerDAO.getCustomerById(con, customerId);
                if (existing == null) {
                    showError("Customer with ID " + customerId + " not found.");
                    con.rollback();
                    return;
                }

                if (customerDAO.emailExistsForOther(con, email, customerId)) {
                    showError("The email address '" + email + "' is already in use by another customer.");
                    con.rollback();
                    return;
                }

                customerDAO.updateCustomer(con, customerId, first, middle.isEmpty() ? null : middle, last, email, sex, ageValue);

                if (existing.customerAccountID > 0) {
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
                } else if (!username.isEmpty()) {
                    if (password.isEmpty()) {
                        showError("Please specify a password when creating a login account for this customer.");
                        con.rollback();
                        return;
                    }
                    if (accountDAO.usernameExists(con, username)) {
                        showError("Username '" + username + "' is already taken. Please choose another.");
                        con.rollback();
                        return;
                    }

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