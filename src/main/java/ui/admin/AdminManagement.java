package ui.admin;

import common.DBConnection;
import common.Screen;
import common.Session;
import dao.AdminAccountDAO;
import model.AdminAccount;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class AdminManagement extends Screen {

    private final AdminAccountDAO adminDAO = new AdminAccountDAO();

    // Form inputs
    private JTextField idField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    // Search input
    private JTextField searchField;

    // Table and Accounts Cache
    private DefaultTableModel tableModel;
    private JTable accountsTable;
    private List<AdminAccount> currentAccounts = new java.util.ArrayList<>();

    public AdminManagement() {
        super("Admin Account Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);

        initUI();
        loadAccounts(null);
    }

    private void initUI() {
        // Top Header Bar
        createTopHeader(
            "ADMIN CONSOLE  •  SECURITY & ACCESS",
            "Admin Account Management",
            "Manage administrator credentials, security accounts, and role privileges.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Modern Card Container for Account Form
        JPanel formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "ADMIN CREDENTIALS");

        // Hidden ID field to preserve programmatic access
        idField = new JTextField();

        // Fields inside Card
        usernameField = addFormField(formCard, "Username *:", 45, true);

        // Password Field
        JLabel passLbl = createLabel(formCard, "Password:", 16, 90, 95, 26);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 11);

        passwordField = createPasswordField(formCard, 115, 90, 214, 26);

        JLabel passNote = createLabel(formCard, "(leave blank to keep unchanged)", 115, 118, 214, 14);
        setFontSize(passNote, 10);
        setTextColor(passNote, COLOR_TEXT_MUTED);

        // Role Combo
        JLabel roleLbl = createLabel(formCard, "Role *:", 16, 145, 95, 26);
        setFontStyle(roleLbl, Font.BOLD);
        setFontSize(roleLbl, 11);

        roleCombo = createComboBox(formCard, new String[]{"Admin", "Manager"}, 115, 145, 214, 26);

        // Action Buttons inside Card
        createSuccessButton(formCard, "Save", 16, 205, 98, 32, this::handleSave);
        createPrimaryButton(formCard, "Update", 122, 205, 98, 32, this::handleUpdate);
        createDangerButton(formCard, "Delete", 228, 205, 101, 32, this::handleDelete);
        createSecondaryButton(formCard, "Clear Form", 16, 247, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Select an admin from the table to modify credentials", 16, 290, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Search Bar & Table
        searchField = createSearchBar(410, 88, 745, 34, this::handleSearch, () -> loadAccounts(null));
        searchField.addActionListener(e -> handleSearch());

        tableModel = createReadOnlyTableModel(new String[]{"Username", "Role"});
        accountsTable = createTable(tableModel, 410, 132, 745, 511);
        setColumnWidths(accountsTable, 495, 250);

        accountsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadAccounts(String keyword) {
        tableModel.setRowCount(0);
        currentAccounts.clear();
        try (Connection con = DBConnection.getConnection()) {
            List<AdminAccount> accounts;
            if (keyword == null || keyword.trim().isEmpty()) {
                accounts = adminDAO.getAllAccounts(con);
            } else {
                accounts = adminDAO.searchAccounts(con, keyword);
            }
            currentAccounts = accounts;

            for (AdminAccount a : accounts) {
                tableModel.addRow(new Object[]{
                    a.getusername(),
                    a.getrole()
                });
            }
        } catch (SQLException ex) {
            showError("Database error loading accounts: " + ex.getMessage());
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentAccounts.size()) {
            AdminAccount a = currentAccounts.get(selectedRow);
            idField.setText(String.valueOf(a.getadminAccountId()));
            usernameField.setText(a.getusername());
            passwordField.setText("");
            roleCombo.setSelectedItem(a.getrole());
        }
    }

    private boolean isSuperAdmin() {
        return "Admin".equalsIgnoreCase(Session.getUserRole());
    }

    private void handleSave() {
        String username = getTrimmedText(usernameField);
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please provide both username and password to create a new account.");
            return;
        }

        if (!isSuperAdmin() && "Admin".equalsIgnoreCase(role)) {
            showError("Access Denied: Managers can only create accounts with the Manager role.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            int newId = adminDAO.createAccount(con, username, password, role);
            if (newId == -1) {
                showError("Username '" + username + "' is already taken. Please choose another.");
                return;
            }
            showInfo("Admin account '" + username + "' created successfully (ID: " + newId + ").");
            clearForm();
            loadAccounts(null);
        } catch (SQLException ex) {
            showError("Failed to save admin account: " + ex.getMessage());
        }
    }

    private void handleUpdate() {
        Integer adminId = getIntOrNull(idField);
        if (adminId == null) {
            showError("Please select an admin account from the table to update.");
            return;
        }

        String username = getTrimmedText(usernameField);
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }

        if (!isSuperAdmin()) {
            int row = accountsTable.getSelectedRow();
            String targetRole = (row >= 0 && row < tableModel.getRowCount())
                    ? tableModel.getValueAt(row, 2).toString() : "";
            if ("Admin".equalsIgnoreCase(targetRole)) {
                showError("Access Denied: Managers cannot modify Administrator accounts.");
                return;
            }
            if ("Admin".equalsIgnoreCase(role)) {
                showError("Access Denied: Managers cannot promote accounts to Administrator.");
                return;
            }
        }

        try (Connection con = DBConnection.getConnection()) {
            boolean updated = adminDAO.editAccount(con, adminId, username, password, role);
            if (!updated) {
                showError("Update failed. The username may already be in use by another admin.");
                return;
            }
            showInfo("Admin account updated successfully.");
            clearForm();
            loadAccounts(null);
        } catch (SQLException ex) {
            showError("Failed to update admin account: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        Integer adminId = getIntOrNull(idField);
        if (adminId == null) {
            showError("Please select an admin account from the table to delete.");
            return;
        }

        if (!isSuperAdmin()) {
            showError("Access Denied: Only system Administrators have permission to delete accounts.");
            return;
        }

        String username = getTrimmedText(usernameField);

        // Prevent self-deletion if logged in
        if (Session.getCurrentAdmin() != null && Session.getCurrentAdmin().getadminAccountId() == adminId) {
            showError("You cannot delete your own currently logged-in account!");
            return;
        }

        if (confirm("Are you sure you want to permanently delete admin account '" + username + "' (ID: " + adminId + ")?")) {
            try (Connection con = DBConnection.getConnection()) {
                boolean deleted = adminDAO.deleteAccount(con, adminId);
                if (deleted) {
                    showInfo("Admin account '" + username + "' was deleted successfully.");
                    clearForm();
                    loadAccounts(null);
                } else {
                    showError("Could not delete account. Account not found.");
                }
            } catch (SQLException ex) {
                showError("Failed to delete admin account: " + ex.getMessage());
            }
        }
    }

    private void handleSearch() {
        String keyword = getTrimmedText(searchField);
        if (keyword.isEmpty()) {
            loadAccounts(null);
            return;
        }
        loadAccounts(keyword);
    }

    private void clearForm() {
        idField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedIndex(0);
        accountsTable.clearSelection();
    }
}