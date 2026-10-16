package ui.customer;

import common.Screen;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class PaymentScreen extends Screen {

    private JTextField bookingIdField;
    private JTextField amountField;
    private JComboBox<String> methodCombo;
    private JTextField referenceField;

    public PaymentScreen() {
        super("Payment & Checkout", 650, 480);

        initUI();
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("Payment & Checkout", 50, 25, 400, 30);
        createLabel("Select your payment method to settle your booking.", 50, 55, 450, 20);

        // Form fields
        createLabel("Booking ID:", 50, 100, 130, 25);
        bookingIdField = createTextField(190, 100, 400, 30);

        createLabel("Amount Due (PHP):", 50, 145, 130, 25);
        amountField = createTextField(190, 145, 400, 30);

        createLabel("Payment Method *:", 50, 190, 130, 25);
        String[] methods = {"Cash", "Credit Card", "Debit Card", "GCash", "Bank Transfer"};
        methodCombo = createComboBox(methods, 190, 190, 400, 30);

        createLabel("Reference / Source:", 50, 235, 130, 25);
        referenceField = createTextField(190, 235, 400, 30);

        // Action Buttons
        JButton payBtn = createButton("Confirm Payment", 190, 310, 195, 38);
        // Note: No DAO function attached per instruction

        JButton backBtn = createButton("Back to Dashboard", 395, 310, 195, 38);
        backBtn.addActionListener(e -> navigateTo(new CustomerDashboard()));
    }
}