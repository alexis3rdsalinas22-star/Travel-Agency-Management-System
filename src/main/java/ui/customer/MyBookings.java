package ui.customer;

import common.Screen;
import common.Session;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;
import dao.BookingDAO.TravelerRecord;
import dao.PaymentDAO;
import dao.PaymentDAO.PaymentRecord;
import ui.BrowsePackages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MyBookings extends Screen {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final List<BookingRecord> customerBookings = new ArrayList<>();

    private JLabel logoLbl;
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

    private JButton viewVoucherBtn;
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
        ImageIcon logo = getLogoIcon(38, 38);
        if (logo != null) {
            logoLbl = new JLabel(logo);
            add(logoLbl);
        }

        int textX = 93;
        categoryTag = createLabel("CUSTOMER PORTAL  •  MY RESERVATIONS", textX, 14, 550, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        header = createHeader("My Bookings & Reservations", textX, 30, 550, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        subheader = createLabel("Review your travel reservations, check departure dates, and settle pending payments.", textX, 60, 650, 18);
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
        ImageIcon pkgIcon = loadIcon("/images/icons/packages.png", 16, 16);
        if (pkgIcon != null) {
            bookNewBtn.setIcon(pkgIcon);
        }

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

        searchBtn = createPrimaryButton(this, "Search", 720, 88, 95, 34, this::applyFilter);
        searchBtn.setIcon(loadIcon("/images/icons/search.png", 14, 14));
        setFontSize(searchBtn, 12);

        showAllBtn = createSecondaryButton(this, "Show All", 825, 88, 95, 34, () -> {
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

        bookingsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bookingsTable.getSelectedRow() >= 0) {
                    handleViewVoucher();
                }
            }
        });

        // Bottom Action Buttons
        viewVoucherBtn = createPrimaryButton(this, "View Voucher & Manifest →", 45, 580, 235, 38, this::handleViewVoucher);
        viewVoucherBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewVoucherBtn.setBackground(new Color(79, 70, 229)); // Indigo
        viewVoucherBtn.setIcon(loadIcon("/images/icons/receipt.png", 16, 16));
        viewVoucherBtn.setEnabled(false);

        payNowBtn = createPrimaryButton(this, "Pay Now →", 750, 580, 140, 38, this::handlePayNow);
        payNowBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        payNowBtn.setBackground(new Color(5, 150, 105)); // Emerald
        payNowBtn.setIcon(loadIcon("/images/icons/wallet.png", 16, 16));
        payNowBtn.setEnabled(false);

        cancelBtn = createDangerButton(this, "Cancel Booking", 900, 580, 155, 38, this::handleCancelBooking);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelBtn.setIcon(loadIcon("/images/icons/delete.png", 16, 16));
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

        int textX = pad;
        if (logoLbl != null) {
            logoLbl.setBounds(pad, 18, 38, 38);
            textX = pad + 48;
        }

        categoryTag.setBounds(textX, 14, 550, 16);
        header.setBounds(textX, 30, 550, 30);
        subheader.setBounds(textX, 60, 650, 18);

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

        viewVoucherBtn.setBounds(pad, btnY, 230, btnHeight);
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
            viewVoucherBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            payNowBtn.setEnabled(false);
            return;
        }

        int modelRow = bookingsTable.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < customerBookings.size()) {
            BookingRecord b = customerBookings.get(modelRow);
            String status = b.status != null ? b.status : "";
            boolean isPastDeparture = b.travelDate != null && b.travelDate.before(new java.util.Date());

            viewVoucherBtn.setEnabled(true);

            boolean canCancel = (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Confirmed")) && !isPastDeparture;
            cancelBtn.setEnabled(canCancel);

            boolean canPay = status.equalsIgnoreCase("Pending") && !isPastDeparture;
            payNowBtn.setEnabled(canPay);
        }
    }

    private void handleCancelBooking() {
        int viewRow = bookingsTable.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = bookingsTable.convertRowIndexToModel(viewRow);
        BookingRecord b = customerBookings.get(modelRow);

        if (b.travelDate != null && b.travelDate.before(new java.util.Date())) {
            showError("Cannot cancel a reservation whose scheduled travel date has already passed.");
            return;
        }
        if ("Completed".equalsIgnoreCase(b.status)) {
            showError("Completed trips cannot be cancelled.");
            return;
        }
        if ("Cancelled".equalsIgnoreCase(b.status)) {
            showError("This booking is already cancelled.");
            return;
        }

        List<PaymentRecord> payments;
        try {
            payments = paymentDAO.getPaymentsByBooking(b.bookingID);
        } catch (SQLException ex) {
            payments = new ArrayList<>();
        }

        BigDecimal totalPaid = BigDecimal.ZERO;
        for (PaymentRecord p : payments) {
            if (p.amount != null) {
                totalPaid = totalPaid.add(p.amount);
            }
        }

        String message;
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            message = "CANCELLATION & REFUND POLICY NOTICE:\n\n"
                    + "A payment of PHP " + String.format("%,.2f", totalPaid) + " has already been recorded for Booking #BK-" + b.bookingID + " (\"" + b.packageName + "\").\n\n"
                    + "In accordance with agency terms, cancellation of paid or confirmed reservations is subject to administrative review\n"
                    + "and standard cancellation policy before refund eligibility is confirmed.\n\n"
                    + "Are you sure you wish to proceed with cancelling this reservation?";
        } else {
            message = "Are you sure you want to cancel your reservation for \"" + b.packageName + "\" (#BK-" + b.bookingID + ")?";
        }

        if (confirm(message)) {
            try {
                boolean ok = bookingDAO.updateBookingStatus(b.bookingID, "Cancelled");
                if (ok) {
                    showInfo("Your reservation for \"" + b.packageName + "\" (#BK-" + b.bookingID + ") has been cancelled.");
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

    private void handleViewVoucher() {
        int viewRow = bookingsTable.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = bookingsTable.convertRowIndexToModel(viewRow);
        BookingRecord b = customerBookings.get(modelRow);
        showVoucherDialog(b);
    }

    private void showVoucherDialog(BookingRecord b) {
        JDialog dialog = new JDialog(this, "Itinerary Voucher & Manifest — #BK-" + b.bookingID, true);
        dialog.setSize(880, 780);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_BG);

        // Header Card
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHeader.setOpaque(false);
        ImageIcon logo = getLogoIcon(40, 40);
        if (logo != null) {
            leftHeader.add(new JLabel(logo));
        }
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel brandTitle = new JLabel("GOD TRAVEL & TOURS MANAGEMENT");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandTitle.setForeground(COLOR_PRIMARY);

        JLabel subDocTitle = new JLabel("OFFICIAL BOOKING ITINERARY VOUCHER & PASSENGER MANIFEST");
        subDocTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subDocTitle.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(brandTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subDocTitle);
        leftHeader.add(titleBox);

        JPanel rightHeader = new JPanel();
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));
        rightHeader.setOpaque(false);

        JLabel refLbl = new JLabel("BOOKING REF: #BK-" + b.bookingID);
        refLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refLbl.setForeground(COLOR_TEXT_MAIN);
        refLbl.setAlignmentX(1.0f);

        JLabel statusBadge = new JLabel(b.status != null ? b.status.toUpperCase() : "PENDING");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBadge.setAlignmentX(1.0f);

        String st = b.status != null ? b.status : "Pending";
        if ("Confirmed".equalsIgnoreCase(st)) {
            statusBadge.setBackground(new Color(220, 252, 231));
            statusBadge.setForeground(new Color(22, 101, 52));
        } else if ("Completed".equalsIgnoreCase(st)) {
            statusBadge.setBackground(new Color(224, 242, 254));
            statusBadge.setForeground(new Color(7, 89, 133));
        } else if ("Cancelled".equalsIgnoreCase(st)) {
            statusBadge.setBackground(new Color(254, 226, 226));
            statusBadge.setForeground(new Color(153, 27, 27));
        } else {
            statusBadge.setBackground(new Color(254, 243, 199));
            statusBadge.setForeground(new Color(146, 64, 14));
        }

        rightHeader.add(refLbl);
        rightHeader.add(Box.createVerticalStrut(4));
        rightHeader.add(statusBadge);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        dialog.add(headerPanel, BorderLayout.NORTH);

        // Printable Body Content
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Trip & Reservation Details Card
        JPanel tripCard = createCardPanel();
        tripCard.setLayout(new BorderLayout(0, 10));
        JLabel tripHeading = createSectionHeader("TRIP & RESERVATION DETAILS");
        tripCard.add(tripHeading, BorderLayout.NORTH);

        JPanel tripGrid = new JPanel(new GridLayout(3, 2, 16, 8));
        tripGrid.setOpaque(false);
        tripGrid.add(createFieldPair("Tour Package:", b.packageName != null ? b.packageName : ("Package #" + b.packageID)));
        tripGrid.add(createFieldPair("Destination:", b.destinationName != null ? b.destinationName : "General Destination"));
        tripGrid.add(createFieldPair("Departure Date:", b.travelDate != null ? dateFormat.format(b.travelDate) : "TBD"));
        tripGrid.add(createFieldPair("Date Booked:", b.bookingDate != null ? dateFormat.format(b.bookingDate) : "—"));
        tripGrid.add(createFieldPair("Primary Booker:", b.customerName != null ? b.customerName : "Customer Account #" + b.customerAccountID));
        int paxCount = b.travelerCount > 0 ? b.travelerCount : 1;
        tripGrid.add(createFieldPair("Passenger Count:", paxCount + " Passenger" + (paxCount > 1 ? "s" : "")));
        tripCard.add(tripGrid, BorderLayout.CENTER);

        if (b.specialRequests != null && !b.specialRequests.trim().isEmpty()) {
            JPanel notesWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            notesWrap.setOpaque(false);
            JLabel nLbl = new JLabel("Special Requests / Notes:");
            nLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nLbl.setForeground(new Color(71, 85, 105));
            JLabel nVal = new JLabel(b.specialRequests);
            nVal.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            nVal.setForeground(COLOR_TEXT_MAIN);
            notesWrap.add(nLbl);
            notesWrap.add(nVal);
            tripCard.add(notesWrap, BorderLayout.SOUTH);
        }

        bodyPanel.add(tripCard);
        bodyPanel.add(Box.createVerticalStrut(16));

        // 2. Passenger Manifest Card
        JPanel manifestCard = createCardPanel();
        manifestCard.setLayout(new BorderLayout(0, 10));

        List<TravelerRecord> travelers = new ArrayList<>();
        try {
            travelers = bookingDAO.getTravelersByBooking(b.bookingID);
        } catch (SQLException ex) {
            // fallback
        }

        JLabel manifestHeading = createSectionHeader("PASSENGER MANIFEST (" + (travelers.isEmpty() ? 1 : travelers.size()) + " Registered)");
        manifestCard.add(manifestHeading, BorderLayout.NORTH);

        if (!travelers.isEmpty()) {
            String[] tCols = {"#", "First Name", "Last Name", "Date of Birth", "Contact Number"};
            DefaultTableModel tModel = new DefaultTableModel(tCols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            int idx = 1;
            for (TravelerRecord tr : travelers) {
                String dobStr = tr.dateOfBirth != null ? dateFormat.format(tr.dateOfBirth) : "—";
                tModel.addRow(new Object[]{
                    "Pax " + idx++,
                    tr.firstName != null ? tr.firstName : "—",
                    tr.lastName != null ? tr.lastName : "—",
                    dobStr,
                    tr.contactNumber != null && !tr.contactNumber.isEmpty() ? tr.contactNumber : "—"
                });
            }
            JTable tTable = new JTable(tModel);
            styleVoucherTable(tTable);
            tTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setColumnWidths(tTable, 70, 170, 170, 150, 150);
            JScrollPane tScroll = new JScrollPane(tTable);
            tScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
            tScroll.setPreferredSize(new Dimension(0, Math.min(160, 36 + travelers.size() * 28)));
            manifestCard.add(tScroll, BorderLayout.CENTER);
        } else {
            JPanel emptyManifest = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            emptyManifest.setOpaque(false);
            JLabel emptyLbl = new JLabel("<html><i>No individual passenger manifest recorded. Primary customer (<b>"
                    + (b.customerName != null ? b.customerName : "Primary Account") + "</b>) is the sole traveler on this reservation.</i></html>");
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            emptyLbl.setForeground(COLOR_TEXT_MUTED);
            emptyManifest.add(emptyLbl);
            manifestCard.add(emptyManifest, BorderLayout.CENTER);
        }
        bodyPanel.add(manifestCard);
        bodyPanel.add(Box.createVerticalStrut(16));

        // 3. Financial Summary & Payment Ledger Card
        JPanel ledgerCard = createCardPanel();
        ledgerCard.setLayout(new BorderLayout(0, 10));
        JLabel ledgerHeading = createSectionHeader("FINANCIAL SUMMARY & PAYMENT LEDGER");
        ledgerCard.add(ledgerHeading, BorderLayout.NORTH);

        List<PaymentRecord> payments = new ArrayList<>();
        try {
            payments = paymentDAO.getPaymentsByBooking(b.bookingID);
        } catch (SQLException ignored) {}

        BigDecimal totalCost = b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (PaymentRecord p : payments) {
            if (p.amount != null) {
                totalPaid = totalPaid.add(p.amount);
            }
        }
        BigDecimal balance = totalCost.subtract(totalPaid).max(BigDecimal.ZERO);

        JPanel finSummaryRow = new JPanel(new GridLayout(1, 3, 14, 0));
        finSummaryRow.setOpaque(false);
        finSummaryRow.add(createSummaryTile("TOTAL PACKAGE COST", String.format("PHP %,.2f", totalCost), COLOR_TEXT_MAIN));
        finSummaryRow.add(createSummaryTile("TOTAL AMOUNT PAID", String.format("PHP %,.2f", totalPaid), new Color(5, 150, 105)));
        finSummaryRow.add(createSummaryTile("BALANCE DUE", String.format("PHP %,.2f", balance), balance.compareTo(BigDecimal.ZERO) == 0 ? new Color(5, 150, 105) : new Color(217, 119, 6)));
        ledgerCard.add(finSummaryRow, BorderLayout.CENTER);

        if (!payments.isEmpty()) {
            final List<PaymentRecord> finalPayments = payments;
            final BigDecimal finalTotalPaid = totalPaid;
            final BigDecimal finalBalance = balance;

            String[] pCols = {"Transaction Date", "Payment Method", "Channel / Reference", "Amount"};
            DefaultTableModel pModel = new DefaultTableModel(pCols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            SimpleDateFormat dtFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            for (PaymentRecord pr : payments) {
                String pDate = pr.paymentDate != null ? dtFormat.format(pr.paymentDate) : "—";
                pModel.addRow(new Object[]{
                    pDate,
                    pr.method != null ? pr.method : "Cash",
                    pr.source != null && !pr.source.isEmpty() ? pr.source : "Customer Portal",
                    String.format("PHP %,.2f", pr.amount != null ? pr.amount : BigDecimal.ZERO)
                });
            }
            JTable pTable = new JTable(pModel);
            styleVoucherTable(pTable);
            pTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setColumnWidths(pTable, 180, 130, 160, 130);
            pTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            if (!finalPayments.isEmpty()) {
                pTable.setRowSelectionInterval(0, 0);
            }
            JScrollPane pScroll = new JScrollPane(pTable);
            pScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
            pScroll.setPreferredSize(new Dimension(0, Math.min(140, 36 + payments.size() * 28)));

            JButton viewOrBtn = createPrimaryButton(this, "View Official Receipt (OR) →", 0, 0, 240, 32, () -> {
                int row = pTable.getSelectedRow();
                if (row < 0 && !finalPayments.isEmpty()) {
                    row = 0;
                    pTable.setRowSelectionInterval(0, 0);
                }
                if (row >= 0 && row < finalPayments.size()) {
                    PaymentRecord pr = finalPayments.get(row);
                    PaymentScreen.showReceiptDialog(dialog, pr.paymentID, b.bookingID, pr.amount, pr.method, pr.source, pr.paymentDate, b, finalTotalPaid, finalBalance, null);
                } else {
                    showInfo("No payment records found for this reservation.");
                }
            });
            viewOrBtn.setIcon(loadIcon("/images/icons/receipt.png", 15, 15));
            viewOrBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            viewOrBtn.setBackground(new Color(5, 150, 105));

            pTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = pTable.getSelectedRow();
                        if (row < 0 && !finalPayments.isEmpty()) row = 0;
                        if (row >= 0 && row < finalPayments.size()) {
                            PaymentRecord pr = finalPayments.get(row);
                            PaymentScreen.showReceiptDialog(dialog, pr.paymentID, b.bookingID, pr.amount, pr.method, pr.source, pr.paymentDate, b, finalTotalPaid, finalBalance, null);
                        }
                    }
                }
            });

            JPanel pWrap = new JPanel(new BorderLayout(0, 8));
            pWrap.setOpaque(false);
            pWrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            pWrap.add(pScroll, BorderLayout.CENTER);

            JPanel pBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pBtnRow.setOpaque(false);
            pBtnRow.add(viewOrBtn);
            pWrap.add(pBtnRow, BorderLayout.SOUTH);

            ledgerCard.add(pWrap, BorderLayout.SOUTH);
        }

        bodyPanel.add(ledgerCard);
        bodyPanel.add(Box.createVerticalStrut(16));

        // 4. Important Travel Advisory & Terms
        JPanel advisoryCard = new JPanel(new BorderLayout(8, 6));
        advisoryCard.setBackground(new Color(241, 245, 249));
        advisoryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel advTitle = new JLabel("IMPORTANT TRAVEL ADVISORY & CHECK-IN NOTICE");
        advTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        advTitle.setForeground(new Color(71, 85, 105));

        JLabel advText = new JLabel("<html><body style='width: 680px;'>"
            + "• Present this official digital voucher and valid government-issued photo IDs (Passport / National ID) during departure check-in.<br>"
            + "• Check-in counters open 2 hours prior to scheduled departure. Please arrive promptly to complete baggage and manifest processing.<br>"
            + "• Cancellation and modification of confirmed bookings are subject to the agency refund and terms policy.</body></html>");
        advText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        advText.setForeground(new Color(100, 116, 139));

        advisoryCard.add(advTitle, BorderLayout.NORTH);
        advisoryCard.add(advText, BorderLayout.CENTER);
        bodyPanel.add(advisoryCard);

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton printBtn = createPrimaryButton(this, "Print / Save Voucher", 0, 0, 200, 36, () -> {
            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("GOD_Travel_Voucher_BK" + b.bookingID);
                job.setPrintable((graphics, pageFormat, pageIndex) -> {
                    if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    double scale = Math.min(
                        pageFormat.getImageableWidth() / bodyPanel.getWidth(),
                        pageFormat.getImageableHeight() / bodyPanel.getHeight()
                    );
                    if (scale < 1.0) {
                        g2d.scale(scale, scale);
                    }
                    bodyPanel.paint(g2d);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });
                if (job.printDialog()) {
                    job.print();
                    showInfo("Voucher sent to printer successfully.");
                }
            } catch (Exception ex) {
                showError("Printing error: " + ex.getMessage());
            }
        });
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        printBtn.setBackground(COLOR_PRIMARY);
        printBtn.setIcon(loadIcon("/images/icons/print.png", 16, 16));

        JButton closeBtn = createSecondaryButton(this, "Close", 0, 0, 100, 36, dialog::dispose);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        footerPanel.add(printBtn);
        footerPanel.add(closeBtn);
        dialog.add(footerPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        return card;
    }

    private JLabel createSectionHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_PRIMARY);
        return lbl;
    }

    private JPanel createFieldPair(String labelText, String valueText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105));
        JLabel val = new JLabel(valueText);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(COLOR_TEXT_MAIN);
        panel.add(lbl);
        panel.add(val);
        return panel;
    }

    private JPanel createSummaryTile(String title, String value, Color valueColor) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(new Color(248, 250, 252));
        tile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tLbl.setForeground(COLOR_TEXT_MUTED);

        JLabel vLbl = new JLabel(value);
        vLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vLbl.setForeground(valueColor);

        tile.add(tLbl);
        tile.add(Box.createVerticalStrut(4));
        tile.add(vLbl);
        return tile;
    }

    private void styleVoucherTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean isSel, boolean hasFocus, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                comp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                comp.setForeground(COLOR_TEXT_MAIN);
                return comp;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        if (table.getTableHeader() != null) {
            table.getTableHeader().setPreferredSize(new Dimension(0, 32));
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
            table.getTableHeader().setBackground(new Color(241, 245, 249));
            table.getTableHeader().setForeground(new Color(71, 85, 105));
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
            table.getTableHeader().setReorderingAllowed(false);
        }
    }
}