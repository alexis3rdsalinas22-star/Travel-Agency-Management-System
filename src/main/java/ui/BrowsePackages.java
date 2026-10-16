package ui;

import common.Screen;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BrowsePackages extends Screen {

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable packageTable;

    public BrowsePackages() {
        super("Browse Travel Packages", 960, 560);

        initUI();
    }

    private void initUI() {
        // Header and subtitle
        createHeader("Browse Travel Packages", 40, 20, 400, 30);
        createLabel("Discover curated travel packages and tour offerings.", 40, 50, 450, 20);

        // Search bar using Screen helper (no DAO function attached as requested)
        searchField = createSearchBar(40, 85, 880, 30, () -> {}, () -> {});

        // Read-only Table with columns
        String[] columns = {"Package ID", "Package Name", "Destination", "Price / Traveler", "Min Travelers", "Max Travelers", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        packageTable = createTable(tableModel, 40, 125, 880, 330);

        // Action Buttons
        JButton bookNowBtn = createButton("Book Now", 40, 470, 160, 35);
        // Note: No DAO function attached per instruction

        JButton backBtn = createButton("Back to Home", 220, 470, 160, 35);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
    }
}
