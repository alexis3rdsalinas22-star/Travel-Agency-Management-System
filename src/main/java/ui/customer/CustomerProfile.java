package ui.customer;

import common.DBConnection;
import common.Screen;
import common.Session;
import common.ValidationUtil;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class CustomerProfile extends Screen {

    private final CustomerDAO customerDAO = new CustomerDAO();

    private JLabel logoLbl;
    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;

    private JScrollPane scrollPane;
    private JPanel container;

    private JPanel accountBadgeCard;
    private JLabel avatarBadge;
    private JLabel usernameHeader;
    private JLabel accountStatusPill;

    private JPanel personalCard;
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField lastNameField;

    private JPanel contactCard;
    private JTextField emailField;
    private JComboBox<String> genderCombo;
    private JTextField ageField;

    private JButton saveBtn;

    public CustomerProfile() {
        super("My Profile - Customer Portal", 1150, 700);

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        initUI();
        loadProfile();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    private void initUI() {
        ImageIcon logo = getLogoIcon(38, 38);
        if (logo != null) {
            logoLbl = new JLabel(logo);
            add(logoLbl);
        }

        int textX = 93;
        categoryTag = createLabel("CUSTOMER PORTAL  •  ACCOUNT SETTINGS", textX, 14, 600, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        header = createHeader("My Customer Profile", textX, 30, 600, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        subheader = createLabel("Review and update your personal details, travel preferences, and contact information.", textX, 60, 650, 18);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        backBtn = createSecondaryButton(this, "← Dashboard", 930, 20, 130, 36, () -> {
            navigateTo(new CustomerDashboard());
        });
        setFontStyle(backBtn, Font.BOLD);
        setFontSize(backBtn, 12);

        container = new JPanel(null);
        container.setBackground(COLOR_BG);

        scrollPane = new JScrollPane(container);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane);

        buildAccountBadgeCard();
        buildPersonalCard();
        buildContactCard();

        saveBtn = createSuccessButton(container, "Save Profile Changes →", 0, 0, 230, 42, this::handleSaveProfile);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void buildAccountBadgeCard() {
        accountBadgeCard = new JPanel(null);
        accountBadgeCard.setBackground(Color.WHITE);
        accountBadgeCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        ImageIcon userIcon = loadIcon("/images/icons/customers.png", 26, 26);
        if (userIcon != null) {
            avatarBadge = new JLabel(userIcon, JLabel.CENTER);
        } else {
            avatarBadge = new JLabel("👤", JLabel.CENTER);
            avatarBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        }
        avatarBadge.setOpaque(true);
        avatarBadge.setBackground(new Color(238, 242, 255));
        avatarBadge.setBorder(BorderFactory.createLineBorder(new Color(199, 210, 254), 1, true));
        avatarBadge.setBounds(20, 16, 52, 52);
        accountBadgeCard.add(avatarBadge);

        String user = Session.getUsername() != null ? Session.getUsername() : "Passenger";
        usernameHeader = new JLabel(user);
        usernameHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));
        usernameHeader.setForeground(COLOR_TEXT_MAIN);
        usernameHeader.setBounds(86, 18, 300, 24);
        accountBadgeCard.add(usernameHeader);

        JLabel sub = new JLabel("Verified Customer Account  •  Active Travel Profile");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(COLOR_TEXT_MUTED);
        sub.setBounds(86, 42, 300, 18);
        accountBadgeCard.add(sub);

        accountStatusPill = new JLabel("ACTIVE", JLabel.CENTER);
        accountStatusPill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        accountStatusPill.setForeground(new Color(5, 150, 105));
        accountStatusPill.setOpaque(true);
        accountStatusPill.setBackground(new Color(236, 253, 245));
        accountStatusPill.setBorder(BorderFactory.createLineBorder(new Color(167, 243, 208), 1, true));
        accountBadgeCard.add(accountStatusPill);

        container.add(accountBadgeCard);
    }

    private void buildPersonalCard() {
        personalCard = new JPanel(null);
        personalCard.setBackground(Color.WHITE);
        personalCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        createCardTag(personalCard, "PERSONAL INFORMATION", "customers.png");

        firstNameField  = addFormField(personalCard, "First Name *", 48,  true);
        middleNameField = addFormField(personalCard, "Middle Name",  102, true);
        lastNameField   = addFormField(personalCard, "Last Name *",  156, true);

        container.add(personalCard);
    }

    private void buildContactCard() {
        contactCard = new JPanel(null);
        contactCard.setBackground(Color.WHITE);
        contactCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        createCardTag(contactCard, "CONTACT & DEMOGRAPHICS", "destinations.png");

        emailField  = addFormField(contactCard, "Email Address *", 48, true);
        genderCombo = addComboRow(contactCard, "Sex / Gender *", new String[]{"Male", "Female"}, 102);
        ageField    = addFormField(contactCard, "Age *", 156, true);

        container.add(contactCard);
    }

    private JComboBox<String> addComboRow(JPanel parent, String labelText, String[] items, int y) {
        JLabel lbl = createLabel(parent, labelText, 16, y, 120, 26);
        setFontStyle(lbl, Font.BOLD);
        setFontSize(lbl, 11);
        setTextColor(lbl, COLOR_TEXT_MAIN);

        JComboBox<String> combo = createComboBox(parent, items, 140, y, 220, 26);
        setFontSize(combo, 12);
        return combo;
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int textX = pad;
        if (logoLbl != null) {
            logoLbl.setBounds(pad, 18, 38, 38);
            textX = pad + 48;
        }

        categoryTag.setBounds(textX, 14, 600, 16);
        header.setBounds(textX, 30, 600, 30);
        subheader.setBounds(textX, 60, 650, 18);
        backBtn.setBounds(w - pad - 130, 20, 130, 36);

        int scrollY = 96;
        int scrollHeight = Math.max(300, h - scrollY);
        scrollPane.setBounds(0, scrollY, w, scrollHeight);

        int scrollbarW = 18;
        int contentWidth = Math.max(700, w - (2 * pad) - scrollbarW);

        // Badge card
        int badgeH = 84;
        accountBadgeCard.setBounds(pad, 10, contentWidth, badgeH);
        accountStatusPill.setBounds(contentWidth - 100, 28, 75, 24);

        int gap = 20;
        int colW = (contentWidth - gap) / 2;
        int colH = 225;
        int formY = 10 + badgeH + 16;

        personalCard.setBounds(pad, formY, colW, colH);
        contactCard.setBounds(pad + colW + gap, formY, colW, colH);

        saveBtn.setBounds(pad + contentWidth - 230, formY + colH + 20, 230, 42);

        container.setPreferredSize(new Dimension(w - scrollbarW, formY + colH + 90));
        container.revalidate();
        container.repaint();
    }

    private void loadProfile() {
        int customerId = Session.getCustomerId();
        if (customerId <= 0) return;

        try (Connection con = DBConnection.getConnection()) {
            CustomerRecord record = customerDAO.getCustomerById(con, customerId);
            if (record != null) {
                if (record.username != null) {
                    usernameHeader.setText(record.username);
                }
                firstNameField.setText(record.firstName != null ? record.firstName : "");
                middleNameField.setText(record.middleName != null ? record.middleName : "");
                lastNameField.setText(record.lastName != null ? record.lastName : "");
                emailField.setText(record.email != null ? record.email : "");
                if (record.sex != null) {
                    genderCombo.setSelectedItem(record.sex);
                }
                ageField.setText(String.valueOf(record.age));
                if (record.status != null) {
                    accountStatusPill.setText(record.status.toUpperCase());
                }
            }
        } catch (SQLException ex) {
            showError("Database error loading profile: " + ex.getMessage());
        }
    }

    private void handleSaveProfile() {
        int customerId = Session.getCustomerId();
        if (customerId <= 0) {
            showError("No active customer session found.");
            return;
        }

        String first = firstNameField.getText().trim();
        String middle = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String sex = (String) genderCombo.getSelectedItem();
        String ageStr = ageField.getText().trim();

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || ageStr.isEmpty()) {
            showError("Please fill in all required fields (*).");
            return;
        }

        Integer ageValue = ValidationUtil.parsePositiveInt(ageStr);
        if (ageValue == null || ageValue > 130) {
            showError("Please enter a valid age.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (customerDAO.emailExistsForOther(con, email, customerId)) {
                showError("The email address '" + email + "' is already in use by another account.");
                return;
            }

            boolean updated = customerDAO.updateCustomer(
                    con, customerId, first, middle.isEmpty() ? null : middle, last, email, sex, ageValue);

            if (updated) {
                showInfo("Profile updated successfully!");
                loadProfile();
            } else {
                showError("Update failed: customer record not found.");
            }
        } catch (SQLException ex) {
            showError("Failed to update profile: " + ex.getMessage());
        }
    }
}