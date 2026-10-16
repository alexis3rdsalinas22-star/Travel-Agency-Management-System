package ui.admin;

import common.Screen;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SalesManagement extends Screen {

    // Form / Detail fields
    private JTextField paymentIdField;
    private JTextField bookingIdField;
    private JTextField amountField;
    private JTextField methodField;
    private JTextField dateField;
    private JTextField sourceField;

    // Search and Table
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable salesTable;

    public SalesManagement() {
        super("Sales & Revenue Management", 1000, 560);

        initUI();
    }

    private void initUI() {
        // Header & Subtitle
        createHeader("Sales Management", 40, 20, 320, 30);
        createLabel("View payment records and revenue reports.", 40, 55, 320, 20);

        // Detail / Filter fields
        createLabel("Payment ID:", 40, 95, 100, 25);
        paymentIdField = createTextField(150, 95, 200, 30);
        paymentIdField.setEditable(false);

        createLabel("Booking ID:", 40, 135, 100, 25);
        bookingIdField = createTextField(150, 135, 200, 30);
        bookingIdField.setEditable(false);

        createLabel("Amount (PHP):", 40, 175, 100, 25);
        amountField = createTextField(150, 175, 200, 30);
        amountField.setEditable(false);

        createLabel("Method:", 40, 215, 100, 25);
        methodField = createTextField(150, 215, 200, 30);
        methodField.setEditable(false);

        createLabel("Source:", 40, 255, 100, 25);
        sourceField = createTextField(150, 255, 200, 30);
        sourceField.setEditable(false);

        createLabel("Date:", 40, 295, 100, 25);
        dateField = createTextField(150, 295, 200, 30);
        dateField.setEditable(false);

        // Action Buttons
        JButton clearBtn = createButton("Clear Selection", 40, 355, 310, 35);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 405, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section using Screen helper (no DAO function attached)
        searchField = createSearchBar(380, 20, 580, 30, () -> {}, () -> {});

        // Read-only Table with columns
        String[] columns = {"Payment ID", "Booking ID", "Date", "Amount", "Method", "Source"};
        tableModel = createReadOnlyTableModel(columns);
        salesTable = createTable(tableModel, 380, 60, 580, 440);
    }

    private void clearForm() {
        paymentIdField.setText("");
        bookingIdField.setText("");
        amountField.setText("");
        methodField.setText("");
        sourceField.setText("");
        dateField.setText("");
        salesTable.clearSelection();
    }
}