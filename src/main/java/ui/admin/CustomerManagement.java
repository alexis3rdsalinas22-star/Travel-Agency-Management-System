package ui.admin;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class CustomerManagement extends Screen {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final CustomerAccountDAO accountDAO = new CustomerAccountDAO();

    // UI Structure
    private JButton headerBackBtn;
    private JPanel formCard;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchBtn;
    private JButton showAllBtn;
    private JScrollPane tableScrollPane;

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

    // Table
    private DefaultTableModel tableModel;
    private JTable customerTable;

    // Memory cache for currently displayed customer records
    private List<CustomerRecord> currentRecords = new ArrayList<>();

    public CustomerManagement() {
        super("Customer Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);
        setFullscreen(true);

        initUI();
        loadCustomers(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    private void initUI() {
        // Top Header Bar
        headerBackBtn = createTopHeader(
            "ADMIN CONSOLE  •  CUSTOMER DIRECTORY",
            "Customer Management",
            "Register, edit, search, and manage registered customer profiles.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Modern Card Container for Customer Form
        formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "CUSTOMER PROFILE");

        // Hidden ID Field to preserve programmatic access without cluttering the UI
        idField = new JTextField();

        // Fields inside Card
        usernameField = addFormField(formCard, "Username *:", 38, true);
        firstNameField = addFormField(formCard, "First Name *:", 72, true);
        middleNameField = addFormField(formCard, "Middle Name:", 106, true);
        lastNameField = addFormField(formCard, "Last Name *:", 140, true);
        emailField = addFormField(formCard, "Email *:", 174, true);

        // Gender Combo
        JLabel genderLbl = createLabel(formCard, "Gender *:", 16, 208, 95, 28);
        setFontStyle(genderLbl, Font.BOLD);
        setFontSize(genderLbl, 11);
        genderCombo = createComboBox(formCard, new String[]{"Male", "Female"}, 115, 208, 214, 28);

        ageField = addFormField(formCard, "Age *:", 242, true);

        // Status Combo
        JLabel statusLbl = createLabel(formCard, "Status *:", 16, 276, 95, 28);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 11);
        statusCombo = createComboBox(formCard, new String[]{"Active", "Inactive", "Suspended"}, 115, 276, 214, 28);

        // Password Field
        JLabel passLbl = createLabel(formCard, "Password:", 16, 310, 95, 28);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 11);

        passwordField = createPasswordField(formCard, 115, 310, 214, 28);

        JLabel passNote = createLabel(formCard, "(leave blank to keep unchanged)", 115, 338, 214, 14);
        setFontSize(passNote, 10);
        setTextColor(passNote, COLOR_TEXT_MUTED);

        // Action Buttons inside Card
        createSuccessButton(formCard, "Save", 16, 380, 98, 32, this::handleSave);
        createPrimaryButton(formCard, "Update", 122, 380, 98, 32, this::handleUpdate);
        createDangerButton(formCard, "Delete", 228, 380, 101, 32, this::handleDelete);
        createSecondaryButton(formCard, "Clear Form", 16, 422, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Select a customer in the table to edit or delete", 16, 470, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Search Bar & Table
        searchLabel = createLabel("Search:", 410, 88, 55, 34);
        setFontStyle(searchLabel, Font.BOLD);
        setFontSize(searchLabel, 11);
        setTextColor(searchLabel, new Color(71, 85, 105));

        searchField = createTextField(470, 88, 480, 34);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBackground(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));
        searchField.addActionListener(e -> handleSearch());

        searchBtn = createPrimaryButton(this, "Search", 960, 88, 85, 34, this::handleSearch);
        setFontSize(searchBtn, 12);

        showAllBtn = createSecondaryButton(this, "Show All", 1055, 88, 95, 34, () -> {
            searchField.setText("");
            loadCustomers(null);
        });
        setFontSize(showAllBtn, 12);

        String[] columnNames = {"Username", "First Name", "Last Name", "Email", "Gender", "Age", "Status"};
        tableModel = createReadOnlyTableModel(columnNames);
        customerTable = createTable(tableModel, 410, 132, 745, 511);
        customerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setColumnWidths(customerTable, 120, 110, 110, 195, 65, 55, 90);
        setStatusBadgeRenderer(customerTable, 6);

        if (customerTable.getParent() != null && customerTable.getParent().getParent() instanceof JScrollPane) {
            tableScrollPane = (JScrollPane) customerTable.getParent().getParent();
        }

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void relayout() {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        if (headerBackBtn != null) {
            headerBackBtn.setBounds(w - pad - 135, 18, 135, 36);
        }

        int formW = 345;
        int formH = Math.max(555, h - 88 - 35);
        if (formCard != null) {
            formCard.setBounds(pad, 88, formW, formH);
        }

        int rightX = pad + formW + 20;
        int rightW = w - pad - rightX;
        if (rightW < 300) rightW = 300;

        int searchBtnW = 85;
        int showAllW = 95;
        int gap = 8;
        int buttonsTotal = searchBtnW + gap + showAllW;
        int searchLblW = 55;

        if (searchLabel != null) searchLabel.setBounds(rightX, 88, searchLblW, 34);
        if (searchField != null) searchField.setBounds(rightX + searchLblW + 5, 88, rightW - searchLblW - 5 - buttonsTotal - gap, 34);
        if (searchBtn != null) searchBtn.setBounds(rightX + rightW - buttonsTotal, 88, searchBtnW, 34);
        if (showAllBtn != null) showAllBtn.setBounds(rightX + rightW - showAllW, 88, showAllW, 34);

        int tableH = h - 132 - 35;
        if (tableH < 300) tableH = 300;
        if (tableScrollPane != null) {
            tableScrollPane.setBounds(rightX, 132, rightW, tableH);
        }
        repaint();
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
                    c.username != null ? c.username : "(no account)",
                    c.firstName != null ? c.firstName : "",
                    c.lastName != null ? c.lastName : "",
                    c.email != null ? c.email : "",
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
            genderCombo.setSelectedItem(c.sex != null ? c.sex : "Male");
            ageField.setText(String.valueOf(c.age));
            statusCombo.setSelectedItem(c.status != null ? c.status : "Active");
            passwordField.setText("");
        }
    }

    private void handleSave() {
        String username = getTrimmedText(usernameField);
        String first = getTrimmedText(firstNameField);
        String middle = getTrimmedText(middleNameField);
        String last = getTrimmedText(lastNameField);
        String email = getTrimmedText(emailField);
        String sex = (String) genderCombo.getSelectedItem();
        Integer ageValue = getIntOrNull(ageField);
        String status = (String) statusCombo.getSelectedItem();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || first.isEmpty() || last.isEmpty() || email.isEmpty() || ageValue == null) {
            showError("Please fill in all required customer fields (*).");
            return;
        }

        if (password.isEmpty()) {
            showError("Please specify a password when creating a new customer account.");
            return;
        }

        if (ageValue <= 0 || ageValue > 130) {
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
                    CustomerRecord rec = customerDAO.getCustomerById(con, customerId);
                    if (rec != null && rec.customerAccountID > 0) {
                        accountDAO.updateStatus(con, rec.customerAccountID, status);
                    }
                }

                con.commit();
                showInfo("Customer '" + first + " " + last + "' registered successfully.");
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
        Integer customerId = getIntOrNull(idField);
        if (customerId == null) {
            showError("Please select a customer from the table to update.");
            return;
        }

        String username = getTrimmedText(usernameField);
        String first = getTrimmedText(firstNameField);
        String middle = getTrimmedText(middleNameField);
        String last = getTrimmedText(lastNameField);
        String email = getTrimmedText(emailField);
        String sex = (String) genderCombo.getSelectedItem();
        Integer ageValue = getIntOrNull(ageField);
        String status = (String) statusCombo.getSelectedItem();
        String password = new String(passwordField.getPassword());

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || ageValue == null) {
            showError("Please fill in all required customer details (*).");
            return;
        }

        if (ageValue <= 0 || ageValue > 130) {
            showError("Please enter a valid age.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                CustomerRecord existing = customerDAO.getCustomerById(con, customerId);
                if (existing == null) {
                    showError("Selected customer record not found.");
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
        Integer customerId = getIntOrNull(idField);
        if (customerId == null) {
            showError("Please select a customer from the table to delete.");
            return;
        }

        String name = getTrimmedText(firstNameField) + " " + getTrimmedText(lastNameField);

        if (confirm("Are you sure you want to permanently delete customer '" + name + "'?\nThis will also remove their login account and bookings.")) {
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
        String keyword = getTrimmedText(searchField);
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