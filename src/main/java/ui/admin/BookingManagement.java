package ui.admin;

import common.Screen;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;

import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
        super("Booking Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);

        initUI();
        loadBookings(null);
    }

    private void initUI() {
        // Top Header Bar
        createTopHeader(
            "ADMIN CONSOLE  •  RESERVATIONS",
            "Booking Management",
            "Review passenger reservations, schedule departure dates, and update statuses.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Form Card Container
        JPanel formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "RESERVATION DETAILS");

        // Fields inside Card
        idField = addFormField(formCard, "Booking ID:", 42, false);
        customerField = addFormField(formCard, "Customer:", 84, false);
        packageField = addFormField(formCard, "Package:", 126, false);
        travelDateField = addFormField(formCard, "Travel Date:", 168, false);
        bookingDateField = addFormField(formCard, "Booking Date:", 210, false);

        // Status Combo (Editable status transition)
        JLabel statusLbl = createLabel(formCard, "Status *:", 16, 256, 95, 28);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 11);

        statusCombo = createComboBox(formCard, new String[]{"Pending", "Confirmed", "Completed", "Cancelled"}, 115, 256, 214, 28);

        JLabel statusHint = createLabel(formCard, "Select status and click Update Status below", 16, 290, 313, 16);
        setFontSize(statusHint, 10);
        setTextColor(statusHint, COLOR_TEXT_MUTED);

        // Action Buttons inside Card
        createPrimaryButton(formCard, "Update Status", 16, 335, 313, 38, this::handleUpdateStatus);
        createSecondaryButton(formCard, "Clear Selection", 16, 385, 313, 32, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Click any reservation in table to view or update", 16, 520, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Search Bar & Table
        searchField = createSearchBar(410, 88, 745, 34, this::handleSearch, () -> loadBookings(null));
        searchField.addActionListener(e -> handleSearch());

        String[] columns = {"ID", "Customer", "Package", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingTable = createTable(tableModel, 410, 132, 745, 511);
        setColumnWidths(bookingTable, 50, 150, 165, 100, 135, 135);

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
        Integer id = getIntOrNull(idField);
        if (id == null) {
            showError("Please select a booking from the table to update.");
            return;
        }

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
        String keyword = getTrimmedText(searchField);
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