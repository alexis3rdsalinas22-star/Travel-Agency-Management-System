package ui.customer;

import common.Screen;
import common.Session;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ui.BrowseDestinations;
import ui.BrowsePackages;
import ui.Homepage;

public class CustomerDashboard extends Screen {

    private int customerId;

    public CustomerDashboard(int customerId) {
        super("Customer Dashboard");
        this.customerId = customerId;

      
        String welcomeMsg = "Welcome! Explore destinations, manage your bookings, and plan your trips.";
        if (Session.getUsername() != null) {
            welcomeMsg = "Welcome back, " + Session.getUsername() + "! Plan your journeys and track reservations.";
        }

 
        createTopHeader("CUSTOMER", "Customer Dashboard", welcomeMsg, null);

    
        JPanel reservationsCard = createCard(45, 105, 545, 250);
        createCardTag(reservationsCard, "MY RESERVATIONS");

        createPrimaryButton(reservationsCard, "My Bookings", 16, 48, 513, 56,
                () -> navigateTo(new MyBookings()));
        addHint(reservationsCard, "View, track, and manage your reservations.", 110);

        createSuccessButton(reservationsCard, "Book a Package", 16, 148, 513, 56,
                () -> navigateTo(new BookingScreen()));
        addHint(reservationsCard, "Reserve a travel package for your next trip.", 210);

    
        JPanel exploreCard = createCard(610, 105, 545, 250);
        createCardTag(exploreCard, "EXPLORE & DISCOVER");

        createPrimaryButton(exploreCard, "Browse Destinations", 16, 48, 513, 56,
                () -> navigateTo(new BrowseDestinations()));
        addHint(exploreCard, "Discover places you can travel to.", 110);

        createPrimaryButton(exploreCard, "Browse Packages", 16, 148, 513, 56,
                () -> navigateTo(new BrowsePackages()));
        addHint(exploreCard, "See available packages and their prices.", 210);

     
        JPanel accountCard = createCard(45, 375, 1110, 110);
        createCardTag(accountCard, "ACCOUNT");

        createSecondaryButton(accountCard, "My Profile", 16, 48, 531, 48,
                () -> navigateTo(new CustomerProfile()));

        createDangerButton(accountCard, "Log Out", 563, 48, 531, 48, () -> {
            if (confirm("Are you sure you want to log out?")) {
                Session.clear();
                navigateTo(new Homepage());
            }
        });
    }

    public CustomerDashboard() {
        this(Session.getCustomerId());
    }

    public int getCustomerId() {
        return customerId;
    }

   
    private JLabel addHint(JPanel parent, String text, int y) {
        JLabel hint = createLabel(parent, text, 18, y, 509, 18);
        setFontSize(hint, 12);
        setTextColor(hint, COLOR_TEXT_MUTED);
        return hint;
    }
}