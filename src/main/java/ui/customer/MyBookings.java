package ui.customer;

import common.Screen;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MyBookings extends Screen {

    private DefaultTableModel tableModel;
    private JTable bookingsTable;

    public MyBookings() {
        super("My Bookings", 1000, 600);

        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(new Color(245, 247, 250));

        // Header & Subtitle
        JLabel header = createHeader("My Bookings", 40, 20, 400, 32);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(28, 55, 90));

        JLabel subHeader = createLabel("Review your travel reservations and booking status.", 40, 54, 450, 20);
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeader.setForeground(new Color(100, 116, 139));

        // Read-only Table with columns
        String[] columns = {"Booking ID", "Package Name", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingsTable = createTable(tableModel, 40, 90, 920, 390);
        bookingsTable.setRowHeight(24);
        bookingsTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        bookingsTable.getColumnModel().getColumn(1).setPreferredWidth(310);
        bookingsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        bookingsTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        bookingsTable.getColumnModel().getColumn(4).setPreferredWidth(200);

        // Action Buttons
        JButton cancelBtn = createButton("Cancel Booking", 40, 505, 180, 36);
        cancelBtn.setBackground(new Color(220, 38, 38));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setFocusPainted(false);
        // Note: No DAO function attached per instruction

        JButton backBtn = createButton("Back to Dashboard", 235, 505, 180, 36);
        backBtn.setBackground(new Color(220, 223, 231));
        backBtn.setForeground(new Color(51, 65, 85));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new CustomerDashboard()));
    }
}