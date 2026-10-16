package ui;

import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;

import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BrowseDestinations extends Screen {

    private final DestinationDAO destinationDAO = new DestinationDAO();

    private JTextField searchField;
    private JLabel countLabel;
    private DefaultTableModel tableModel;
    private JTable destTable;

    public BrowseDestinations() {
        super("Browse Destinations", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
        loadDestinations(null);
    }

    private void initUI() {
        // Header and subtitle
        JLabel header = createHeader("Browse Destinations", 40, 20, 400, 30);
        setFontSize(header, 24);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Explore available travel locations and popular tour spots.", 40, 52, 500, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Result count indicator
        countLabel = createLabel("", 740, 52, 220, 20);
        setFontSize(countLabel, 12);
        setTextColor(countLabel, new Color(100, 116, 139));
        countLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        // Search bar using Screen helper
        searchField = createSearchBar(40, 80, 920, 32, this::handleSearch, () -> loadDestinations(null));
        searchField.addActionListener(e -> handleSearch());

        // Read-only Table using Screen helpers
        String[] columns = {"ID", "Destination Name", "Country", "Description", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        destTable = createTable(tableModel, 40, 122, 920, 370);
        destTable.setRowHeight(24);

        // Configure column widths for better visual readability
        if (destTable.getColumnModel().getColumnCount() >= 5) {
            destTable.getColumnModel().getColumn(0).setPreferredWidth(60);
            destTable.getColumnModel().getColumn(0).setMaxWidth(80);
            destTable.getColumnModel().getColumn(1).setPreferredWidth(210);
            destTable.getColumnModel().getColumn(2).setPreferredWidth(150);
            destTable.getColumnModel().getColumn(3).setPreferredWidth(400);
            destTable.getColumnModel().getColumn(4).setPreferredWidth(100);
            destTable.getColumnModel().getColumn(4).setMaxWidth(120);
        }

        // Navigation
        JButton backBtn = createButton(common.Session.isCustomer() ? "Back to Dashboard" : "Back to Home", 40, 505, 200, 36);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(51, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            if (common.Session.isCustomer()) {
                navigateTo(new ui.customer.CustomerDashboard());
            } else {
                navigateTo(new Homepage());
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
            if (countLabel != null) {
                countLabel.setText("Showing: " + list.size() + " destination(s)");
            }
        } catch (SQLException ex) {
            showError("Database error loading destinations: " + ex.getMessage());
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
}
