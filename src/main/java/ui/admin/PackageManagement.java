package ui.admin;

import common.ImageUtil;
import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;
import dao.TravelPackageDAO;
import dao.TravelPackageDAO.PackageRecord;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
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
            return name;
        }
    }

    // UI Structure
    private JButton headerBackBtn;
    private JPanel formCard;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchBtn;
    private JButton showAllBtn;
    private JScrollPane tableScrollPane;

    // Form fields
    private JTextField idField;
    private JTextField nameField;
    private JComboBox<DestinationItem> destinationCombo;
    private JTextField priceField;
    private JTextField minTravelersField;
    private JTextField maxTravelersField;
    private JComboBox<String> statusCombo;
    private JTextArea descArea;

    // Image preview and path
    private JLabel imagePreviewLabel;
    private String selectedImagePath = null;

    // Table
    private DefaultTableModel tableModel;
    private JTable packageTable;

    // Current records cache
    private List<PackageRecord> currentRecords = new ArrayList<>();

    public PackageManagement() {
        super("Package Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);
        setFullscreen(true);

        initUI();
        loadDestinations();
        loadPackages(null);

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
            "ADMIN CONSOLE  •  TOUR PACKAGES",
            "Package Management",
            "Create, edit, and configure curated tour packages.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Form Card Container
        formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "PACKAGE DETAILS");

        // Hidden ID Field to preserve programmatic access
        idField = new JTextField();

        // Fields inside Card
        nameField = addFormField(formCard, "Name *:", 36, true);

        // Destination Dropdown
        JLabel destLbl = createLabel(formCard, "Destination *:", 16, 68, 95, 28);
        setFontStyle(destLbl, Font.BOLD);
        setFontSize(destLbl, 11);

        destinationCombo = new JComboBox<>();
        destinationCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        destinationCombo.setBackground(Color.WHITE);
        destinationCombo.setBounds(115, 68, 214, 28);
        formCard.add(destinationCombo);

        priceField = addFormField(formCard, "Price/Pax *:", 100, true);

        // Min & Max Pax side-by-side
        JLabel minLbl = createLabel(formCard, "Min Pax *:", 16, 132, 60, 28);
        setFontStyle(minLbl, Font.BOLD);
        setFontSize(minLbl, 11);
        minTravelersField = new JTextField();
        minTravelersField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        minTravelersField.setBackground(Color.WHITE);
        minTravelersField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        minTravelersField.setBounds(78, 132, 76, 28);
        formCard.add(minTravelersField);

        JLabel maxLbl = createLabel(formCard, "Max Pax *:", 168, 132, 65, 28);
        setFontStyle(maxLbl, Font.BOLD);
        setFontSize(maxLbl, 11);
        maxTravelersField = new JTextField();
        maxTravelersField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        maxTravelersField.setBackground(Color.WHITE);
        maxTravelersField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        maxTravelersField.setBounds(238, 132, 91, 28);
        formCard.add(maxTravelersField);

        // Status Combo
        JLabel statusLbl = createLabel(formCard, "Status *:", 16, 164, 95, 28);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 11);
        statusCombo = createComboBox(formCard, new String[]{"Active", "Inactive"}, 115, 164, 214, 28);

        // Description - Full Width
        JLabel descLbl = createLabel(formCard, "Description:", 16, 196, 150, 18);
        setFontStyle(descLbl, Font.BOLD);
        setFontSize(descLbl, 11);
        descArea = createTextArea(formCard, 16, 216, 313, 66);

        // Image Preview & Browse
        JLabel imgHeader = createLabel(formCard, "Package Photo:", 16, 288, 110, 22);
        setFontStyle(imgHeader, Font.BOLD);
        setFontSize(imgHeader, 11);

        JButton browseBtn = createStyledButton(formCard, "Browse...", 230, 286, 99, 24,
                Color.WHITE, new Color(51, 65, 85), new Color(203, 213, 225), 11, this::handleBrowseImage);

        imagePreviewLabel = new JLabel("", JLabel.CENTER);
        imagePreviewLabel.setBounds(16, 312, 313, 76);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(248, 250, 252));
        imagePreviewLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(null, 313, 76, ImageUtil.DEFAULT_PACKAGE_PLACEHOLDER));
        formCard.add(imagePreviewLabel);

        // Action Buttons inside Card
        createSuccessButton(formCard, "Save", 16, 398, 98, 32, this::handleSave);
        createPrimaryButton(formCard, "Update", 122, 398, 98, 32, this::handleUpdate);
        createDangerButton(formCard, "Delete", 228, 398, 101, 32, this::handleDelete);
        createSecondaryButton(formCard, "Clear Form", 16, 438, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Select a package from the table to view or edit", 16, 478, 313, 16);
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
            loadPackages(null);
        });
        setFontSize(showAllBtn, 12);

        String[] columns = {"Package Name", "Destination", "Price / Pax", "Min", "Max", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        packageTable = createTable(tableModel, 410, 132, 745, 511);
        packageTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setColumnWidths(packageTable, 220, 155, 125, 75, 75, 95);
        setStatusBadgeRenderer(packageTable, 5);

        if (packageTable.getParent() != null && packageTable.getParent().getParent() instanceof JScrollPane) {
            tableScrollPane = (JScrollPane) packageTable.getParent().getParent();
        }

        packageTable.getSelectionModel().addListSelectionListener(e -> {
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
                list = packageDAO.searchByName(keyword);
            }
            currentRecords = list;

            for (PackageRecord p : list) {
                tableModel.addRow(new Object[]{
                    p.name,
                    p.destinationName != null ? p.destinationName : "Destination",
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
        int viewRow = packageTable.getSelectedRow();
        if (viewRow >= 0) {
            int row = packageTable.convertRowIndexToModel(viewRow);
            if (row >= 0 && row < currentRecords.size()) {
                PackageRecord p = currentRecords.get(row);
                idField.setText(String.valueOf(p.packageID));
                nameField.setText(p.name);
                priceField.setText(p.pricePerTraveller != null ? p.pricePerTraveller.toPlainString() : "");
                minTravelersField.setText(String.valueOf(p.minTraveller));
                maxTravelersField.setText(String.valueOf(p.maxTraveller));
                statusCombo.setSelectedItem(p.status != null ? p.status : "Active");
                descArea.setText(p.description != null ? p.description : "");

                // Image Preview
                selectedImagePath = p.imagePath;
                imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(selectedImagePath, 313, 76, ImageUtil.DEFAULT_PACKAGE_PLACEHOLDER));

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
    }

    private void handleBrowseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Package Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.png, *.webp, *.jpeg)", "jpg", "jpeg", "png", "webp", "gif"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                String savedName = ImageUtil.saveUploadedImage(selectedFile, ImageUtil.DATA_PACKAGES_DIR, "pkg");
                selectedImagePath = savedName;
                imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(selectedImagePath, 313, 76, ImageUtil.DEFAULT_PACKAGE_PLACEHOLDER));
                showInfo("Image selected and staged for this package.");
            } catch (Exception ex) {
                showError("Failed to save image file: " + ex.getMessage());
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
            int newId = packageDAO.addPackage(selectedDest.id, name, desc.isEmpty() ? null : desc,
                    selectedImagePath, price, min, max);
            if (newId > 0) {
                showInfo("Package '" + name + "' created successfully.");
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
                    desc.isEmpty() ? null : desc, selectedImagePath, price, status, min, max);
            if (updated) {
                showInfo("Package '" + name + "' updated successfully.");
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

        if (confirm("Are you sure you want to delete package '" + name + "'?")) {
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
        selectedImagePath = null;
        imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(null, 313, 76, ImageUtil.DEFAULT_PACKAGE_PLACEHOLDER));
        packageTable.clearSelection();
    }
}