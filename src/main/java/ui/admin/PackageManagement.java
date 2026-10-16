package ui.admin;

import common.Screen;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
    }

    private void initUI() {
        // Header & Subtitle
        JLabel header = createHeader("Package Management", 40, 15, 320, 30);
        setFontSize(header, 22);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Create, edit, and configure tour packages.", 40, 48, 320, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form fields
        JLabel idLbl = createLabel("Package ID:", 40, 80, 110, 25);
        setFontStyle(idLbl, Font.BOLD);
        setFontSize(idLbl, 13);
        setTextColor(idLbl, new Color(55, 65, 81));
        idField = createTextField(150, 80, 210, 30);
        idField.setEditable(false);

        JLabel nameLbl = createLabel("Package Name *:", 40, 115, 110, 25);
        setFontStyle(nameLbl, Font.BOLD);
        setFontSize(nameLbl, 13);
        setTextColor(nameLbl, new Color(55, 65, 81));
        nameField = createTextField(150, 115, 210, 30);

        JLabel destLbl = createLabel("Destination *:", 40, 150, 110, 25);
        setFontStyle(destLbl, Font.BOLD);
        setFontSize(destLbl, 13);
        setTextColor(destLbl, new Color(55, 65, 81));
        destinationField = createTextField(150, 150, 210, 30);

        JLabel priceLbl = createLabel("Price/Person *:", 40, 185, 110, 25);
        setFontStyle(priceLbl, Font.BOLD);
        setFontSize(priceLbl, 13);
        setTextColor(priceLbl, new Color(55, 65, 81));
        priceField = createTextField(150, 185, 210, 30);

        JLabel minLbl = createLabel("Min Travelers *:", 40, 220, 110, 25);
        setFontStyle(minLbl, Font.BOLD);
        setFontSize(minLbl, 13);
        setTextColor(minLbl, new Color(55, 65, 81));
        minTravelersField = createTextField(150, 220, 210, 30);

        JLabel maxLbl = createLabel("Max Travelers *:", 40, 255, 110, 25);
        setFontStyle(maxLbl, Font.BOLD);
        setFontSize(maxLbl, 13);
        setTextColor(maxLbl, new Color(55, 65, 81));
        maxTravelersField = createTextField(150, 255, 210, 30);

        JLabel statusLbl = createLabel("Status *:", 40, 290, 110, 25);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 13);
        setTextColor(statusLbl, new Color(55, 65, 81));
        statusCombo = createComboBox(new String[]{"Active", "Inactive"}, 150, 290, 210, 30);

        JLabel descLbl = createLabel("Description:", 40, 325, 110, 25);
        setFontStyle(descLbl, Font.BOLD);
        setFontSize(descLbl, 13);
        setTextColor(descLbl, new Color(55, 65, 81));
        descArea = createTextArea(150, 325, 210, 60);

        // Action Buttons
        JButton saveBtn = createButton("Save", 40, 400, 100, 34);
        setFontStyle(saveBtn, Font.BOLD);
        setBackground(saveBtn, new Color(16, 149, 93));
        setTextColor(saveBtn, Color.WHITE);
        saveBtn.setFocusPainted(false);

        JButton updateBtn = createButton("Update", 150, 400, 100, 34);
        setFontStyle(updateBtn, Font.BOLD);
        setBackground(updateBtn, new Color(37, 99, 235));
        setTextColor(updateBtn, Color.WHITE);
        updateBtn.setFocusPainted(false);

        JButton deleteBtn = createButton("Delete", 260, 400, 100, 34);
        setFontStyle(deleteBtn, Font.BOLD);
        setBackground(deleteBtn, new Color(220, 53, 69));
        setTextColor(deleteBtn, Color.WHITE);
        deleteBtn.setFocusPainted(false);

        JButton clearBtn = createButton("Clear Form", 40, 440, 320, 34);
        setFontSize(clearBtn, 13);
        setBackground(clearBtn, new Color(229, 231, 235));
        setTextColor(clearBtn, new Color(55, 65, 81));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 480, 320, 34);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(220, 225, 235));
        setTextColor(backBtn, new Color(51, 65, 85));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section using Screen helper (no DAO function attached)
        searchField = createSearchBar(370, 18, 590, 30, () -> {}, () -> {});

        // Read-only Table with columns
        String[] columns = {"ID", "Package Name", "Destination", "Price / Traveler", "Min Travelers", "Max Travelers", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        packageTable = createTable(tableModel, 370, 52, 590, 480);
        packageTable.setRowHeight(24);
        if (packageTable.getColumnModel().getColumnCount() >= 7) {
            packageTable.getColumnModel().getColumn(0).setPreferredWidth(45);
            packageTable.getColumnModel().getColumn(0).setMaxWidth(60);
            packageTable.getColumnModel().getColumn(1).setPreferredWidth(145);
            packageTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            packageTable.getColumnModel().getColumn(3).setPreferredWidth(95);
            packageTable.getColumnModel().getColumn(4).setPreferredWidth(70);
            packageTable.getColumnModel().getColumn(5).setPreferredWidth(70);
            packageTable.getColumnModel().getColumn(6).setPreferredWidth(65);
        }
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