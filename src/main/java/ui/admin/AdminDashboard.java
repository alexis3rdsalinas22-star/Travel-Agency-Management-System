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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ui.Homepage;

public class AdminDashboard extends Screen {

    private static class StatCardHolder {
        JPanel panel;
        JLabel tagLbl;
        JLabel valLbl;
        JLabel subLbl;
        JLabel iconBadge;
    }

    private static class ModuleCardHolder {
        JPanel panel;
        JLabel iconBadge;
        JLabel tagLbl;
        JLabel titleLbl;
        JLabel descLbl;
        JButton actionBtn;
        String descText;
    }

    private JButton logoutBtn;
    private JLabel footerLbl;
    private final List<StatCardHolder> statCards = new ArrayList<>();
    private final List<ModuleCardHolder> moduleCards = new ArrayList<>();

    public AdminDashboard() {
        super("Admin Dashboard - Executive Suite", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);
        setFullscreen(true);

        initUI();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    private void initUI() {
        // -------------------------------------------------------------
        // Header & Active Session Info
        // -------------------------------------------------------------
        ImageIcon logo = getLogoIcon(38, 38);
        int textX = 45;
        if (logo != null) {
            JLabel logoLbl = new JLabel(logo);
            logoLbl.setBounds(45, 18, 38, 38);
            add(logoLbl);
            textX = 95;
        }

        JLabel categoryTag = createLabel("ADMINISTRATION PORTAL  •  COMMAND CENTER", textX, 14, 550, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        JLabel title = createHeader("Administrator Dashboard", textX, 30, 600, 32);
        setFontSize(title, 24);
        setTextColor(title, COLOR_TEXT_MAIN);

        String username = Session.getUsername() != null ? Session.getUsername() : "Administrator";
        String role = Session.getUserRole() != null ? Session.getUserRole() : "Admin";
        JLabel subtitle = createLabel("Active Session: " + username + "  (" + role + ")  •  System-wide operations and catalogs overview", textX, 64, 800, 18);
        setFontSize(subtitle, 12);
        setTextColor(subtitle, COLOR_TEXT_MUTED);

        // Quick Logout Button in Header
        logoutBtn = createDangerButton("Log Out", 1015, 20, 135, 36, () -> {
            if (confirm("Are you sure you want to log out of the administration portal?")) {
                Session.clear();
                navigateTo(new Homepage());
            }
        });
        logoutBtn.setIcon(loadIcon("/images/icons/logout.png", 16, 16));

        // -------------------------------------------------------------
        // Quick Stat Strip (4 Metric Indicator Pills with Live Counts)
        // -------------------------------------------------------------
        String destStat = "Catalog Spots";
        try {
            int count = new DestinationDAO().getAllDestinations().size();
            destStat = count + " Active";
        } catch (Exception ignored) {}

        String pkgStat = "Itineraries & Pricing";
        try {
            int count = new TravelPackageDAO().getAllPackages().size();
            pkgStat = count + " Packages";
        } catch (Exception ignored) {}

        String resStat = "Bookings & Manifests";
        try {
            int count = new BookingDAO().getAllBookings().size();
            resStat = count + " Total";
        } catch (Exception ignored) {}

        String revStat = "Revenue & Sales";
        try {
            SalesSummary summary = new PaymentDAO().getSalesReportSummary();
            if (summary != null && summary.totalRevenue != null) {
                revStat = String.format("PHP %,.2f", summary.totalRevenue);
            }
        } catch (Exception ignored) {}

        statCards.add(createStatCard("destinations.png", "DESTINATIONS", destStat, "Catalog Spots", new Color(14, 116, 144)));
        statCards.add(createStatCard("packages.png", "TOUR PACKAGES", pkgStat, "Curated Itineraries", new Color(79, 70, 229)));
        statCards.add(createStatCard("calendar.png", "RESERVATIONS", resStat, "Client Bookings", COLOR_PRIMARY));
        statCards.add(createStatCard("wallet.png", "TOTAL REVENUE", revStat, "Audited Sales", COLOR_SUCCESS));

        // -------------------------------------------------------------
        // Module Launcher Grid (6 Executive Feature Cards with Icon Badges)
        // -------------------------------------------------------------

        // 1. Destinations
        moduleCards.add(createModuleCard(
            "destinations.png", "CATALOG", "Destinations",
            "Configure tourist spots, countries, regional descriptions, and operational statuses.",
            "Manage Destinations →", new Color(14, 116, 144),
            () -> navigateTo(new DestinationManagement())
        ));

        // 2. Packages
        moduleCards.add(createModuleCard(
            "packages.png", "ITINERARIES", "Travel Packages",
            "Set bundle pricing, group traveler capacities, and destination links.",
            "Manage Packages →", new Color(79, 70, 229),
            () -> navigateTo(new PackageManagement())
        ));

        // 3. Bookings
        moduleCards.add(createModuleCard(
            "bookings.png", "RESERVATIONS", "Booking Oversight",
            "Track customer reservations, review departure dates, and update statuses.",
            "Manage Bookings →", COLOR_PRIMARY,
            () -> navigateTo(new BookingManagement())
        ));

        // 4. Customers
        moduleCards.add(createModuleCard(
            "customers.png", "DIRECTORY", "Customer Accounts",
            "Inspect traveler records, manage login credentials, and update customer profiles.",
            "Manage Customers →", COLOR_SUCCESS,
            () -> navigateTo(new CustomerManagement())
        ));

        // 5. Sales & Reports
        moduleCards.add(createModuleCard(
            "sales.png", "AUDITING", "Sales & Revenue",
            "Audit payment transactions, revenue summaries, and financial payment methods.",
            "View Sales Reports →", new Color(217, 119, 6),
            () -> navigateTo(new SalesManagement())
        ));

        // 6. Admin Management
        moduleCards.add(createModuleCard(
            "security.png", "SECURITY", "Administrator Accounts",
            "Manage administrative credentials, assign roles (Admin / Manager), and access logs.",
            "Manage Admin Accounts →", new Color(51, 65, 85),
            () -> navigateTo(new AdminManagement())
        ));

        // -------------------------------------------------------------
        // Footer Status Strip
        // -------------------------------------------------------------
        footerLbl = createLabel("TAMS Enterprise • Offline Desktop Solution • Connected to Apache Derby RDBMS", 45, 595, 800, 20);
        setFontSize(footerLbl, 11);
        setTextColor(footerLbl, COLOR_TEXT_MUTED);
    }

    private void relayout() {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        if (logoutBtn != null) {
            logoutBtn.setBounds(w - pad - 135, 20, 135, 36);
        }

        // 4 Stat Cards
        int statGap = 16;
        int statWidth = (w - (2 * pad) - (3 * statGap)) / 4;
        int statHeight = 74;
        for (int i = 0; i < statCards.size(); i++) {
            StatCardHolder sc = statCards.get(i);
            int sx = pad + i * (statWidth + statGap);
            sc.panel.setBounds(sx, 92, statWidth, statHeight);
            int badgeSize = 34;
            int textW = statWidth - 28 - badgeSize - 8;
            sc.tagLbl.setSize(textW, 14);
            sc.valLbl.setSize(textW, 24);
            sc.subLbl.setSize(textW, 16);
            if (sc.iconBadge != null) {
                sc.iconBadge.setBounds(statWidth - 14 - badgeSize, (statHeight - badgeSize) / 2, badgeSize, badgeSize);
            }
        }

        // 6 Module Cards (2 rows of 3 columns)
        int modGap = 20;
        int colWidth = (w - (2 * pad) - (2 * modGap)) / 3;
        int startY = 92 + statHeight + 16;
        int footerY = h - 35;
        int availableH = footerY - startY - 10;
        int rowHeight = (availableH - modGap) / 2;
        if (rowHeight < 175) rowHeight = 175;

        for (int i = 0; i < moduleCards.size(); i++) {
            ModuleCardHolder mc = moduleCards.get(i);
            int col = i % 3;
            int row = i / 3;
            int mx = pad + col * (colWidth + modGap);
            int my = startY + row * (rowHeight + modGap);
            mc.panel.setBounds(mx, my, colWidth, rowHeight);

            int badgeSize = 38;
            int badgeX = colWidth - 18 - badgeSize;
            mc.iconBadge.setBounds(badgeX, 14, badgeSize, badgeSize);

            int textWidth = badgeX - 18 - 8;
            mc.tagLbl.setBounds(18, 14, textWidth, 16);
            mc.titleLbl.setBounds(18, 34, textWidth, 24);

            int descH = rowHeight - 64 - 52;
            if (descH < 35) descH = 35;
            mc.descLbl.setBounds(18, 64, colWidth - 36, descH);
            mc.descLbl.setText("<html><body style='width: " + (colWidth - 45) + "px;'>" + mc.descText + "</body></html>");

            mc.actionBtn.setBounds(18, rowHeight - 46, colWidth - 36, 36);
        }

        if (footerLbl != null) {
            footerLbl.setBounds(pad, h - 30, w - 2 * pad, 20);
        }
        repaint();
    }

    private StatCardHolder createStatCard(String iconFileName, String tag, String value, String subtitle, Color accentColor) {
        StatCardHolder holder = new StatCardHolder();
        JPanel card = new JPanel(null);
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
            )
        ));

