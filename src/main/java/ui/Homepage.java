package ui;

import common.Screen;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Homepage extends Screen {

    private JLabel categoryTag;
    private JLabel title;
    private JLabel subtitle;
    private JPanel card1;
    private JPanel card2;
    private JPanel card3;
    private JLabel footerLbl;

    public Homepage() {
        super("Travel Agency Management System - Home", 1100, 650);

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
        categoryTag = createLabel("TRAVEL AGENCY MANAGEMENT SYSTEM  •  MAIN PORTAL", 50, 24, 600, 16);
        setFontStyle(categoryTag, Font.BOLD);
        setFontSize(categoryTag, 10);
        setTextColor(categoryTag, COLOR_PRIMARY);

        title = createHeader("Explore, Plan & Manage Travel Experiences", 50, 44, 800, 36);
        setFontSize(title, 24);
        setTextColor(title, COLOR_TEXT_MAIN);

        subtitle = createLabel("A comprehensive offline desktop suite for travelers, tour operators, and agency staff.", 50, 82, 800, 20);
        setFontSize(subtitle, 13);
        setTextColor(subtitle, COLOR_TEXT_MUTED);

        // Column 1: Traveler Portal Card
        card1 = createPortalCard(
            "TRAVELER PORTAL",
            "Customer Portal",
            "Access your passenger bookings, view reservation vouchers, check itinerary departure dates, and manage your account profile.",
            COLOR_PRIMARY,
            50, 120, 310, 440,
            new ActionButtonDef("Sign In to Account →", COLOR_PRIMARY, Color.WHITE, () -> navigateTo(new CustomerLogin())),
            new ActionButtonDef("Create New Account", COLOR_SUCCESS, Color.WHITE, () -> navigateTo(new CustomerRegister()))
        );

        // Column 2: Public Exploration Card
        card2 = createPortalCard(
            "EXPLORE & DISCOVER",
            "Trip Catalogs",
            "Browse curated vacation destinations and tour packages with transparent pricing, traveler capacity limits, and trip descriptions.",
            new Color(14, 116, 144),
            395, 120, 310, 440,
            new ActionButtonDef("Browse Destinations →", new Color(14, 116, 144), Color.WHITE, () -> navigateTo(new BrowseDestinations())),
            new ActionButtonDef("Browse Tour Packages →", new Color(79, 70, 229), Color.WHITE, () -> navigateTo(new BrowsePackages()))
        );

        // Column 3: Administration Card
        card3 = createPortalCard(
            "STAFF & SECURITY",
            "Administration",
            "Access the administrative control center to configure trip catalogs, process customer bookings, audit sales reports, and manage access.",
            new Color(51, 65, 85),
            740, 120, 310, 440,
            new ActionButtonDef("Administrator Login →", new Color(51, 65, 85), Color.WHITE, () -> navigateTo(new AdminLogin())),
            new ActionButtonDef("Exit Application", COLOR_SECONDARY, new Color(71, 85, 105), () -> {
                if (confirm("Are you sure you want to exit the application?")) {
                    System.exit(0);
                }
            })
        );

        // Footer Status Strip
        footerLbl = createLabel("TAMS Enterprise • Offline Desktop Solution • Connected to Apache Derby RDBMS", 50, 580, 800, 20);
        setFontSize(footerLbl, 11);
        setTextColor(footerLbl, COLOR_TEXT_MUTED);
    }

    private void relayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int contentWidth = Math.min(1160, Math.max(880, w - 80));
        int startX = (w - contentWidth) / 2;
        int startY = Math.max(25, (h - 600) / 2);

        categoryTag.setBounds(startX, startY, contentWidth, 16);
        title.setBounds(startX, startY + 20, contentWidth, 36);
        subtitle.setBounds(startX, startY + 58, contentWidth, 20);

        int cardGap = 20;
        int cardWidth = (contentWidth - (2 * cardGap)) / 3;
        int cardHeight = 440;

        card1.setBounds(startX, startY + 95, cardWidth, cardHeight);
        card2.setBounds(startX + cardWidth + cardGap, startY + 95, cardWidth, cardHeight);
        card3.setBounds(startX + 2 * (cardWidth + cardGap), startY + 95, cardWidth, cardHeight);

        relayoutCard(card1, cardWidth);
        relayoutCard(card2, cardWidth);
        relayoutCard(card3, cardWidth);

        footerLbl.setBounds(startX, startY + 95 + cardHeight + 20, contentWidth, 20);
    }

    private void relayoutCard(JPanel card, int cardWidth) {
        if (card == null) return;
        for (Component c : card.getComponents()) {
            Rectangle b = c.getBounds();
            c.setBounds(b.x, b.y, cardWidth - 40, b.height);
        }
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

    private JPanel createPortalCard(String tagText, String titleText, String descText, Color accentColor,
                                    int x, int y, int w, int h, ActionButtonDef btn1, ActionButtonDef btn2) {
        JPanel card = createCard(x, y, w, h);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
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
        tag.setBounds(20, 20, w - 40, 22);
        card.add(tag);

        // Title
        JLabel cTitle = createLabel(card, titleText, 20, 52, w - 40, 26);
        setFontStyle(cTitle, Font.BOLD);
        setFontSize(cTitle, 18);
        setTextColor(cTitle, COLOR_TEXT_MAIN);

        // Description
        JLabel desc = createLabel(card, "<html><body style='width: " + (w - 50) + "px; line-height: 1.4;'>" + descText + "</body></html>", 20, 86, w - 40, 120);
        setFontSize(desc, 12);
        setTextColor(desc, COLOR_TEXT_MUTED);

        // Button 1
        if (btn1 != null) {
            JButton b1 = createStyledButton(card, btn1.text, 20, 290, w - 40, 42, btn1.bg, btn1.fg, btn1.action);
            setFontStyle(b1, Font.BOLD);
            setFontSize(b1, 13);
            attachButtonHover(b1, btn1.bg);
        }

        // Button 2
        if (btn2 != null) {
            JButton b2 = createStyledButton(card, btn2.text, 20, 344, w - 40, 42, btn2.bg, btn2.fg, btn2.action);
            setFontStyle(b2, Font.BOLD);
            setFontSize(b2, 13);
            if (btn2.bg.equals(COLOR_SECONDARY)) {
                b2.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));
            }
            attachButtonHover(b2, btn2.bg);
        }
        return card;
    }

    private void attachButtonHover(JButton btn, Color baseBg) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (baseBg.equals(COLOR_PRIMARY)) {
                    btn.setBackground(COLOR_PRIMARY_HOVER);
                } else if (baseBg.equals(COLOR_SUCCESS)) {
                    btn.setBackground(COLOR_SUCCESS_HOVER);
                } else if (baseBg.equals(COLOR_SECONDARY)) {
                    btn.setBackground(COLOR_SECONDARY_HOVER);
                } else {
                    btn.setBackground(baseBg.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseBg);
            }
        });
    }
}