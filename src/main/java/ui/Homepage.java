package ui;

import common.Screen;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Homepage extends Screen {

    public Homepage() {
        super("Travel Agency Management System", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        // Welcome Header & Subtitle
        JLabel title = createHeader("Travel Agency Management System", 60, 45, 880, 40);
        setFontSize(title, 28);
        setTextColor(title, new Color(28, 55, 90));

        JLabel subtitle = createLabel("Plan, book, and explore vacation packages and destinations with ease.", 60, 90, 880, 22);
        setFontSize(subtitle, 14);
        setTextColor(subtitle, new Color(100, 116, 139));

        // -------------------------------------------------------------
        // Column 1: Traveler Portal (x = 60, width = 270)
        // -------------------------------------------------------------
        JLabel col1Badge = createLabel("TRAVELER PORTAL", 60, 155, 270, 20);
        setFontStyle(col1Badge, Font.BOLD);
        setFontSize(col1Badge, 12);
        setTextColor(col1Badge, new Color(37, 99, 235));

        JLabel col1Desc = createLabel("Access your reservations & account", 60, 177, 270, 20);
        setFontSize(col1Desc, 13);
        setTextColor(col1Desc, new Color(75, 85, 99));

        JButton loginBtn = createButton("Login to Account", 60, 210, 270, 42);
        setFontStyle(loginBtn, Font.BOLD);
        setFontSize(loginBtn, 14);
        setBackground(loginBtn, new Color(37, 99, 235));
        setTextColor(loginBtn, Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> navigateTo(new CustomerLogin()));

        JButton registerBtn = createButton("Create New Account", 60, 265, 270, 42);
        setFontStyle(registerBtn, Font.BOLD);
        setFontSize(registerBtn, 14);
        setBackground(registerBtn, new Color(16, 149, 93));
        setTextColor(registerBtn, Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> navigateTo(new CustomerRegister()));

        JButton exitBtn = createButton("Exit Application", 60, 450, 270, 40);
        setFontSize(exitBtn, 13);
        setBackground(exitBtn, new Color(229, 231, 235));
        setTextColor(exitBtn, new Color(55, 65, 81));
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> {
            if (confirm("Are you sure you want to exit the application?")) {
                System.exit(0);
            }
        });

        // -------------------------------------------------------------
        // Column 2: Public Exploration (x = 365, width = 270)
        // -------------------------------------------------------------
        JLabel col2Badge = createLabel("EXPLORE & DISCOVER", 365, 155, 270, 20);
        setFontStyle(col2Badge, Font.BOLD);
        setFontSize(col2Badge, 12);
        setTextColor(col2Badge, new Color(14, 116, 144));

        JLabel col2Desc = createLabel("Browse available spots & tours", 365, 177, 270, 20);
        setFontSize(col2Desc, 13);
        setTextColor(col2Desc, new Color(75, 85, 99));

        JButton destBtn = createButton("Browse Destinations", 365, 210, 270, 42);
        setFontStyle(destBtn, Font.BOLD);
        setFontSize(destBtn, 14);
        setBackground(destBtn, new Color(14, 116, 144));
        setTextColor(destBtn, Color.WHITE);
        destBtn.setFocusPainted(false);
        destBtn.addActionListener(e -> navigateTo(new BrowseDestinations()));

        JButton pkgBtn = createButton("Browse Tour Packages", 365, 265, 270, 42);
        setFontStyle(pkgBtn, Font.BOLD);
        setFontSize(pkgBtn, 14);
        setBackground(pkgBtn, new Color(79, 70, 229));
        setTextColor(pkgBtn, Color.WHITE);
        pkgBtn.setFocusPainted(false);
        pkgBtn.addActionListener(e -> navigateTo(new BrowsePackages()));

        // -------------------------------------------------------------
        // Column 3: Administration (x = 670, width = 270)
        // -------------------------------------------------------------
        JLabel col3Badge = createLabel("STAFF & MANAGEMENT", 670, 155, 270, 20);
        setFontStyle(col3Badge, Font.BOLD);
        setFontSize(col3Badge, 12);
        setTextColor(col3Badge, new Color(75, 85, 99));

        JLabel col3Desc = createLabel("Admin dashboard & controls", 670, 177, 270, 20);
        setFontSize(col3Desc, 13);
        setTextColor(col3Desc, new Color(75, 85, 99));

        JButton adminBtn = createButton("Administrator Login", 670, 210, 270, 42);
        setFontStyle(adminBtn, Font.BOLD);
        setFontSize(adminBtn, 14);
        setBackground(adminBtn, new Color(51, 65, 85));
        setTextColor(adminBtn, Color.WHITE);
        adminBtn.setFocusPainted(false);
        adminBtn.addActionListener(e -> navigateTo(new AdminLogin()));
    }
}