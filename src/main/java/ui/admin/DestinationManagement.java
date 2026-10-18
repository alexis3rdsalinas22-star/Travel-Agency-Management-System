package ui.admin;

import common.ImageUtil;
import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
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

public class DestinationManagement extends Screen {

    private final DestinationDAO destinationDAO = new DestinationDAO();

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
    private JTextField countryField;
    private JComboBox<String> statusCombo;
    private JTextArea descArea;

    // Image preview and path
    private JLabel imagePreviewLabel;
    private String selectedImagePath = null;

    // Table
    private DefaultTableModel tableModel;
    private JTable destTable;

    // Current records cache
    private List<DestinationRecord> currentRecords = new ArrayList<>();

    public DestinationManagement() {
        super("Destination Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);
        setFullscreen(true);

        initUI();
        loadDestinations(null);

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
            "ADMIN CONSOLE  •  TRAVEL CATALOG",
            "Destination Management",
            "Catalog tourist spots, countries, regional descriptions, and statuses.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Form Card Container
        formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "DESTINATION DETAILS");

        // Hidden ID Field to preserve programmatic access
        idField = new JTextField();

        // Fields inside Card
        nameField = addFormField(formCard, "Name *:", 40, true);
        countryField = addFormField(formCard, "Country *:", 74, true);

        // Status Combo
        JLabel statusLbl = createLabel(formCard, "Status *:", 16, 108, 95, 28);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 11);
        statusCombo = createComboBox(formCard, new String[]{"Active", "Inactive"}, 115, 108, 214, 28);

        // Description - Full Width and Taller
        JLabel descLbl = createLabel(formCard, "Description:", 16, 142, 150, 18);
        setFontStyle(descLbl, Font.BOLD);
        setFontSize(descLbl, 11);
        descArea = createTextArea(formCard, 16, 164, 313, 90);

        // Image Preview & Browse
        JLabel imgHeader = createLabel(formCard, "Destination Photo:", 16, 264, 130, 22);
        setFontStyle(imgHeader, Font.BOLD);
        setFontSize(imgHeader, 11);

        JButton browseBtn = createStyledButton(formCard, "Browse...", 230, 262, 99, 24,
                Color.WHITE, new Color(51, 65, 85), new Color(203, 213, 225), 11, this::handleBrowseImage);

