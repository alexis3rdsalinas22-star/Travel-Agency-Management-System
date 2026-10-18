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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class BookingScreen extends Screen {

    private final TravelPackageDAO packageDAO = new TravelPackageDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final List<PackageRecord> availablePackages = new ArrayList<>();

    private String preselectedPackageName;

    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;

    private JScrollPane scrollPane;
    private JPanel container;

    private JPanel formCard;
    private JComboBox<String> packageCombo;
    private JTextField travelDateField;
    private JSpinner travelersSpinner;
    private JTextField contactField;
    private JTextArea notesArea;

    private JPanel summaryCard;
    private JLabel pkgNameLabel;
    private JLabel destLabel;
    private JLabel rateLabel;
    private JLabel capacityLabel;
    private JLabel totalAmountLabel;
    private JButton proceedBtn;

    public BookingScreen() {
        this(null);
    }

    public BookingScreen(String packageName) {
        super("Book a Tour Package - Customer Portal", 1150, 700);
        this.preselectedPackageName = packageName;

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

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
        categoryTag = createLabel("CUSTOMER PORTAL  •  RESERVATION CHECKOUT", 45, 14, 600, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        header = createHeader("Book a Tour Package", 45, 30, 600, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        subheader = createLabel("Select your package, specify your preferred departure date, and review pricing.", 45, 60, 650, 18);
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
        buildSummaryCard();

        updatePricingSummary();
    }

    private void buildFormCard() {
        formCard = new JPanel(null);
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        createCardTag(formCard, "TRAVEL SPECIFICATIONS");

        // 1. Tour Package Dropdown
        JLabel pkgLbl = new JLabel("Select Tour Package *");
        pkgLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pkgLbl.setForeground(COLOR_TEXT_MAIN);
        pkgLbl.setBounds(24, 46, 300, 16);
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
        packageCombo.addActionListener(e -> updatePricingSummary());
        formCard.add(packageCombo);

        // 2. Travel Date (Default 14 days from now)
        JLabel dateLbl = new JLabel("Departure Date (YYYY-MM-DD) *");
        dateLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        dateLbl.setForeground(COLOR_TEXT_MAIN);
        dateLbl.setBounds(24, 114, 300, 16);
        formCard.add(dateLbl);

        travelDateField = createTextField(24, 134, 450, 34);
        travelDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        travelDateField.setText(LocalDate.now().plusDays(14).toString());
        travelDateField.setBackground(Color.WHITE);
        travelDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));
        formCard.add(travelDateField);

        // 3. Number of Travelers (Pax Spinner)
        JLabel paxLbl = new JLabel("Number of Travelers *");
        paxLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        paxLbl.setForeground(COLOR_TEXT_MAIN);
        paxLbl.setBounds(24, 180, 300, 16);
        formCard.add(paxLbl);

        travelersSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
        travelersSpinner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        travelersSpinner.addChangeListener(e -> updatePricingSummary());
        formCard.add(travelersSpinner);

        // 4. Contact Number
        JLabel contactLbl = new JLabel("Primary Contact Number *");
        contactLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        contactLbl.setForeground(COLOR_TEXT_MAIN);
        contactLbl.setBounds(24, 246, 300, 16);
        formCard.add(contactLbl);

        contactField = createTextField(24, 266, 450, 34);
        contactField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactField.setText("+63 9");
        contactField.setBackground(Color.WHITE);
        contactField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));
        formCard.add(contactField);

        // 5. Special Requests / Notes
        JLabel notesLbl = new JLabel("Special Requests or Dietary Requirements (Optional)");
        notesLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        notesLbl.setForeground(COLOR_TEXT_MAIN);
        notesLbl.setBounds(24, 312, 400, 16);
        formCard.add(notesLbl);

        notesArea = new JTextArea();
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formCard.add(notesArea);

        container.add(formCard);
    }

    private void buildSummaryCard() {
        summaryCard = new JPanel(null);
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        createCardTag(summaryCard, "RESERVATION SUMMARY");

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

        JPanel divider = new JPanel();
        divider.setBackground(new Color(241, 245, 249));
        divider.setBounds(24, 180, 400, 1);
        summaryCard.add(divider);

        JLabel totalTitle = new JLabel("ESTIMATED TOTAL AMOUNT DUE");
        totalTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        totalTitle.setForeground(new Color(100, 116, 139));
        totalTitle.setBounds(24, 195, 300, 16);
        summaryCard.add(totalTitle);

        totalAmountLabel = new JLabel("PHP 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        totalAmountLabel.setForeground(new Color(5, 150, 105)); // Emerald 600
        totalAmountLabel.setBounds(24, 215, 350, 36);
        summaryCard.add(totalAmountLabel);

        JLabel noteLbl = new JLabel("<html><body style='color: #64748B; font-size: 11px;'>"
            + "Instant booking reservation with online checkout. Your booking status will be set to Pending until payment confirmation."
            + "</body></html>");
        noteLbl.setBounds(24, 260, 380, 48);
        summaryCard.add(noteLbl);

        proceedBtn = createPrimaryButton(summaryCard, "Proceed to Payment →", 24, 325, 380, 44, this::handleProceedToPayment);
        proceedBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        proceedBtn.setBackground(new Color(79, 70, 229));

        container.add(summaryCard);
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int contentWidth = w - (2 * pad);

        categoryTag.setBounds(pad, 14, 600, 16);
        header.setBounds(pad, 30, 600, 30);
        subheader.setBounds(pad, 60, 650, 18);
        backBtn.setBounds(w - pad - 130, 20, 130, 36);

        int scrollY = 96;
        int scrollHeight = Math.max(300, h - scrollY - 25);
        scrollPane.setBounds(pad, scrollY, contentWidth, scrollHeight);

        int gap = 24;
        int formW = (int) (contentWidth * 0.58);
        int summaryW = contentWidth - formW - gap - 10;
        int cardH = 430;

        formCard.setBounds(0, 10, formW, cardH);
        packageCombo.setBounds(24, 66, formW - 48, 34);
        travelDateField.setBounds(24, 134, formW - 48, 34);
        travelersSpinner.setBounds(24, 200, 140, 34);
        contactField.setBounds(24, 266, formW - 48, 34);
        notesArea.setBounds(24, 332, formW - 48, 70);

        summaryCard.setBounds(formW + gap, 10, summaryW, cardH);
        pkgNameLabel.setBounds(24, 48, summaryW - 48, 24);
        destLabel.setBounds(24, 76, summaryW - 48, 20);
        rateLabel.setBounds(24, 100, summaryW - 48, 20);
        capacityLabel.setBounds(24, 124, summaryW - 48, 20);
        proceedBtn.setBounds(24, 345, summaryW - 48, 44);

        container.setPreferredSize(new Dimension(contentWidth - 10, cardH + 30));
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
        } else {
            totalAmountLabel.setText("PHP 0.00");
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
                showError("Travel date cannot be in the past.");
                return;
            }
        } catch (DateTimeParseException ex) {
            showError("Invalid travel date format. Please use YYYY-MM-DD.");
            return;
        }

        int pax = (Integer) travelersSpinner.getValue();
        if (pax < p.minTraveller || pax > p.maxTraveller) {
            showError("This package requires between " + p.minTraveller + " and " + p.maxTraveller + " travelers.");
            return;
        }

        String contact = contactField.getText().trim();
        if (contact.isEmpty() || contact.equals("+63 9")) {
            showError("Please provide a valid contact number.");
            return;
        }

        int custAccId = Session.getCustomerAccountId();
        if (custAccId <= 0) {
            showError("Your session has expired. Please log in again.");
            return;
        }

        BigDecimal total = p.pricePerTraveller != null
            ? p.pricePerTraveller.multiply(BigDecimal.valueOf(pax))
            : BigDecimal.ZERO;

        if (confirm("Confirm reservation for \"" + p.name + "\"?\nTotal Amount: " + String.format("PHP %,.2f", total))) {
            try {
                Timestamp ts = Timestamp.valueOf(travelDate.atStartOfDay());
                int newBookingId = bookingDAO.createBooking(custAccId, p.packageID, ts);
                if (newBookingId > 0) {
                    // Record full passenger manifest in the database
                    String leadFirst = Session.getUsername() != null ? Session.getUsername() : "Passenger";
                    String leadLast = "Lead";
                    int custId = Session.getCustomerId();
                    if (custId > 0) {
                        try {
                            CustomerDAO.CustomerRecord cr = customerDAO.getCustomerById(custId);
                            if (cr != null) {
                                if (cr.firstName != null && !cr.firstName.trim().isEmpty()) leadFirst = cr.firstName.trim();
                                if (cr.lastName != null && !cr.lastName.trim().isEmpty()) leadLast = cr.lastName.trim();
                            }
                        } catch (SQLException ignored) {}
                    }

                    // Lead passenger
                    bookingDAO.addTraveler(newBookingId, leadFirst, leadLast,
                        java.sql.Date.valueOf(LocalDate.now().minusYears(25)), contact);

                    // Additional companion passengers
                    for (int i = 2; i <= pax; i++) {
                        bookingDAO.addTraveler(newBookingId, leadFirst + " Guest " + i, leadLast,
                            java.sql.Date.valueOf(LocalDate.now().minusYears(25)), contact);
                    }

                    showInfo("Reservation created successfully for " + pax + " traveler(s)! Proceeding to payment...");
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