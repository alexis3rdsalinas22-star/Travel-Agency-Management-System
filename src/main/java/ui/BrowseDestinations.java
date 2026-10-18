package ui;

import common.ImageUtil;
import common.Screen;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;

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

public class BrowseDestinations extends Screen {

    private final DestinationDAO destinationDAO = new DestinationDAO();
    private final List<DestinationRecord> currentDestinations = new ArrayList<>();

    private JLabel categoryTag;
    private JLabel header;
    private JLabel subheader;
    private JButton backBtn;
    private JLabel countLabel;

    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchBtn;
    private JButton showAllBtn;

    private JPanel cardsContainer;
    private JScrollPane cardsScrollPane;
    private final List<DestinationCard> cardPanels = new ArrayList<>();

    public BrowseDestinations() {
        this(null);
    }

    public BrowseDestinations(String initialSearch) {
        super("Browse Destinations - Travel Catalog", 1100, 650);

        setFullscreen(true);
        getContentPane().setBackground(COLOR_BG);

        initUI();

        if (initialSearch != null && !initialSearch.trim().isEmpty()) {
            searchField.setText(initialSearch.trim());
            loadDestinations(initialSearch.trim());
        } else {
            loadDestinations(null);
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        relayout();
    }

    private void initUI() {
        categoryTag = createLabel("PUBLIC TRAVEL DIRECTORY  •  DESTINATIONS", 45, 14, 550, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        header = createHeader("Browse Destinations", 45, 30, 550, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        subheader = createLabel("Explore available travel locations, scenic wonders, and tourist spots.", 45, 60, 650, 18);
        setFontSize(subheader, 12);
        setTextColor(subheader, COLOR_TEXT_MUTED);

        backBtn = createSecondaryButton(this, common.Session.isCustomer() ? "← Dashboard" : "← Home", 920, 20, 135, 36, () -> {
            if (common.Session.isCustomer()) {
                navigateTo(new ui.customer.CustomerDashboard());
            } else {
                navigateTo(new Homepage());
            }
        });
        setFontStyle(backBtn, Font.BOLD);
        setFontSize(backBtn, 12);

        countLabel = createLabel("", 750, 60, 305, 18);
        setFontStyle(countLabel, Font.BOLD);
        setFontSize(countLabel, 11);
        setTextColor(countLabel, COLOR_PRIMARY);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Search Bar components
        searchLabel = createLabel("Search:", 45, 88, 55, 34);
        setFontStyle(searchLabel, Font.BOLD);
        setFontSize(searchLabel, 11);
        setTextColor(searchLabel, new Color(71, 85, 105));

        searchField = createTextField(105, 88, 600, 34);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBackground(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));
        searchField.addActionListener(e -> handleSearch());

        searchBtn = createPrimaryButton(this, "Search", 720, 88, 85, 34, this::handleSearch);
        setFontSize(searchBtn, 12);

        showAllBtn = createSecondaryButton(this, "Show All", 815, 88, 95, 34, () -> {
            searchField.setText("");
            loadDestinations(null);
        });
        setFontSize(showAllBtn, 12);

        // Scrollable Cards Container
        cardsContainer = new JPanel(null);
        cardsContainer.setBackground(COLOR_BG);

        cardsScrollPane = new JScrollPane(cardsContainer);
        cardsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        cardsScrollPane.setBackground(COLOR_BG);
        cardsScrollPane.getViewport().setBackground(COLOR_BG);
        cardsScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        cardsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(cardsScrollPane);
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int pad = 45;
        int contentWidth = w - (2 * pad);

        categoryTag.setBounds(pad, 14, 550, 16);
        header.setBounds(pad, 30, 550, 30);
        subheader.setBounds(pad, 60, 650, 18);

        backBtn.setBounds(w - pad - 135, 18, 135, 36);
        countLabel.setBounds(w - pad - 350, 58, 340, 18);

        int showAllWidth = 95;
        int searchBtnWidth = 85;
        int gap = 8;
        int buttonsTotal = searchBtnWidth + gap + showAllWidth;
        int labelWidth = 55;

        searchLabel.setBounds(pad, 88, labelWidth, 34);
        searchField.setBounds(pad + labelWidth + 5, 88, contentWidth - labelWidth - 5 - buttonsTotal - gap, 34);
        searchBtn.setBounds(pad + contentWidth - buttonsTotal, 88, searchBtnWidth, 34);
        showAllBtn.setBounds(pad + contentWidth - showAllWidth, 88, showAllWidth, 34);

        if (cardsScrollPane != null) {
            int scrollHeight = Math.max(250, h - 134 - 45);
            cardsScrollPane.setBounds(pad, 134, contentWidth, scrollHeight);
            layoutCards(contentWidth);
        }
    }

    private void layoutCards(int contentWidth) {
        if (cardsContainer == null) return;

        if (cardPanels.isEmpty()) {
            cardsContainer.setPreferredSize(new Dimension(contentWidth, 300));
            cardsContainer.revalidate();
            cardsContainer.repaint();
            return;
        }

        // Reserve 24px right margin so the vertical scrollbar never overlaps cards
        int scrollbarMargin = 24;
        int availableWidth = contentWidth - scrollbarMargin;

        int gap = 20;
        int minCardWidth = 320;
        int cols = Math.max(1, (availableWidth + gap) / (minCardWidth + gap));
        if (cols > 4) cols = 4; // Max 4 columns for balanced proportion

        int cardWidth = (availableWidth - ((cols - 1) * gap)) / cols;
        int cardHeight = 365;

        for (int i = 0; i < cardPanels.size(); i++) {
            DestinationCard card = cardPanels.get(i);
            int col = i % cols;
            int row = i / cols;
            int x = col * (cardWidth + gap);
            int y = row * (cardHeight + gap) + 6;

            card.setBounds(x, y, cardWidth, cardHeight);
            card.updateDimensions(cardWidth, cardHeight);
        }

        int totalRows = (cardPanels.size() + cols - 1) / cols;
        int totalHeight = totalRows * (cardHeight + gap) + 30;
        cardsContainer.setPreferredSize(new Dimension(availableWidth, totalHeight));
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void loadDestinations(String keyword) {
        cardsContainer.removeAll();
        cardPanels.clear();
        currentDestinations.clear();

        try {
            List<DestinationRecord> list;
            if (keyword == null || keyword.trim().isEmpty()) {
                list = destinationDAO.getAllDestinations();
            } else {
                list = destinationDAO.searchByName(keyword);
            }
            currentDestinations.addAll(list);

            if (countLabel != null) {
                countLabel.setText("Showing: " + list.size() + " destination(s)");
            }

            if (list.isEmpty()) {
                JPanel emptyPanel = createEmptyState(keyword);
                cardsContainer.add(emptyPanel);
            } else {
                for (DestinationRecord d : list) {
                    DestinationCard card = new DestinationCard(d);
                    cardPanels.add(card);
                    cardsContainer.add(card);
                }
            }

            int w = getWidth();
            int pad = 45;
            int contentWidth = Math.max(800, w - (2 * pad));
            layoutCards(contentWidth);

        } catch (SQLException ex) {
            showError("Database error loading destinations: " + ex.getMessage());
        }
    }

    private JPanel createEmptyState(String keyword) {
        JPanel emptyPanel = new JPanel(null);
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        emptyPanel.setBounds(10, 20, 700, 220);

        JLabel msgLabel = new JLabel("No Destinations Found");
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        msgLabel.setForeground(COLOR_TEXT_MAIN);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        msgLabel.setBounds(20, 50, 660, 26);
        emptyPanel.add(msgLabel);

        String sub = keyword != null && !keyword.isEmpty()
            ? "No destinations matched \"" + keyword + "\". Try a different search term or view all locations."
            : "There are currently no destinations available in the catalog.";
        JLabel subLabel = new JLabel("<html><center>" + sub + "</center></html>");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(COLOR_TEXT_MUTED);
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subLabel.setBounds(50, 85, 600, 36);
        emptyPanel.add(subLabel);

        JButton resetBtn = createSecondaryButton(emptyPanel, "View All Destinations", 260, 140, 180, 34, () -> {
            searchField.setText("");
            loadDestinations(null);
        });
        setFontSize(resetBtn, 12);

        return emptyPanel;
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadDestinations(null);
            return;
        }
        loadDestinations(keyword);
    }

    // ==========================================
    // Destination Card Component
    // ==========================================
    private class DestinationCard extends JPanel {
        private final DestinationRecord dest;
        private final BannerPanel banner;
        private final JLabel countryTag;
        private final JLabel titleLabel;
        private final JLabel descLabel;
        private final JPanel divider;
        private final JButton detailsBtn;
        private final JButton exploreBtn;

        public DestinationCard(DestinationRecord dest) {
            this.dest = dest;
            setLayout(null);
            setBackground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));

            // Clicking card navigates to DestinationDetails
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navigateTo(new DestinationDetails(dest));
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(165, 180, 252), 1, true),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                }
            });

            // 1. Photo Banner
            banner = new BannerPanel(dest.imagePath, ImageUtil.DEFAULT_DESTINATION_PLACEHOLDER, "DESTINATION SPOT");
            banner.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navigateTo(new DestinationDetails(dest));
                }
            });
            add(banner);

            // Status Badge on banner
            StatusBadge badge = new StatusBadge(dest.status);
            banner.add(badge);

            // 2. Country Tag (Clean text without missing font tofu box)
            String countryText = "LOCATION  •  " + (dest.country != null ? dest.country.toUpperCase() : "INTERNATIONAL");
            countryTag = new JLabel(countryText);
            countryTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
            countryTag.setForeground(new Color(2, 132, 199)); // Sky 600
            add(countryTag);

            // 3. Destination Name
            titleLabel = new JLabel("<html><b>" + escapeHtml(dest.name) + "</b></html>");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLabel.setForeground(COLOR_TEXT_MAIN);
            add(titleLabel);

            // 4. Description snippet
            String desc = dest.description != null && !dest.description.trim().isEmpty()
                ? dest.description.trim()
                : "Scenic tourist destination offering unforgettable travel experiences and natural wonders.";
            descLabel = new JLabel("<html><body style='color: #64748B; font-family: Segoe UI; font-size: 11px;'>"
                + escapeHtml(truncate(desc, 115)) + "</body></html>");
            add(descLabel);

            // 5. Divider
            divider = new JPanel();
            divider.setBackground(new Color(241, 245, 249));
            add(divider);

            // 6. Dual Action Buttons: "Details" and "Packages →"
            detailsBtn = createSecondaryButton(this, "Details", 0, 0, 10, 10, () -> {
                navigateTo(new DestinationDetails(dest));
            });
            detailsBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            add(detailsBtn);

            exploreBtn = createPrimaryButton(this, "Packages →", 0, 0, 10, 10, () -> {
                navigateTo(new BrowsePackages(dest.name));
            });
            exploreBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            exploreBtn.setBackground(new Color(79, 70, 229));
            add(exploreBtn);
        }

        public void updateDimensions(int cardW, int cardH) {
            int pad = 14;
            int innerW = cardW - (2 * pad);
            int bannerH = 165;

            banner.setBounds(0, 0, cardW, bannerH);
            banner.layoutBadge(cardW);

            countryTag.setBounds(pad, bannerH + 10, innerW, 16);
            titleLabel.setBounds(pad, bannerH + 28, innerW, 22);
            descLabel.setBounds(pad, bannerH + 52, innerW, 46);

            divider.setBounds(pad, bannerH + 104, innerW, 1);

            int gapBtn = 8;
            int detailsW = (int) (innerW * 0.45);
            int exploreW = innerW - detailsW - gapBtn;

            detailsBtn.setBounds(pad, bannerH + 115, detailsW, 35);
            exploreBtn.setBounds(pad + detailsW + gapBtn, bannerH + 115, exploreW, 35);
        }
    }

    // ==========================================
    // Visual Banner Panel with Graceful Fallback
    // ==========================================
    private static class BannerPanel extends JPanel {
        private final String imagePath;
        private final String placeholderPath;
        private final String fallbackTitle;
        private Image cachedImage = null;
        private int cachedW = -1;
        private int cachedH = -1;
        private StatusBadge statusBadge;

        public BannerPanel(String imagePath, String placeholderPath, String fallbackTitle) {
            this.imagePath = imagePath;
            this.placeholderPath = placeholderPath;
            this.fallbackTitle = fallbackTitle;
            setLayout(null);
            setOpaque(false);
        }

        public void add(StatusBadge badge) {
            this.statusBadge = badge;
            super.add(badge);
        }

        public void layoutBadge(int bannerW) {
            if (statusBadge != null) {
                int bWidth = 68;
                int bHeight = 22;
                statusBadge.setBounds(bannerW - bWidth - 12, 12, bWidth, bHeight);
            }
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
                // Soft bottom gradient overlay
                GradientPaint overlay = new GradientPaint(
                    0, h - 35, new Color(0, 0, 0, 0),
                    0, h, new Color(15, 23, 42, 35)
                );
                g2.setPaint(overlay);
                g2.fillRect(0, h - 35, w, 35);
            } else {
                // Stylish sky/indigo gradient placeholder
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(240, 249, 255),
                    w, h, new Color(224, 242, 254)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(2, 132, 199, 180));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
                g2.setColor(new Color(236, 253, 245, 240));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setColor(new Color(167, 243, 208));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            } else {
                g2.setColor(new Color(255, 241, 242, 240));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.setColor(new Color(254, 205, 211));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            }

            g2.dispose();
            super.paintComponent(g);
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
}
