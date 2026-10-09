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
        JLabel title = createHeader("Travel Agency Management System", 80, 60, 700, 45);
        setFontSize(title, 30);
        setTextColor(title, new Color(28, 55, 90));

        JLabel subtitle = createLabel("Welcome! Plan, book, and explore destinations with ease.", 80, 115, 650, 25);
        setTextColor(subtitle, new Color(100, 116, 139));

        // Login Section
        JLabel loginLabel = createLabel("Already registered with us?", 80, 190, 300, 25);
        setTextColor(loginLabel, new Color(55, 65, 81));

        JButton loginBtn = createButton("Login to Account", 80, 225, 240, 42);
        setFontStyle(loginBtn, Font.BOLD);
        setBackground(loginBtn, new Color(37, 99, 235));
        setTextColor(loginBtn, Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> navigateTo(new UserLogin()));

        // Register Section
        JLabel registerLabel = createLabel("New customer? Join today!", 80, 295, 300, 25);
        setTextColor(registerLabel, new Color(55, 65, 81));

        JButton registerBtn = createButton("Create New Account", 80, 330, 240, 42);
        setFontStyle(registerBtn, Font.BOLD);
        setBackground(registerBtn, new Color(16, 149, 93));
        setTextColor(registerBtn, Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> navigateTo(new UserRegister()));

        // Exit Application Button
        JButton exitBtn = createButton("Exit System", 80, 400, 240, 38);
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