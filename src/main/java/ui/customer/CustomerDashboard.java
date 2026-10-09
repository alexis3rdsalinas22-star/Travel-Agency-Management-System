package ui.customer;

import common.Screen;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
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

        JLabel subtitle = createLabel("Welcome! Explore destinations, manage your bookings, and plan your trips.", 80, 95, 650, 25);
        setTextColor(subtitle, new Color(100, 116, 139));

        // Quick Navigation Buttons
        JButton myBookingsBtn = createButton("My Bookings", 80, 160, 240, 42);
        setFontStyle(myBookingsBtn, Font.BOLD);
        setBackground(myBookingsBtn, new Color(37, 99, 235));
        setTextColor(myBookingsBtn, Color.WHITE);
        myBookingsBtn.setFocusPainted(false);
        myBookingsBtn.addActionListener(e -> navigateTo(new MyBookings()));

        JButton profileBtn = createButton("My Profile", 80, 220, 240, 42);
        setFontStyle(profileBtn, Font.BOLD);
        setBackground(profileBtn, new Color(16, 149, 93));
        setTextColor(profileBtn, Color.WHITE);
        profileBtn.setFocusPainted(false);
        profileBtn.addActionListener(e -> navigateTo(new CustomerProfile()));

        // Functional Logout Button
        JButton logoutBtn = createButton("Log Out", 80, 300, 240, 40);
        setFontStyle(logoutBtn, Font.BOLD);
        setBackground(logoutBtn, new Color(220, 53, 69));
        setTextColor(logoutBtn, Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            if (confirm("Are you sure you want to log out?")) {
                navigateTo(new Homepage());
            }
        });
    }

    public CustomerDashboard() {
        this(-1);
    }

    public int getCustomerId() {
        return customerId;
    }
}