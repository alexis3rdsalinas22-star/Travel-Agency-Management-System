package ui.customer;

import common.Screen;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class BookingScreen extends Screen {

    private JTextField packageField;
    private JTextField travelDateField;
    private JTextField travelersField;
    private JTextField contactField;
    private JTextArea notesArea;

    public BookingScreen() {
        super("Book a Package", 1000, 600);

        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(new Color(245, 247, 250));

        // Header & Subtitle
        JLabel header = createHeader("Book a Package", 200, 35, 600, 32);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(28, 55, 90));

        JLabel subHeader = createLabel("Fill in your travel details to reserve a package.", 200, 70, 600, 20);
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeader.setForeground(new Color(100, 116, 139));

        // Form fields
        createFieldLabel("Package Name *:", 200, 110, 150, 25);
        packageField = createTextField(360, 110, 440, 32);
        packageField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createFieldLabel("Travel Date *:", 200, 155, 150, 25);
        travelDateField = createTextField(360, 155, 440, 32);
        travelDateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createFieldLabel("No. of Travelers *:", 200, 200, 150, 25);
        travelersField = createTextField(360, 200, 440, 32);
        travelersField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createFieldLabel("Contact Number *:", 200, 245, 150, 25);
        contactField = createTextField(360, 245, 440, 32);
        contactField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createFieldLabel("Special Requests:", 200, 290, 150, 25);
        notesArea = createTextArea(360, 290, 440, 80);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Action Buttons
        JButton proceedBtn = createButton("Proceed to Payment", 360, 400, 215, 38);
        proceedBtn.setBackground(new Color(79, 70, 229));
        proceedBtn.setForeground(Color.WHITE);
        proceedBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        proceedBtn.setFocusPainted(false);
        proceedBtn.addActionListener(e -> navigateTo(new PaymentScreen()));

        JButton backBtn = createButton("Back to Dashboard", 585, 400, 215, 38);
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