        holder.tagLbl = createLabel(card, tag, 14, 8, 200, 14);
        setFontStyle(holder.tagLbl, Font.BOLD);
        setFontSize(holder.tagLbl, 10);
        setTextColor(holder.tagLbl, accentColor);

        holder.valLbl = createLabel(card, value, 14, 24, 200, 24);
        setFontStyle(holder.valLbl, Font.BOLD);
        setFontSize(holder.valLbl, 16);
        setTextColor(holder.valLbl, COLOR_TEXT_MAIN);

        holder.subLbl = createLabel(card, subtitle, 14, 48, 200, 16);
        setFontSize(holder.subLbl, 11);
        setTextColor(holder.subLbl, COLOR_TEXT_MUTED);

        int badgeSize = 34;
        JLabel iconBadge = new JLabel();
        iconBadge.setOpaque(true);
        iconBadge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 20));
        iconBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60), 1, true),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        iconBadge.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon icon = loadIcon("/images/icons/" + iconFileName, 18, 18);
        if (icon != null) {
            iconBadge.setIcon(icon);
        }
        card.add(iconBadge);
        holder.iconBadge = iconBadge;

        holder.panel = card;
        add(card);
        return holder;
    }

    private ModuleCardHolder createModuleCard(String iconFileName, String tag, String title, String desc, String btnText,
                                              Color accentColor, Runnable onAction) {
        ModuleCardHolder holder = new ModuleCardHolder();
        holder.descText = desc;

        JPanel card = createCard(0, 0, 300, 180);
        holder.panel = card;

        // Top-right Icon Badge
        int badgeSize = 38;
        JLabel iconBadge = new JLabel();
        iconBadge.setOpaque(true);
        iconBadge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 22));
        iconBadge.setBorder(BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 70), 1));
        iconBadge.setHorizontalAlignment(JLabel.CENTER);

        javax.swing.ImageIcon icon = loadIcon("/images/icons/" + iconFileName, 22, 22);
        if (icon != null) {
            iconBadge.setIcon(icon);
        }
        card.add(iconBadge);
        holder.iconBadge = iconBadge;

        // Tag & Title
        JLabel tagLbl = createLabel(card, tag, 18, 14, 200, 16);
        setFontStyle(tagLbl, Font.BOLD);
        setFontSize(tagLbl, 10);
        setTextColor(tagLbl, accentColor);
        holder.tagLbl = tagLbl;

        JLabel titleLbl = createLabel(card, title, 18, 34, 200, 24);
        setFontStyle(titleLbl, Font.BOLD);
        setFontSize(titleLbl, 16);
        setTextColor(titleLbl, COLOR_TEXT_MAIN);
        holder.titleLbl = titleLbl;

        // Body Description
        JLabel descLbl = createLabel(card, "<html><body>" + desc + "</body></html>", 18, 64, 250, 46);
        setFontSize(descLbl, 12);
        setTextColor(descLbl, COLOR_TEXT_MUTED);
        holder.descLbl = descLbl;

        // Bottom Action Button with interactive hover
        JButton actionBtn = createStyledButton(card, btnText, 18, 126, 250, 36,
            new Color(248, 250, 252), accentColor, new Color(226, 232, 240), 12, onAction);
        setFontStyle(actionBtn, Font.BOLD);

        actionBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                actionBtn.setBackground(accentColor);
                actionBtn.setForeground(Color.WHITE);
                actionBtn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                actionBtn.setBackground(new Color(248, 250, 252));
                actionBtn.setForeground(accentColor);
                actionBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));
            }
        });
        holder.actionBtn = actionBtn;

        return holder;
    }
}
