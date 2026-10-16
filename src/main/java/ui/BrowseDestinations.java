package ui;

import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BrowseDestinations extends Screen {

    private final DestinationDAO destinationDAO = new DestinationDAO();

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable destTable;

    public BrowseDestinations() {
        super("Browse Destinations", 960, 560);

        initUI();
        loadDestinations(null);
    }

    private void initUI() {
        // Header and subtitle
        createHeader("Browse Destinations", 40, 20, 400, 30);
        createLabel("Explore available travel locations and tour spots.", 40, 50, 450, 20);

        // Search bar using Screen helper
        searchField = createSearchBar(40, 85, 880, 30, this::handleSearch, () -> loadDestinations(null));

        // Read-only Table using Screen helpers
        String[] columns = {"ID", "Destination Name", "Country", "Description", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        destTable = createTable(tableModel, 40, 125, 880, 330);

        // Navigation
        JButton backBtn = createButton("Back to Home", 40, 470, 200, 35);
        backBtn.addActionListener(e -> navigateTo(new Homepage()));
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

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadDestinations(null);
            return;
        }
        loadDestinations(keyword);
    }
}
