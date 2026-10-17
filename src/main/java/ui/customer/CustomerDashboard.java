package ui.customer;

import common.Screen;
import common.Session;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import ui.BrowseDestinations;
import ui.BrowsePackages;
import ui.Homepage;

public class CustomerDashboard extends Screen {

   
    private static final Color COLOR_HOVER = new Color(239, 246, 255);
    private static final Color COLOR_ON_PRIMARY_MUTED = new Color(191, 219, 254);

    private int customerId;

    public CustomerDashboard(int customerId) {
        super("Customer Dashboard");
        this.customerId = customerId;

        createTopHeader(
                "CUSTOMER",
                "Customer Dashboard",
                "Manage your trips, explore destinations, and book your next adventure.",
                null 
        );

        buildHero();
        buildActionTiles();
        buildHowItWorks();
        buildAccountBar();
    }

    public CustomerDashboard() {
        this(Session.getCustomerId());
    }

    public int getCustomerId() {
        return customerId;
    }



    private void buildHero() {
        JPanel hero = createCard(45, 100, 1110, 110);
        hero.setBorder(null);
        setBackground(hero, COLOR_PRIMARY);

        JLabel greeting = createLabel(hero, greetingText().toUpperCase(), 24, 16, 500, 16);
        setFontStyle(greeting, Font.BOLD);
        setFontSize(greeting, 11);
        setTextColor(greeting, COLOR_ON_PRIMARY_MUTED);

        String username = Session.getUsername();
        String title = username != null ? "Welcome back, " + username + "!" : "Welcome!";
        JLabel name = createHeader(hero, title, 24, 34, 700, 36);
        setFontSize(name, 26);
        setTextColor(name, Color.WHITE);

        JLabel subtitle = createLabel(hero,
                "Plan your journeys and track your reservations.", 24, 74, 700, 20);
        setFontSize(subtitle, 13);
        setTextColor(subtitle, COLOR_ON_PRIMARY_MUTED);

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH))
                .toUpperCase();
        JLabel date = createLabel(hero, today, 586, 16, 500, 16);
        date.setHorizontalAlignment(SwingConstants.RIGHT);
        setFontStyle(date, Font.BOLD);
        setFontSize(date, 11);
        setTextColor(date, COLOR_ON_PRIMARY_MUTED);

        createStyledButton(hero, "Book a Package  →", 886, 50, 200, 40,
                Color.WHITE, COLOR_PRIMARY, Color.WHITE, 13,
                () -> navigateTo(new BookingScreen()));
    }

    private static String greetingText() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 18) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    

    private void buildActionTiles() {
        int y = 235;
        int w = 264;
        int step = w + 18;

        createTile(45, y, w, "My Bookings",
                "View, track, and manage your reservations.",
                COLOR_PRIMARY, () -> navigateTo(new MyBookings()));

        createTile(45 + step, y, w, "Book a Package",
                "Reserve a travel package for your next trip.",
                COLOR_SUCCESS, () -> navigateTo(new BookingScreen()));

        createTile(45 + step * 2, y, w, "Browse Destinations",
                "Discover places you can travel to.",
                COLOR_PRIMARY, () -> navigateTo(new BrowseDestinations()));

        createTile(45 + step * 3, y, w, "Browse Packages",
                "See available packages and their prices.",
                COLOR_PRIMARY, () -> navigateTo(new BrowsePackages()));
    }

    private JPanel createTile(int x, int y, int w, String title, String hint,
                              Color accent, Runnable action) {
        JPanel tile = createCard(x, y, w, 150);

        JPanel bar = new JPanel();
        bar.setBounds(1, 1, w - 2, 4);
        bar.setBackground(accent);
        tile.add(bar);

        JLabel titleLbl = createLabel(tile, title, 16, 24, w - 32, 24);
        setFontStyle(titleLbl, Font.BOLD);
        setFontSize(titleLbl, 15);
        setTextColor(titleLbl, COLOR_TEXT_MAIN);

        JLabel hintLbl = createLabel(tile, "<html>" + hint + "</html>", 16, 54, w - 32, 40);
        setFontSize(hintLbl, 12);
        setTextColor(hintLbl, COLOR_TEXT_MUTED);

        JLabel openLbl = createLabel(tile, "Open  →", 16, 114, w - 32, 18);
        setFontStyle(openLbl, Font.BOLD);
        setFontSize(openLbl, 12);
        setTextColor(openLbl, accent);

        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tile.setBackground(COLOR_HOVER);
                tile.setBorder(BorderFactory.createLineBorder(accent, 1));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!tile.contains(e.getPoint())) { // ignore moves onto child labels
                    tile.setBackground(COLOR_CARD_BG);
                    tile.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (tile.contains(e.getPoint())) {
                    action.run();
                }
            }
        });
        return tile;
    }

   

    private void buildHowItWorks() {
        JPanel card = createCard(45, 405, 1110, 120);
        createCardTag(card, "HOW IT WORKS");

        String[][] steps = {
            {"Browse", "Explore destinations and compare packages."},
            {"Book",   "Pick a package, date, and number of travelers."},
            {"Pay",    "Settle your booking with your preferred method."},
            {"Travel", "Track your reservation and enjoy your trip."}
        };

        for (int i = 0; i < steps.length; i++) {
            int x = 16 + i * 272;

            JLabel num = createLabel(card, String.valueOf(i + 1), x, 44, 30, 36);
            setFontStyle(num, Font.BOLD);
            setFontSize(num, 26);
            setTextColor(num, COLOR_PRIMARY);

            JLabel stepTitle = createLabel(card, steps[i][0], x + 34, 44, 220, 20);
            setFontStyle(stepTitle, Font.BOLD);
            setFontSize(stepTitle, 14);
            setTextColor(stepTitle, COLOR_TEXT_MAIN);

            JLabel desc = createLabel(card, "<html>" + steps[i][1] + "</html>", x + 34, 66, 220, 40);
            setFontSize(desc, 12);
            setTextColor(desc, COLOR_TEXT_MUTED);
        }
    }

 

    private void buildAccountBar() {
        JPanel card = createCard(45, 545, 1110, 72);

        createSecondaryButton(card, "My Profile", 16, 16, 532, 40,
                () -> navigateTo(new CustomerProfile()));

        createDangerButton(card, "Log Out", 562, 16, 532, 40, () -> {
            if (confirm("Are you sure you want to log out?")) {
                Session.clear();
                navigateTo(new Homepage());
            }
        });
    }
}