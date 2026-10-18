package ui;

import common.DBConnection;
import common.Screen;
import dao.CustomerAccountDAO;
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
import ui.customer.CustomerDashboard;

public class CustomerLogin extends Screen {

    public CustomerLogin() {
        super("Customer Account Login", 540, 560);

        getContentPane().setBackground(COLOR_BG);

        initUI();
    }

    private void initUI() {
        JPanel card = createCard(45, 30, 450, 465);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, COLOR_PRIMARY),
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

        JLabel tag = createCardTag(card, "TRAVELER ACCESS  •  ACCOUNT LOGIN", "customers.png");
        tag.setBounds(28, 18, 335, 22);

        // Header and subtitle
        JLabel header = createHeader(card, "Sign In to Account", 28, 48, 340, 28);
        setFontSize(header, 20);
        setTextColor(header, COLOR_TEXT_MAIN);

        JLabel subheader = createLabel(card, "Enter your credentials to manage trips and vouchers.", 28, 76, 394, 20);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        // Form Fields
        JLabel userLabel = createLabel(card, "Username", 28, 110, 394, 18);
        setFontStyle(userLabel, Font.BOLD);
        setFontSize(userLabel, 12);
        setTextColor(userLabel, new Color(51, 65, 85));

        JTextField username = createTextField(card, 28, 132, 394, 36);
        setFontSize(username, 13);
        username.setBackground(Color.WHITE);
        username.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        JLabel passLabel = createLabel(card, "Password", 28, 178, 394, 18);
        setFontStyle(passLabel, Font.BOLD);
        setFontSize(passLabel, 12);
        setTextColor(passLabel, new Color(51, 65, 85));

        JPasswordField password = createPasswordField(card, 28, 200, 394, 36);
        setFontSize(password, 13);
        password.setBackground(Color.WHITE);
        password.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        // Submit Button
        JButton loginBtn = createPrimaryButton(card, "Sign In to Account", 28, 258, 394, 42, null);
        setFontStyle(loginBtn, Font.BOLD);
        setFontSize(loginBtn, 14);
        loginBtn.setIcon(loadIcon("/images/icons/user.png", 18, 18));

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
                CustomerAccountDAO.CustomerLoginResult result = new CustomerAccountDAO().login(con, user, pass);
                if (result != null) {
                    common.Session.setCustomerSession(result.customerId, result.customerAccountId, user);
                    showInfo("Welcome back, " + user + "! Login successful.");
                    navigateTo(new CustomerDashboard(result.customerId));
                } else {
                    showError("Invalid username or password, or account is not active.");
                }
            } catch (SQLException ex) {
                showError("Database connection error: " + ex.getMessage());
            }
        });

        // Register link button
        JButton registerBtn = createStyledButton(card, "Don't have an account? Register here", 28, 312, 394, 36,
            new Color(239, 246, 255), COLOR_PRIMARY, new Color(191, 219, 254), 12, () -> navigateTo(new CustomerRegister()));
        setFontStyle(registerBtn, Font.BOLD);
        registerBtn.setIcon(loadIcon("/images/icons/add.png", 16, 16));

        // Back to home
        JButton backBtn = createSecondaryButton(card, "← Back to Home", 28, 360, 394, 36, () -> navigateTo(new Homepage()));
        setFontSize(backBtn, 12);
    }
}