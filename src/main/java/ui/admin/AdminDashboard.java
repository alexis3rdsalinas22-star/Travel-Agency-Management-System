package ui.admin;

import common.Screen;
import common.Session;
import ui.Homepage;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;

public class AdminDashboard extends Screen {
    public AdminDashboard() {
        super("Admin Dashboard", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        // Welcome Header & Subtitle
        JLabel title = createHeader("Admin Dashboard", 100, 45, 800, 35);
        setFontSize(title, 26);
        setTextColor(title, new Color(28, 55, 90));

        String welcome = "Welcome to the Admin Portal";
        if (Session.getUsername() != null) {
            welcome += ", " + Session.getUsername() + " (" + Session.getUserRole() + ")";
        }

        JLabel subtitle = createLabel(welcome + " | Manage trips, reservations, and system users.", 100, 85, 800, 22);
        setFontSize(subtitle, 13);
        setTextColor(subtitle, new Color(100, 116, 139));

        // -------------------------------------------------------------
        // Column 1: Catalog & Reservations (x = 100, width = 360)
        // -------------------------------------------------------------
        JLabel col1Badge = createLabel("CATALOG & TRIPS", 100, 145, 360, 20);
        setFontStyle(col1Badge, Font.BOLD);
        setFontSize(col1Badge, 12);
        setTextColor(col1Badge, new Color(14, 116, 144));

        JButton destBtn = createButton("Manage Destinations", 100, 175, 360, 44);
        setFontStyle(destBtn, Font.BOLD);
        setFontSize(destBtn, 14);
        setBackground(destBtn, new Color(14, 116, 144));
        setTextColor(destBtn, Color.WHITE);
        destBtn.setFocusPainted(false);
        destBtn.addActionListener(e -> navigateTo(new DestinationManagement()));

        JButton pkgBtn = createButton("Manage Packages", 100, 235, 360, 44);
        setFontStyle(pkgBtn, Font.BOLD);
        setFontSize(pkgBtn, 14);
        setBackground(pkgBtn, new Color(79, 70, 229));
        setTextColor(pkgBtn, Color.WHITE);
        pkgBtn.setFocusPainted(false);
        pkgBtn.addActionListener(e -> navigateTo(new PackageManagement()));

        JButton bookBtn = createButton("Manage Bookings", 100, 295, 360, 44);
        setFontStyle(bookBtn, Font.BOLD);
        setFontSize(bookBtn, 14);
        setBackground(bookBtn, new Color(37, 99, 235));
        setTextColor(bookBtn, Color.WHITE);
        bookBtn.setFocusPainted(false);
        bookBtn.addActionListener(e -> navigateTo(new BookingManagement()));

        // -------------------------------------------------------------
        // Column 2: Accounts & Financials (x = 540, width = 360)
        // -------------------------------------------------------------
        JLabel col2Badge = createLabel("USERS & OPERATIONS", 540, 145, 360, 20);
        setFontStyle(col2Badge, Font.BOLD);
        setFontSize(col2Badge, 12);
        setTextColor(col2Badge, new Color(75, 85, 99));

        JButton custBtn = createButton("Manage Customers", 540, 175, 360, 44);
        setFontStyle(custBtn, Font.BOLD);
        setFontSize(custBtn, 14);
        setBackground(custBtn, new Color(16, 149, 93));
        setTextColor(custBtn, Color.WHITE);
        custBtn.setFocusPainted(false);
        custBtn.addActionListener(e -> navigateTo(new CustomerManagement()));

        JButton salesBtn = createButton("Sales & Reports", 540, 235, 360, 44);
        setFontStyle(salesBtn, Font.BOLD);
        setFontSize(salesBtn, 14);
        setBackground(salesBtn, new Color(217, 119, 6));
        setTextColor(salesBtn, Color.WHITE);
        salesBtn.setFocusPainted(false);
        salesBtn.addActionListener(e -> navigateTo(new SalesManagement()));

        JButton adminMgmtBtn = createButton("Admin Accounts", 540, 295, 360, 44);
        setFontStyle(adminMgmtBtn, Font.BOLD);
        setFontSize(adminMgmtBtn, 14);
        setBackground(adminMgmtBtn, new Color(51, 65, 85));
        setTextColor(adminMgmtBtn, Color.WHITE);
        adminMgmtBtn.setFocusPainted(false);
        adminMgmtBtn.addActionListener(e -> navigateTo(new AdminManagement()));

        // Logout Button
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
}
