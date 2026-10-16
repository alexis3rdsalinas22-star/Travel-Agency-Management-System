package ui.admin;

import common.Screen;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;

import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
        super("Booking Management", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
        loadBookings(null);
    }

    private void initUI() {
        // Header & Subtitle
        JLabel header = createHeader("Booking Management", 40, 20, 320, 30);
        setFontSize(header, 22);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Monitor and manage customer reservations.", 40, 52, 320, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Form fields
        JLabel idLbl = createLabel("Booking ID:", 40, 95, 100, 25);
        setFontStyle(idLbl, Font.BOLD);
        setFontSize(idLbl, 13);
        setTextColor(idLbl, new Color(55, 65, 81));
        idField = createTextField(150, 95, 200, 30);
        idField.setEditable(false);

        JLabel custLbl = createLabel("Customer:", 40, 135, 100, 25);
        setFontStyle(custLbl, Font.BOLD);
        setFontSize(custLbl, 13);
        setTextColor(custLbl, new Color(55, 65, 81));
        customerField = createTextField(150, 135, 200, 30);
        customerField.setEditable(false);

        JLabel pkgLbl = createLabel("Package:", 40, 175, 100, 25);
        setFontStyle(pkgLbl, Font.BOLD);
        setFontSize(pkgLbl, 13);
        setTextColor(pkgLbl, new Color(55, 65, 81));
        packageField = createTextField(150, 175, 200, 30);
        packageField.setEditable(false);

        JLabel travelLbl = createLabel("Travel Date:", 40, 215, 100, 25);
        setFontStyle(travelLbl, Font.BOLD);
        setFontSize(travelLbl, 13);
        setTextColor(travelLbl, new Color(55, 65, 81));
        travelDateField = createTextField(150, 215, 200, 30);
        travelDateField.setEditable(false);

        JLabel bookLbl = createLabel("Booking Date:", 40, 255, 100, 25);
        setFontStyle(bookLbl, Font.BOLD);
        setFontSize(bookLbl, 13);
        setTextColor(bookLbl, new Color(55, 65, 81));
        bookingDateField = createTextField(150, 255, 200, 30);
        bookingDateField.setEditable(false);

        JLabel statusLbl = createLabel("Status *:", 40, 295, 100, 25);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 13);
        setTextColor(statusLbl, new Color(55, 65, 81));
        statusCombo = createComboBox(new String[]{"Pending", "Confirmed", "Completed", "Cancelled"}, 150, 295, 200, 30);

        // Action Buttons
        JButton updateStatusBtn = createButton("Update Status", 40, 345, 310, 36);
        setFontStyle(updateStatusBtn, Font.BOLD);
        setBackground(updateStatusBtn, new Color(37, 99, 235));
        setTextColor(updateStatusBtn, Color.WHITE);
        updateStatusBtn.setFocusPainted(false);
        updateStatusBtn.addActionListener(e -> handleUpdateStatus());

        JButton clearBtn = createButton("Clear Form", 40, 390, 310, 36);
        setFontSize(clearBtn, 13);
        setBackground(clearBtn, new Color(229, 231, 235));
        setTextColor(clearBtn, new Color(55, 65, 81));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 435, 310, 36);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(220, 225, 235));
        setTextColor(backBtn, new Color(51, 65, 85));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section using Screen helper
        searchField = createSearchBar(370, 18, 590, 30, this::handleSearch, () -> loadBookings(null));
        searchField.addActionListener(e -> handleSearch());

        // Read-only Table using Screen helpers
        String[] columns = {"ID", "Customer", "Package", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingTable = createTable(tableModel, 370, 52, 590, 480);
        bookingTable.setRowHeight(24);
        if (bookingTable.getColumnModel().getColumnCount() >= 6) {
            bookingTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            bookingTable.getColumnModel().getColumn(0).setMaxWidth(70);
            bookingTable.getColumnModel().getColumn(1).setPreferredWidth(130);
            bookingTable.getColumnModel().getColumn(2).setPreferredWidth(140);
            bookingTable.getColumnModel().getColumn(3).setPreferredWidth(90);
            bookingTable.getColumnModel().getColumn(4).setPreferredWidth(90);
            bookingTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        }
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