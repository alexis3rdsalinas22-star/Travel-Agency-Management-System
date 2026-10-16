package ui.customer;

import common.Screen;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MyBookings extends Screen {

    private DefaultTableModel tableModel;
    private JTable bookingsTable;

    public MyBookings() {
        super("My Bookings", 960, 560);

        initUI();
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("My Bookings", 40, 20, 400, 30);
        createLabel("Review your travel reservations and booking status.", 40, 50, 450, 20);

        // Read-only Table with columns (empty, no placeholders per instruction)
        String[] columns = {"Booking ID", "Package Name", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingsTable = createTable(tableModel, 40, 95, 880, 360);

        // Action Buttons
        JButton cancelBtn = createButton("Cancel Booking", 40, 475, 180, 35);
        // Note: No DAO function attached per instruction

        JButton backBtn = createButton("Back to Dashboard", 240, 475, 180, 35);
        backBtn.addActionListener(e -> navigateTo(new CustomerDashboard()));
    }
}