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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
        super("Admin Account Management", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
        loadAccounts(null);
    }

    private void initUI() {
        JLabel header = createHeader("Admin Account Management", 40, 20, 320, 30);
        setFontSize(header, 22);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Manage administrator credentials and roles.", 40, 52, 320, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form Fields
        JLabel idLbl = createLabel("Admin ID:", 40, 95, 100, 25);
        setFontStyle(idLbl, Font.BOLD);
        setFontSize(idLbl, 13);
        setTextColor(idLbl, new Color(55, 65, 81));
        idField = createTextField(150, 95, 200, 30);
        idField.setEditable(false);

        JLabel userLbl = createLabel("Username *:", 40, 135, 100, 25);
        setFontStyle(userLbl, Font.BOLD);
        setFontSize(userLbl, 13);
        setTextColor(userLbl, new Color(55, 65, 81));
        usernameField = createTextField(150, 135, 200, 30);

        JLabel passLbl = createLabel("Password:", 40, 175, 100, 25);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 13);
        setTextColor(passLbl, new Color(55, 65, 81));
        passwordField = createPasswordField(150, 175, 200, 30);

        JLabel passNote = createLabel("(Leave blank to keep unchanged)", 150, 205, 210, 18);
        setFontSize(passNote, 11);
        setTextColor(passNote, new Color(100, 116, 139));

        JLabel roleLbl = createLabel("Role *:", 40, 230, 100, 25);
        setFontStyle(roleLbl, Font.BOLD);
        setFontSize(roleLbl, 13);
        setTextColor(roleLbl, new Color(55, 65, 81));
        roleCombo = createComboBox(new String[]{"Admin", "Manager"}, 150, 230, 200, 30);

        // CRUD Action Buttons
        JButton saveBtn = createButton("Save", 40, 280, 95, 35);
        setFontStyle(saveBtn, Font.BOLD);
        setBackground(saveBtn, new Color(16, 149, 93));
        setTextColor(saveBtn, Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> handleSave());

        JButton updateBtn = createButton("Update", 145, 280, 95, 35);
        setFontStyle(updateBtn, Font.BOLD);
        setBackground(updateBtn, new Color(37, 99, 235));
        setTextColor(updateBtn, Color.WHITE);
        updateBtn.setFocusPainted(false);
        updateBtn.addActionListener(e -> handleUpdate());

        JButton deleteBtn = createButton("Delete", 250, 280, 100, 35);
        setFontStyle(deleteBtn, Font.BOLD);
        setBackground(deleteBtn, new Color(220, 53, 69));
        setTextColor(deleteBtn, Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> handleDelete());

        JButton clearBtn = createButton("Clear Form", 40, 325, 310, 35);
        setFontSize(clearBtn, 13);
        setBackground(clearBtn, new Color(229, 231, 235));
        setTextColor(clearBtn, new Color(55, 65, 81));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 375, 310, 35);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(220, 225, 235));
        setTextColor(backBtn, new Color(51, 65, 85));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section
        searchField = createSearchBar(370, 18, 590, 30, this::handleSearch, () -> loadAccounts(null));
        searchField.addActionListener(e -> handleSearch());

        // Table
        tableModel = createReadOnlyTableModel(new String[]{"Admin ID", "Username", "Role"});
        accountsTable = createTable(tableModel, 370, 52, 590, 480);
        accountsTable.setRowHeight(24);
        if (accountsTable.getColumnModel().getColumnCount() >= 3) {
            accountsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            accountsTable.getColumnModel().getColumn(0).setMaxWidth(120);
            accountsTable.getColumnModel().getColumn(1).setPreferredWidth(310);
            accountsTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        }
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
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            usernameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            passwordField.setText("");
            roleCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
        }
    }

    private void handleSave() {
        String username = usernameField.getText().trim();
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
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please select an admin account from the table to update.");
            return;
        }

        int adminId = Integer.parseInt(idText);
        String username = usernameField.getText().trim();
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
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please select an admin account from the table to delete.");
            return;
        }

        int adminId = Integer.parseInt(idText);
        String username = usernameField.getText().trim();

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
        String keyword = searchField.getText().trim();
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