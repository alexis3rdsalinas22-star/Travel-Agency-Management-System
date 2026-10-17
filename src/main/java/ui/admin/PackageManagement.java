package ui.admin;

import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;
import dao.TravelPackageDAO;
import dao.TravelPackageDAO.PackageRecord;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PackageManagement extends Screen {

    private final TravelPackageDAO packageDAO = new TravelPackageDAO();
    private final DestinationDAO destinationDAO = new DestinationDAO();

    // Destination selector helper
    public static class DestinationItem {
        public final int id;
        public final String name;

        public DestinationItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }

    // Form fields
    private JTextField idField;
    private JTextField nameField;
    private JComboBox<DestinationItem> destinationCombo;
    private JTextField priceField;
    private JTextField minTravelersField;
    private JTextField maxTravelersField;
    private JComboBox<String> statusCombo;
    private JTextArea descArea;

    // Search and Table
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable packageTable;

    // Current records cache
    private List<PackageRecord> currentRecords = new ArrayList<>();

    public PackageManagement() {
        super("Package Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);

        initUI();
        loadDestinations();
        loadPackages(null);
    }

    private void initUI() {
        // Top Header Bar
        createTopHeader(
            "ADMIN CONSOLE  •  TOUR PACKAGES",
            "Package Management",
            "Create, edit, and configure curated tour packages.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Form Card Container
        JPanel formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "PACKAGE DETAILS");

        // Fields inside Card
        idField = addFormField(formCard, "Package ID:", 36, false);
        nameField = addFormField(formCard, "Name *:", 72, true);

        // Destination Dropdown
        JLabel destLbl = createLabel(formCard, "Destination *:", 16, 108, 95, 26);
        setFontStyle(destLbl, Font.BOLD);
        setFontSize(destLbl, 11);

        destinationCombo = new JComboBox<>();
        destinationCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        destinationCombo.setBounds(115, 108, 214, 26);
        formCard.add(destinationCombo);

        priceField = addFormField(formCard, "Price/Person *:", 144, true);
        minTravelersField = addFormField(formCard, "Min Travelers *:", 180, true);
        maxTravelersField = addFormField(formCard, "Max Travelers *:", 216, true);

        // Status Combo
        JLabel statusLbl = createLabel(formCard, "Status *:", 16, 252, 95, 26);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 11);
        statusCombo = createComboBox(formCard, new String[]{"Active", "Inactive"}, 115, 252, 214, 26);

        // Description
        JLabel descLbl = createLabel(formCard, "Description:", 16, 288, 95, 24);
        setFontStyle(descLbl, Font.BOLD);
        setFontSize(descLbl, 11);
        descArea = createTextArea(formCard, 115, 288, 214, 65);

        // Action Buttons inside Card
        createSuccessButton(formCard, "Save", 16, 365, 98, 32, this::handleSave);
        createPrimaryButton(formCard, "Update", 122, 365, 98, 32, this::handleUpdate);
        createDangerButton(formCard, "Delete", 228, 365, 101, 32, this::handleDelete);
        createSecondaryButton(formCard, "Clear Form", 16, 407, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Select a package from the table to view or edit", 16, 520, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Search Bar & Table
        searchField = createSearchBar(410, 88, 745, 34, this::handleSearch, () -> loadPackages(null));
        searchField.addActionListener(e -> handleSearch());

        String[] columns = {"ID", "Package Name", "Destination", "Price / Person", "Min", "Max", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        packageTable = createTable(tableModel, 410, 132, 745, 511);
        setColumnWidths(packageTable, 50, 175, 135, 115, 75, 75, 90);

        packageTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadDestinations() {
        destinationCombo.removeAllItems();
        try {
            List<DestinationRecord> destinations = destinationDAO.getAllDestinations();
            for (DestinationRecord d : destinations) {
                destinationCombo.addItem(new DestinationItem(d.destinationID, d.name));
            }
        } catch (SQLException ex) {
            showError("Failed to load destinations: " + ex.getMessage());
        }
    }

    private void loadPackages(String keyword) {
        tableModel.setRowCount(0);
        currentRecords.clear();
        try {
            List<PackageRecord> list;
            if (keyword == null || keyword.trim().isEmpty()) {
                list = packageDAO.getAllPackages();
            } else {
                list = packageDAO.searchPackage(keyword);
            }
            currentRecords = list;

            for (PackageRecord p : list) {
                tableModel.addRow(new Object[]{
                    p.packageID,
                    p.name,
                    p.destinationName != null ? p.destinationName : ("Dest ID: " + p.destinationID),
                    p.pricePerTraveller != null ? String.format("PHP %,.2f", p.pricePerTraveller) : "PHP 0.00",
                    p.minTraveller,
                    p.maxTraveller,
                    p.status
                });
            }
        } catch (SQLException ex) {
            showError("Database error loading packages: " + ex.getMessage());
        }
    }

    private void populateFormFromSelectedRow() {
        int row = packageTable.getSelectedRow();
        if (row >= 0 && row < currentRecords.size()) {
            PackageRecord p = currentRecords.get(row);
            idField.setText(String.valueOf(p.packageID));
            nameField.setText(p.name);
            priceField.setText(p.pricePerTraveller != null ? p.pricePerTraveller.toPlainString() : "");
            minTravelersField.setText(String.valueOf(p.minTraveller));
            maxTravelersField.setText(String.valueOf(p.maxTraveller));
            statusCombo.setSelectedItem(p.status != null ? p.status : "Active");
            descArea.setText(p.description != null ? p.description : "");

            // Select destination in combo
            for (int i = 0; i < destinationCombo.getItemCount(); i++) {
                DestinationItem item = destinationCombo.getItemAt(i);
                if (item.id == p.destinationID) {
                    destinationCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void handleSave() {
        String name = getTrimmedText(nameField);
        DestinationItem selectedDest = (DestinationItem) destinationCombo.getSelectedItem();
        BigDecimal price = getPriceOrNull(priceField);
        Integer min = getIntOrNull(minTravelersField);
        Integer max = getIntOrNull(maxTravelersField);
        String desc = getTrimmedText(descArea);

        if (name.isEmpty()) {
            showError("Package name is required.");
            return;
        }
        if (selectedDest == null) {
            showError("Please select a destination from the dropdown.");
            return;
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Please enter a valid numeric price greater than zero.");
            return;
        }

        if (min == null || max == null) {
            showError("Min and Max travelers must be valid integer numbers.");
            return;
        }

        if (min < 1) {
            showError("Minimum travelers must be at least 1.");
            return;
        }
        if (max < min) {
            showError("Maximum travelers cannot be less than minimum travelers.");
            return;
        }

        try {
            int newId = packageDAO.addPackage(selectedDest.id, name, desc.isEmpty() ? null : desc, price, min, max);
            if (newId > 0) {
                showInfo("Package '" + name + "' created successfully (ID: " + newId + ").");
                clearForm();
                loadPackages(null);
            } else {
                showError("Failed to save package record.");
            }
        } catch (SQLException ex) {
            showError("Database error while creating package: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleUpdate() {
        Integer packageId = getIntOrNull(idField);
        if (packageId == null) {
            showError("Please select a package from the table to update.");
            return;
        }

        String name = getTrimmedText(nameField);
        DestinationItem selectedDest = (DestinationItem) destinationCombo.getSelectedItem();
        BigDecimal price = getPriceOrNull(priceField);
        Integer min = getIntOrNull(minTravelersField);
        Integer max = getIntOrNull(maxTravelersField);
        String status = (String) statusCombo.getSelectedItem();
        String desc = getTrimmedText(descArea);

        if (name.isEmpty()) {
            showError("Package name is required.");
            return;
        }
        if (selectedDest == null) {
            showError("Please select a destination from the dropdown.");
            return;
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Please enter a valid numeric price greater than zero.");
            return;
        }

        if (min == null || max == null) {
            showError("Min and Max travelers must be valid integer numbers.");
            return;
        }

        if (min < 1) {
            showError("Minimum travelers must be at least 1.");
            return;
        }
        if (max < min) {
            showError("Maximum travelers cannot be less than minimum travelers.");
            return;
        }

        try {
            boolean updated = packageDAO.updatePackage(packageId, selectedDest.id, name,
                    desc.isEmpty() ? null : desc, price, status, min, max);
            if (updated) {
                showInfo("Package ID " + packageId + " updated successfully.");
                clearForm();
                loadPackages(null);
            } else {
                showError("Could not update package. Package not found.");
            }
        } catch (SQLException ex) {
            showError("Database error while updating package: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleDelete() {
        Integer packageId = getIntOrNull(idField);
        if (packageId == null) {
            showError("Please select a package from the table to delete.");
            return;
        }

        String name = getTrimmedText(nameField);

        if (confirm("Are you sure you want to delete package '" + name + "' (ID: " + packageId + ")?")) {
            try {
                boolean deleted = packageDAO.deletePackage(packageId);
                if (deleted) {
                    showInfo("Package '" + name + "' was deleted successfully.");
                    clearForm();
                    loadPackages(null);
                } else {
                    showError("Could not delete package. Package not found.");
                }
            } catch (SQLException ex) {
                showError("Cannot delete package: " + ex.getMessage());
            }
        }
    }

    private void handleSearch() {
        String keyword = getTrimmedText(searchField);
        if (keyword.isEmpty()) {
            loadPackages(null);
            return;
        }
        loadPackages(keyword);
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        if (destinationCombo.getItemCount() > 0) {
            destinationCombo.setSelectedIndex(0);
        }
        priceField.setText("");
        minTravelersField.setText("");
        maxTravelersField.setText("");
        statusCombo.setSelectedIndex(0);
        descArea.setText("");
        packageTable.clearSelection();
    }
}