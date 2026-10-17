package ui.admin;

import common.Screen;
import common.Session;
import dao.BookingDAO;
import dao.DestinationDAO;
import dao.PaymentDAO;
import dao.PaymentDAO.SalesSummary;
import dao.TravelPackageDAO;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ui.Homepage;

public class AdminDashboard extends Screen {

    public AdminDashboard() {
        super("Admin Dashboard - Executive Suite", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);

        initUI();
    }

    private void initUI() {
        // -------------------------------------------------------------
        // Header & Active Session Info
        // -------------------------------------------------------------
        JLabel categoryTag = createLabel("ADMINISTRATION PORTAL  •  COMMAND CENTER", 45, 16, 500, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 11);
        setTextColor(categoryTag, COLOR_PRIMARY);

        JLabel title = createHeader("Administrator Dashboard", 45, 34, 600, 32);
        setFontSize(title, 24);
        setTextColor(title, COLOR_TEXT_MAIN);

        String welcome = "Welcome to the Admin Portal";
        if (Session.getUsername() != null) {
            welcome = "Active Session: " + Session.getUsername() + " (" + Session.getUserRole() + ")";
        }
        JLabel subtitle = createLabel(welcome + "  •  Manage trip catalogs, reservations, accounts, and audit reports.", 45, 68, 800, 18);
        setFontSize(subtitle, 12);
        setTextColor(subtitle, COLOR_TEXT_MUTED);

        // Quick Logout Button in Header
        createDangerButton("Log Out", 1015, 24, 135, 36, () -> {
            if (confirm("Are you sure you want to log out of the administration portal?")) {
                Session.clear();
                navigateTo(new Homepage());
            }
        });

        // -------------------------------------------------------------
        // Quick Stat Strip (4 Metric Indicator Pills with Live Counts)
        // -------------------------------------------------------------
        String destStat = "Catalog Spots";
        try {
            int count = new DestinationDAO().getAllDestinations().size();
            destStat = count + " Active Destinations";
        } catch (Exception ignored) {}

        String pkgStat = "Itineraries & Pricing";
        try {
            int count = new TravelPackageDAO().getAllPackages().size();
            pkgStat = count + " Tour Packages";
        } catch (Exception ignored) {}

        String resStat = "Bookings & Manifests";
        try {
            int count = new BookingDAO().getAllBookings().size();
            resStat = count + " Total Bookings";
        } catch (Exception ignored) {}

        String revStat = "Revenue & Sales";
        try {
            SalesSummary summary = new PaymentDAO().getSalesReportSummary();
            if (summary != null && summary.totalRevenue != null) {
                revStat = String.format("PHP %,.2f Revenue", summary.totalRevenue);
            }
        } catch (Exception ignored) {}

        createStatCard("DESTINATIONS", destStat, new Color(14, 116, 144), 45, 96, 265, 64);
        createStatCard("PACKAGES", pkgStat, new Color(79, 70, 229), 325, 96, 265, 64);
        createStatCard("RESERVATIONS", resStat, COLOR_PRIMARY, 605, 96, 265, 64);
        createStatCard("ACCOUNTS & SALES", revStat, COLOR_SUCCESS, 885, 96, 265, 64);

        // -------------------------------------------------------------
        // Module Launcher Grid (6 Executive Feature Cards)
        // Row 1: y = 178, Row 2: y = 380
        // Col 1: x = 45, Col 2: x = 421, Col 3: x = 797 (width = 353 each)
        // -------------------------------------------------------------

        // -------------------------------------------------------------
        // Module Launcher Grid (6 Executive Feature Cards with Icon Badges)
        // Row 1: y = 178, Row 2: y = 380
        // Col 1: x = 45, Col 2: x = 421, Col 3: x = 797 (width = 353 each)
        // -------------------------------------------------------------

        // 1. Destinations
        createModuleCard(
            "destinations.png", "CATALOG", "Destinations",
            "Configure tourist spots, countries, regional descriptions, and operational statuses.",
            "Manage Destinations →", new Color(14, 116, 144),
            45, 178, 353, 185,
            () -> navigateTo(new DestinationManagement())
        );

        // 2. Packages
        createModuleCard(
            "packages.png", "ITINERARIES", "Travel Packages",
            "Set bundle pricing, group traveler capacities, and destination links.",
            "Manage Packages →", new Color(79, 70, 229),
            421, 178, 353, 185,
            () -> navigateTo(new PackageManagement())
        );

        // 3. Bookings
        createModuleCard(
            "bookings.png", "RESERVATIONS", "Booking Oversight",
            "Track customer reservations, review departure dates, and update statuses.",
            "Manage Bookings →", COLOR_PRIMARY,
            797, 178, 353, 185,
            () -> navigateTo(new BookingManagement())
        );

        // 4. Customers
        createModuleCard(
            "customers.png", "DIRECTORY", "Customer Accounts",
            "Inspect traveler records, manage login credentials, and update customer profiles.",
            "Manage Customers →", COLOR_SUCCESS,
            45, 380, 353, 185,
            () -> navigateTo(new CustomerManagement())
        );

        // 5. Sales & Reports
        createModuleCard(
            "sales.png", "AUDITING", "Sales & Revenue",
            "Audit payment transactions, revenue summaries, and financial payment methods.",
            "View Sales Reports →", new Color(217, 119, 6),
            421, 380, 353, 185,
            () -> navigateTo(new SalesManagement())
        );

        // 6. Admin Management
        createModuleCard(
            "security.png", "SECURITY", "Administrator Accounts",
            "Manage administrative credentials, assign roles (Admin / Manager), and access logs.",
            "Manage Admin Accounts →", new Color(51, 65, 85),
            797, 380, 353, 185,
            () -> navigateTo(new AdminManagement())
        );

        // -------------------------------------------------------------
        // Footer Status Strip
        // -------------------------------------------------------------
        JLabel footerLbl = createLabel("TAMS Enterprise • Offline Desktop Solution • Connected to Apache Derby RDBMS", 45, 595, 800, 20);
        setFontSize(footerLbl, 11);
        setTextColor(footerLbl, COLOR_TEXT_MUTED);
    }

