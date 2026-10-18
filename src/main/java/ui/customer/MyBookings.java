package ui.customer;

import common.Screen;
import common.Session;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;
import ui.BrowsePackages;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MyBookings extends Screen {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final List<BookingRecord> customerBookings = new ArrayList<>();

    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;
    private JButton bookNewBtn;

    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchBtn;
    private JButton showAllBtn;
    private JLabel countLabel;

    private DefaultTableModel tableModel;
    private JTable bookingsTable;
    private JScrollPane tableScrollPane;
    private TableRowSorter<DefaultTableModel> sorter;

    private JButton cancelBtn;
    private JButton payNowBtn;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");

    public MyBookings() {
        super("My Bookings - Customer Portal", 1100, 650);

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        initUI();
        loadBookings();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    private void initUI() {
        categoryTag = createLabel("CUSTOMER PORTAL  •  MY RESERVATIONS", 45, 14, 550, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        header = createHeader("My Bookings & Reservations", 45, 30, 550, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        subheader = createLabel("Review your travel reservations, check departure dates, and settle pending payments.", 45, 60, 650, 18);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        backBtn = createSecondaryButton(this, "← Dashboard", 930, 20, 130, 36, () -> {
            navigateTo(new CustomerDashboard());
        });
        setFontStyle(backBtn, Font.BOLD);
        setFontSize(backBtn, 12);

        bookNewBtn = createPrimaryButton(this, "Book New Tour →", 760, 20, 155, 36, () -> {
            navigateTo(new BrowsePackages());
        });
        setFontSize(bookNewBtn, 12);

        countLabel = createLabel("", 750, 62, 310, 18);
        setFontStyle(countLabel, Font.BOLD);
        setFontSize(countLabel, 11);
        setTextColor(countLabel, COLOR_PRIMARY);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Search Bar components
        searchLabel = createLabel("Search:", 45, 88, 55, 34);
        setFontStyle(searchLabel, Font.BOLD);
        setFontSize(searchLabel, 11);
        setTextColor(searchLabel, new Color(71, 85, 105));

        searchField = createTextField(105, 88, 600, 34);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBackground(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));
        searchField.addActionListener(e -> applyFilter());

        searchBtn = createPrimaryButton(this, "Search", 720, 88, 85, 34, this::applyFilter);
        setFontSize(searchBtn, 12);

        showAllBtn = createSecondaryButton(this, "Show All", 815, 88, 95, 34, () -> {
            searchField.setText("");
            applyFilter();
        });
        setFontSize(showAllBtn, 12);

        // Read-only Table (Zero ID columns)
        String[] columns = {"Package Name", "Destination", "Travel Date", "Booking Date", "Amount Due", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        bookingsTable = createTable(tableModel, 45, 134, 1010, 430);
        tableScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, bookingsTable);

        sorter = new TableRowSorter<>(tableModel);
        bookingsTable.setRowSorter(sorter);
        setStatusBadgeRenderer(bookingsTable, 5);

        bookingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        });

        // Bottom Action Buttons
        payNowBtn = createPrimaryButton(this, "Pay Now →", 750, 580, 140, 38, this::handlePayNow);
        payNowBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        payNowBtn.setBackground(new Color(5, 150, 105)); // Emerald
        payNowBtn.setEnabled(false);

        cancelBtn = createDangerButton(this, "Cancel Booking", 900, 580, 155, 38, this::handleCancelBooking);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelBtn.setEnabled(false);
    }

    private void relayout() {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();
        if (w <= 0 || h <= 0) {
            w = getWidth() - 16;
            h = getHeight() - 39;
        }
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int contentWidth = w - (2 * pad);

        categoryTag.setBounds(pad, 14, 550, 16);
        header.setBounds(pad, 30, 550, 30);
        subheader.setBounds(pad, 60, 650, 18);

        backBtn.setBounds(w - pad - 130, 20, 130, 36);
        bookNewBtn.setBounds(w - pad - 130 - 170, 20, 155, 36);
        countLabel.setBounds(w - pad - 350, 62, 340, 18);

        int showAllWidth = 95;
        int searchBtnWidth = 85;
        int gap = 8;
        int buttonsTotal = searchBtnWidth + gap + showAllWidth;
        int labelWidth = 55;

        searchLabel.setBounds(pad, 88, labelWidth, 34);
        searchField.setBounds(pad + labelWidth + 5, 88, contentWidth - labelWidth - 5 - buttonsTotal - gap, 34);
        searchBtn.setBounds(pad + contentWidth - buttonsTotal, 88, searchBtnWidth, 34);
        showAllBtn.setBounds(pad + contentWidth - showAllWidth, 88, showAllWidth, 34);

        int bottomMargin = 22;
        int btnHeight = 38;
        int btnY = h - btnHeight - bottomMargin;

        int tableY = 134;
        int tableHeight = Math.max(180, btnY - tableY - 16);

        if (tableScrollPane != null) {
            tableScrollPane.setBounds(pad, tableY, contentWidth, tableHeight);
            bookingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setColumnWidths(bookingsTable,
                (int)(contentWidth * 0.28),
                (int)(contentWidth * 0.20),
                (int)(contentWidth * 0.15),
                (int)(contentWidth * 0.15),
                (int)(contentWidth * 0.11),
                (int)(contentWidth * 0.11)
            );
        }

        cancelBtn.setBounds(pad + contentWidth - 155, btnY, 155, btnHeight);
        payNowBtn.setBounds(pad + contentWidth - 155 - 150, btnY, 140, btnHeight);
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        customerBookings.clear();

        int custAccId = Session.getCustomerAccountId();
        int custId = Session.getCustomerId();

        try {
            List<BookingRecord> list = bookingDAO.getBookingsByCustomer(custAccId, custId);
            customerBookings.addAll(list);

            for (BookingRecord b : list) {
                String travelDateStr = b.travelDate != null ? dateFormat.format(b.travelDate) : "TBD";
                String bookDateStr = b.bookingDate != null ? dateFormat.format(b.bookingDate) : "—";
                BigDecimal totalDue = b.getTotalAmount();
                String priceStr;
                if (totalDue != null && totalDue.compareTo(BigDecimal.ZERO) > 0) {
                    if (b.travelerCount > 1) {
                        priceStr = String.format("PHP %,.2f (%d pax)", totalDue, b.travelerCount);
                    } else {
                        priceStr = String.format("PHP %,.2f", totalDue);
                    }
                } else {
                    priceStr = "PHP 0.00";
                }

                tableModel.addRow(new Object[]{
                    b.packageName != null ? b.packageName : ("Package #" + b.packageID),
                    b.destinationName != null ? b.destinationName : "General Destination",
                    travelDateStr,
                    bookDateStr,
                    priceStr,
                    b.status != null ? b.status : "Pending"
                });
            }

            refreshCount();
            updateActionButtons();

        } catch (SQLException ex) {
            showError("Database error loading reservations: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
        refreshCount();
    }

    private void refreshCount() {
        int shown = bookingsTable.getRowCount();
        if (countLabel != null) {
            countLabel.setText(shown == 0 ? "No reservations found" : "Showing: " + shown + " reservation(s)");
        }
    }

    private void updateActionButtons() {
        int viewRow = bookingsTable.getSelectedRow();
        if (viewRow < 0) {
            cancelBtn.setEnabled(false);
            payNowBtn.setEnabled(false);
            return;
        }

        int modelRow = bookingsTable.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < customerBookings.size()) {
            BookingRecord b = customerBookings.get(modelRow);
            String status = b.status != null ? b.status : "";

            boolean canCancel = status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Confirmed");
            cancelBtn.setEnabled(canCancel);

            boolean canPay = status.equalsIgnoreCase("Pending");
            payNowBtn.setEnabled(canPay);
        }
    }

    private void handleCancelBooking() {
        int viewRow = bookingsTable.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = bookingsTable.convertRowIndexToModel(viewRow);
        BookingRecord b = customerBookings.get(modelRow);

        if (confirm("Are you sure you want to cancel your reservation for \"" + b.packageName + "\"?")) {
            try {
                boolean ok = bookingDAO.updateBookingStatus(b.bookingID, "Cancelled");
                if (ok) {
                    showInfo("Your reservation for \"" + b.packageName + "\" has been cancelled.");
                    loadBookings();
                } else {
                    showError("Failed to cancel reservation. Please try again.");
                }
            } catch (SQLException ex) {
                showError("Database error updating booking: " + ex.getMessage());
            }
        }
    }

    private void handlePayNow() {
        int viewRow = bookingsTable.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = bookingsTable.convertRowIndexToModel(viewRow);
        BookingRecord b = customerBookings.get(modelRow);

        BigDecimal amount = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
        navigateTo(new PaymentScreen(b.bookingID, amount, b.packageName));
    }
}