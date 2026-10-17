package ui.customer;

import common.Screen;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PaymentScreen extends Screen {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99"); 
    private static final int MAX_REFERENCE_LENGTH = 45;                          

    private JTextField bookingIdField;
    private JTextField amountField;
    private JComboBox<String> methodCombo;
    private JTextField referenceField;


    private JLabel amountDisplay;
    private JLabel bookingValue;
    private JLabel methodValue;
    private JLabel referenceValue;
    private JLabel hintLabel;

    public PaymentScreen() {
        super("Payment & Checkout");
        initUI();
    }


    public PaymentScreen(int bookingId, BigDecimal amount) {
        this();
        bookingIdField.setText(String.valueOf(bookingId));
        amountField.setText(amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lockField(bookingIdField);
        lockField(amountField);
    }

    private void initUI() {
  
        createTopHeader(
                "CUSTOMER",
                "Payment & Checkout",
                "Select your payment method to settle your booking.",
                () -> navigateTo(new CustomerDashboard())
        );

 
        JPanel detailsCard = createCard(45, 105, 345, 285);
        createCardTag(detailsCard, "PAYMENT DETAILS");

        bookingIdField = addFormField(detailsCard, "Booking ID *", 48, true);
        amountField    = addFormField(detailsCard, "Amount *", 100, true);
        methodCombo    = addComboRow(detailsCard, "Method *",
                new String[]{"Cash", "Credit Card", "Debit Card", "GCash", "Bank Transfer"}, 152);
        referenceField = addFormField(detailsCard, "Reference", 204, true);

    
        JPanel summaryCard = createCard(410, 105, 745, 285);
        createCardTag(summaryCard, "PAYMENT SUMMARY");

        JLabel totalLbl = createLabel(summaryCard, "TOTAL AMOUNT DUE", 24, 44, 300, 16);
        setFontStyle(totalLbl, Font.BOLD);
        setFontSize(totalLbl, 11);
        setTextColor(totalLbl, COLOR_TEXT_MUTED);

        amountDisplay = createLabel(summaryCard, "PHP 0.00", 24, 64, 697, 50);
        setFontStyle(amountDisplay, Font.BOLD);
        setFontSize(amountDisplay, 34);
        setTextColor(amountDisplay, COLOR_PRIMARY);

        JPanel divider = new JPanel();
        divider.setBounds(24, 128, 697, 1);
        divider.setBackground(COLOR_BORDER);
        summaryCard.add(divider);

        bookingValue   = addSummaryRow(summaryCard, "Booking",   148);
        methodValue    = addSummaryRow(summaryCard, "Method",    176);
        referenceValue = addSummaryRow(summaryCard, "Reference", 204);

        hintLabel = createLabel(summaryCard, "", 24, 240, 697, 20);
        setFontSize(hintLabel, 12);
        setTextColor(hintLabel, COLOR_TEXT_MUTED);


        createSecondaryButton("Clear Form", 710, 415, 215, 38, this::clearForm);
        createSuccessButton("Confirm Payment", 940, 415, 215, 38, this::handleConfirmPayment);


        onChange(bookingIdField, this::updateSummary);
        onChange(amountField, this::updateSummary);
        onChange(referenceField, this::updateSummary);
        methodCombo.addActionListener(e -> {
            if ("Cash".equals(methodCombo.getSelectedItem())) {
                referenceField.setText(""); // cash needs no reference
            }
            updateSummary();
        });

        updateSummary();
    }



    private void updateSummary() {
        BigDecimal amount = getPriceOrNull(amountField); // silent: null if empty/invalid
        amountDisplay.setText(String.format("PHP %,.2f", amount != null ? amount : BigDecimal.ZERO));

        String booking = getTrimmedText(bookingIdField);
        bookingValue.setText(booking.isEmpty() ? "—" : "#" + booking);

        String method = (String) methodCombo.getSelectedItem();
        methodValue.setText(method);

        boolean cash = "Cash".equals(method);
        referenceField.setEditable(!cash);
        setBackground(referenceField, cash ? COLOR_SECONDARY : Color.WHITE);

        String ref = getTrimmedText(referenceField);
        referenceValue.setText(cash ? "Not required" : (ref.isEmpty() ? "—" : ref));
        hintLabel.setText(hintFor(method));
    }

    private static String hintFor(String method) {
        switch (method) {
            case "Cash":
                return "Cash payments don't need a reference number.";
            case "GCash":
                return "Required: enter your GCash transaction reference number.";
            case "Bank Transfer":
                return "Required: enter your bank transfer reference number.";
            default:
                return "Optional: enter the card approval code.";
        }
    }



    private void handleConfirmPayment() {
        Integer bookingId = getIntOrNull(bookingIdField, "Booking ID");
        if (bookingId == null) {
            return;
        }
        if (bookingId <= 0) {
            showError("Booking ID must be greater than zero.");
            return;
        }

        BigDecimal amount = getPriceOrNull(amountField, "Amount");
        if (amount == null) {
            return;
        }
        if (amount.scale() > 2) {
            showError("Amount can have at most 2 decimal places.");
            return;
        }
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

        if (!confirm(String.format("Pay PHP %,.2f via %s for booking #%d?", amount, method, bookingId))) {
            return;
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
        JLabel label = createLabel(parent, labelText, 24, y, 110, 22);
        setFontSize(label, 12);
        setTextColor(label, COLOR_TEXT_MUTED);

        JLabel value = createLabel(parent, "—", 140, y, 581, 22);
        setFontStyle(value, Font.BOLD);
        setFontSize(value, 13);
        setTextColor(value, COLOR_TEXT_MAIN);
        return value;
    }

    
    private JComboBox<String> addComboRow(JPanel parent, String labelText, String[] items, int y) {
        JLabel lbl = createLabel(parent, labelText, 16, y, 95, 26);
        setFontStyle(lbl, Font.BOLD);
        setFontSize(lbl, 11);
        setTextColor(lbl, COLOR_TEXT_MAIN);

        JComboBox<String> combo = createComboBox(parent, items, 115, y, 214, 26);
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