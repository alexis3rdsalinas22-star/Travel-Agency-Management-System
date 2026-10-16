package ui.admin;

import common.Screen;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PackageManagement extends Screen {

    // Form fields
    private JTextField idField;
    private JTextField nameField;
    private JTextField destinationField;
    private JTextField priceField;
    private JTextField minTravelersField;
    private JTextField maxTravelersField;
    private JComboBox<String> statusCombo;
    private JTextArea descArea;

    // Search and Table
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable packageTable;

    public PackageManagement() {
        super("Package Management", 1000, 600);

        initUI();
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("Package Management", 40, 15, 320, 30);
        createLabel("Create, edit, and configure tour packages.", 40, 45, 320, 20);

        // Form fields
        createLabel("Package ID:", 40, 80, 110, 25);
        idField = createTextField(150, 80, 210, 30);
        idField.setEditable(false);

        createLabel("Package Name *:", 40, 115, 110, 25);
        nameField = createTextField(150, 115, 210, 30);

        createLabel("Destination *:", 40, 150, 110, 25);
        destinationField = createTextField(150, 150, 210, 30);

        createLabel("Price/Person *:", 40, 185, 110, 25);
        priceField = createTextField(150, 185, 210, 30);

        createLabel("Min Travelers *:", 40, 220, 110, 25);
        minTravelersField = createTextField(150, 220, 210, 30);

        createLabel("Max Travelers *:", 40, 255, 110, 25);
        maxTravelersField = createTextField(150, 255, 210, 30);

        createLabel("Status *:", 40, 290, 110, 25);
        statusCombo = createComboBox(new String[]{"Active", "Inactive"}, 150, 290, 210, 30);

        createLabel("Description:", 40, 325, 110, 25);
        descArea = createTextArea(150, 325, 210, 60);

        // Action Buttons
        JButton saveBtn = createButton("Save", 40, 400, 100, 32);
        // Note: No DAO function attached per instruction

        JButton updateBtn = createButton("Update", 150, 400, 100, 32);
        // Note: No DAO function attached per instruction

        JButton deleteBtn = createButton("Delete", 260, 400, 100, 32);
        // Note: No DAO function attached per instruction

        JButton clearBtn = createButton("Clear Form", 40, 440, 320, 32);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 480, 320, 32);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section using Screen helper (no DAO function attached)
        searchField = createSearchBar(380, 15, 580, 30, () -> {}, () -> {});

        // Read-only Table with columns
        String[] columns = {"ID", "Package Name", "Destination", "Price / Traveler", "Min Travelers", "Max Travelers", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        packageTable = createTable(tableModel, 380, 55, 580, 480);
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        destinationField.setText("");
        priceField.setText("");
        minTravelersField.setText("");
        maxTravelersField.setText("");
        statusCombo.setSelectedIndex(0);
        descArea.setText("");
        packageTable.clearSelection();
    }
}