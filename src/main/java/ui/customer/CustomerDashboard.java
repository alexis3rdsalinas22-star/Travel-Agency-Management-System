package ui.customer;

import common.Screen;
import common.Session;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import ui.BrowseDestinations;
import ui.BrowsePackages;
import ui.Homepage;

public class CustomerDashboard extends Screen {

    private int customerId;

    public CustomerDashboard(int customerId) {
        super("Customer Dashboard", 1000, 600);
        this.customerId = customerId;

        getContentPane().setBackground(new Color(245, 247, 250));

        // Welcome Header & Subtitle
        JLabel title = createHeader("Customer Dashboard", 100, 45, 800, 35);
        setFontSize(title, 26);
        setTextColor(title, new Color(28, 55, 90));

        String welcomeMsg = "Welcome! Explore destinations, manage your bookings, and plan your trips.";
        if (Session.getUsername() != null) {
            welcomeMsg = "Welcome back, " + Session.getUsername() + "! Plan your journeys and track reservations.";
        }
        JLabel subtitle = createLabel(welcomeMsg, 100, 85, 800, 22);
        setFontSize(subtitle, 13);
        setTextColor(subtitle, new Color(100, 116, 139));

        // -------------------------------------------------------------
        // Column 1: My Reservations & Account (x = 100, width = 360)
        // -------------------------------------------------------------
        JLabel col1Badge = createLabel("MY RESERVATIONS", 100, 145, 360, 20);
        setFontStyle(col1Badge, Font.BOLD);
        setFontSize(col1Badge, 12);
        setTextColor(col1Badge, new Color(37, 99, 235));

        JButton myBookingsBtn = createButton("My Bookings", 100, 175, 360, 44);
        setFontStyle(myBookingsBtn, Font.BOLD);
        setFontSize(myBookingsBtn, 14);
        setBackground(myBookingsBtn, new Color(37, 99, 235));
        setTextColor(myBookingsBtn, Color.WHITE);
        myBookingsBtn.setFocusPainted(false);
        myBookingsBtn.addActionListener(e -> navigateTo(new MyBookings()));

        JButton bookPackageBtn = createButton("Book a Package", 100, 235, 360, 44);
        setFontStyle(bookPackageBtn, Font.BOLD);
        setFontSize(bookPackageBtn, 14);
        setBackground(bookPackageBtn, new Color(79, 70, 229));
        setTextColor(bookPackageBtn, Color.WHITE);
        bookPackageBtn.setFocusPainted(false);
        bookPackageBtn.addActionListener(e -> navigateTo(new BookingScreen()));

        JButton profileBtn = createButton("My Profile", 100, 295, 360, 44);
        setFontStyle(profileBtn, Font.BOLD);
        setFontSize(profileBtn, 14);
        setBackground(profileBtn, new Color(16, 149, 93));
        setTextColor(profileBtn, Color.WHITE);
        profileBtn.setFocusPainted(false);
        profileBtn.addActionListener(e -> navigateTo(new CustomerProfile()));

        // -------------------------------------------------------------
        // Column 2: Public Exploration (x = 540, width = 360)
        // -------------------------------------------------------------
        JLabel col2Badge = createLabel("EXPLORE & DISCOVER", 540, 145, 360, 20);
        setFontStyle(col2Badge, Font.BOLD);
        setFontSize(col2Badge, 12);
        setTextColor(col2Badge, new Color(14, 116, 144));

        JButton browseDestBtn = createButton("Browse Destinations", 540, 175, 360, 44);
        setFontStyle(browseDestBtn, Font.BOLD);
        setFontSize(browseDestBtn, 14);
        setBackground(browseDestBtn, new Color(14, 116, 144));
        setTextColor(browseDestBtn, Color.WHITE);
        browseDestBtn.setFocusPainted(false);
        browseDestBtn.addActionListener(e -> navigateTo(new BrowseDestinations()));

        JButton browsePkgBtn = createButton("Browse Packages", 540, 235, 360, 44);
        setFontStyle(browsePkgBtn, Font.BOLD);
        setFontSize(browsePkgBtn, 14);
        setBackground(browsePkgBtn, new Color(217, 119, 6));
        setTextColor(browsePkgBtn, Color.WHITE);
        browsePkgBtn.setFocusPainted(false);
        browsePkgBtn.addActionListener(e -> navigateTo(new BrowsePackages()));

        // Log out button spanning bottom
        JButton logoutBtn = createButton("Log Out", 100, 390, 800, 42);
        setFontStyle(logoutBtn, Font.BOLD);
        setFontSize(logoutBtn, 14);
        setBackground(logoutBtn, new Color(220, 53, 69));
        setTextColor(logoutBtn, Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
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
}