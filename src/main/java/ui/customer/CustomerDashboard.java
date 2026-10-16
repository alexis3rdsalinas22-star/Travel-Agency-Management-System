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
        super("Customer Dashboard");
        this.customerId = customerId;

        getContentPane().setBackground(new Color(245, 247, 250));

        // Welcome Header & Subtitle
        JLabel title = createHeader("Customer Dashboard", 80, 50, 600, 40);
        setFontSize(title, 28);
        setTextColor(title, new Color(28, 55, 90));

        String welcomeMsg = "Welcome! Explore destinations, manage your bookings, and plan your trips.";
        if (Session.getUsername() != null) {
            welcomeMsg = "Welcome, " + Session.getUsername() + "! Explore destinations, manage your bookings, and plan your trips.";
        }
        JLabel subtitle = createLabel(welcomeMsg, 80, 95, 750, 25);
        setTextColor(subtitle, new Color(100, 116, 139));

        // Navigation Buttons - Column 1
        JButton myBookingsBtn = createButton("My Bookings", 80, 160, 230, 42);
        setFontStyle(myBookingsBtn, Font.BOLD);
        setBackground(myBookingsBtn, new Color(37, 99, 235));
        setTextColor(myBookingsBtn, Color.WHITE);
        myBookingsBtn.setFocusPainted(false);
        myBookingsBtn.addActionListener(e -> navigateTo(new MyBookings()));

        JButton bookPackageBtn = createButton("Book a Package", 80, 215, 230, 42);
        setFontStyle(bookPackageBtn, Font.BOLD);
        setBackground(bookPackageBtn, new Color(79, 70, 229));
        setTextColor(bookPackageBtn, Color.WHITE);
        bookPackageBtn.setFocusPainted(false);
        bookPackageBtn.addActionListener(e -> navigateTo(new BookingScreen()));

        JButton profileBtn = createButton("My Profile", 80, 270, 230, 42);
        setFontStyle(profileBtn, Font.BOLD);
        setBackground(profileBtn, new Color(16, 149, 93));
        setTextColor(profileBtn, Color.WHITE);
        profileBtn.setFocusPainted(false);
        profileBtn.addActionListener(e -> navigateTo(new CustomerProfile()));

        JButton logoutBtn = createButton("Log Out", 80, 340, 230, 40);
        setFontStyle(logoutBtn, Font.BOLD);
        setBackground(logoutBtn, new Color(220, 53, 69));
        setTextColor(logoutBtn, Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            if (confirm("Are you sure you want to log out?")) {
                Session.clear();
                navigateTo(new Homepage());
            }
        });

        // Navigation Buttons - Column 2
        JButton browseDestBtn = createButton("Browse Destinations", 340, 160, 230, 42);
        setFontStyle(browseDestBtn, Font.BOLD);
        setBackground(browseDestBtn, new Color(14, 116, 144));
        setTextColor(browseDestBtn, Color.WHITE);
        browseDestBtn.setFocusPainted(false);
        browseDestBtn.addActionListener(e -> navigateTo(new BrowseDestinations()));

        JButton browsePkgBtn = createButton("Browse Packages", 340, 215, 230, 42);
        setFontStyle(browsePkgBtn, Font.BOLD);
        setBackground(browsePkgBtn, new Color(217, 119, 6));
        setTextColor(browsePkgBtn, Color.WHITE);
        browsePkgBtn.setFocusPainted(false);
        browsePkgBtn.addActionListener(e -> navigateTo(new BrowsePackages()));
    }

    public CustomerDashboard() {
        this(Session.getCustomerId());
    }

    public int getCustomerId() {
        return customerId;
    }
}