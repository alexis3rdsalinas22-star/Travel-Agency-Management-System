package ui.admin;

import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class DestinationManagement extends Screen {

    private final DestinationDAO destinationDAO = new DestinationDAO();

    // Form fields
    private JTextField idField;
    private JTextField nameField;
    private JTextField countryField;
    private JComboBox<String> statusCombo;
    private JTextArea descArea;

    // Search and Table
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable destTable;

    public DestinationManagement() {
        super("Destination Management", 960, 560);

        initUI();
        loadDestinations(null);
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("Destination Management", 40, 20, 320, 30);
        createLabel("Add, update, or remove travel destinations.", 40, 55, 320, 20);

        // Form fields
        createLabel("Destination ID:", 40, 95, 100, 25);
        idField = createTextField(150, 95, 200, 30);
        idField.setEditable(false);

        createLabel("Name *:", 40, 135, 100, 25);
        nameField = createTextField(150, 135, 200, 30);

        createLabel("Country *:", 40, 175, 100, 25);
        countryField = createTextField(150, 175, 200, 30);

        createLabel("Status *:", 40, 215, 100, 25);
        statusCombo = createComboBox(new String[]{"Active", "Inactive"}, 150, 215, 200, 30);

        createLabel("Description:", 40, 255, 100, 25);
        descArea = createTextArea(150, 255, 200, 65);

        // Action Buttons
        JButton saveBtn = createButton("Save", 40, 335, 95, 35);
        saveBtn.addActionListener(e -> handleSave());

        JButton updateBtn = createButton("Update", 145, 335, 95, 35);
        updateBtn.addActionListener(e -> handleUpdate());

        JButton deleteBtn = createButton("Delete", 250, 335, 100, 35);
        deleteBtn.addActionListener(e -> handleDelete());

        JButton clearBtn = createButton("Clear Form", 40, 380, 310, 35);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 425, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section using Screen helper
        searchField = createSearchBar(380, 20, 530, 30, this::handleSearch, () -> loadDestinations(null));

        // Read-only Table using Screen helpers
        String[] columns = {"ID", "Destination Name", "Country", "Description", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        destTable = createTable(tableModel, 380, 60, 530, 440);
        destTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadDestinations(String keyword) {
        tableModel.setRowCount(0);
        try {
            List<DestinationRecord> list;
            if (keyword == null || keyword.trim().isEmpty()) {
                list = destinationDAO.getAllDestinations();
            } else {
                list = destinationDAO.search(keyword);
            }

            for (DestinationRecord d : list) {
                tableModel.addRow(new Object[]{
                    d.destinationID,
                    d.name,
                    d.country,
                    d.description != null ? d.description : "",
                    d.status
                });
            }
        } catch (SQLException ex) {
            showError("Database error loading destinations: " + ex.getMessage());
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = destTable.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            countryField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            descArea.setText(tableModel.getValueAt(selectedRow, 3).toString());
            statusCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());
        }
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String country = countryField.getText().trim();
        String desc = descArea.getText().trim();

        if (name.isEmpty() || country.isEmpty()) {
            showError("Please enter both destination name and country.");
            return;
        }

        try {
            int newId = destinationDAO.addDestination(name, country, desc.isEmpty() ? null : desc);
            showInfo("Destination '" + name + "' added successfully (ID: " + newId + ").");
            clearForm();
            loadDestinations(null);
        } catch (SQLException ex) {
            showError("Failed to add destination: " + ex.getMessage());
        }
    }

    private void handleUpdate() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please select a destination from the table to update.");
            return;
        }

        int id = Integer.parseInt(idText);
        String name = nameField.getText().trim();
        String country = countryField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        String desc = descArea.getText().trim();

        if (name.isEmpty() || country.isEmpty()) {
            showError("Name and Country cannot be empty.");
            return;
        }

        try {
            boolean updated = destinationDAO.editDestination(id, name, country, desc.isEmpty() ? null : desc, status);
            if (updated) {
                showInfo("Destination updated successfully.");
                clearForm();
                loadDestinations(null);
            } else {
                showError("Update failed: destination not found.");
            }
        } catch (SQLException ex) {
            showError("Failed to update destination: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please select a destination from the table to delete.");
            return;
        }

        int id = Integer.parseInt(idText);
        String name = nameField.getText().trim();

        if (confirm("Are you sure you want to delete destination '" + name + "' (ID: " + id + ")?")) {
            try {
                boolean deleted = destinationDAO.deleteDestination(id);
                if (deleted) {
                    showInfo("Destination '" + name + "' deleted successfully.");
                    clearForm();
                    loadDestinations(null);
                } else {
                    showError("Delete failed: destination not found.");
                }
            } catch (SQLException ex) {
                showError("Cannot delete destination: " + ex.getMessage());
            }
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadDestinations(null);
            return;
        }
        loadDestinations(keyword);
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        countryField.setText("");
        statusCombo.setSelectedIndex(0);
        descArea.setText("");
        destTable.clearSelection();
    }
}