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
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class PackageDetails extends Screen {

    private final TravelPackageDAO packageDAO = new TravelPackageDAO();
    private final DestinationDAO destinationDAO = new DestinationDAO();
    private PackageRecord pkg;

    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;

    private JScrollPane contentScrollPane;
    private JPanel mainContainer;

    private JPanel leftColumn;
    private JPanel rightColumn;

    public PackageDetails(PackageRecord pkg) {
        super("Tour Package Details - " + (pkg != null ? pkg.name : "Travel Catalog"), 1150, 700);
        this.pkg = pkg;
        init();
    }

    public PackageDetails(int packageId) {
        super("Tour Package Details", 1150, 700);
        try {
            this.pkg = packageDAO.getPackageById(packageId);
        } catch (SQLException ex) {
            this.pkg = null;
        }
        init();
    }

    private void init() {
        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

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
        categoryTag = createLabel("TOUR PACKAGE DETAILS  •  CURATED CATALOG", 45, 14, 600, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, new Color(79, 70, 229));

        String titleText = pkg != null ? pkg.name : "Package Not Found";
        header = createHeader(titleText, 45, 30, 750, 32);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        String subtitleText = pkg != null && pkg.destinationName != null
            ? "Destination: " + pkg.destinationName + (pkg.country != null ? ", " + pkg.country : "")
            : "Detailed travel itinerary and booking specifications.";
        subheader = createLabel(subtitleText, 45, 62, 750, 18);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        backBtn = createSecondaryButton(this, "← Back to Packages", 930, 20, 160, 36, () -> {
            navigateTo(new BrowsePackages());
        });
        setFontStyle(backBtn, Font.BOLD);
        setFontSize(backBtn, 12);

        // Scrollable content area
        mainContainer = new JPanel(null);
        mainContainer.setBackground(COLOR_BG);

        contentScrollPane = new JScrollPane(mainContainer);
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentScrollPane.setBackground(COLOR_BG);
        contentScrollPane.getViewport().setBackground(COLOR_BG);
        contentScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(contentScrollPane);

        if (pkg != null) {
            buildLeftColumn();
            buildRightColumn();
        } else {
            buildNotFoundView();
        }
    }

    private void buildNotFoundView() {
        JPanel empty = new JPanel(null);
        empty.setBackground(Color.WHITE);
        empty.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        empty.setBounds(45, 20, 700, 200);

        JLabel msg = new JLabel("Package information could not be retrieved from the database.");
        msg.setFont(new Font("Segoe UI", Font.BOLD, 15));
        msg.setForeground(COLOR_TEXT_MAIN);
        msg.setBounds(30, 40, 640, 25);
        empty.add(msg);

        JButton retBtn = createPrimaryButton(empty, "Return to Catalog", 30, 90, 160, 36, () -> {
            navigateTo(new BrowsePackages());
        });
        setFontSize(retBtn, 12);

        mainContainer.add(empty);
    }

    private void buildLeftColumn() {
        leftColumn = new JPanel(null);
        leftColumn.setBackground(Color.WHITE);
        leftColumn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Hero Image Banner (height 260px)
        HeroBanner banner = new HeroBanner(pkg.imagePath, ImageUtil.DEFAULT_PACKAGE_PLACEHOLDER, "TOUR PACKAGE");
        banner.setBounds(0, 0, 460, 250);
        leftColumn.add(banner);

        // Status badge
        StatusBadge badge = new StatusBadge(pkg.status);
        badge.setBounds(460 - 80, 14, 68, 24);
        banner.add(badge);

        // Price Callout Section
        JPanel priceBox = new JPanel(null);
        priceBox.setBackground(new Color(248, 250, 252));
        priceBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(241, 245, 249), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        priceBox.setBounds(20, 270, 420, 75);

        JLabel priceTag = new JLabel("STANDARD TRAVEL RATE");
        priceTag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        priceTag.setForeground(new Color(100, 116, 139));
        priceTag.setBounds(16, 10, 200, 14);
        priceBox.add(priceTag);

        String formattedPrice = pkg.pricePerTraveller != null
            ? String.format("PHP %,.2f", pkg.pricePerTraveller)
            : "PHP 0.00";
        JLabel priceVal = new JLabel(formattedPrice);
        priceVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        priceVal.setForeground(new Color(5, 150, 105)); // Emerald 600
        priceVal.setBounds(16, 28, 240, 32);
        priceBox.add(priceVal);

        JLabel perPerson = new JLabel("/ traveler");
        perPerson.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        perPerson.setForeground(new Color(148, 163, 184));
        perPerson.setBounds(260, 38, 140, 20);
        priceBox.add(perPerson);

        leftColumn.add(priceBox);

        // Key Specs Grid
        int startY = 360;
        addSpecRow(leftColumn, "Destination", pkg.destinationName != null ? pkg.destinationName : "General", startY);
        addSpecRow(leftColumn, "Country", pkg.country != null ? pkg.country : "International", startY + 36);
        addSpecRow(leftColumn, "Capacity", pkg.minTraveller + " - " + pkg.maxTraveller + " Travelers", startY + 72);
        addSpecRow(leftColumn, "Listing Status", pkg.status != null ? pkg.status : "Active", startY + 108);

        // Booking Action Button
        JButton bookNowBtn = createPrimaryButton(leftColumn, "Book This Package Now →", 20, startY + 155, 420, 44, () -> {
            if (common.Session.isCustomer()) {
                navigateTo(new ui.customer.BookingScreen(pkg.name));
            } else {
                if (confirm("You must be logged in as a customer to book \"" + pkg.name + "\".\nWould you like to log in now?")) {
                    navigateTo(new CustomerLogin());
                }
            }
        });
        bookNowBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookNowBtn.setBackground(new Color(79, 70, 229));

        mainContainer.add(leftColumn);
    }

    private void addSpecRow(JPanel parent, String label, String value, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBounds(20, y, 140, 20);
        parent.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(COLOR_TEXT_MAIN);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        val.setBounds(180, y, 260, 20);
        parent.add(val);

        JPanel line = new JPanel();
        line.setBackground(new Color(241, 245, 249));
        line.setBounds(20, y + 26, 420, 1);
        parent.add(line);
    }

    private void buildRightColumn() {
        rightColumn = new JPanel(null);
        rightColumn.setBackground(Color.WHITE);
        rightColumn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        // Category Tag
        JLabel tag = new JLabel("COMPREHENSIVE PACKAGE OVERVIEW");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tag.setForeground(new Color(79, 70, 229));
        tag.setBounds(28, 24, 450, 16);
        rightColumn.add(tag);

        // Big Package Title
        JLabel title = new JLabel("<html><b>" + escapeHtml(pkg.name) + "</b></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(COLOR_TEXT_MAIN);
        title.setBounds(28, 44, 550, 32);
        rightColumn.add(title);

        // Section Title: Description
        JLabel descTitle = new JLabel("Itinerary & Experience Overview");
        descTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descTitle.setForeground(new Color(30, 41, 59));
        descTitle.setBounds(28, 92, 400, 22);
        rightColumn.add(descTitle);

        // Full Description paragraph
        String desc = pkg.description != null && !pkg.description.trim().isEmpty()
            ? pkg.description.trim()
            : "No detailed itinerary description provided yet for this tour package.";
        JLabel descArea = new JLabel("<html><body style='width: 480px; color: #475569; font-family: Segoe UI; font-size: 12px; line-height: 1.5;'>"
            + escapeHtml(desc) + "</body></html>");
        descArea.setBounds(28, 120, 540, 130);
        descArea.setVerticalAlignment(SwingConstants.TOP);
        rightColumn.add(descArea);

        // What's Included Card
        JPanel incBox = new JPanel(null);
        incBox.setBackground(new Color(248, 250, 252));
        incBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        incBox.setBounds(28, 265, 540, 160);

        JLabel incTitle = new JLabel("PACKAGE HIGHLIGHTS & INCLUSIONS");
        incTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        incTitle.setForeground(new Color(79, 70, 229));
        incTitle.setBounds(18, 14, 400, 16);
        incBox.add(incTitle);

        String[] perks = {
            "✓  Curated guided itinerary with accredited tour guides",
            "✓  Comprehensive local admissions, environmental permits, and activity passes",
            "✓  Pre-arranged ground transfers between listed tour spots",
            "✓  Flexible customer support & booking rescheduling protection"
        };
        int py = 38;
        for (String perk : perks) {
            JLabel perkLbl = new JLabel(perk);
            perkLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            perkLbl.setForeground(new Color(51, 65, 85));
            perkLbl.setBounds(18, py, 500, 22);
            incBox.add(perkLbl);
            py += 26;
        }
        rightColumn.add(incBox);

        // Destination Guide Link Box
        JPanel destBox = new JPanel(null);
        destBox.setBackground(new Color(240, 249, 255));
        destBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(186, 230, 253), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        destBox.setBounds(28, 440, 540, 75);

        JLabel destHeader = new JLabel("Explore the Destination Location");
        destHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        destHeader.setForeground(new Color(3, 105, 161));
        destHeader.setBounds(16, 14, 300, 18);
        destBox.add(destHeader);

        JLabel destSub = new JLabel("Read travel guides, climate notes, and explore more packages.");
        destSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        destSub.setForeground(new Color(14, 116, 144));
        destSub.setBounds(16, 36, 320, 18);
        destBox.add(destSub);

        JButton viewDestBtn = createPrimaryButton(destBox, "View Destination →", 350, 18, 170, 36, () -> {
            try {
                DestinationRecord d = destinationDAO.getDestinationById(pkg.destinationID);
                if (d != null) {
                    navigateTo(new DestinationDetails(d));
                } else {
                    navigateTo(new BrowseDestinations());
                }
            } catch (SQLException ex) {
                navigateTo(new BrowseDestinations());
            }
        });
        viewDestBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewDestBtn.setBackground(new Color(2, 132, 199));

        rightColumn.add(destBox);

        mainContainer.add(rightColumn);
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int contentWidth = w - (2 * pad);

        categoryTag.setBounds(pad, 14, 600, 16);
        header.setBounds(pad, 30, Math.max(300, w - pad - 200), 32);
        subheader.setBounds(pad, 62, Math.max(300, w - pad - 200), 18);

        backBtn.setBounds(w - pad - 160, 20, 160, 36);

        int scrollY = 96;
        int scrollHeight = Math.max(300, h - scrollY - 30);
        contentScrollPane.setBounds(pad, scrollY, contentWidth, scrollHeight);

        // Layout left & right columns
        if (leftColumn != null && rightColumn != null) {
            int gap = 24;
            int leftW = 460;
            int rightW = Math.max(500, contentWidth - leftW - gap - 20);
            int columnH = 580;

            leftColumn.setBounds(0, 10, leftW, columnH);
            rightColumn.setBounds(leftW + gap, 10, rightW, columnH);

            mainContainer.setPreferredSize(new Dimension(contentWidth - 10, columnH + 30));
            mainContainer.revalidate();
            mainContainer.repaint();
        }
    }

    // ==========================================
    // Hero Banner Panel with Smooth Scale
    // ==========================================
    private static class HeroBanner extends JPanel {
        private final String imagePath;
        private final String placeholderPath;
        private final String fallbackTitle;
        private Image cachedImage = null;
        private int cachedW = -1;
        private int cachedH = -1;

        public HeroBanner(String imagePath, String placeholderPath, String fallbackTitle) {
            this.imagePath = imagePath;
            this.placeholderPath = placeholderPath;
            this.fallbackTitle = fallbackTitle;
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

                g2.setColor(new Color(79, 70, 229, 180));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(fallbackTitle)) / 2;
                int ty = (h / 2) + 5;
                g2.drawString(fallbackTitle, tx, ty);
            }

            g2.dispose();
        }
    }

    // ==========================================
    // Status Badge Pill Component
    // ==========================================
    private static class StatusBadge extends JLabel {
        private final boolean isActive;

        public StatusBadge(String status) {
            super(status != null ? status.toUpperCase() : "ACTIVE");
            this.isActive = "Active".equalsIgnoreCase(status);
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setHorizontalAlignment(CENTER);
            setOpaque(false);
            setForeground(isActive ? new Color(5, 150, 105) : new Color(225, 29, 72));
            setCursor(Cursor.getDefaultCursor());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isActive) {
                g2.setColor(new Color(236, 253, 245, 245));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setColor(new Color(167, 243, 208));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            } else {
                g2.setColor(new Color(255, 241, 242, 245));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setColor(new Color(254, 205, 211));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
