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

    // Table
    private DefaultTableModel tableModel;
    private JTable accountsTable;

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

        // Fields inside Card
        idField = addFormField(formCard, "Admin ID:", 45, false);
        usernameField = addFormField(formCard, "Username *:", 90, true);

        // Password Field
        JLabel passLbl = createLabel(formCard, "Password:", 16, 135, 95, 26);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 11);

        passwordField = createPasswordField(formCard, 115, 135, 214, 26);

        JLabel passNote = createLabel(formCard, "(leave blank to keep unchanged)", 115, 163, 214, 14);
        setFontSize(passNote, 10);
        setTextColor(passNote, COLOR_TEXT_MUTED);

        // Role Combo
        JLabel roleLbl = createLabel(formCard, "Role *:", 16, 190, 95, 26);
        setFontStyle(roleLbl, Font.BOLD);
        setFontSize(roleLbl, 11);

        roleCombo = createComboBox(formCard, new String[]{"Admin", "Manager"}, 115, 190, 214, 26);

        // Action Buttons inside Card
        createSuccessButton(formCard, "Save", 16, 250, 98, 32, this::handleSave);
        createPrimaryButton(formCard, "Update", 122, 250, 98, 32, this::handleUpdate);
        createDangerButton(formCard, "Delete", 228, 250, 101, 32, this::handleDelete);
        createSecondaryButton(formCard, "Clear Form", 16, 292, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Select an admin from the table to modify credentials", 16, 520, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Search Bar & Table
        searchField = createSearchBar(410, 88, 745, 34, this::handleSearch, () -> loadAccounts(null));
        searchField.addActionListener(e -> handleSearch());

        tableModel = createReadOnlyTableModel(new String[]{"Admin ID", "Username", "Role"});
        accountsTable = createTable(tableModel, 410, 132, 745, 511);
        setColumnWidths(accountsTable, 120, 390, 225);

        accountsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadAccounts(String keyword) {
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            List<AdminAccount> accounts;
            if (keyword == null || keyword.trim().isEmpty()) {
                accounts = adminDAO.getAllAccounts(con);
            } else {
                accounts = adminDAO.searchAccounts(con, keyword);
            }

            for (AdminAccount a : accounts) {
                tableModel.addRow(new Object[]{
                    a.getadminAccountId(),
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
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            usernameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            passwordField.setText("");
            roleCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
        }
    }

    private void handleSave() {
        String username = getTrimmedText(usernameField);
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please provide both username and password to create a new account.");
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