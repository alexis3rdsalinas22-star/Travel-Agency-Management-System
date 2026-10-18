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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    private JTextField customerField;
    private JTextField packageField;
    private JTextField travelDateField;
    private JTextField bookingDateField;
    private JComboBox<String> statusCombo;
    private JLabel paymentStatusLbl;

    // Table
    private DefaultTableModel tableModel;
    private JTable bookingTable;

    // Current records cache
    private List<BookingRecord> currentRecords = new ArrayList<>();

    public BookingManagement() {
        super("Booking Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);
        setFullscreen(true);

        initUI();
        loadBookings(null);

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
            "ADMIN CONSOLE  •  RESERVATIONS",
            "Booking Management",
            "Review passenger reservations, schedule departure dates, and update statuses.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Form Card Container
        formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "RESERVATION DETAILS");

        // Hidden ID Field to preserve programmatic access
        idField = new JTextField();

        // Fields inside Card
        customerField = addFormField(formCard, "Customer:", 40, false);
        packageField = addFormField(formCard, "Package:", 74, false);
        travelDateField = addFormField(formCard, "Travel Date:", 108, false);
        bookingDateField = addFormField(formCard, "Booking Date:", 142, false);

        // Status Combo (Editable status transition)
        JLabel statusLbl = createLabel(formCard, "Status *:", 16, 176, 95, 28);
        setFontStyle(statusLbl, Font.BOLD);
        setFontSize(statusLbl, 11);
        statusCombo = createComboBox(formCard, new String[]{"Pending", "Confirmed", "Completed", "Cancelled"}, 115, 176, 214, 28);

        // Payment Summary Box
        JPanel payCard = new JPanel(null);
        payCard.setBounds(16, 216, 313, 68);
        payCard.setBackground(new Color(248, 250, 252));
        payCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, COLOR_PRIMARY),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            )
        ));

        JLabel payTitle = createLabel(payCard, "PAYMENT SUMMARY", 12, 8, 280, 16);
        setFontStyle(payTitle, Font.BOLD);
        setFontSize(payTitle, 10);
        setTextColor(payTitle, COLOR_PRIMARY);

        paymentStatusLbl = createLabel(payCard, "Select a booking to view payments", 12, 32, 280, 24);
        setFontStyle(paymentStatusLbl, Font.BOLD);
        setFontSize(paymentStatusLbl, 12);
        setTextColor(paymentStatusLbl, COLOR_TEXT_MUTED);
        formCard.add(payCard);

        // Action Buttons inside Card
        createStyledButton(formCard, "View Passenger Manifest", 16, 296, 313, 34,
                new Color(241, 245, 249), new Color(15, 23, 42), new Color(203, 213, 225), 12, this::handleViewManifest);

        createPrimaryButton(formCard, "Update Status", 16, 338, 313, 34, this::handleUpdateStatus);
        createSecondaryButton(formCard, "Clear Selection", 16, 380, 313, 30, this::clearForm);

        JLabel helperHint = createLabel(formCard, "Click any reservation in table to view or update", 16, 424, 313, 16);
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
            loadBookings(null);
        });
        setFontSize(showAllBtn, 12);

        String[] columns = {"Customer", "Package", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingTable = createTable(tableModel, 410, 132, 745, 511);
        bookingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setColumnWidths(bookingTable, 175, 195, 105, 135, 135);
        setStatusBadgeRenderer(bookingTable, 2);

        if (bookingTable.getParent() != null && bookingTable.getParent().getParent() instanceof JScrollPane) {
            tableScrollPane = (JScrollPane) bookingTable.getParent().getParent();
        }

        bookingTable.getSelectionModel().addListSelectionListener(e -> {
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
                    b.customerName != null ? b.customerName : "Customer",
                    b.packageName != null ? b.packageName : "Package",
                    b.status != null ? b.status : "Pending",
                    b.travelDate != null ? b.travelDate.toString() : "TBD",
                    b.bookingDate != null ? b.bookingDate.toString() : "N/A"
                });
            }
        } catch (SQLException ex) {
            showError("Database error loading reservations: " + ex.getMessage());
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < currentRecords.size()) {
                BookingRecord b = currentRecords.get(modelRow);
                idField.setText(String.valueOf(b.bookingID));
                customerField.setText(b.customerName != null ? b.customerName : "Customer");
                packageField.setText(b.packageName != null ? b.packageName : "Package");
                travelDateField.setText(b.travelDate != null ? b.travelDate.toString() : "");
                bookingDateField.setText(b.bookingDate != null ? b.bookingDate.toString() : "");
                statusCombo.setSelectedItem(b.status != null ? b.status : "Pending");

                // Look up payments for this booking
                try {
                    BigDecimal paid = paymentDAO.getTotalPaidForBooking(b.bookingID);
                    if (paid != null && paid.compareTo(BigDecimal.ZERO) > 0) {
                        paymentStatusLbl.setText("Paid: PHP " + String.format("%,.2f", paid));
                        setTextColor(paymentStatusLbl, COLOR_SUCCESS);
                    } else {
                        paymentStatusLbl.setText("Unpaid (0.00 collected)");
                        setTextColor(paymentStatusLbl, COLOR_DANGER);
                    }
                } catch (SQLException ex) {
                    paymentStatusLbl.setText("Payment info unavailable");
                    setTextColor(paymentStatusLbl, COLOR_TEXT_MUTED);
                }
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
        JDialog dialog = new JDialog(this, "Passenger Manifest - " + customerName, true);
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

        JLabel titleLbl = new JLabel("Passenger Manifest");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(new Color(15, 23, 42));

        JLabel subLbl = new JLabel("Customer: " + customerName + "  •  Package: " + packageName);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(COLOR_TEXT_MUTED);

        topPanel.add(titleLbl, BorderLayout.NORTH);
        topPanel.add(subLbl, BorderLayout.SOUTH);
        dialog.add(topPanel, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"First Name", "Last Name", "Date of Birth", "Contact Number"};
        DefaultTableModel manifestModel = createReadOnlyTableModel(columns);
        JTable manifestTable = new JTable(manifestModel);
        manifestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        manifestTable.setRowHeight(30);
        manifestTable.setShowVerticalLines(false);
        manifestTable.setShowHorizontalLines(true);
        manifestTable.setGridColor(new Color(241, 245, 249));
        manifestTable.setSelectionBackground(new Color(219, 234, 254));

        manifestTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    lbl.setForeground(COLOR_TEXT_MAIN);
                }
                return lbl;
            }
        });

        if (manifestTable.getTableHeader() != null) {
            manifestTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            manifestTable.getTableHeader().setBackground(new Color(241, 245, 249));
            manifestTable.getTableHeader().setForeground(new Color(51, 65, 85));
            manifestTable.getTableHeader().setPreferredSize(new Dimension(dialog.getWidth(), 32));
        }

        try {
            List<TravelerRecord> travelers = bookingDAO.getTravelersByBooking(bookingId);
            if (travelers.isEmpty()) {
                manifestModel.addRow(new Object[]{"No travelers recorded", "-", "-", "-"});
            } else {
                for (TravelerRecord t : travelers) {
                    manifestModel.addRow(new Object[]{
                        t.firstName != null ? t.firstName : "",
                        t.lastName != null ? t.lastName : "",
                        t.dateOfBirth != null ? t.dateOfBirth.toString() : "N/A",
                        t.contactNumber != null ? t.contactNumber : "N/A"
                    });
                }
            }
        } catch (SQLException ex) {
            manifestModel.addRow(new Object[]{"Error loading travelers: " + ex.getMessage(), "-", "-", "-"});
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
                showInfo("Reservation status updated to '" + newStatus + "'.");
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