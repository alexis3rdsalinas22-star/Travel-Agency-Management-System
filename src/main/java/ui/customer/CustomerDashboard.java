package ui.customer;

import common.Screen;
import common.Session;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;
import ui.BrowseDestinations;
import ui.BrowsePackages;
import ui.Homepage;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class CustomerDashboard extends Screen {

    private static final Color COLOR_HOVER = new Color(239, 246, 255);
    private static final Color COLOR_ON_PRIMARY_MUTED = new Color(191, 219, 254);

    private final BookingDAO bookingDAO = new BookingDAO();
    private final List<BookingRecord> recentBookings = new ArrayList<>();

    private int customerId;
    private int totalBookingsCount = 0;
    private int pendingPaymentsCount = 0;

    private JScrollPane scrollPane;
    private JPanel container;

    private JPanel hero;
    private JLabel greeting;
    private JLabel nameHeader;
    private JLabel subtitle;
    private JLabel dateLabel;
    private JButton heroBookBtn;
    private JPanel statsPill;

    private final List<JPanel> actionTiles = new ArrayList<>();
    private JPanel recentSection;
    private JPanel howItWorks;
    private JPanel accountBar;
    private JButton profileBtn;
    private JButton logoutBtn;

    public CustomerDashboard(int customerId) {
        super("Customer Dashboard - Travel Suite", 1150, 720);
        this.customerId = customerId;

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        loadCustomerStats();
        initUI();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    public CustomerDashboard() {
        this(Session.getCustomerId());
    }

    private void loadCustomerStats() {
        recentBookings.clear();
        totalBookingsCount = 0;
        pendingPaymentsCount = 0;

        int custAccId = Session.getCustomerAccountId();
        int custId = Session.getCustomerId();

        try {
            List<BookingRecord> list = bookingDAO.getBookingsByCustomer(custAccId, custId);
            totalBookingsCount = list.size();
            for (BookingRecord b : list) {
                if ("Pending".equalsIgnoreCase(b.status)) {
                    pendingPaymentsCount++;
                }
            }
            for (int i = 0; i < Math.min(3, list.size()); i++) {
                recentBookings.add(list.get(i));
            }
        } catch (SQLException ignored) {}
    }

    private void initUI() {
        createTopHeader(
                "CUSTOMER PORTAL",
                "Customer Dashboard",
                "Manage your trips, explore destinations, and book your next adventure.",
                null
        );

        container = new JPanel(null);
        container.setBackground(COLOR_BG);

        scrollPane = new JScrollPane(container);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane);

        buildHero();
        buildActionTiles();
        if (!recentBookings.isEmpty()) {
            buildRecentBookingsSection();
        }
        buildHowItWorks();
        buildAccountBar();
    }

    private void buildHero() {
        hero = new JPanel(null);
        hero.setBackground(COLOR_PRIMARY);
        hero.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(67, 56, 202), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        greeting = new JLabel(greetingText().toUpperCase());
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 10));
        greeting.setForeground(COLOR_ON_PRIMARY_MUTED);
        hero.add(greeting);

        String username = Session.getUsername();
        String title = username != null ? "Welcome back, " + username + "!" : "Welcome!";
        nameHeader = new JLabel(title);
        nameHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameHeader.setForeground(Color.WHITE);
        hero.add(nameHeader);

        subtitle = new JLabel("Plan your journeys, review reservations, and track departures.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(COLOR_ON_PRIMARY_MUTED);
        hero.add(subtitle);

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH))
                .toUpperCase();
        dateLabel = new JLabel(today, SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        dateLabel.setForeground(COLOR_ON_PRIMARY_MUTED);
        hero.add(dateLabel);

        // Stats Pill inside Hero
        statsPill = new JPanel(null);
        statsPill.setOpaque(true);
        statsPill.setBackground(new Color(255, 255, 255, 25));
        statsPill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 45), 1, true),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        String statsText = totalBookingsCount + " Reservation" + (totalBookingsCount == 1 ? "" : "s")
                + (pendingPaymentsCount > 0 ? "  •  " + pendingPaymentsCount + " Pending Payment" : "  •  All Settled");
        JLabel statsLbl = new JLabel(statsText);
        statsLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statsLbl.setForeground(Color.WHITE);
        statsLbl.setBounds(10, 4, 280, 20);
        statsPill.add(statsLbl);

        hero.add(statsPill);

        heroBookBtn = createStyledButton(hero, "Book a Package →", 0, 0, 180, 38,
                Color.WHITE, COLOR_PRIMARY, () -> navigateTo(new BookingScreen()));
        heroBookBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        container.add(hero);
    }

    private static String greetingText() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 18) return "Good afternoon";
        return "Good evening";
    }

    private void buildActionTiles() {
        actionTiles.clear();

        actionTiles.add(createTile("My Bookings",
                totalBookingsCount > 0 ? totalBookingsCount + " Active reservation(s) on file." : "View, track, and manage your trips.",
                COLOR_PRIMARY, () -> navigateTo(new MyBookings())));

        actionTiles.add(createTile("Book a Package",
                "Reserve a vacation package for your next trip.",
                COLOR_SUCCESS, () -> navigateTo(new BookingScreen())));

        actionTiles.add(createTile("Browse Destinations",
                "Discover scenic places and tourist spots.",
                new Color(2, 132, 199), () -> navigateTo(new BrowseDestinations())));

        actionTiles.add(createTile("Browse Tour Packages",
                "Compare available packages, itineraries and pricing.",
                new Color(79, 70, 229), () -> navigateTo(new BrowsePackages())));

        for (JPanel tile : actionTiles) {
            container.add(tile);
        }
    }

    private JPanel createTile(String title, String hint, Color accent, Runnable action) {
        JPanel tile = new JPanel(null);
        tile.setBackground(Color.WHITE);
        tile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
            )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(COLOR_TEXT_MAIN);
        titleLbl.setBounds(20, 20, 240, 22);
        tile.add(titleLbl);

        JLabel hintLbl = new JLabel("<html><body style='width: 200px; color: #64748B; font-family: Segoe UI; font-size: 11px;'>"
            + hint + "</body></html>");
        hintLbl.setBounds(20, 48, 240, 36);
        tile.add(hintLbl);

        JLabel openLbl = new JLabel("Open Portal →");
        openLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        openLbl.setForeground(accent);
        openLbl.setBounds(20, 96, 240, 18);
        tile.add(openLbl);

        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tile.setBackground(COLOR_HOVER);
                tile.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent, 1),
                        BorderFactory.createEmptyBorder(16, 20, 16, 20)
                    )
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                tile.setBackground(Color.WHITE);
                tile.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_BORDER, 1),
                        BorderFactory.createEmptyBorder(16, 20, 16, 20)
                    )
                ));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
        return tile;
    }

    private void buildRecentBookingsSection() {
        recentSection = new JPanel(null);
        recentSection.setBackground(Color.WHITE);
        recentSection.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        createCardTag(recentSection, "RECENT RESERVATIONS");

        int ry = 44;
        for (BookingRecord b : recentBookings) {
            JPanel row = new JPanel(null);
            row.setBackground(new Color(248, 250, 252));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 245, 249), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
            row.setBounds(20, ry, 1000, 48);

            JLabel pName = new JLabel("<html><b>" + b.packageName + "</b></html>");
            pName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pName.setForeground(COLOR_TEXT_MAIN);
            pName.setBounds(12, 12, 380, 24);
            row.add(pName);

            String dest = b.destinationName != null ? b.destinationName : "General Destination";
            JLabel dName = new JLabel(dest);
            dName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            dName.setForeground(COLOR_TEXT_MUTED);
            dName.setBounds(400, 12, 220, 24);
            row.add(dName);

            JLabel st = new JLabel(b.status != null ? b.status.toUpperCase() : "PENDING", SwingConstants.CENTER);
            st.setFont(new Font("Segoe UI", Font.BOLD, 10));
            boolean isPending = "Pending".equalsIgnoreCase(b.status);
            st.setForeground(isPending ? new Color(217, 119, 6) : new Color(5, 150, 105));
            st.setOpaque(true);
            st.setBackground(isPending ? new Color(254, 243, 199) : new Color(236, 253, 245));
            st.setBounds(640, 13, 90, 22);
            row.add(st);

            JButton actBtn = createSecondaryButton(row, "View →", 750, 8, 80, 30, () -> {
                navigateTo(new MyBookings());
            });
            setFontSize(actBtn, 11);

            recentSection.add(row);
            ry += 54;
        }

        container.add(recentSection);
    }

    private final List<JPanel> stepCols = new ArrayList<>();
    private final List<JLabel> stepDescs = new ArrayList<>();

    private void buildHowItWorks() {
        howItWorks = new JPanel(null);
        howItWorks.setBackground(Color.WHITE);
        howItWorks.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        createCardTag(howItWorks, "HOW IT WORKS");

        String[][] steps = {
            {"1", "Browse", "Explore scenic destinations and compare all-inclusive tour packages."},
            {"2", "Book",   "Select your preferred dates, specify travelers, and confirm your itinerary."},
            {"3", "Pay",    "Settle safely with GCash, bank transfer, card, or cash counter checkout."},
            {"4", "Travel", "Track vouchers, check status updates, and enjoy your holiday!"}
        };

        stepCols.clear();
        stepDescs.clear();

        for (int i = 0; i < steps.length; i++) {
            JPanel stepCol = new JPanel(null);
            stepCol.setOpaque(false);

            JLabel num = new JLabel(steps[i][0]);
            num.setFont(new Font("Segoe UI", Font.BOLD, 24));
            num.setForeground(COLOR_PRIMARY);
            num.setBounds(0, 0, 28, 30);
            stepCol.add(num);

            JLabel stepTitle = new JLabel(steps[i][1]);
            stepTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            stepTitle.setForeground(COLOR_TEXT_MAIN);
            stepTitle.setBounds(34, 4, 180, 20);
            stepCol.add(stepTitle);

            JLabel desc = new JLabel("<html><body style='color: #64748B; font-family: Segoe UI; font-size: 11px; line-height: 1.35;'>"
                + steps[i][2] + "</body></html>");
            desc.setVerticalAlignment(SwingConstants.TOP);
            desc.setBounds(34, 26, 240, 56);
            stepCol.add(desc);

            stepCols.add(stepCol);
            stepDescs.add(desc);
            howItWorks.add(stepCol);
        }

        container.add(howItWorks);
    }

    private void buildAccountBar() {
        accountBar = new JPanel(null);
        accountBar.setBackground(Color.WHITE);
        accountBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        profileBtn = createSecondaryButton(accountBar, "My Profile & Account Settings", 0, 0, 10, 10,
                () -> navigateTo(new CustomerProfile()));
        profileBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        logoutBtn = createDangerButton(accountBar, "Log Out", 0, 0, 10, 10, () -> {
            if (confirm("Are you sure you want to log out?")) {
                Session.clear();
                navigateTo(new Homepage());
            }
        });
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        container.add(accountBar);
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int scrollbarMargin = 20;
        int contentWidth = Math.max(880, w - (2 * pad) - scrollbarMargin);

        int scrollY = 90;
        int scrollHeight = Math.max(300, h - scrollY - 20);
        scrollPane.setBounds(pad, scrollY, contentWidth, scrollHeight);

        int currentY = 10;

        // 1. Hero
        int heroH = 115;
        hero.setBounds(0, currentY, contentWidth, heroH);

        greeting.setBounds(24, 14, 400, 16);
        nameHeader.setBounds(24, 32, 500, 30);
        subtitle.setBounds(24, 62, 500, 18);

        statsPill.setBounds(24, 82, 300, 26);

        dateLabel.setBounds(contentWidth - 360, 14, 336, 16);
        heroBookBtn.setBounds(contentWidth - 204, 52, 180, 38);

        currentY += heroH + 18;

        // 2. 4 Action Tiles
        int numTiles = 4;
        int tileGap = 16;
        int tileW = (contentWidth - ((numTiles - 1) * tileGap)) / numTiles;
        int tileH = 135;

        for (int i = 0; i < actionTiles.size(); i++) {
            JPanel tile = actionTiles.get(i);
            tile.setBounds(i * (tileW + tileGap), currentY, tileW, tileH);
        }

        currentY += tileH + 18;

        // 3. Recent Bookings (if any)
        if (recentSection != null) {
            int recentH = 44 + recentBookings.size() * 54 + 14;
            recentSection.setBounds(0, currentY, contentWidth, recentH);

            for (java.awt.Component c : recentSection.getComponents()) {
                if (c instanceof JPanel && c != recentSection) {
                    JPanel row = (JPanel) c;
                    row.setBounds(20, row.getY(), contentWidth - 40, row.getHeight());
                }
            }

            currentY += recentH + 18;
        }

        // 4. How It Works
        int hiwH = 145;
        howItWorks.setBounds(0, currentY, contentWidth, hiwH);
        int stepGap = 16;
        int stepW = (contentWidth - 48 - (3 * stepGap)) / 4;
        for (int i = 0; i < stepCols.size(); i++) {
            JPanel col = stepCols.get(i);
            col.setBounds(24 + i * (stepW + stepGap), 42, stepW, 92);

            if (i < stepDescs.size()) {
                JLabel desc = stepDescs.get(i);
                int textW = Math.max(160, stepW - 36);
                desc.setBounds(34, 26, textW, 60);
            }
        }

        currentY += hiwH + 18;

        // 5. Account Bar
        int accH = 64;
        accountBar.setBounds(0, currentY, contentWidth, accH);
        int btnW = (contentWidth - 48) / 2;
        profileBtn.setBounds(20, 14, btnW - 8, 38);
        logoutBtn.setBounds(20 + btnW + 8, 14, btnW - 8, 38);

        currentY += accH + 25;

        container.setPreferredSize(new Dimension(contentWidth - 10, currentY));
        container.revalidate();
        container.repaint();
    }
}