        imagePreviewLabel = new JLabel("", JLabel.CENTER);
        imagePreviewLabel.setBounds(16, 292, 313, 95);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(248, 250, 252));
        imagePreviewLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(null, 313, 95, ImageUtil.DEFAULT_DESTINATION_PLACEHOLDER));
        formCard.add(imagePreviewLabel);

        // Action Buttons inside Card
        createSuccessButton(formCard, "Save", 16, 398, 98, 32, this::handleSave);
        createPrimaryButton(formCard, "Update", 122, 398, 98, 32, this::handleUpdate);
        createDangerButton(formCard, "Delete", 228, 398, 101, 32, this::handleDelete);
        createSecondaryButton(formCard, "Clear Form", 16, 438, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Select a destination row to edit or delete", 16, 478, 313, 16);
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
            loadDestinations(null);
        });
        setFontSize(showAllBtn, 12);

        String[] columns = {"Destination Name", "Country", "Description", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        destTable = createTable(tableModel, 410, 132, 745, 511);
        destTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setColumnWidths(destTable, 160, 120, 380, 85);
        setStatusBadgeRenderer(destTable, 3);

        if (destTable.getParent() != null && destTable.getParent().getParent() instanceof JScrollPane) {
            tableScrollPane = (JScrollPane) destTable.getParent().getParent();
        }

        destTable.getSelectionModel().addListSelectionListener(e -> {
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

    private void loadDestinations(String keyword) {
        tableModel.setRowCount(0);
        currentRecords.clear();
        try {
            List<DestinationRecord> list;
            if (keyword == null || keyword.trim().isEmpty()) {
                list = destinationDAO.getAllDestinations();
            } else {
                list = destinationDAO.searchByName(keyword);
            }
            currentRecords = list;

            for (DestinationRecord d : list) {
                tableModel.addRow(new Object[]{
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
            int modelRow = destTable.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < currentRecords.size()) {
                DestinationRecord d = currentRecords.get(modelRow);
                idField.setText(String.valueOf(d.destinationID));
                nameField.setText(d.name);
                countryField.setText(d.country);
                descArea.setText(d.description != null ? d.description : "");
                statusCombo.setSelectedItem(d.status != null ? d.status : "Active");

                selectedImagePath = d.imagePath;
                imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(selectedImagePath, 313, 95, ImageUtil.DEFAULT_DESTINATION_PLACEHOLDER));
            }
        }
    }

    private void handleBrowseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Destination Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.png, *.webp, *.jpeg)", "jpg", "jpeg", "png", "webp", "gif"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                String savedName = ImageUtil.saveUploadedImage(selectedFile, ImageUtil.DATA_DESTINATIONS_DIR, "dest");
                selectedImagePath = savedName;
                imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(selectedImagePath, 313, 95, ImageUtil.DEFAULT_DESTINATION_PLACEHOLDER));
                showInfo("Destination image staged successfully.");
            } catch (Exception ex) {
                showError("Failed to save image file: " + ex.getMessage());
            }
        }
    }

    private void handleSave() {
        String name = getTrimmedText(nameField);
        String country = getTrimmedText(countryField);
        String desc = getTrimmedText(descArea);

        if (name.isEmpty() || country.isEmpty()) {
            showError("Please enter both destination name and country.");
            return;
        }

        try {
            destinationDAO.addDestination(name, country, desc.isEmpty() ? null : desc, selectedImagePath);
            showInfo("Destination '" + name + "' added successfully.");
            clearForm();
            loadDestinations(null);
        } catch (SQLException ex) {
            showError("Failed to add destination: " + ex.getMessage());
        }
    }

    private void handleUpdate() {
        Integer destId = getIntOrNull(idField);
        if (destId == null) {
            showError("Please select a destination from the table to update.");
            return;
        }

        String name = getTrimmedText(nameField);
        String country = getTrimmedText(countryField);
        String desc = getTrimmedText(descArea);
        String status = (String) statusCombo.getSelectedItem();

        if (name.isEmpty() || country.isEmpty()) {
            showError("Please enter both destination name and country.");
            return;
        }

        try {
            boolean updated = destinationDAO.editDestination(destId, name, country,
                    desc.isEmpty() ? null : desc, selectedImagePath, status);
            if (updated) {
                showInfo("Destination '" + name + "' updated successfully.");
                clearForm();
                loadDestinations(null);
            } else {
                showError("Could not update destination. Record not found.");
            }
        } catch (SQLException ex) {
            showError("Failed to update destination: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        Integer destId = getIntOrNull(idField);
        if (destId == null) {
            showError("Please select a destination from the table to delete.");
            return;
        }

        String name = getTrimmedText(nameField);

        if (confirm("Are you sure you want to delete destination '" + name + "'?")) {
            try {
                boolean deleted = destinationDAO.deleteDestination(destId);
                if (deleted) {
                    showInfo("Destination '" + name + "' was deleted successfully.");
                    clearForm();
                    loadDestinations(null);
                } else {
                    showError("Could not delete destination. Record not found.");
                }
            } catch (SQLException ex) {
                showError("Cannot delete destination because it is currently linked to tour packages: " + ex.getMessage());
            }
        }
    }

    private void handleSearch() {
        String keyword = getTrimmedText(searchField);
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
        descArea.setText("");
        statusCombo.setSelectedIndex(0);
        selectedImagePath = null;
        imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(null, 313, 95, ImageUtil.DEFAULT_DESTINATION_PLACEHOLDER));
        destTable.clearSelection();
    }
}