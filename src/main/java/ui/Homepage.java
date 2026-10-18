package ui;

import common.ImageUtil;
import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;
import dao.TravelPackageDAO;
import dao.TravelPackageDAO.PackageRecord;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Homepage extends Screen {

    private final DestinationDAO destinationDAO = new DestinationDAO();
    private final TravelPackageDAO packageDAO = new TravelPackageDAO();

    private final List<DestinationRecord> featuredDestinations = new ArrayList<>();
    private final List<PackageRecord> featuredPackages = new ArrayList<>();

    private JScrollPane pageScrollPane;
    private JPanel pageContainer;

    // Navbar
    private JPanel navbar;
    private JLabel navBrandLogo;
    private JLabel navBrandTitle;
    private JLabel navBrandSubtitle;
    private JButton navDestBtn;
    private JButton navPkgBtn;
    private JButton navLoginBtn;
    private JButton navAdminBtn;
    private JLabel navDbStatus;

    // Hero Section
    private HeroBanner heroBanner;
    private JLabel heroTag;
    private JLabel heroTitle;
    private JLabel heroSubtitle;
    private JPanel heroSearchPanel;
    private JTextField heroSearchField;
    private JButton heroSearchBtn;
    private JPanel heroStatsRow;

    // Featured Highlights Section
    private JLabel featuredSectionTag;
    private JLabel featuredSectionTitle;
    private JButton featuredViewAllBtn;
    private JPanel featuredCardsContainer;
    private final List<JPanel> featuredCardPanels = new ArrayList<>();

    // Main 3 Portal Access Cards
    private JLabel portalsSectionTag;
    private JLabel portalsSectionTitle;
    private JPanel cardTraveler;
    private JPanel cardCatalogs;
    private JPanel cardAdmin;

    // Footer
    private JPanel footerPanel;
    private JLabel footerBrand;
    private JLabel footerStatus;

    public Homepage() {
        super("Travel Agency Management System - Home", 1180, 720);

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        loadFeaturedData();
        initUI();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    private void loadFeaturedData() {
        try {
            List<DestinationRecord> dList = destinationDAO.getAllDestinations();
            for (int i = 0; i < Math.min(2, dList.size()); i++) {
                featuredDestinations.add(dList.get(i));
            }
        } catch (SQLException ignored) {}

        try {
            List<PackageRecord> pList = packageDAO.getAllPackages();
            for (int i = 0; i < Math.min(2, pList.size()); i++) {
                featuredPackages.add(pList.get(i));
            }
        } catch (SQLException ignored) {}
    }

    private void initUI() {
        pageContainer = new JPanel(null);
        pageContainer.setBackground(COLOR_BG);

        pageScrollPane = new JScrollPane(pageContainer);
        pageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        pageScrollPane.setBackground(COLOR_BG);
        pageScrollPane.getViewport().setBackground(COLOR_BG);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        pageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(pageScrollPane);

        buildNavbar();
        buildHeroBanner();
        buildFeaturedSection();
        buildPortalsSection();
        buildFooter();
    }

    // ==========================================
    // 1. Navigation Bar
    // ==========================================
    private void buildNavbar() {
        navbar = new JPanel(null);
        navbar.setBackground(Color.WHITE);
        navbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        navBrandLogo = new JLabel("TAMS", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(79, 70, 229)); // Indigo 600
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        navBrandLogo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        navBrandLogo.setForeground(Color.WHITE);
        navBrandLogo.setOpaque(false);
        navbar.add(navBrandLogo);

        navBrandTitle = new JLabel("Travel Agency Management System");
        navBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navBrandTitle.setForeground(COLOR_TEXT_MAIN);
        navbar.add(navBrandTitle);

        navBrandSubtitle = new JLabel("Enterprise Desktop Edition");
        navBrandSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        navBrandSubtitle.setForeground(COLOR_TEXT_MUTED);
        navbar.add(navBrandSubtitle);

        navDbStatus = new JLabel("● Derby DB Connected");
        navDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navDbStatus.setForeground(new Color(5, 150, 105)); // Emerald 600
        navbar.add(navDbStatus);

        navDestBtn = createSecondaryButton(navbar, "Destinations", 0, 0, 110, 32, () -> navigateTo(new BrowseDestinations()));
        setFontSize(navDestBtn, 11);

        navPkgBtn = createSecondaryButton(navbar, "Tour Packages", 0, 0, 115, 32, () -> navigateTo(new BrowsePackages()));
        setFontSize(navPkgBtn, 11);

        navLoginBtn = createPrimaryButton(navbar, "Customer Sign In", 0, 0, 135, 32, () -> navigateTo(new CustomerLogin()));
        setFontSize(navLoginBtn, 11);

        navAdminBtn = createSecondaryButton(navbar, "Admin Portal", 0, 0, 105, 32, () -> navigateTo(new AdminLogin()));
        setFontSize(navAdminBtn, 11);

        pageContainer.add(navbar);
    }

    // ==========================================
    // 2. Hero Section with Interactive Search
    // ==========================================
    private void buildHeroBanner() {
        heroBanner = new HeroBanner();
        heroBanner.setLayout(null);

        heroTag = new JLabel("PREMIUM TRAVEL DIRECTORY & RESERVATION SYSTEM");
        heroTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        heroTag.setForeground(new Color(56, 189, 248)); // Sky 400
        heroBanner.add(heroTag);

        heroTitle = new JLabel("Explore, Plan & Experience Extraordinary Journeys");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heroTitle.setForeground(Color.WHITE);
        heroBanner.add(heroTitle);

        heroSubtitle = new JLabel("Curated vacation packages, scenic world destinations, and seamless offline reservation management.");
        heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heroSubtitle.setForeground(new Color(226, 232, 240));
        heroBanner.add(heroSubtitle);

        // Search Bar embedded inside Hero
        heroSearchPanel = new JPanel(null);
        heroSearchPanel.setBackground(Color.WHITE);
        heroSearchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        heroSearchField = new JTextField();
        heroSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heroSearchField.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
        heroSearchField.setText("Search destinations, tour packages, or travel spots...");
        heroSearchField.setForeground(new Color(148, 163, 184));
        heroSearchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (heroSearchField.getText().startsWith("Search destinations")) {
                    heroSearchField.setText("");
                    heroSearchField.setForeground(COLOR_TEXT_MAIN);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (heroSearchField.getText().trim().isEmpty()) {
                    heroSearchField.setText("Search destinations, tour packages, or travel spots...");
                    heroSearchField.setForeground(new Color(148, 163, 184));
                }
            }
        });
        heroSearchField.addActionListener(e -> executeHeroSearch());
        heroSearchPanel.add(heroSearchField);

        heroSearchBtn = createPrimaryButton(heroSearchPanel, "Search Catalog →", 0, 0, 155, 38, this::executeHeroSearch);
        heroSearchBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heroSearchBtn.setBackground(new Color(79, 70, 229));

        heroBanner.add(heroSearchPanel);

        // Stats Row inside Hero
        heroStatsRow = new JPanel(null);
        heroStatsRow.setOpaque(false);

        addStatPill(heroStatsRow, "FEATURED LOCATIONS", (featuredDestinations.size() > 0 ? "5+" : "Active") + " Destinations", 0);
        addStatPill(heroStatsRow, "CURATED TOURS", (featuredPackages.size() > 0 ? "5+" : "Active") + " Packages", 190);
        addStatPill(heroStatsRow, "INSTANT BOOKING", "100% Offline RDBMS", 380);
        addStatPill(heroStatsRow, "CUSTOMER PORTAL", "Self-Service Suite", 570);

        heroBanner.add(heroStatsRow);

        pageContainer.add(heroBanner);
    }

    private void addStatPill(JPanel parent, String label, String value, int x) {
        JPanel pill = new JPanel(null);
        pill.setOpaque(true);
        pill.setBackground(new Color(255, 255, 255, 25));
        pill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 45), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        pill.setBounds(x, 0, 175, 46);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(new Color(199, 210, 254)); // Indigo 200
        l.setBounds(10, 6, 155, 14);
        pill.add(l);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(Color.WHITE);
        v.setBounds(10, 22, 155, 18);
        pill.add(v);

        parent.add(pill);
    }

    private void executeHeroSearch() {
        String query = heroSearchField.getText().trim();
        if (query.isEmpty() || query.startsWith("Search destinations")) {
            navigateTo(new BrowsePackages());
        } else {
            navigateTo(new BrowsePackages(query));
        }
    }

    // ==========================================
    // 3. Featured Destinations & Packages Showcase
    // ==========================================
    private void buildFeaturedSection() {
        featuredSectionTag = new JLabel("TRENDING OFFERINGS  •  HANDPICKED HIGHLIGHTS");
        featuredSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        featuredSectionTag.setForeground(new Color(79, 70, 229));
        pageContainer.add(featuredSectionTag);

        featuredSectionTitle = new JLabel("Featured Destinations & Tour Packages");
        featuredSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        featuredSectionTitle.setForeground(COLOR_TEXT_MAIN);
        pageContainer.add(featuredSectionTitle);

        featuredViewAllBtn = createSecondaryButton(pageContainer, "View Full Catalog →", 0, 0, 160, 34, () -> {
            navigateTo(new BrowsePackages());
        });
        setFontSize(featuredViewAllBtn, 11);

        featuredCardsContainer = new JPanel(null);
        featuredCardsContainer.setOpaque(false);
        pageContainer.add(featuredCardsContainer);

        // Add 2 featured destinations and 2 featured packages
        for (DestinationRecord d : featuredDestinations) {
            featuredCardPanels.add(createFeaturedDestCard(d));
        }
        for (PackageRecord p : featuredPackages) {
            featuredCardPanels.add(createFeaturedPkgCard(p));
        }

        for (JPanel card : featuredCardPanels) {
            featuredCardsContainer.add(card);
        }
    }

    private JPanel createFeaturedDestCard(DestinationRecord dest) {
        JPanel card = new JPanel(null);
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigateTo(new DestinationDetails(dest));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(165, 180, 252), 1, true),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
        });

        // Banner (height 120px)
        FeaturedCardBanner banner = new FeaturedCardBanner(dest.imagePath, ImageUtil.DEFAULT_DESTINATION_PLACEHOLDER, "DESTINATION");
        banner.setBounds(0, 0, 260, 120);
        card.add(banner);

        // Location Tag
        JLabel tag = new JLabel("DESTINATION  •  " + (dest.country != null ? dest.country.toUpperCase() : "GLOBAL"));
        tag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        tag.setForeground(new Color(2, 132, 199)); // Sky 600
        tag.setBounds(14, 128, 230, 14);
        card.add(tag);

        // Title
        JLabel title = new JLabel("<html><b>" + escapeHtml(dest.name) + "</b></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT_MAIN);
        title.setBounds(14, 144, 230, 20);
        card.add(title);

        // Description
        String dDesc = dest.description != null ? dest.description : "Scenic vacation destination.";
        JLabel desc = new JLabel("<html><body style='color: #64748B; font-family: Segoe UI; font-size: 11px;'>"
            + escapeHtml(truncate(dDesc, 65)) + "</body></html>");
        desc.setBounds(14, 166, 230, 32);
        card.add(desc);

        // Action button
        JButton btn = createSecondaryButton(card, "Explore Destination →", 14, 204, 232, 30, () -> {
            navigateTo(new DestinationDetails(dest));
        });
        setFontSize(btn, 11);

        return card;
    }

    private JPanel createFeaturedPkgCard(PackageRecord pkg) {
        JPanel card = new JPanel(null);
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigateTo(new PackageDetails(pkg));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(165, 180, 252), 1, true),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
        });

        // Banner (height 120px)
        FeaturedCardBanner banner = new FeaturedCardBanner(pkg.imagePath, ImageUtil.DEFAULT_PACKAGE_PLACEHOLDER, "PACKAGE");
        banner.setBounds(0, 0, 260, 120);
        card.add(banner);

        // Tag with Price
        String priceStr = pkg.pricePerTraveller != null ? String.format("PHP %,.2f", pkg.pricePerTraveller) : "PHP 0.00";
        JLabel tag = new JLabel("TOUR PACKAGE  •  " + priceStr);
        tag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        tag.setForeground(new Color(5, 150, 105)); // Emerald 600
        tag.setBounds(14, 128, 230, 14);
        card.add(tag);

        // Title
        JLabel title = new JLabel("<html><b>" + escapeHtml(pkg.name) + "</b></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT_MAIN);
        title.setBounds(14, 144, 230, 20);
        card.add(title);

        // Description
        String pDesc = pkg.description != null ? pkg.description : "All-inclusive tour package.";
        JLabel desc = new JLabel("<html><body style='color: #64748B; font-family: Segoe UI; font-size: 11px;'>"
            + escapeHtml(truncate(pDesc, 65)) + "</body></html>");
        desc.setBounds(14, 166, 230, 32);
        card.add(desc);

        // Action button
        JButton btn = createPrimaryButton(card, "View Package Details →", 14, 204, 232, 30, () -> {
            navigateTo(new PackageDetails(pkg));
        });
        setFontSize(btn, 11);
        btn.setBackground(new Color(79, 70, 229));

        return card;
    }

    // ==========================================
    // 4. Main 3 Portal Access Cards
    // ==========================================
    private void buildPortalsSection() {
        portalsSectionTag = new JLabel("ACCESS GATEWAYS  •  PORTAL SELECTION");
        portalsSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        portalsSectionTag.setForeground(COLOR_TEXT_MUTED);
        pageContainer.add(portalsSectionTag);

        portalsSectionTitle = new JLabel("Application Portals & System Access");
        portalsSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        portalsSectionTitle.setForeground(COLOR_TEXT_MAIN);
        pageContainer.add(portalsSectionTitle);

        // Card 1: Traveler Portal
        cardTraveler = createPortalCard(
            "TRAVELER SERVICES",
            "Customer Portal",
            "Access your passenger bookings, view reservation vouchers, check itinerary departure dates, and manage your account profile.",
            COLOR_PRIMARY,
            new ActionButtonDef("Sign In to Account →", COLOR_PRIMARY, Color.WHITE, () -> navigateTo(new CustomerLogin())),
            new ActionButtonDef("Create New Account", COLOR_SUCCESS, Color.WHITE, () -> navigateTo(new CustomerRegister()))
        );
        pageContainer.add(cardTraveler);

        // Card 2: Trip Catalogs
        cardCatalogs = createPortalCard(
            "EXPLORE & DISCOVER",
            "Public Travel Catalogs",
            "Browse curated vacation destinations and tour packages with transparent pricing, traveler capacity limits, and trip descriptions.",
            new Color(14, 116, 144), // Cyan/Teal
            new ActionButtonDef("Browse Destinations →", new Color(14, 116, 144), Color.WHITE, () -> navigateTo(new BrowseDestinations())),
            new ActionButtonDef("Browse Tour Packages →", new Color(79, 70, 229), Color.WHITE, () -> navigateTo(new BrowsePackages()))
        );
        pageContainer.add(cardCatalogs);

        // Card 3: Administration
        cardAdmin = createPortalCard(
            "STAFF & SECURITY",
            "Administration Suite",
            "Access the administrative control center to configure trip catalogs, process customer bookings, audit sales reports, and manage access.",
            new Color(51, 65, 85), // Slate
            new ActionButtonDef("Administrator Login →", new Color(51, 65, 85), Color.WHITE, () -> navigateTo(new AdminLogin())),
            new ActionButtonDef("Exit Application", COLOR_SECONDARY, new Color(71, 85, 105), () -> {
                if (confirm("Are you sure you want to exit the application?")) {
                    System.exit(0);
                }
            })
        );
        pageContainer.add(cardAdmin);
    }

    private JPanel createPortalCard(String tagText, String titleText, String descText, Color accentColor,
                                    ActionButtonDef btn1, ActionButtonDef btn2) {
        JPanel card = new JPanel(null);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
            )
        ));

        // Pill Tag
        JLabel tag = new JLabel("  " + tagText + "  ");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tag.setForeground(accentColor);
        tag.setOpaque(true);
        tag.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 22));
        tag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 70), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        tag.setBounds(24, 20, 250, 22);
        card.add(tag);

        // Title
        JLabel cTitle = new JLabel(titleText);
        cTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        cTitle.setForeground(COLOR_TEXT_MAIN);
        cTitle.setBounds(24, 52, 280, 26);
        card.add(cTitle);

        // Description
        JLabel desc = new JLabel("<html><body style='width: 260px; line-height: 1.45; color: #64748B; font-family: Segoe UI; font-size: 11px;'>"
            + descText + "</body></html>");
        desc.setBounds(24, 82, 280, 85);
        desc.setVerticalAlignment(SwingConstants.TOP);
        card.add(desc);

        // Button 1
        if (btn1 != null) {
            JButton b1 = createStyledButton(card, btn1.text, 24, 185, 280, 38, btn1.bg, btn1.fg, btn1.action);
            setFontStyle(b1, Font.BOLD);
            setFontSize(b1, 12);
        }

        // Button 2
        if (btn2 != null) {
            JButton b2 = createStyledButton(card, btn2.text, 24, 232, 280, 38, btn2.bg, btn2.fg, btn2.action);
            setFontStyle(b2, Font.BOLD);
            setFontSize(b2, 12);
            if (btn2.bg.equals(COLOR_SECONDARY)) {
                b2.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));
            }
        }
        return card;
    }

    // ==========================================
    // 5. Footer Strip
    // ==========================================
    private void buildFooter() {
        footerPanel = new JPanel(null);
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        footerBrand = new JLabel("TAMS Enterprise • Offline Travel Agency Management System • Apache Derby SQL");
        footerBrand.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerBrand.setForeground(COLOR_TEXT_MUTED);
        footerPanel.add(footerBrand);

        footerStatus = new JLabel("Status: Fully Operational  •  Ready for Bookings", SwingConstants.RIGHT);
        footerStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        footerStatus.setForeground(new Color(5, 150, 105));
        footerPanel.add(footerStatus);

        pageContainer.add(footerPanel);
    }

    // ==========================================
    // Responsive Relayout
    // ==========================================
    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int scrollbarMargin = 20;
        int contentWidth = Math.max(880, w - (2 * pad) - scrollbarMargin);

        // 1. Navbar
        int navH = 58;
        navbar.setBounds(0, 0, w, navH);

        navBrandLogo.setBounds(pad, 13, 50, 32);
        navBrandTitle.setBounds(pad + 60, 12, 300, 20);
        navBrandSubtitle.setBounds(pad + 60, 32, 300, 14);

        int rightX = w - pad - 120;
        navAdminBtn.setBounds(rightX, 13, 105, 32);

        rightX -= 145;
        navLoginBtn.setBounds(rightX, 13, 135, 32);

        rightX -= 125;
        navPkgBtn.setBounds(rightX, 13, 115, 32);

        rightX -= 120;
        navDestBtn.setBounds(rightX, 13, 110, 32);

        rightX -= 160;
        navDbStatus.setBounds(rightX, 13, 150, 32);

        // 2. Hero Section
        int currentY = navH + 20;
        int heroH = 245;
        heroBanner.setBounds(pad, currentY, contentWidth, heroH);

        heroTag.setBounds(32, 20, 500, 16);
        heroTitle.setBounds(32, 40, contentWidth - 64, 34);
        heroSubtitle.setBounds(32, 76, contentWidth - 64, 20);

        int searchW = Math.min(680, contentWidth - 64);
        int searchH = 46;
        heroSearchPanel.setBounds(32, 108, searchW, searchH);
        heroSearchField.setBounds(4, 4, searchW - 165, 38);
        heroSearchBtn.setBounds(searchW - 160, 4, 156, 38);

        heroStatsRow.setBounds(32, 172, contentWidth - 64, 48);

        currentY += heroH + 32;

        // 3. Featured Section
        featuredSectionTag.setBounds(pad, currentY, 500, 16);
        featuredSectionTitle.setBounds(pad, currentY + 18, 500, 28);
        featuredViewAllBtn.setBounds(pad + contentWidth - 160, currentY + 12, 160, 34);

        currentY += 56;

        int numCards = Math.max(1, featuredCardPanels.size());
        int cardGap = 18;
        int cardW = (contentWidth - ((numCards - 1) * cardGap)) / numCards;
        int cardH = 250;

        featuredCardsContainer.setBounds(pad, currentY, contentWidth, cardH);
        for (int i = 0; i < featuredCardPanels.size(); i++) {
            JPanel fCard = featuredCardPanels.get(i);
            fCard.setBounds(i * (cardW + cardGap), 0, cardW, cardH);
            relayoutFeaturedCard(fCard, cardW);
        }

        currentY += cardH + 36;

        // 4. Portals Section
        portalsSectionTag.setBounds(pad, currentY, 500, 16);
        portalsSectionTitle.setBounds(pad, currentY + 18, 500, 28);

        currentY += 56;

        int portalCols = 3;
        int portalGap = 20;
        int portalW = (contentWidth - ((portalCols - 1) * portalGap)) / portalCols;
        int portalH = 295;

        cardTraveler.setBounds(pad, currentY, portalW, portalH);
        cardCatalogs.setBounds(pad + portalW + portalGap, currentY, portalW, portalH);
        cardAdmin.setBounds(pad + 2 * (portalW + portalGap), currentY, portalW, portalH);

        relayoutPortalCard(cardTraveler, portalW);
        relayoutPortalCard(cardCatalogs, portalW);
        relayoutPortalCard(cardAdmin, portalW);

        currentY += portalH + 40;

        // 5. Footer Strip
        footerPanel.setBounds(0, currentY, w, 52);
        footerBrand.setBounds(pad, 16, 600, 20);
        footerStatus.setBounds(w - pad - 400, 16, 400, 20);

        currentY += 52;

        // Set scroll content size
        pageScrollPane.setBounds(0, 0, w, h);
        pageContainer.setPreferredSize(new Dimension(w - 20, currentY));
        pageContainer.revalidate();
        pageContainer.repaint();
    }

    private void relayoutFeaturedCard(JPanel card, int cardW) {
        if (card == null) return;
        int innerPad = 14;
        int innerW = cardW - (2 * innerPad);

        for (java.awt.Component c : card.getComponents()) {
            if (c instanceof FeaturedCardBanner) {
                c.setBounds(0, 0, cardW, 120);
            } else if (c instanceof JButton) {
                c.setBounds(innerPad, 204, innerW, 32);
            } else if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                java.awt.Rectangle b = l.getBounds();
                l.setBounds(innerPad, b.y, innerW, b.height);
            }
        }
    }

    private void relayoutPortalCard(JPanel card, int cardW) {
        if (card == null) return;
        int innerPad = 24;
        int innerW = cardW - (2 * innerPad);

        for (java.awt.Component c : card.getComponents()) {
            if (c instanceof JButton) {
                java.awt.Rectangle b = c.getBounds();
                c.setBounds(innerPad, b.y, innerW, b.height);
            } else if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                java.awt.Rectangle b = l.getBounds();
                l.setBounds(innerPad, b.y, innerW, b.height);
            }
        }
    }

    // ==========================================
    // Custom Hero Banner with Gradient
    // ==========================================
    private static class HeroBanner extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Rich modern Indigo / Slate gradient
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(30, 27, 75),       // Indigo 950
                w, h, new Color(30, 41, 59)        // Slate 800
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 16, 16);

            // Subtle decorative glowing arcs
            g2.setColor(new Color(99, 102, 241, 35)); // Indigo glow
            g2.fillOval(w - 280, -80, 360, 360);

            g2.setColor(new Color(56, 189, 248, 25)); // Sky glow
            g2.fillOval(w - 180, h - 140, 260, 260);

            g2.dispose();
        }
    }

    // ==========================================
    // Featured Card Banner
    // ==========================================
    private static class FeaturedCardBanner extends JPanel {
        private final String imagePath;
        private final String placeholderPath;
        private final String fallbackTag;
        private Image cachedImage = null;
        private int cachedW = -1;
        private int cachedH = -1;

        public FeaturedCardBanner(String imagePath, String placeholderPath, String fallbackTag) {
            this.imagePath = imagePath;
            this.placeholderPath = placeholderPath;
            this.fallbackTag = fallbackTag;
            setLayout(null);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }

            // Top rounded clip (arc 12)
            RoundRectangle2D clip = new RoundRectangle2D.Float(0, 0, w, h + 16, 14, 14);
            g2.setClip(clip);

            if (cachedImage == null || cachedW != w || cachedH != h) {
                ImageIcon icon = ImageUtil.loadScaledImage(imagePath, w, h, placeholderPath);
                if (icon != null && icon.getIconWidth() > 0) {
                    cachedImage = icon.getImage();
                } else {
                    cachedImage = null;
                }
                cachedW = w;
                cachedH = h;
            }

            if (cachedImage != null) {
                g2.drawImage(cachedImage, 0, 0, w, h, this);
            } else {
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(238, 242, 255),
                    w, h, new Color(224, 231, 255)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(79, 70, 229, 170));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(fallbackTag)) / 2;
                int ty = (h / 2) + 4;
                g2.drawString(fallbackTag, tx, ty);
            }

            g2.dispose();
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength).trim() + "...";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    private static class ActionButtonDef {
        final String text;
        final Color bg;
        final Color fg;
        final Runnable action;

        ActionButtonDef(String text, Color bg, Color fg, Runnable action) {
            this.text = text;
            this.bg = bg;
            this.fg = fg;
            this.action = action;
        }
    }
}