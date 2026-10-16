package ui.customer;

import common.Screen;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class PaymentScreen extends Screen {

    private JTextField bookingIdField;
    private JTextField amountField;
    private JComboBox<String> methodCombo;
    private JTextField referenceField;

    public PaymentScreen() {
        super("Payment & Checkout", 1000, 600);

        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(new Color(245, 247, 250));

        // Header & Subtitle
        JLabel header = createHeader("Payment & Checkout", 200, 35, 600, 32);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(28, 55, 90));

        JLabel subHeader = createLabel("Select your payment method to settle your booking.", 200, 70, 600, 20);
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeader.setForeground(new Color(100, 116, 139));

        // Form fields
        createFieldLabel("Booking ID *:", 200, 120, 150, 25);
        bookingIdField = createTextField(360, 120, 440, 32);
        bookingIdField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createFieldLabel("Amount Due (PHP) *:", 200, 170, 150, 25);
        amountField = createTextField(360, 170, 440, 32);
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createFieldLabel("Payment Method *:", 200, 220, 150, 25);
        String[] methods = {"Cash", "Credit Card", "Debit Card", "GCash", "Bank Transfer"};
        methodCombo = createComboBox(methods, 360, 220, 440, 32);
        methodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createFieldLabel("Reference / Source:", 200, 270, 150, 25);
        referenceField = createTextField(360, 270, 440, 32);
        referenceField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Action Buttons
        JButton payBtn = createButton("Confirm Payment", 360, 350, 215, 38);
        payBtn.setBackground(new Color(16, 185, 129));
        payBtn.setForeground(Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        payBtn.setFocusPainted(false);
        // Note: No DAO function attached per instruction

        JButton backBtn = createButton("Back to Dashboard", 585, 350, 215, 38);
        backBtn.setBackground(new Color(220, 223, 231));
        backBtn.setForeground(new Color(51, 65, 85));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new CustomerDashboard()));
    }

    private JLabel createFieldLabel(String text, int x, int y, int width, int height) {
        JLabel label = createLabel(text, x, y, width, height);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }
}