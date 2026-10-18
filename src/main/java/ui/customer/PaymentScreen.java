package ui.customer;

import common.Screen;
import dao.BookingDAO;
import dao.PaymentDAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PaymentScreen extends Screen {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");
    private static final int MAX_REFERENCE_LENGTH = 45;

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;

    private JScrollPane scrollPane;
    private JPanel container;

    private JPanel detailsCard;
    private JTextField bookingIdField;
    private JTextField amountField;
    private JComboBox<String> methodCombo;
    private JTextField referenceField;
    private JLabel refHelperLbl;

    private JPanel summaryCard;
    private JLabel amountDisplay;
    private JLabel packageValue;
    private JLabel bookingValue;
    private JLabel methodValue;
    private JLabel referenceValue;
    private JLabel hintLabel;
    private JButton confirmBtn;
    private JButton clearBtn;

    private String packageName = null;

    public PaymentScreen() {
        super("Payment & Checkout - Customer Portal", 1150, 700);

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        initUI();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    public PaymentScreen(int bookingId, BigDecimal amount) {
        this(bookingId, amount, null);
    }

    public PaymentScreen(int bookingId, BigDecimal amount, String packageName) {
        this();
        this.packageName = packageName;
        bookingIdField.setText(String.valueOf(bookingId));
        amountField.setText(amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lockField(bookingIdField);
        lockField(amountField);
        updateSummary();
    }

    private void initUI() {
        categoryTag = createLabel("CUSTOMER PORTAL  •  SECURE CHECKOUT", 45, 14, 600, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        header = createHeader("Payment & Checkout", 45, 30, 600, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        subheader = createLabel("Select your payment method to settle your reservation and confirm your travel itinerary.", 45, 60, 650, 18);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        backBtn = createSecondaryButton(this, "← My Bookings", 930, 20, 130, 36, () -> {
            navigateTo(new MyBookings());
        });
        setFontStyle(backBtn, Font.BOLD);
        setFontSize(backBtn, 12);

        container = new JPanel(null);
        container.setBackground(COLOR_BG);

        scrollPane = new JScrollPane(container);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane);

        buildDetailsCard();
        buildSummaryCard();
    }

    private void buildDetailsCard() {
        detailsCard = new JPanel(null);
        detailsCard.setBackground(Color.WHITE);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        createCardTag(detailsCard, "PAYMENT SPECIFICATIONS");

        bookingIdField = addFormField(detailsCard, "Reservation Ref *", 48, true);
        amountField    = addFormField(detailsCard, "Amount Due *", 100, true);
        methodCombo    = addComboRow(detailsCard, "Payment Method *",
                new String[]{"GCash", "Credit Card", "Debit Card", "Bank Transfer", "Cash"}, 152);
        referenceField = addFormField(detailsCard, "Transaction Ref *", 204, true);

        refHelperLbl = createLabel(detailsCard, "Required: enter your GCash reference or transaction code.", 140, 236, 280, 22);
        setFontSize(refHelperLbl, 10);
        setTextColor(refHelperLbl, COLOR_TEXT_MUTED);

        methodCombo.addActionListener(e -> {
            if ("Cash".equals(methodCombo.getSelectedItem())) {
                referenceField.setText("");
            }
            updateSummary();
        });

        onChange(bookingIdField, this::updateSummary);
        onChange(amountField, this::updateSummary);
        onChange(referenceField, this::updateSummary);

        container.add(detailsCard);
    }

    private void buildSummaryCard() {
        summaryCard = new JPanel(null);
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        createCardTag(summaryCard, "PAYMENT SUMMARY");

        JLabel totalLbl = createLabel(summaryCard, "TOTAL AMOUNT TO SETTLE", 24, 44, 300, 16);
        setFontStyle(totalLbl, Font.BOLD);
        setFontSize(totalLbl, 10);
        setTextColor(totalLbl, COLOR_TEXT_MUTED);

        amountDisplay = createLabel(summaryCard, "PHP 0.00", 24, 64, 400, 36);
        setFontStyle(amountDisplay, Font.BOLD);
        setFontSize(amountDisplay, 28);
        setTextColor(amountDisplay, new Color(5, 150, 105)); // Emerald 600

        JPanel divider = new JPanel();
        divider.setBounds(24, 110, 400, 1);
        divider.setBackground(new Color(241, 245, 249));
        summaryCard.add(divider);

        packageValue   = addSummaryRow(summaryCard, "Tour Package", 120);
        bookingValue   = addSummaryRow(summaryCard, "Reservation",  148);
        methodValue    = addSummaryRow(summaryCard, "Method",       176);
        referenceValue = addSummaryRow(summaryCard, "Reference",    204);

        hintLabel = createLabel(summaryCard, "", 24, 236, 400, 20);
        setFontSize(hintLabel, 11);
        setTextColor(hintLabel, COLOR_TEXT_MUTED);

        confirmBtn = createPrimaryButton(summaryCard, "Confirm Payment →", 24, 285, 230, 42, this::handleConfirmPayment);
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmBtn.setBackground(new Color(5, 150, 105));

        clearBtn = createSecondaryButton(summaryCard, "Clear", 265, 285, 90, 42, this::clearForm);
        setFontSize(clearBtn, 12);

        container.add(summaryCard);
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int contentWidth = w - (2 * pad);

        categoryTag.setBounds(pad, 14, 600, 16);
        header.setBounds(pad, 30, 600, 30);
        subheader.setBounds(pad, 60, 650, 18);
        backBtn.setBounds(w - pad - 130, 20, 130, 36);

        int scrollY = 96;
        int scrollHeight = Math.max(300, h - scrollY - 25);
        scrollPane.setBounds(pad, scrollY, contentWidth, scrollHeight);

        int gap = 24;
        int detailsW = 440;
        int summaryW = contentWidth - detailsW - gap - 10;
        int cardH = 390;

        detailsCard.setBounds(0, 10, detailsW, cardH);
        summaryCard.setBounds(detailsW + gap, 10, summaryW, cardH);

        container.setPreferredSize(new Dimension(contentWidth - 10, cardH + 40));
        container.revalidate();
        container.repaint();
    }

    private void updateSummary() {
        BigDecimal amount = getPriceOrNull(amountField);
        amountDisplay.setText(String.format("PHP %,.2f", amount != null ? amount : BigDecimal.ZERO));

        if (packageName != null) {
            packageValue.setText(packageName);
        } else {
            packageValue.setText("Standard Tour");
        }

        String booking = getTrimmedText(bookingIdField);
        bookingValue.setText(booking.isEmpty() ? "—" : "Reservation #" + booking);

        String method = (String) methodCombo.getSelectedItem();
        methodValue.setText(method != null ? method : "—");

        boolean cash = "Cash".equals(method);
        referenceField.setEditable(!cash);
        setBackground(referenceField, cash ? COLOR_SECONDARY : Color.WHITE);

        String ref = getTrimmedText(referenceField);
        referenceValue.setText(cash ? "Not required (Counter Clearance)" : (ref.isEmpty() ? "Pending entry" : ref));
        String hint = hintFor(method);
        hintLabel.setText(hint);
        if (refHelperLbl != null) {
            refHelperLbl.setText(hint);
        }
    }

    private static String hintFor(String method) {
        if (method == null) return "";
        switch (method) {
            case "Cash":
                return "Counter settlement verified upon cashier check-in.";
            case "GCash":
                return "Required: enter GCash 13-digit reference code.";
            case "Bank Transfer":
                return "Required: enter online bank deposit / transfer confirmation ref.";
            case "Credit Card":
                return "Required: enter card authorization / approval code.";
            case "Debit Card":
                return "Required: enter debit transaction authorization code.";
            default:
                return "Enter payment transaction code or approval number.";
        }
    }

    private void handleConfirmPayment() {
        Integer bookingId = getIntOrNull(bookingIdField, "Booking ID");
        if (bookingId == null || bookingId <= 0) {
            showError("Please specify a valid Booking ID.");
            return;
        }

        BigDecimal amount = getPriceOrNull(amountField, "Amount");
        if (amount == null) return;

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            showError("Amount is too large.");
            return;
        }

        // Verify booking existence and business status
        BookingDAO.BookingRecord b;
        try {
            b = bookingDAO.getBookingById(bookingId);
        } catch (SQLException ex) {
            showError("Database error checking reservation: " + ex.getMessage());
            return;
        }

        if (b == null) {
            showError("Reservation #" + bookingId + " does not exist in the system.");
            return;
        }

        if ("Cancelled".equalsIgnoreCase(b.status)) {
            showError("Reservation #" + bookingId + " has been cancelled. Payments cannot be accepted.");
            return;
        }

        if ("Completed".equalsIgnoreCase(b.status)) {
            showError("Reservation #" + bookingId + " is already completed.");
            return;
        }

        if (common.Session.isCustomer()) {
            int custAccId = common.Session.getCustomerAccountId();
            if (custAccId > 0 && b.customerAccountID != custAccId) {
                showError("Unauthorized: You may only make payments for your own reservations.");
                return;
            }
        }

        try {
            BigDecimal totalPaid = paymentDAO.getTotalPaidForBooking(bookingId);
            BigDecimal totalDue = b.getTotalAmount();
            if (totalPaid != null && totalDue != null && totalDue.compareTo(BigDecimal.ZERO) > 0 && totalPaid.compareTo(totalDue) >= 0) {
                showInfo(String.format("Reservation #%d is already fully paid (Total paid: PHP %,.2f).", bookingId, totalPaid));
                return;
            }
        } catch (SQLException ignored) {
            // Non-fatal check
        }

        String method = (String) methodCombo.getSelectedItem();
        String reference = getTrimmedText(referenceField);
        boolean needsReference = !"Cash".equals(method);
        if (needsReference && reference.isEmpty()) {
            showError("A transaction reference / confirmation code is required for " + method + ".");
            referenceField.requestFocusInWindow();
            return;
        }
        if (reference.length() > MAX_REFERENCE_LENGTH) {
            showError("Reference must be " + MAX_REFERENCE_LENGTH + " characters or less.");
            referenceField.requestFocusInWindow();
            return;
        }

        String targetPkg = (b.packageName != null && !b.packageName.isEmpty())
            ? b.packageName
            : (packageName != null ? packageName : "Reservation #" + bookingId);

        if (confirm(String.format("Confirm payment of PHP %,.2f via %s for \"%s\"?", amount, method, targetPkg))) {
            try {
                String source = reference != null && !reference.isEmpty() ? reference : "Cash / Counter Clearance";
                int payId = paymentDAO.recordPayment(bookingId, amount, method, source);
                if (payId > 0) {
                    bookingDAO.updateBookingStatus(bookingId, "Confirmed");
                    BigDecimal totalPaidNow = paymentDAO.getTotalPaidForBooking(bookingId);
                    BigDecimal totalDue = b.getTotalAmount();
                    BigDecimal remaining = totalDue != null ? totalDue.subtract(totalPaidNow).max(BigDecimal.ZERO) : BigDecimal.ZERO;
                    Timestamp payDate = new Timestamp(System.currentTimeMillis());

                    showReceiptDialog(this, payId, bookingId, amount, method, source, payDate, b, totalPaidNow, remaining, () -> {
                        navigateTo(new MyBookings());
                    });
                } else {
                    showError("Failed to record payment transaction.");
                }
            } catch (SQLException ex) {
                showError("Database error recording payment: " + ex.getMessage());
            }
        }
    }

    public static void showReceiptDialog(java.awt.Window parent, int payId, int bookingId, BigDecimal amount,
                                         String method, String source, Timestamp paymentDate,
                                         BookingDAO.BookingRecord b, BigDecimal totalPaid, BigDecimal balanceRemaining,
                                         Runnable onClose) {
        String orNumber = String.format("#OR-%06d", payId);
        JDialog dialog;
        if (parent instanceof java.awt.Frame) {
            dialog = new JDialog((java.awt.Frame) parent, "Official Receipt — " + orNumber, true);
        } else if (parent instanceof java.awt.Dialog) {
            dialog = new JDialog((java.awt.Dialog) parent, "Official Receipt — " + orNumber, true);
        } else {
            dialog = new JDialog();
            dialog.setTitle("Official Receipt — " + orNumber);
            dialog.setModal(true);
        }
        dialog.setSize(740, 720);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_BG);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHeader.setOpaque(false);
        ImageIcon logo = null;
        try {
            java.net.URL url = PaymentScreen.class.getResource("/images/icons/logo.png");
            if (url != null) {
                java.awt.Image img = new ImageIcon(url).getImage().getScaledInstance(42, 42, java.awt.Image.SCALE_SMOOTH);
                logo = new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        if (logo != null) {
            leftHeader.add(new JLabel(logo));
        }

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel brandTitle = new JLabel("GOD TRAVEL & TOURS MANAGEMENT");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandTitle.setForeground(COLOR_PRIMARY);

        JLabel docTitle = new JLabel("ELECTRONIC OFFICIAL RECEIPT (OR)");
        docTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        docTitle.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(brandTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(docTitle);
        leftHeader.add(titleBox);

        JPanel rightHeader = new JPanel();
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));
        rightHeader.setOpaque(false);

        JLabel orLbl = new JLabel(orNumber);
        orLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        orLbl.setForeground(COLOR_TEXT_MAIN);
        orLbl.setAlignmentX(1.0f);

        SimpleDateFormat dtFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String dateStr = paymentDate != null ? dtFormat.format(paymentDate) : dtFormat.format(new Date());
        JLabel dateLbl = new JLabel(dateStr);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLbl.setForeground(COLOR_TEXT_MUTED);
        dateLbl.setAlignmentX(1.0f);

        JLabel statusBadge = new JLabel("PAID / CONFIRMED");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(220, 252, 231));
        statusBadge.setForeground(new Color(22, 101, 52));
        statusBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        statusBadge.setAlignmentX(1.0f);

        rightHeader.add(orLbl);
        rightHeader.add(Box.createVerticalStrut(2));
        rightHeader.add(dateLbl);
        rightHeader.add(Box.createVerticalStrut(4));
        rightHeader.add(statusBadge);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        dialog.add(headerPanel, BorderLayout.NORTH);

        // Body Content (Printable Panel)
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Reservation & Payer Card
        JPanel resCard = createReceiptCard("RESERVATION & PAYER SPECIFICATIONS");
        JPanel resGrid = new JPanel(new GridLayout(3, 2, 16, 8));
        resGrid.setOpaque(false);
        resGrid.add(createPair("Reservation Ref:", "#BK-" + bookingId));
        String pkgName = b != null && b.packageName != null ? b.packageName : ("Booking #" + bookingId);
        resGrid.add(createPair("Tour Package:", pkgName));
        String destName = b != null && b.destinationName != null ? b.destinationName : "General Tour";
        resGrid.add(createPair("Destination:", destName));
        String payerName = b != null && b.customerName != null ? b.customerName : "Primary Customer";
        resGrid.add(createPair("Payer / Account:", payerName));
        SimpleDateFormat dFormat = new SimpleDateFormat("MMM dd, yyyy");
        String travelDateStr = b != null && b.travelDate != null ? dFormat.format(b.travelDate) : "TBD";
        resGrid.add(createPair("Departure Date:", travelDateStr));
        int pax = b != null && b.travelerCount > 0 ? b.travelerCount : 1;
        resGrid.add(createPair("Party Size:", pax + " Traveler" + (pax > 1 ? "s" : "")));
        resCard.add(resGrid, BorderLayout.CENTER);
        bodyPanel.add(resCard);
        bodyPanel.add(Box.createVerticalStrut(16));

        // 2. Transaction Details & Amount Tendered Card
        JPanel txCard = createReceiptCard("TRANSACTION DETAILS");
        JPanel txGrid = new JPanel(new GridLayout(2, 2, 16, 8));
        txGrid.setOpaque(false);
        txGrid.add(createPair("Payment Method:", method != null ? method : "Cash"));
        txGrid.add(createPair("Transaction / Auth Ref:", source != null && !source.isEmpty() ? source : "Counter Clearance"));
        txGrid.add(createPair("Transaction Timestamp:", dateStr));
        txGrid.add(createPair("Payment Status:", "Approved / Recorded"));

        // Tendered Callout Box
        JPanel amountBox = new JPanel(new BorderLayout(8, 0));
        amountBox.setBackground(new Color(240, 253, 244));
        amountBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(187, 247, 208), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel amtTitle = new JLabel("TOTAL AMOUNT PAID THIS TRANSACTION:");
        amtTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        amtTitle.setForeground(new Color(22, 101, 52));

        JLabel amtVal = new JLabel(String.format("PHP %,.2f", amount != null ? amount : BigDecimal.ZERO));
        amtVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        amtVal.setForeground(new Color(21, 128, 61));

        amountBox.add(amtTitle, BorderLayout.WEST);
        amountBox.add(amtVal, BorderLayout.EAST);

        JPanel txWrap = new JPanel(new BorderLayout(0, 10));
        txWrap.setOpaque(false);
        txWrap.add(txGrid, BorderLayout.CENTER);
        txWrap.add(amountBox, BorderLayout.SOUTH);
        txCard.add(txWrap, BorderLayout.CENTER);

        bodyPanel.add(txCard);
        bodyPanel.add(Box.createVerticalStrut(16));

        // 3. Billing Ledger & Account Balance Card
        JPanel ledgerCard = createReceiptCard("FINANCIAL ACCOUNT LEDGER");
        BigDecimal totalCost = b != null ? b.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal curPaid = totalPaid != null ? totalPaid : amount;
        BigDecimal remBalance = balanceRemaining != null ? balanceRemaining : totalCost.subtract(curPaid).max(BigDecimal.ZERO);

        JPanel ledgerGrid = new JPanel(new GridLayout(1, 3, 12, 0));
        ledgerGrid.setOpaque(false);
        ledgerGrid.add(createTile("PACKAGE TOTAL VALUE", String.format("PHP %,.2f", totalCost), COLOR_TEXT_MAIN));
        ledgerGrid.add(createTile("TOTAL CUMULATIVE PAID", String.format("PHP %,.2f", curPaid), new Color(5, 150, 105)));
        String balStr = remBalance.compareTo(BigDecimal.ZERO) == 0 ? "PHP 0.00 (Fully Settled)" : String.format("PHP %,.2f", remBalance);
        Color balColor = remBalance.compareTo(BigDecimal.ZERO) == 0 ? new Color(5, 150, 105) : new Color(217, 119, 6);
        ledgerGrid.add(createTile("REMAINING BALANCE DUE", balStr, balColor));
        ledgerCard.add(ledgerGrid, BorderLayout.CENTER);
        bodyPanel.add(ledgerCard);
        bodyPanel.add(Box.createVerticalStrut(16));

        // 4. Legal / Tax Disclaimer Box
        JPanel noticeCard = new JPanel(new BorderLayout(8, 6));
        noticeCard.setBackground(new Color(241, 245, 249));
        noticeCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel noticeTitle = new JLabel("RECEIPT VERIFICATION & AUDIT ADVISORY");
        noticeTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        noticeTitle.setForeground(new Color(71, 85, 105));

        JLabel noticeBody = new JLabel("<html><body style='width: 600px;'>"
            + "• This Official Receipt (OR) acknowledges receipt of payment by GOD Travel & Tours Management.<br>"
            + "• Keep a digital or physical copy of this receipt for airline manifest, hotel boarding, and refund claim validation.<br>"
            + "• For billing concerns or invoice disputes, contact support@godtravel.com quoting your OR Number.</body></html>");
        noticeBody.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        noticeBody.setForeground(new Color(100, 116, 139));

        noticeCard.add(noticeTitle, BorderLayout.NORTH);
        noticeCard.add(noticeBody, BorderLayout.CENTER);
        bodyPanel.add(noticeCard);

        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        dialog.add(scroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton printBtn = new JButton("🖨️ Print / Save Receipt");
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        printBtn.setBackground(COLOR_PRIMARY);
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        printBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        printBtn.addActionListener(e -> {
            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("Receipt_" + orNumber);
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
                    javax.swing.JOptionPane.showMessageDialog(dialog, "Receipt sent to printer successfully.", "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(dialog, "Print failed: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(COLOR_SECONDARY);
        closeBtn.setForeground(new Color(71, 85, 105));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        closeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            dialog.dispose();
            if (onClose != null) {
                onClose.run();
            }
        });

        footer.add(printBtn);
        footer.add(closeBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private static JPanel createReceiptCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private static JPanel createPair(String labelText, String valueText) {
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

    private static JPanel createTile(String title, String value, Color valueColor) {
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
        vLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vLbl.setForeground(valueColor);

        tile.add(tLbl);
        tile.add(Box.createVerticalStrut(4));
        tile.add(vLbl);
        return tile;
    }

    private void clearForm() {
        methodCombo.setSelectedIndex(0);
        referenceField.setText("");
        if (bookingIdField.isEditable()) {
            bookingIdField.setText("");
        }
        if (amountField.isEditable()) {
            amountField.setText("");
        }
        updateSummary();
    }

    private void lockField(JTextField field) {
        field.setEditable(false);
        setBackground(field, COLOR_SECONDARY);
    }

    private JLabel addSummaryRow(JPanel parent, String labelText, int y) {
        JLabel label = createLabel(parent, labelText, 24, y, 120, 20);
        setFontSize(label, 11);
        setTextColor(label, COLOR_TEXT_MUTED);

        JLabel value = createLabel(parent, "—", 150, y, 320, 20);
        setFontStyle(value, Font.BOLD);
        setFontSize(value, 12);
        setTextColor(value, COLOR_TEXT_MAIN);
        return value;
    }

    private JComboBox<String> addComboRow(JPanel parent, String labelText, String[] items, int y) {
        JLabel lbl = createLabel(parent, labelText, 16, y, 120, 26);
        setFontStyle(lbl, Font.BOLD);
        setFontSize(lbl, 11);
        setTextColor(lbl, COLOR_TEXT_MAIN);

        JComboBox<String> combo = createComboBox(parent, items, 140, y, 260, 26);
        setFontSize(combo, 12);
        return combo;
    }

    private void onChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { action.run(); }
            @Override public void removeUpdate(DocumentEvent e)  { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        });
    }
}