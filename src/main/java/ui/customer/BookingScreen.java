package ui.customer;

import common.Screen;
import common.Session;
import dao.BookingDAO;
import dao.CustomerDAO;
import dao.TravelPackageDAO;
import dao.TravelPackageDAO.PackageRecord;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class BookingScreen extends Screen {

    private final TravelPackageDAO packageDAO = new TravelPackageDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final List<PackageRecord> availablePackages = new ArrayList<>();

    private String preselectedPackageName;

    // Header bar components
    private JLabel logoLbl;
    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;

    // Scrollable canvas
    private JScrollPane scrollPane;
    private JPanel container;

    // 1. Trip & Package Specifications Card
    private JPanel formCard;
    private JLabel pkgLbl;
    private JComboBox<String> packageCombo;
    private JLabel dateLbl;
    private JTextField travelDateField;
    private JLabel paxLbl;
    private JSpinner travelersSpinner;
    private JLabel notesLbl;
    private JTextField notesField;

    // 2. Traveler Information Card (FR-09 Manifest)
    public static class TravelerRow {
        public JPanel panel;
        public JLabel numLbl;
        public JTextField firstNameField;
        public JTextField lastNameField;
        public JTextField dobField;
        public JTextField contactField;
    }

    private JPanel travelersCard;
    private JLabel travelersSubLbl;
    private JPanel tableHeaderPanel;
    private final List<TravelerRow> travelerRows = new ArrayList<>();

    // 3. Reservation & Pricing Summary Card
    private JPanel summaryCard;
    private JLabel pkgNameLabel;
    private JLabel destLabel;
    private JLabel rateLabel;
    private JLabel capacityLabel;
    private JPanel divider;
    private JLabel totalTitle;
    private JLabel totalAmountLabel;
    private JLabel paxBreakdownLabel;
    private JLabel noteLbl;
    private JButton proceedBtn;

    // Customer lead passenger cache
    private String defaultLeadFirst = "";
    private String defaultLeadLast = "";
    private String defaultLeadDob = "2000-01-01";
    private String defaultLeadContact = "+63 9";

    public BookingScreen() {
        this(null);
    }

    public BookingScreen(String packageName) {
        super("Book a Tour Package - Customer Portal", 1150, 700);
        this.preselectedPackageName = packageName;

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        loadCustomerLeadDefaults();
        loadPackages();
        initUI();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    private void loadCustomerLeadDefaults() {
        int custId = Session.getCustomerId();
        if (custId > 0) {
            try {
                CustomerDAO.CustomerRecord cr = customerDAO.getCustomerById(custId);
                if (cr != null) {
                    if (cr.firstName != null && !cr.firstName.trim().isEmpty()) {
                        defaultLeadFirst = cr.firstName.trim();
                    }
                    if (cr.lastName != null && !cr.lastName.trim().isEmpty()) {
                        defaultLeadLast = cr.lastName.trim();
                    }
                    if (cr.age > 0) {
                        defaultLeadDob = LocalDate.now().minusYears(cr.age).withMonth(1).withDayOfMonth(1).toString();
                    }
                }
            } catch (Exception ignored) {}
        }
        if (defaultLeadFirst.isEmpty() && Session.getUsername() != null) {
            defaultLeadFirst = Session.getUsername();
        }
    }

    private void loadPackages() {
        availablePackages.clear();
        try {
            List<PackageRecord> list = packageDAO.getAllPackages();
            for (PackageRecord p : list) {
                if ("Active".equalsIgnoreCase(p.status)) {
                    availablePackages.add(p);
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load tour packages: " + ex.getMessage());
        }
    }

    private void initUI() {
        ImageIcon logo = getLogoIcon(38, 38);
        if (logo != null) {
            logoLbl = new JLabel(logo);
            add(logoLbl);
        }

        categoryTag = createLabel("CUSTOMER PORTAL  •  RESERVATION CHECKOUT", 93, 14, 600, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        header = createHeader("Book a Tour Package", 93, 30, 600, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        subheader = createLabel("Select your package, specify your preferred departure date, and enter passenger information.", 93, 60, 650, 18);
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

        buildFormCard();
        buildTravelersCard();
        buildSummaryCard();

        updatePricingSummary();
    }

    private void buildFormCard() {
        formCard = new JPanel(null);
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        createCardTag(formCard, "TRAVEL SPECIFICATIONS", "packages.png");

        // 1. Tour Package Dropdown
        pkgLbl = new JLabel("Select Tour Package *");
        pkgLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pkgLbl.setForeground(COLOR_TEXT_MAIN);
        formCard.add(pkgLbl);

        String[] pkgNames = new String[availablePackages.size()];
        int selectIdx = 0;
        for (int i = 0; i < availablePackages.size(); i++) {
            pkgNames[i] = availablePackages.get(i).name;
            if (preselectedPackageName != null && preselectedPackageName.equalsIgnoreCase(pkgNames[i])) {
                selectIdx = i;
            }
        }
        packageCombo = new JComboBox<>(pkgNames);
        packageCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        packageCombo.setBackground(Color.WHITE);
        if (pkgNames.length > 0) {
            packageCombo.setSelectedIndex(selectIdx);
        }
        packageCombo.addActionListener(e -> onPackageChanged());
        formCard.add(packageCombo);

        // 2. Travel Date (Default 14 days from now)
        dateLbl = new JLabel("Departure Date (YYYY-MM-DD) *");
        dateLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        dateLbl.setForeground(COLOR_TEXT_MAIN);
        formCard.add(dateLbl);

        travelDateField = createStyledTextField();
        travelDateField.setText(LocalDate.now().plusDays(14).toString());
        formCard.add(travelDateField);

        // 3. Number of Travelers (Pax Spinner)
        paxLbl = new JLabel("Number of Travelers *");
        paxLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        paxLbl.setForeground(COLOR_TEXT_MAIN);
        formCard.add(paxLbl);

        int initialMin = 1;
        int initialMax = 10;
        PackageRecord sel = getSelectedPackage();
        if (sel != null) {
            initialMin = sel.minTraveller;
            initialMax = sel.maxTraveller;
        }

        travelersSpinner = new JSpinner(new SpinnerNumberModel(initialMin, initialMin, initialMax, 1));
        travelersSpinner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        travelersSpinner.addChangeListener(e -> {
            int pax = (Integer) travelersSpinner.getValue();
            syncTravelerRows(pax);
            updatePricingSummary();
            relayout();
        });
        formCard.add(travelersSpinner);

        // 4. Special Requests / Notes
        notesLbl = new JLabel("Special Requests or Dietary Requirements (Optional)");
        notesLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        notesLbl.setForeground(COLOR_TEXT_MAIN);
        formCard.add(notesLbl);

        notesField = createStyledTextField();
        formCard.add(notesField);

        container.add(formCard);
    }

    private void buildTravelersCard() {
        travelersCard = new JPanel(null);
        travelersCard.setBackground(Color.WHITE);
        travelersCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        createCardTag(travelersCard, "TRAVELER INFORMATION (PASSENGER MANIFEST - FR-09)", "customers.png");

        travelersSubLbl = new JLabel("Enter full names, birthdates, and contacts for every passenger in this reservation.");
        travelersSubLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        travelersSubLbl.setForeground(COLOR_TEXT_MUTED);
        travelersCard.add(travelersSubLbl);

        // Table column headers bar
        tableHeaderPanel = new JPanel(null);
        tableHeaderPanel.setBackground(new Color(241, 245, 249));
        tableHeaderPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(203, 213, 225)));

        JLabel h0 = new JLabel("PASSENGER");
        JLabel h1 = new JLabel("FIRST NAME *");
        JLabel h2 = new JLabel("LAST NAME *");
        JLabel h3 = new JLabel("BIRTHDATE (YYYY-MM-DD) *");
        JLabel h4 = new JLabel("CONTACT NUMBER *");

        for (JLabel h : new JLabel[]{h0, h1, h2, h3, h4}) {
            h.setFont(new Font("Segoe UI", Font.BOLD, 10));
            h.setForeground(new Color(71, 85, 105));
            tableHeaderPanel.add(h);
        }
        travelersCard.add(tableHeaderPanel);

        container.add(travelersCard);

        int initPax = (Integer) travelersSpinner.getValue();
        syncTravelerRows(initPax);
    }

    private void syncTravelerRows(int targetCount) {
        if (targetCount < 1) targetCount = 1;

        // If we need to add rows
        while (travelerRows.size() < targetCount) {
            int idx = travelerRows.size() + 1;
            TravelerRow row = new TravelerRow();

            JPanel rowPanel = new JPanel(null);
            rowPanel.setBackground(Color.WHITE);
            row.panel = rowPanel;

            String labelText = (idx == 1) ? "Traveler #1 (Lead)" : ("Traveler #" + idx);
            JLabel numLbl = new JLabel(labelText);
            numLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            numLbl.setForeground(idx == 1 ? COLOR_PRIMARY : new Color(71, 85, 105));
            row.numLbl = numLbl;
            rowPanel.add(numLbl);

            row.firstNameField = createStyledTextField();
            row.lastNameField = createStyledTextField();
            row.dobField = createStyledTextField();
            row.contactField = createStyledTextField();

            if (idx == 1) {
                row.firstNameField.setText(defaultLeadFirst);
                row.lastNameField.setText(defaultLeadLast);
                row.dobField.setText(defaultLeadDob);
                row.contactField.setText(defaultLeadContact);
            } else {
                row.dobField.setText("2000-01-01");
                row.contactField.setText("+63 9");
            }

            rowPanel.add(row.firstNameField);
            rowPanel.add(row.lastNameField);
            rowPanel.add(row.dobField);
            rowPanel.add(row.contactField);

            travelersCard.add(rowPanel);
            travelerRows.add(row);
        }

        // If we need to remove extra rows
        while (travelerRows.size() > targetCount) {
            TravelerRow last = travelerRows.remove(travelerRows.size() - 1);
            travelersCard.remove(last.panel);
        }

        travelersCard.revalidate();
        travelersCard.repaint();
    }

    private void buildSummaryCard() {
        summaryCard = new JPanel(null);
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        createCardTag(summaryCard, "RESERVATION SUMMARY", "sales.png");

        pkgNameLabel = new JLabel("Package Name");
        pkgNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pkgNameLabel.setForeground(COLOR_TEXT_MAIN);
        summaryCard.add(pkgNameLabel);

        destLabel = new JLabel("Destination: —");
        destLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        destLabel.setForeground(COLOR_TEXT_MUTED);
        summaryCard.add(destLabel);

        rateLabel = new JLabel("Rate: PHP 0.00 / traveler");
        rateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rateLabel.setForeground(COLOR_TEXT_MUTED);
        summaryCard.add(rateLabel);

        capacityLabel = new JLabel("Allowed Group: 1 - 10 pax");
        capacityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        capacityLabel.setForeground(COLOR_TEXT_MUTED);
        summaryCard.add(capacityLabel);

        divider = new JPanel();
        divider.setBackground(new Color(241, 245, 249));
        summaryCard.add(divider);

        totalTitle = new JLabel("ESTIMATED TOTAL AMOUNT DUE");
        totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        totalTitle.setForeground(new Color(100, 116, 139));
        summaryCard.add(totalTitle);

        totalAmountLabel = new JLabel("PHP 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        totalAmountLabel.setForeground(new Color(5, 150, 105)); // Emerald 600
        summaryCard.add(totalAmountLabel);

        paxBreakdownLabel = new JLabel("1 Traveler × PHP 0.00");
        paxBreakdownLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        paxBreakdownLabel.setForeground(COLOR_TEXT_MUTED);
        summaryCard.add(paxBreakdownLabel);

        noteLbl = new JLabel("<html><body style='color: #64748B; font-size: 11px;'>"
            + "Instant booking reservation with online checkout. Your booking status will be set to Pending until payment confirmation."
            + "</body></html>");
        summaryCard.add(noteLbl);

        proceedBtn = createPrimaryButton(summaryCard, "Proceed to Payment →", 24, 325, 380, 44, this::handleProceedToPayment);
        proceedBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        proceedBtn.setBackground(new Color(79, 70, 229));

        container.add(summaryCard);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        return tf;
    }

    private void onPackageChanged() {
        PackageRecord p = getSelectedPackage();
        if (p != null) {
            int curVal = (Integer) travelersSpinner.getValue();
            if (curVal < p.minTraveller) curVal = p.minTraveller;
            if (curVal > p.maxTraveller) curVal = p.maxTraveller;
            travelersSpinner.setModel(new SpinnerNumberModel(curVal, p.minTraveller, p.maxTraveller, 1));
            syncTravelerRows(curVal);
        }
        updatePricingSummary();
        relayout();
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int textX = pad;
        if (logoLbl != null) {
            logoLbl.setBounds(pad, 18, 38, 38);
            textX = pad + 48; // Clean spacing, 0 overlap!
        }

        categoryTag.setBounds(textX, 14, 600, 16);
        header.setBounds(textX, 30, 600, 30);
        subheader.setBounds(textX, 60, 650, 18);
        backBtn.setBounds(w - pad - 130, 20, 130, 36);

        int scrollY = 96;
        int scrollHeight = Math.max(300, h - scrollY);
        scrollPane.setBounds(0, scrollY, w, scrollHeight);

        int scrollbarW = 18;
        int contentWidth = Math.max(900, w - (2 * pad) - scrollbarW);

        int gap = 20;
        int leftW = (int) (contentWidth * 0.63);
        int summaryW = contentWidth - leftW - gap;

        // 1. formCard
        int formH = 226;
        formCard.setBounds(pad, 10, leftW, formH);

        pkgLbl.setBounds(20, 44, leftW - 40, 16);
        packageCombo.setBounds(20, 64, leftW - 40, 32);

        int halfW = (leftW - 40 - 16) / 2;
        dateLbl.setBounds(20, 104, halfW, 16);
        travelDateField.setBounds(20, 122, halfW, 32);

        paxLbl.setBounds(20 + halfW + 16, 104, halfW, 16);
        travelersSpinner.setBounds(20 + halfW + 16, 122, halfW, 32);

        notesLbl.setBounds(20, 162, leftW - 40, 16);
        notesField.setBounds(20, 180, leftW - 40, 32);

        // 2. travelersCard
        int travelersY = 10 + formH + 16;
        travelersSubLbl.setBounds(20, 40, leftW - 40, 16);

        tableHeaderPanel.setBounds(20, 64, leftW - 40, 24);
        int colBadgeW = 125;
        int remainingW = (leftW - 40) - colBadgeW - (4 * 6);
        int inputColW = remainingW / 4;

        ((JLabel) tableHeaderPanel.getComponent(0)).setBounds(6, 3, colBadgeW, 18);
        ((JLabel) tableHeaderPanel.getComponent(1)).setBounds(colBadgeW + 6, 3, inputColW, 18);
        ((JLabel) tableHeaderPanel.getComponent(2)).setBounds(colBadgeW + 6 + (inputColW + 6), 3, inputColW, 18);
        ((JLabel) tableHeaderPanel.getComponent(3)).setBounds(colBadgeW + 6 + 2 * (inputColW + 6), 3, inputColW, 18);
        ((JLabel) tableHeaderPanel.getComponent(4)).setBounds(colBadgeW + 6 + 3 * (inputColW + 6), 3, inputColW, 18);

        int rowY = 94;
        int rowH = 34;
        int rowGap = 6;
        for (int i = 0; i < travelerRows.size(); i++) {
            TravelerRow row = travelerRows.get(i);
            row.panel.setBounds(20, rowY, leftW - 40, rowH);
            row.numLbl.setBounds(6, 6, colBadgeW, 22);
            row.firstNameField.setBounds(colBadgeW + 6, 2, inputColW, 30);
            row.lastNameField.setBounds(colBadgeW + 6 + (inputColW + 6), 2, inputColW, 30);
            row.dobField.setBounds(colBadgeW + 6 + 2 * (inputColW + 6), 2, inputColW, 30);
            row.contactField.setBounds(colBadgeW + 6 + 3 * (inputColW + 6), 2, inputColW, 30);

            rowY += rowH + rowGap;
        }

        int travelersH = rowY + 16;
        travelersCard.setBounds(pad, travelersY, leftW, travelersH);

        // 3. summaryCard
        int summaryH = 380;
        summaryCard.setBounds(pad + leftW + gap, 10, summaryW, summaryH);

        pkgNameLabel.setBounds(24, 48, summaryW - 48, 24);
        destLabel.setBounds(24, 76, summaryW - 48, 20);
        rateLabel.setBounds(24, 100, summaryW - 48, 20);
        capacityLabel.setBounds(24, 124, summaryW - 48, 20);

        divider.setBounds(24, 154, summaryW - 48, 1);
        totalTitle.setBounds(24, 168, summaryW - 48, 16);
        totalAmountLabel.setBounds(24, 188, summaryW - 48, 36);
        paxBreakdownLabel.setBounds(24, 226, summaryW - 48, 18);
        noteLbl.setBounds(24, 252, summaryW - 48, 56);
        proceedBtn.setBounds(24, 320, summaryW - 48, 44);

        int totalH = Math.max(summaryH, travelersY + travelersH) + 30;
        container.setPreferredSize(new Dimension(w - scrollbarW, totalH));
        container.revalidate();
        container.repaint();
    }

    private PackageRecord getSelectedPackage() {
        int idx = packageCombo.getSelectedIndex();
        if (idx >= 0 && idx < availablePackages.size()) {
            return availablePackages.get(idx);
        }
        return null;
    }

    private void updatePricingSummary() {
        PackageRecord p = getSelectedPackage();
        if (p == null) {
            pkgNameLabel.setText("No Package Selected");
            destLabel.setText("Destination: —");
            rateLabel.setText("Rate: PHP 0.00 / traveler");
            capacityLabel.setText("Capacity: —");
            totalAmountLabel.setText("PHP 0.00");
            paxBreakdownLabel.setText("0 Travelers");
            return;
        }

        pkgNameLabel.setText(p.name);
        destLabel.setText("Destination: " + (p.destinationName != null ? p.destinationName : "General"));
        String rateStr = p.pricePerTraveller != null ? String.format("PHP %,.2f", p.pricePerTraveller) : "PHP 0.00";
        rateLabel.setText("Rate: " + rateStr + " / traveler");
        capacityLabel.setText("Allowed Group: " + p.minTraveller + " - " + p.maxTraveller + " pax");

        int pax = (Integer) travelersSpinner.getValue();
        if (p.pricePerTraveller != null) {
            BigDecimal total = p.pricePerTraveller.multiply(BigDecimal.valueOf(pax));
            totalAmountLabel.setText(String.format("PHP %,.2f", total));
            paxBreakdownLabel.setText(pax + " Traveler" + (pax > 1 ? "s" : "") + " × " + rateStr);
        } else {
            totalAmountLabel.setText("PHP 0.00");
            paxBreakdownLabel.setText(pax + " Traveler(s)");
        }
    }

    private static class TravelerData {
        final String firstName;
        final String lastName;
        final LocalDate dob;
        final String contactNumber;

        TravelerData(String firstName, String lastName, LocalDate dob, String contactNumber) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.dob = dob;
            this.contactNumber = contactNumber;
        }
    }

    private void handleProceedToPayment() {
        PackageRecord p = getSelectedPackage();
        if (p == null) {
            showError("Please select a tour package to reserve.");
            return;
        }

        String dateStr = travelDateField.getText().trim();
        LocalDate travelDate;
        try {
            travelDate = LocalDate.parse(dateStr);
            if (travelDate.isBefore(LocalDate.now())) {
                showError("Travel departure date cannot be in the past.");
                return;
            }
        } catch (DateTimeParseException ex) {
            showError("Invalid travel departure date format. Please use YYYY-MM-DD (e.g. " + LocalDate.now().plusDays(14) + ").");
            return;
        }

        int pax = (Integer) travelersSpinner.getValue();
        if (pax < p.minTraveller || pax > p.maxTraveller) {
            showError("This package requires between " + p.minTraveller + " and " + p.maxTraveller + " travelers.");
            return;
        }

        // Validate individual travelers in travelerRows (FR-09)
        if (travelerRows.size() < pax) {
            syncTravelerRows(pax);
        }

        List<TravelerData> validatedTravelers = new ArrayList<>();
        for (int i = 0; i < pax; i++) {
            TravelerRow row = travelerRows.get(i);
            int passengerNum = i + 1;
            String fName = row.firstNameField.getText().trim();
            String lName = row.lastNameField.getText().trim();
            String dobStr = row.dobField.getText().trim();
            String contact = row.contactField.getText().trim();

            if (fName.isEmpty()) {
                showError("Traveler #" + passengerNum + ": Please enter a first name.");
                row.firstNameField.requestFocusInWindow();
                return;
            }
            if (lName.isEmpty()) {
                showError("Traveler #" + passengerNum + ": Please enter a last name.");
                row.lastNameField.requestFocusInWindow();
                return;
            }

            LocalDate dob;
            try {
                dob = LocalDate.parse(dobStr);
                if (dob.isAfter(LocalDate.now())) {
                    showError("Traveler #" + passengerNum + ": Date of birth cannot be in the future.");
                    row.dobField.requestFocusInWindow();
                    return;
                }
            } catch (DateTimeParseException ex) {
                showError("Traveler #" + passengerNum + ": Invalid date of birth format. Please use YYYY-MM-DD (e.g. 2000-01-15).");
                row.dobField.requestFocusInWindow();
                return;
            }

            if (contact.isEmpty() || contact.equals("+63 9")) {
                showError("Traveler #" + passengerNum + ": Please enter a valid contact number.");
                row.contactField.requestFocusInWindow();
                return;
            }

            validatedTravelers.add(new TravelerData(fName, lName, dob, contact));
        }

        int custAccId = Session.getCustomerAccountId();
        if (custAccId <= 0) {
            showError("Your session has expired. Please log in again.");
            return;
        }

        BigDecimal total = p.pricePerTraveller != null
            ? p.pricePerTraveller.multiply(BigDecimal.valueOf(pax))
            : BigDecimal.ZERO;

        String confirmMsg = "Confirm reservation for \"" + p.name + "\"?\n"
                          + "Number of Travelers: " + pax + "\n"
                          + "Total Amount: " + String.format("PHP %,.2f", total);

        if (confirm(confirmMsg)) {
            try {
                Timestamp ts = Timestamp.valueOf(travelDate.atStartOfDay());
                String specialRequests = notesField != null ? notesField.getText().trim() : null;
                if (specialRequests != null && specialRequests.isEmpty()) {
                    specialRequests = null;
                }
                int newBookingId = bookingDAO.createBookingWithNotes(custAccId, p.packageID, ts, specialRequests);
                if (newBookingId > 0) {
                    // Record full passenger manifest in the database traveler table (FR-09)
                    for (TravelerData td : validatedTravelers) {
                        bookingDAO.addTraveler(newBookingId, td.firstName, td.lastName,
                            java.sql.Date.valueOf(td.dob), td.contactNumber);
                    }

                    showInfo("Reservation successfully created with " + pax + " traveler(s) registered! Proceeding to payment...");
                    navigateTo(new PaymentScreen(newBookingId, total, p.name));
                } else {
                    showError("Failed to record reservation. Please try again.");
                }
            } catch (SQLException ex) {
                showError("Database error recording reservation: " + ex.getMessage());
            }
        }
    }
}