package ui.admin;

import common.DBConnection;
import common.Screen;
import common.Session;
import dao.AdminAccountDAO;
import model.AdminAccount;

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

public class AdminManagement extends Screen {

    private final AdminAccountDAO adminDAO = new AdminAccountDAO();

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
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    // Table and Accounts Cache
    private DefaultTableModel tableModel;
    private JTable accountsTable;
    private List<AdminAccount> currentAccounts = new ArrayList<>();

    public AdminManagement() {
        super("Admin Account Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);
        setFullscreen(true);

        initUI();
        loadAccounts(null);

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
            "ADMIN CONSOLE  •  SECURITY & ACCESS",
            "Admin Account Management",
            "Manage administrator credentials, security accounts, and role privileges.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Modern Card Container for Account Form
        formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "ADMIN CREDENTIALS");

        // Hidden ID field to preserve programmatic access
        idField = new JTextField();

        // Fields inside Card
        usernameField = addFormField(formCard, "Username *:", 42, true);

        // Password Field
        JLabel passLbl = createLabel(formCard, "Password:", 16, 80, 95, 28);
        setFontStyle(passLbl, Font.BOLD);
        setFontSize(passLbl, 11);

        passwordField = createPasswordField(formCard, 115, 80, 214, 28);

        JLabel passNote = createLabel(formCard, "(leave blank to keep unchanged)", 115, 110, 214, 14);
        setFontSize(passNote, 10);
        setTextColor(passNote, COLOR_TEXT_MUTED);

        // Role Combo
        JLabel roleLbl = createLabel(formCard, "Role *:", 16, 138, 95, 28);
        setFontStyle(roleLbl, Font.BOLD);
        setFontSize(roleLbl, 11);

        roleCombo = createComboBox(formCard, new String[]{"Admin", "Manager"}, 115, 138, 214, 28);

        // Action Buttons inside Card
        createSuccessButton(formCard, "Save", 16, 196, 98, 32, this::handleSave);
        createPrimaryButton(formCard, "Update", 122, 196, 98, 32, this::handleUpdate);
        createDangerButton(formCard, "Delete", 228, 196, 101, 32, this::handleDelete);
        createSecondaryButton(formCard, "Clear Form", 16, 238, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Select an admin from the table to modify credentials", 16, 282, 313, 16);
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
            loadAccounts(null);
        });
        setFontSize(showAllBtn, 12);

        tableModel = createReadOnlyTableModel(new String[]{"Username", "Role"});
        accountsTable = createTable(tableModel, 410, 132, 745, 511);
        accountsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setColumnWidths(accountsTable, 495, 250);
        setStatusBadgeRenderer(accountsTable, 1);

        if (accountsTable.getParent() != null && accountsTable.getParent().getParent() instanceof JScrollPane) {
            tableScrollPane = (JScrollPane) accountsTable.getParent().getParent();
        }

        accountsTable.getSelectionModel().addListSelectionListener(e -> {
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
            showInfo("Admin account '" + username + "' created successfully.");
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
            String targetRole = (row >= 0 && row < currentAccounts.size())
                    ? currentAccounts.get(row).getrole() : "";
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

        if (confirm("Are you sure you want to permanently delete admin account '" + username + "'?")) {
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