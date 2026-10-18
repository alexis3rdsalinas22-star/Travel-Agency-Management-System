package ui;

import common.DBConnection;
import common.Screen;
import common.Session;
import dao.AdminAccountDAO;
import model.AdminAccount;
import ui.admin.AdminDashboard;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class AdminLogin extends Screen {

    public AdminLogin() {
        super("Administrator Login", 540, 560);

        getContentPane().setBackground(COLOR_BG);

        initUI();
    }

    private void initUI() {
        Color adminAccent = new Color(51, 65, 85);

        JPanel card = createCard(45, 30, 450, 465);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, adminAccent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
            )
        ));

        // Top Brand Logo & Pill Tag
        ImageIcon logo = getLogoIcon(38, 38);
        if (logo != null) {
            JLabel logoLbl = new JLabel(logo);
            logoLbl.setBounds(450 - 28 - 38, 16, 38, 38);
            card.add(logoLbl);
        }

        JLabel tag = new JLabel("  STAFF CHECKPOINT  •  RESTRICTED ACCESS  ");
        ImageIcon secIcon = loadIcon("/images/icons/security.png", 14, 14);
        if (secIcon != null) {
            tag.setIcon(secIcon);
        }
        tag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tag.setForeground(adminAccent);
        tag.setOpaque(true);
        tag.setBackground(new Color(241, 245, 249));
        tag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        tag.setBounds(28, 18, 335, 22);
        card.add(tag);

        // Header and subtitle
        JLabel header = createHeader(card, "Administrator Login", 28, 48, 340, 28);
        setFontSize(header, 20);
        setTextColor(header, COLOR_TEXT_MAIN);

        JLabel subheader = createLabel(card, "Enter your administrator credentials to enter the console.", 28, 78, 394, 20);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        // Form fields
        JLabel userLabel = createLabel(card, "Admin Username", 28, 114, 394, 18);
        setFontStyle(userLabel, Font.BOLD);
        setFontSize(userLabel, 12);
        setTextColor(userLabel, new Color(51, 65, 85));

        JTextField username = createTextField(card, 28, 136, 394, 36);
        setFontSize(username, 13);
        username.setBackground(Color.WHITE);
        username.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        JLabel passLabel = createLabel(card, "Password", 28, 184, 394, 18);
        setFontStyle(passLabel, Font.BOLD);
        setFontSize(passLabel, 12);
        setTextColor(passLabel, new Color(51, 65, 85));

        JPasswordField password = createPasswordField(card, 28, 206, 394, 36);
        setFontSize(password, 13);
        password.setBackground(Color.WHITE);
        password.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        // Submit Button
        JButton loginBtn = createStyledButton(card, "Sign In as Administrator", 28, 268, 394, 42,
            adminAccent, Color.WHITE, null);
        setFontStyle(loginBtn, Font.BOLD);
        setFontSize(loginBtn, 14);

        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                loginBtn.setBackground(new Color(30, 41, 59));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                loginBtn.setBackground(adminAccent);
            }
        });

        // Keyboard ergonomics
        username.addActionListener(e -> password.requestFocusInWindow());
        password.addActionListener(e -> loginBtn.doClick());

        loginBtn.addActionListener(e -> {
            String user = username.getText().trim();
            String pass = new String(password.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                showError("Please enter both username and password.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                AdminAccount admin = new AdminAccountDAO().login(con, user, pass);
                if (admin != null) {
                    Session.setAdminSession(admin);
                    showInfo("Welcome, " + admin.getusername() + "! Login successful.");
                    navigateTo(new AdminDashboard());
                } else {
                    showError("Invalid administrator credentials.");
                }
            } catch (SQLException ex) {
                showError("Database connection error: " + ex.getMessage());
            }
        });

        JButton backBtn = createSecondaryButton(card, "← Back to Home", 28, 324, 394, 38, () -> navigateTo(new Homepage()));
        setFontSize(backBtn, 12);
    }
}

