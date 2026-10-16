package ui.customer;

import common.Screen;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class BookingScreen extends Screen {

    private JTextField packageField;
    private JTextField travelDateField;
    private JTextField travelersField;
    private JTextField contactField;
    private JTextArea notesArea;

    public BookingScreen() {
        super("Book a Package", 700, 560);

        initUI();
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("Book a Package", 60, 25, 400, 30);
        createLabel("Fill in your travel details to reserve a package.", 60, 55, 450, 20);

        // Form fields
        createLabel("Package Name *:", 60, 95, 120, 25);
        packageField = createTextField(190, 95, 430, 30);

        createLabel("Travel Date *:", 60, 140, 120, 25);
        travelDateField = createTextField(190, 140, 430, 30);

        createLabel("No. of Travelers *:", 60, 185, 120, 25);
        travelersField = createTextField(190, 185, 430, 30);

        createLabel("Contact Number *:", 60, 230, 120, 25);
        contactField = createTextField(190, 230, 430, 30);

        createLabel("Special Requests:", 60, 275, 120, 25);
        notesArea = createTextArea(190, 275, 430, 70);

        // Action Buttons
        JButton proceedBtn = createButton("Proceed to Payment", 190, 380, 210, 38);
        proceedBtn.addActionListener(e -> navigateTo(new PaymentScreen()));

        JButton backBtn = createButton("Back to Dashboard", 410, 380, 210, 38);
        backBtn.addActionListener(e -> navigateTo(new CustomerDashboard()));
    }
}