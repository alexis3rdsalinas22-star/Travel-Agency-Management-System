package ui.admin;

import common.Screen;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;
import dao.BookingDAO.TravelerRecord;
import dao.PaymentDAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BookingManagement extends Screen {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    // Form fields
    private JTextField idField;
    private JTextField customerField;
    private JTextField packageField;
    private JTextField travelDateField;
    private JTextField bookingDateField;
    private JComboBox<String> statusCombo;
    private JLabel paymentStatusLbl;

    // Search and Table
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable bookingTable;

    // Current records cache
    private List<BookingRecord> currentRecords = new ArrayList<>();

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

        // Hidden ID Field to preserve programmatic access
        idField = new JTextField();

        // Fields inside Card
        customerField = addFormField(formCard, "Customer:", 36, false);
        packageField = addFormField(formCard, "Package:", 70, false);
        travelDateField = addFormField(formCard, "Travel Date:", 104, false);
        bookingDateField = addFormField(formCard, "Booking Date:", 138, false);

        // Status Combo (Editable status transition)
        JLabel statusLbl = createLabel(formCard, "Status *:", 16, 172, 95, 26);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 11);
        statusCombo = createComboBox(formCard, new String[]{"Pending", "Confirmed", "Completed", "Cancelled"}, 115, 172, 214, 26);

        // Payment Summary Box
        JPanel payCard = new JPanel(null);
        payCard.setBounds(16, 212, 313, 62);
        payCard.setBackground(new Color(248, 250, 252));
        payCard.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        JLabel payTitle = createLabel(payCard, "PAYMENT SUMMARY", 12, 6, 280, 16);
        setFontStyle(payTitle, Font.BOLD);
        setFontSize(payTitle, 10);
        setTextColor(payTitle, COLOR_PRIMARY);

        paymentStatusLbl = createLabel(payCard, "Select a booking to view payments", 12, 28, 280, 22);
        setFontStyle(paymentStatusLbl, Font.BOLD);
        setFontSize(paymentStatusLbl, 12);
        setTextColor(paymentStatusLbl, COLOR_TEXT_MUTED);
        formCard.add(payCard);

        // Action Buttons inside Card
        createStyledButton(formCard, "View Passenger Manifest", 16, 290, 313, 34,
                new Color(241, 245, 249), new Color(15, 23, 42), new Color(203, 213, 225), 12, this::handleViewManifest);

        createPrimaryButton(formCard, "Update Status", 16, 334, 313, 34, this::handleUpdateStatus);
        createSecondaryButton(formCard, "Clear Selection", 16, 376, 313, 28, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Click any reservation in table to view or update", 16, 416, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Search Bar & Table
        searchField = createSearchBar(410, 88, 745, 34, this::handleSearch, () -> loadBookings(null));
        searchField.addActionListener(e -> handleSearch());

        String[] columns = {"Customer", "Package", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingTable = createTable(tableModel, 410, 132, 745, 511);
        setColumnWidths(bookingTable, 175, 195, 105, 135, 135);

        // Custom Status Badge Renderer on column 2 (Status)
        bookingTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String status = value != null ? value.toString() : "";

                if (!isSelected) {
                    if ("Confirmed".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(220, 252, 231));
                        c.setForeground(new Color(22, 101, 52));
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(254, 243, 199));
                        c.setForeground(new Color(146, 64, 14));
                    } else if ("Completed".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(224, 242, 254));
                        c.setForeground(new Color(7, 89, 133));
                    } else if ("Cancelled".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(254, 226, 226));
                        c.setForeground(new Color(153, 27, 27));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(COLOR_TEXT_MAIN);
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                    if ("Confirmed".equalsIgnoreCase(status)) {
                        c.setForeground(new Color(22, 101, 52));
                    } else if ("Pending".equalsIgnoreCase(status)) {
                        c.setForeground(new Color(146, 64, 14));
                    } else if ("Completed".equalsIgnoreCase(status)) {
                        c.setForeground(new Color(7, 89, 133));
                    } else if ("Cancelled".equalsIgnoreCase(status)) {
                        c.setForeground(new Color(153, 27, 27));
                    } else {
                        c.setForeground(table.getSelectionForeground());
                    }
                }
                c.setOpaque(true);
                return c;
            }
        });

        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadBookings(String keyword) {
        tableModel.setRowCount(0);
        currentRecords.clear();
        try {
            List<BookingRecord> list;
            if (keyword == null || keyword.trim().isEmpty()) {
                list = bookingDAO.getAllBookings();
            } else {
                list = bookingDAO.searchBooking(keyword);
            }
            currentRecords = list;

            for (BookingRecord b : list) {
                tableModel.addRow(new Object[]{
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
        if (selectedRow >= 0 && selectedRow < currentRecords.size()) {
            BookingRecord b = currentRecords.get(selectedRow);
            idField.setText(String.valueOf(b.bookingID));
            customerField.setText(b.customerName != null ? b.customerName : "");
            packageField.setText(b.packageName != null ? b.packageName : "");
            statusCombo.setSelectedItem(b.status != null ? b.status : "Pending");
            travelDateField.setText(b.travelDate != null ? b.travelDate.toString() : "");
            bookingDateField.setText(b.bookingDate != null ? b.bookingDate.toString() : "");

            // Query total payments for this booking
            try {
                BigDecimal totalPaid = paymentDAO.getTotalPaidForBooking(b.bookingID);
                if (totalPaid != null && totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                    paymentStatusLbl.setText("Total Paid: PHP " + String.format("%,.2f", totalPaid));
                    setTextColor(paymentStatusLbl, COLOR_SUCCESS);
                } else {
                    paymentStatusLbl.setText("Total Paid: PHP 0.00 (Unpaid)");
                    setTextColor(paymentStatusLbl, new Color(185, 28, 28));
                }
            } catch (SQLException ex) {
                paymentStatusLbl.setText("Error fetching payments");
                setTextColor(paymentStatusLbl, COLOR_DANGER);
            }
        }
    }

    private void handleViewManifest() {
        Integer bookingId = getIntOrNull(idField);
        if (bookingId == null) {
            showError("Please select a booking from the table first.");
            return;
        }

        String customerName = customerField.getText();
        String packageName = packageField.getText();

        showManifestDialog(bookingId, customerName, packageName);
    }

    private void showManifestDialog(int bookingId, String customerName, String packageName) {
        JDialog dialog = new JDialog(this, "Passenger Manifest - Booking #" + bookingId, true);
        dialog.setSize(720, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(new Color(248, 250, 252));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel("Passenger Manifest for Booking #" + bookingId);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(new Color(15, 23, 42));

        JLabel subLbl = new JLabel("Customer: " + customerName + "  •  Package: " + packageName);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(COLOR_TEXT_MUTED);

        topPanel.add(titleLbl, BorderLayout.NORTH);
        topPanel.add(subLbl, BorderLayout.SOUTH);
        dialog.add(topPanel, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"ID", "First Name", "Last Name", "Date of Birth", "Contact Number"};
        DefaultTableModel manifestModel = createReadOnlyTableModel(columns);
        JTable manifestTable = new JTable(manifestModel);
        manifestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        manifestTable.setRowHeight(26);
        manifestTable.setGridColor(COLOR_SECONDARY);
        manifestTable.setSelectionBackground(new Color(219, 234, 254));

        if (manifestTable.getTableHeader() != null) {
            manifestTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            manifestTable.getTableHeader().setBackground(COLOR_SECONDARY);
            manifestTable.getTableHeader().setForeground(new Color(51, 65, 85));
        }

        try {
            List<TravelerRecord> travelers = bookingDAO.getTravelersByBooking(bookingId);
            if (travelers.isEmpty()) {
                manifestModel.addRow(new Object[]{"-", "No travelers recorded", "-", "-", "-"});
            } else {
                for (TravelerRecord t : travelers) {
                    manifestModel.addRow(new Object[]{
                        t.travelerID,
                        t.firstName != null ? t.firstName : "",
                        t.lastName != null ? t.lastName : "",
                        t.dateOfBirth != null ? t.dateOfBirth.toString() : "N/A",
                        t.contactNumber != null ? t.contactNumber : "N/A"
                    });
                }
            }
        } catch (SQLException ex) {
            manifestModel.addRow(new Object[]{"-", "Error loading travelers: " + ex.getMessage(), "-", "-", "-"});
        }

        JScrollPane scrollPane = new JScrollPane(manifestTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setPreferredSize(new Dimension(95, 30));
        closeBtn.addActionListener(e -> dialog.dispose());
        bottomPanel.add(closeBtn);

        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
        paymentStatusLbl.setText("Select a booking to view payments");
        setTextColor(paymentStatusLbl, COLOR_TEXT_MUTED);
        bookingTable.clearSelection();
    }
}