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
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class DestinationDetails extends Screen {

    private final DestinationDAO destinationDAO = new DestinationDAO();
    private final TravelPackageDAO packageDAO = new TravelPackageDAO();
    private DestinationRecord dest;
    private final List<PackageRecord> destinationPackages = new ArrayList<>();

    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;

    private JScrollPane contentScrollPane;
    private JPanel mainContainer;

    private JPanel leftColumn;
    private JPanel rightColumn;

    public DestinationDetails(DestinationRecord dest) {
        super("Destination Details - " + (dest != null ? dest.name : "Travel Catalog"), 1150, 700);
        this.dest = dest;
        init();
    }

    public DestinationDetails(int destinationId) {
        super("Destination Details", 1150, 700);
        try {
            this.dest = destinationDAO.getDestinationById(destinationId);
        } catch (SQLException ex) {
            this.dest = null;
        }
        init();
    }

    private void init() {
        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        if (dest != null) {
            try {
                destinationPackages.addAll(packageDAO.getPackagesByDestination(dest.destinationID));
            } catch (SQLException ignored) {}
        }

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
        categoryTag = createLabel("DESTINATION GUIDE  •  TRAVEL DIRECTORY", 45, 14, 600, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, new Color(2, 132, 199)); // Sky 600

        String titleText = dest != null ? dest.name : "Destination Not Found";
        header = createHeader(titleText, 45, 30, 750, 32);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        String subtitleText = dest != null && dest.country != null
            ? "Located in " + dest.country + "  •  Detailed travel insights and accompanying vacation packages."
            : "Detailed travel insights and accompanying vacation packages.";
        subheader = createLabel(subtitleText, 45, 62, 750, 18);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        backBtn = createSecondaryButton(this, "← Back to Destinations", 920, 20, 175, 36, () -> {
            navigateTo(new BrowseDestinations());
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

        if (dest != null) {
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

        JLabel msg = new JLabel("Destination information could not be retrieved from the database.");
        msg.setFont(new Font("Segoe UI", Font.BOLD, 15));
        msg.setForeground(COLOR_TEXT_MAIN);
        msg.setBounds(30, 40, 640, 25);
        empty.add(msg);

        JButton retBtn = createPrimaryButton(empty, "Return to Destinations", 30, 90, 180, 36, () -> {
            navigateTo(new BrowseDestinations());
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
        HeroBanner banner = new HeroBanner(dest.imagePath, ImageUtil.DEFAULT_DESTINATION_PLACEHOLDER, "DESTINATION SPOT");
        banner.setBounds(0, 0, 460, 250);
        leftColumn.add(banner);

        // Status badge
        StatusBadge badge = new StatusBadge(dest.status);
        badge.setBounds(460 - 80, 14, 68, 24);
        banner.add(badge);

        // Location Tag & Name inside Left Column
        JLabel locTag = new JLabel("LOCATION OVERVIEW");
        locTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        locTag.setForeground(new Color(2, 132, 199));
        locTag.setBounds(24, 268, 400, 16);
        leftColumn.add(locTag);

        JLabel destName = new JLabel("<html><b>" + escapeHtml(dest.name) + "</b></html>");
        destName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        destName.setForeground(COLOR_TEXT_MAIN);
        destName.setBounds(24, 288, 410, 26);
        leftColumn.add(destName);

        // Key Specs Grid
        int startY = 330;
        addSpecRow(leftColumn, "Country / Territory", dest.country != null ? dest.country : "International", startY);
        addSpecRow(leftColumn, "Destination Status", dest.status != null ? dest.status : "Active", startY + 36);
        addSpecRow(leftColumn, "Active Tour Packages", destinationPackages.size() + " Available", startY + 72);
        addSpecRow(leftColumn, "Travel Recommendation", "All Year Round", startY + 108);

        // Action Button
        JButton viewAllToursBtn = createPrimaryButton(leftColumn, "View All Packages for This Destination →", 20, startY + 160, 420, 44, () -> {
            navigateTo(new BrowsePackages(dest.destinationID, dest.name));
        });
        viewAllToursBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllToursBtn.setBackground(new Color(79, 70, 229));

        mainContainer.add(leftColumn);
    }

    private void addSpecRow(JPanel parent, String label, String value, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBounds(24, y, 160, 20);
        parent.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(COLOR_TEXT_MAIN);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        val.setBounds(190, y, 244, 20);
        parent.add(val);

        JPanel line = new JPanel();
        line.setBackground(new Color(241, 245, 249));
        line.setBounds(24, y + 26, 412, 1);
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
        JLabel tag = new JLabel("ABOUT THIS DESTINATION");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tag.setForeground(new Color(2, 132, 199));
        tag.setBounds(28, 24, 450, 16);
        rightColumn.add(tag);

        // Destination Title
        JLabel title = new JLabel("<html><b>" + escapeHtml(dest.name) + "</b></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(COLOR_TEXT_MAIN);
        title.setBounds(28, 44, 550, 32);
        rightColumn.add(title);

        // Description title
        JLabel descTitle = new JLabel("Travel Highlights & Scenery");
        descTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descTitle.setForeground(new Color(30, 41, 59));
        descTitle.setBounds(28, 90, 400, 22);
        rightColumn.add(descTitle);

        // Full Description paragraph
        String desc = dest.description != null && !dest.description.trim().isEmpty()
            ? dest.description.trim()
            : "Explore stunning attractions, scenic nature, and rich cultural landmarks at this destination.";
        JLabel descArea = new JLabel("<html><body style='width: 480px; color: #475569; font-family: Segoe UI; font-size: 12px; line-height: 1.5;'>"
            + escapeHtml(desc) + "</body></html>");
        descArea.setBounds(28, 118, 540, 100);
        descArea.setVerticalAlignment(SwingConstants.TOP);
        rightColumn.add(descArea);

        // Available Tour Packages Section
        JLabel pkgSectionTitle = new JLabel("AVAILABLE TOUR PACKAGES (" + destinationPackages.size() + ")");
        pkgSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pkgSectionTitle.setForeground(new Color(79, 70, 229));
        pkgSectionTitle.setBounds(28, 232, 450, 22);
        rightColumn.add(pkgSectionTitle);

        int py = 260;
        if (destinationPackages.isEmpty()) {
            JPanel noPkgBox = new JPanel(null);
            noPkgBox.setBackground(new Color(248, 250, 252));
            noPkgBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
            ));
            noPkgBox.setBounds(28, py, 540, 75);

            JLabel noPkgLbl = new JLabel("No tour packages currently listed for this destination.");
            noPkgLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            noPkgLbl.setForeground(COLOR_TEXT_MAIN);
            noPkgLbl.setBounds(18, 14, 450, 18);
            noPkgBox.add(noPkgLbl);

            JLabel noPkgSub = new JLabel("Check back soon or browse other active holiday destinations.");
            noPkgSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            noPkgSub.setForeground(COLOR_TEXT_MUTED);
            noPkgSub.setBounds(18, 36, 450, 18);
            noPkgBox.add(noPkgSub);

            rightColumn.add(noPkgBox);
            py += 90;
        } else {
            for (PackageRecord p : destinationPackages) {
                JPanel pkgItem = new JPanel(null);
                pkgItem.setBackground(new Color(248, 250, 252));
                pkgItem.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
                pkgItem.setBounds(28, py, 540, 80);

                // Package Name
                JLabel pTitle = new JLabel("<html><b>" + escapeHtml(p.name) + "</b></html>");
                pTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
                pTitle.setForeground(COLOR_TEXT_MAIN);
                pTitle.setBounds(14, 10, 310, 20);
                pkgItem.add(pTitle);

                // Price and capacity
                String priceStr = p.pricePerTraveller != null
                    ? String.format("PHP %,.2f", p.pricePerTraveller)
                    : "PHP 0.00";
                JLabel pMeta = new JLabel(priceStr + " / traveler  •  " + p.minTraveller + "-" + p.maxTraveller + " pax");
                pMeta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                pMeta.setForeground(new Color(5, 150, 105));
                pMeta.setBounds(14, 34, 310, 18);
                pkgItem.add(pMeta);

                // Buttons: Details and Book
                JButton detailsBtn = createSecondaryButton(pkgItem, "Details", 335, 22, 85, 34, () -> {
                    navigateTo(new PackageDetails(p));
                });
                setFontSize(detailsBtn, 11);

                JButton bookBtn = createPrimaryButton(pkgItem, "Book", 430, 22, 95, 34, () -> {
                    if (common.Session.isCustomer()) {
                        navigateTo(new ui.customer.BookingScreen(p.name));
                    } else {
                        if (confirm("You must be logged in as a customer to book \"" + p.name + "\".\nWould you like to log in now?")) {
                            navigateTo(new CustomerLogin());
                        }
                    }
                });
                setFontSize(bookBtn, 11);
                bookBtn.setBackground(new Color(79, 70, 229));

                rightColumn.add(pkgItem);
                py += 92;
            }
        }

        mainContainer.add(rightColumn);
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int contentWidth = w - (2 * pad);

        categoryTag.setBounds(pad, 14, 600, 16);
        header.setBounds(pad, 30, Math.max(300, w - pad - 220), 32);
        subheader.setBounds(pad, 62, Math.max(300, w - pad - 220), 18);

        backBtn.setBounds(w - pad - 175, 20, 175, 36);

        int scrollY = 96;
        int scrollHeight = Math.max(300, h - scrollY - 30);
        contentScrollPane.setBounds(pad, scrollY, contentWidth, scrollHeight);

        // Layout left & right columns
        if (leftColumn != null && rightColumn != null) {
            int gap = 24;
            int leftW = 460;
            int rightW = Math.max(500, contentWidth - leftW - gap - 20);
            int columnH = Math.max(580, 280 + Math.max(1, destinationPackages.size()) * 95);

            leftColumn.setBounds(0, 10, leftW, 580);
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
                    0, 0, new Color(240, 249, 255),
                    w, h, new Color(224, 242, 254)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(2, 132, 199, 180));
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
