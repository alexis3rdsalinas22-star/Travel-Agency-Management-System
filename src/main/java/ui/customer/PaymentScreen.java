package ui.customer;

import common.Screen;
import dao.BookingDAO;
import dao.PaymentDAO;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
        referenceField = addFormField(detailsCard, "Transaction Reference", 204, true);

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
        int cardH = 370;

        detailsCard.setBounds(0, 10, detailsW, cardH);
        summaryCard.setBounds(detailsW + gap, 10, summaryW, cardH);

        container.setPreferredSize(new Dimension(contentWidth - 10, cardH + 30));
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
        referenceValue.setText(cash ? "Not required" : (ref.isEmpty() ? "Pending entry" : ref));
        hintLabel.setText(hintFor(method));
    }

    private static String hintFor(String method) {
        if (method == null) return "";
        switch (method) {
            case "Cash":
                return "Cash settlements are verified upon counter check-in.";
            case "GCash":
                return "Required: enter your GCash reference or transaction code.";
            case "Bank Transfer":
                return "Required: enter your online bank deposit / transfer reference.";
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

        String method = (String) methodCombo.getSelectedItem();
        String reference = getTrimmedText(referenceField);
        boolean needsReference = "GCash".equals(method) || "Bank Transfer".equals(method);
        if (needsReference && reference.isEmpty()) {
            showError("A reference number is required for " + method + ".");
            return;
        }
        if (reference.length() > MAX_REFERENCE_LENGTH) {
            showError("Reference must be " + MAX_REFERENCE_LENGTH + " characters or less.");
            return;
        }

        if (confirm(String.format("Confirm payment of PHP %,.2f via %s for Reservation #%d?", amount, method, bookingId))) {
            try {
                String source = reference != null && !reference.isEmpty() ? reference : "Customer Checkout";
                int payId = paymentDAO.recordPayment(bookingId, amount, method, source);
                if (payId > 0) {
                    bookingDAO.updateBookingStatus(bookingId, "Confirmed");
                    showInfo("Payment recorded successfully! (Receipt #" + payId + ")\nYour reservation is now Confirmed.");
                    navigateTo(new MyBookings());
                } else {
                    showError("Failed to record payment transaction.");
                }
            } catch (SQLException ex) {
                showError("Database error recording payment: " + ex.getMessage());
            }
        }
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