    private void createStatCard(String label, String sub, Color accentColor, int x, int y, int w, int h) {
        JPanel card = new JPanel(null);
        card.setBounds(x, y, w, h);
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            )
        ));

        JLabel titleLbl = createLabel(card, label, 14, 12, w - 28, 18);
        setFontStyle(titleLbl, Font.BOLD);
        setFontSize(titleLbl, 12);
        setTextColor(titleLbl, accentColor);

        JLabel descLbl = createLabel(card, sub, 14, 32, w - 28, 18);
        setFontSize(descLbl, 11);
        setTextColor(descLbl, COLOR_TEXT_MUTED);

        add(card);
    }

    private void createModuleCard(String iconFileName, String tag, String title, String desc, String btnText,
                                  Color accentColor, int x, int y, int w, int h, Runnable onAction) {
        JPanel card = createCard(x, y, w, h);

        // Top-right Icon Badge
        int badgeSize = 38;
        int badgeX = w - 18 - badgeSize;
        int badgeY = 14;

        JLabel iconBadge = new JLabel();
        iconBadge.setBounds(badgeX, badgeY, badgeSize, badgeSize);
        iconBadge.setOpaque(true);
        iconBadge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 22));
        iconBadge.setBorder(BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 70), 1));
        iconBadge.setHorizontalAlignment(JLabel.CENTER);

        javax.swing.ImageIcon icon = loadIcon("/images/icons/" + iconFileName, 22, 22);
        if (icon != null) {
            iconBadge.setIcon(icon);
        }
        card.add(iconBadge);

        // Tag & Title (accommodate badge on right)
        int textWidth = badgeX - 18 - 8;
        JLabel tagLbl = createLabel(card, tag, 18, 14, textWidth, 16);
        setFontStyle(tagLbl, Font.BOLD);
        setFontSize(tagLbl, 10);
        setTextColor(tagLbl, accentColor);

        JLabel titleLbl = createLabel(card, title, 18, 34, textWidth, 24);
        setFontStyle(titleLbl, Font.BOLD);
        setFontSize(titleLbl, 16);
        setTextColor(titleLbl, COLOR_TEXT_MAIN);

        // Body Description
        JLabel descLbl = createLabel(card, "<html><body style='width: " + (w - 40) + "px;'>" + desc + "</body></html>", 18, 64, w - 36, 44);
        setFontSize(descLbl, 12);
        setTextColor(descLbl, COLOR_TEXT_MUTED);

        // Bottom Action Button
        JButton actionBtn = createStyledButton(card, btnText, 18, 126, w - 36, 38,
            COLOR_BG, accentColor, COLOR_BORDER, 12, onAction);
        setFontStyle(actionBtn, Font.BOLD);
    }
}
