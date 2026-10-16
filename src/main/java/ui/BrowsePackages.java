package ui;

import common.Screen;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BrowsePackages extends Screen {

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable packageTable;

    public BrowsePackages() {
        super("Browse Travel Packages", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
    }

    private void initUI() {
        // Header and subtitle
        JLabel header = createHeader("Browse Travel Packages", 40, 20, 400, 30);
        setFontSize(header, 24);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("Discover curated vacation packages and seasonal tour offerings.", 40, 52, 500, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Search bar using Screen helper (no DAO function attached per instruction)
        searchField = createSearchBar(40, 80, 920, 32, () -> {}, () -> {});

        // Read-only Table with columns
        String[] columns = {"Package ID", "Package Name", "Destination", "Price / Traveler", "Min Travelers", "Max Travelers", "Status"};
        tableModel = createReadOnlyTableModel(columns);
        packageTable = createTable(tableModel, 40, 122, 920, 370);
        packageTable.setRowHeight(24);

        // Configure column widths for better visual balance
        if (packageTable.getColumnModel().getColumnCount() >= 7) {
            packageTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            packageTable.getColumnModel().getColumn(0).setMaxWidth(100);
            packageTable.getColumnModel().getColumn(1).setPreferredWidth(220);
            packageTable.getColumnModel().getColumn(2).setPreferredWidth(150);
            packageTable.getColumnModel().getColumn(3).setPreferredWidth(140);
            packageTable.getColumnModel().getColumn(4).setPreferredWidth(110);
            packageTable.getColumnModel().getColumn(5).setPreferredWidth(110);
            packageTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        }

        // Action Buttons
        JButton bookNowBtn = createButton("Book a Package", 40, 505, 180, 36);
        setFontStyle(bookNowBtn, Font.BOLD);
        setFontSize(bookNowBtn, 13);
        setBackground(bookNowBtn, new Color(79, 70, 229));
        setTextColor(bookNowBtn, Color.WHITE);
        bookNowBtn.setFocusPainted(false);
        bookNowBtn.addActionListener(e -> {
            if (common.Session.isCustomer()) {
                navigateTo(new ui.customer.BookingScreen());
            } else {
                if (confirm("You must be logged in as a customer to book a package. Would you like to log in now?")) {
                    navigateTo(new CustomerLogin());
                }
            }
        });

        JButton backBtn = createButton(common.Session.isCustomer() ? "Back to Dashboard" : "Back to Home", 235, 505, 180, 36);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(229, 231, 235));
        setTextColor(backBtn, new Color(51, 65, 81));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            if (common.Session.isCustomer()) {
                navigateTo(new ui.customer.CustomerDashboard());
            } else {
                navigateTo(new Homepage());
            }
        });
    }
}
