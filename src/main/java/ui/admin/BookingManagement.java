package ui.admin;

import common.Screen;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BookingManagement extends Screen {

    private final BookingDAO bookingDAO = new BookingDAO();

    // Form fields
    private JTextField idField;
    private JTextField customerField;
    private JTextField packageField;
    private JTextField travelDateField;
    private JTextField bookingDateField;
    private JComboBox<String> statusCombo;

    // Search and Table
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable bookingTable;

    public BookingManagement() {
        super("Booking Management", 1000, 560);

        initUI();
        loadBookings(null);
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("Booking Management", 40, 20, 320, 30);
        createLabel("Monitor and manage customer reservations.", 40, 55, 320, 20);

        // Form fields
        createLabel("Booking ID:", 40, 95, 100, 25);
        idField = createTextField(150, 95, 200, 30);
        idField.setEditable(false);

        createLabel("Customer:", 40, 135, 100, 25);
        customerField = createTextField(150, 135, 200, 30);
        customerField.setEditable(false);

        createLabel("Package:", 40, 175, 100, 25);
        packageField = createTextField(150, 175, 200, 30);
        packageField.setEditable(false);

        createLabel("Travel Date:", 40, 215, 100, 25);
        travelDateField = createTextField(150, 215, 200, 30);
        travelDateField.setEditable(false);

        createLabel("Booking Date:", 40, 255, 100, 25);
        bookingDateField = createTextField(150, 255, 200, 30);
        bookingDateField.setEditable(false);

        createLabel("Status *:", 40, 295, 100, 25);
        statusCombo = createComboBox(new String[]{"Pending", "Confirmed", "Completed", "Cancelled"}, 150, 295, 200, 30);

        // Action Buttons
        JButton updateStatusBtn = createButton("Update Status", 40, 345, 310, 35);
        updateStatusBtn.addActionListener(e -> handleUpdateStatus());

        JButton clearBtn = createButton("Clear Form", 40, 390, 310, 35);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 435, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section using Screen helper
        searchField = createSearchBar(380, 20, 580, 30, this::handleSearch, () -> loadBookings(null));

        // Read-only Table using Screen helpers
        String[] columns = {"ID", "Customer", "Package", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingTable = createTable(tableModel, 380, 60, 580, 440);
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadBookings(String keyword) {
        tableModel.setRowCount(0);
        try {
            List<BookingRecord> list;
            if (keyword == null || keyword.trim().isEmpty()) {
                list = bookingDAO.getAllBookings();
            } else {
                list = bookingDAO.searchBooking(keyword);
            }

            for (BookingRecord b : list) {
                tableModel.addRow(new Object[]{
                    b.bookingID,
                    b.customerName,
                    b.packageName,
                    b.status,
                    b.travelDate != null ? b.travelDate.toString() : "",
                    b.bookingDate != null ? b.bookingDate.toString() : ""
                });
            }
        } catch (SQLException ex) {
            showError("Database error loading bookings: " + ex.getMessage());
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            customerField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            packageField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            statusCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
            travelDateField.setText(tableModel.getValueAt(selectedRow, 4).toString());
            bookingDateField.setText(tableModel.getValueAt(selectedRow, 5).toString());
        }
    }

    private void handleUpdateStatus() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please select a booking from the table to update.");
            return;
        }

        int id = Integer.parseInt(idText);
        String newStatus = (String) statusCombo.getSelectedItem();

        try {
            boolean updated = bookingDAO.updateBookingStatus(id, newStatus);
            if (updated) {
                showInfo("Booking ID " + id + " updated to '" + newStatus + "'.");
                clearForm();
                loadBookings(null);
            } else {
                showError("Update failed: booking not found.");
            }
        } catch (SQLException ex) {
            showError("Failed to update booking status: " + ex.getMessage());
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadBookings(null);
            return;
        }
        loadBookings(keyword);
    }

    private void clearForm() {
        idField.setText("");
        customerField.setText("");
        packageField.setText("");
        travelDateField.setText("");
        bookingDateField.setText("");
        statusCombo.setSelectedIndex(0);
        bookingTable.clearSelection();
    }
}