package ui;

import common.Screen;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Homepage extends Screen {

    public Homepage() {
        super("Travel Agency Management System");

        getContentPane().setBackground(new Color(245, 247, 250));

        // Welcome Header & Subtitle
        JLabel title = createHeader("Travel Agency Management System", 80, 50, 700, 45);
        setFontSize(title, 30);
        setTextColor(title, new Color(28, 55, 90));

        JLabel subtitle = createLabel("Welcome! Plan, book, and explore destinations with ease.", 80, 105, 650, 25);
        setTextColor(subtitle, new Color(100, 116, 139));

        // Column 1: Customer Account
        JLabel loginLabel = createLabel("Already registered with us?", 80, 175, 240, 25);
        setTextColor(loginLabel, new Color(55, 65, 81));

        JButton loginBtn = createButton("Login to Account", 80, 205, 240, 40);
        setFontStyle(loginBtn, Font.BOLD);
        setBackground(loginBtn, new Color(37, 99, 235));
        setTextColor(loginBtn, Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> navigateTo(new CustomerLogin()));

        JLabel registerLabel = createLabel("New customer? Join today!", 80, 265, 240, 25);
        setTextColor(registerLabel, new Color(55, 65, 81));

        JButton registerBtn = createButton("Create New Account", 80, 295, 240, 40);
        setFontStyle(registerBtn, Font.BOLD);
        setBackground(registerBtn, new Color(16, 149, 93));
        setTextColor(registerBtn, Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> navigateTo(new CustomerRegister()));

        JButton exitBtn = createButton("Exit System", 80, 365, 240, 38);
        setBackground(exitBtn, new Color(229, 231, 235));
        setTextColor(exitBtn, new Color(55, 65, 81));
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> {
            if (confirm("Are you sure you want to exit the application?")) {
                System.exit(0);
            }
        });

        // Column 2: Public Exploration
        JLabel destLabel = createLabel("Explore travel locations:", 360, 175, 240, 25);
        setTextColor(destLabel, new Color(55, 65, 81));

        JButton destBtn = createButton("Browse Destinations", 360, 205, 240, 40);
        setFontStyle(destBtn, Font.BOLD);
        setBackground(destBtn, new Color(14, 116, 144));
        setTextColor(destBtn, Color.WHITE);
        destBtn.setFocusPainted(false);
        destBtn.addActionListener(e -> navigateTo(new BrowseDestinations()));

        JLabel pkgLabel = createLabel("Explore tour packages:", 360, 265, 240, 25);
        setTextColor(pkgLabel, new Color(55, 65, 81));

        JButton pkgBtn = createButton("Browse Packages", 360, 295, 240, 40);
        setFontStyle(pkgBtn, Font.BOLD);
        setBackground(pkgBtn, new Color(79, 70, 229));
        setTextColor(pkgBtn, Color.WHITE);
        pkgBtn.setFocusPainted(false);
        pkgBtn.addActionListener(e -> navigateTo(new BrowsePackages()));

        // Column 3: Administration
        JLabel adminLabel = createLabel("System Administrator?", 640, 175, 240, 25);
        setTextColor(adminLabel, new Color(55, 65, 81));

        JButton adminBtn = createButton("Admin Login", 640, 205, 240, 40);
        setFontStyle(adminBtn, Font.BOLD);
        setBackground(adminBtn, new Color(75, 85, 99));
        setTextColor(adminBtn, Color.WHITE);
        adminBtn.setFocusPainted(false);
        adminBtn.addActionListener(e -> navigateTo(new AdminLogin()));
    }
}