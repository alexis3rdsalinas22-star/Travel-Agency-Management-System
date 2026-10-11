package ui.admin;

import common.DBConnection;
import common.Screen;
import common.Session;
import dao.AdminAccountDAO;
import model.AdminAccount;

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
        super("Admin Account Management", 960, 560);

        initUI();
        loadAccounts(null);
    }

    private void initUI() {
        createHeader("Admin Account Management", 40, 20, 320, 30);
        createLabel("Manage administrator credentials and roles.", 40, 55, 320, 20);

        // Form Fields
        createLabel("Admin ID:", 40, 95, 100, 25);
        idField = createTextField(150, 95, 200, 30);
        idField.setEditable(false);

        createLabel("Username *:", 40, 135, 100, 25);
        usernameField = createTextField(150, 135, 200, 30);

        createLabel("Password:", 40, 175, 100, 25);
        passwordField = createPasswordField(150, 175, 200, 30);

        JLabel passNote = createLabel("(Leave blank to keep unchanged)", 150, 205, 210, 18);
        setFontSize(passNote, 11);

        createLabel("Role *:", 40, 230, 100, 25);
        roleCombo = createComboBox(new String[]{"Admin", "Manager"}, 150, 230, 200, 30);

        // CRUD Action Buttons
        JButton saveBtn = createButton("Save", 40, 280, 95, 35);
        saveBtn.addActionListener(e -> handleSave());

        JButton updateBtn = createButton("Update", 145, 280, 95, 35);
        updateBtn.addActionListener(e -> handleUpdate());

        JButton deleteBtn = createButton("Delete", 250, 280, 100, 35);
        deleteBtn.addActionListener(e -> handleDelete());

        JButton clearBtn = createButton("Clear Form", 40, 325, 310, 35);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 375, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section
        createLabel("Search:", 380, 20, 60, 30);
        searchField = createTextField(445, 20, 230, 30);

        JButton searchBtn = createButton("Search", 685, 20, 100, 30);
        searchBtn.addActionListener(e -> handleSearch());

        JButton showAllBtn = createButton("Show All", 795, 20, 105, 30);
        showAllBtn.addActionListener(e -> {
            searchField.setText("");
            loadAccounts(null);
        });

        // Table
        String[] columnNames = {"Admin ID", "Username", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountsTable = new JTable(tableModel);
        accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(accountsTable);
        addComponent(scrollPane, 380, 60, 520, 430);
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