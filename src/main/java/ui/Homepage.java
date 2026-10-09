package ui;

import common.Screen;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Homepage extends Screen {

    public Homepage() {
        super("Travel Agency Management System");

        // Clean light background
        getContentPane().setBackground(new Color(245, 247, 250));

        // Welcome Header & Subtitle
        JLabel title = createHeader("Travel Agency Management System", 80, 60, 700, 45);
        setFont(title, "Segoe UI", Font.BOLD, 30);
        setTextColor(title, new Color(28, 55, 90));

        JLabel subtitle = createLabel("Welcome! Plan, book, and explore destinations with ease.", 80, 115, 650, 25);
        setFont(subtitle, "Segoe UI", Font.PLAIN, 15);
        setTextColor(subtitle, new Color(100, 116, 139));

        // Login Section
        JLabel loginLabel = createLabel("Already registered with us?", 80, 190, 300, 25);
        setFont(loginLabel, "Segoe UI", Font.PLAIN, 14);
        setTextColor(loginLabel, new Color(55, 65, 81));

        JButton loginBtn = createButton("Login to Account", 80, 225, 240, 42);
        setFont(loginBtn, "Segoe UI", Font.BOLD, 14);
        setBackground(loginBtn, new Color(37, 99, 235));
        setTextColor(loginBtn, Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> navigateTo(new UserLogin()));

        // Register Section
        JLabel registerLabel = createLabel("New customer? Join today!", 80, 295, 300, 25);
        setFont(registerLabel, "Segoe UI", Font.PLAIN, 14);
        setTextColor(registerLabel, new Color(55, 65, 81));

        JButton registerBtn = createButton("Create New Account", 80, 330, 240, 42);
        setFont(registerBtn, "Segoe UI", Font.BOLD, 14);
        setBackground(registerBtn, new Color(16, 149, 93));
        setTextColor(registerBtn, Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> navigateTo(new UserRegister()));

        // Exit Application Button
        JButton exitBtn = createButton("Exit System", 80, 400, 240, 38);
        setFont(exitBtn, "Segoe UI", Font.PLAIN, 13);
        setBackground(exitBtn, new Color(229, 231, 235));
        setTextColor(exitBtn, new Color(55, 65, 81));
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> {
            if (confirm("Are you sure you want to exit the application?")) {
                System.exit(0);
            }
        });
    }
}