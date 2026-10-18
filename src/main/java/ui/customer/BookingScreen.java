package ui.customer;

import common.Screen;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class BookingScreen extends Screen {

    private JTextField packageField;
    private JTextField travelDateField;
    private JTextField travelersField;
    private JTextField contactField;
    private JTextArea notesArea;

    public BookingScreen() {
        this(null);
    }

    public BookingScreen(String packageName) {
        super("Book a Package"); // uses STD_WIDTH x STD_HEIGHT so the header's back button fits
        initUI();
        if (packageName != null && packageField != null) {
            packageField.setText(packageName);
        }
    }

    private void initUI() {
        // Header (tag + title + subtitle + back button), same as the other screens
        createTopHeader(
                "CUSTOMER",
                "Book a Package",
                "Fill in your travel details to reserve a package.",
                () -> navigateTo(new CustomerDashboard())
        );

        // Left card: booking details
        JPanel detailsCard = createCard(45, 100, 345, 270);
        createCardTag(detailsCard, "TRAVEL DETAILS");

        packageField    = addFormField(detailsCard, "Package *",    48,  true);
        travelDateField = addFormField(detailsCard, "Travel Date *", 100, true);
        travelersField  = addFormField(detailsCard, "Travelers *",   152, true);
        contactField    = addFormField(detailsCard, "Contact No. *", 204, true);

        // Right card: special requests
        JPanel notesCard = createCard(410, 100, 745, 270);
        createCardTag(notesCard, "SPECIAL REQUESTS");

        notesArea = createTextArea(notesCard, 16, 44, 713, 210);
        setFontSize(notesArea, 12);

        // Action button (right-aligned with the notes card)
        createPrimaryButton("Proceed to Payment", 940, 395, 215, 38,
                () -> navigateTo(new PaymentScreen()));
    }
}