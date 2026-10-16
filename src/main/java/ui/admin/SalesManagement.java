package ui.admin;

import common.Screen;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
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
        super("Sales & Revenue Management", 1000, 600);

        getContentPane().setBackground(new Color(245, 247, 250));

        initUI();
    }

    private void initUI() {
        // Header & Subtitle
        JLabel header = createHeader("Sales & Revenue Management", 40, 20, 320, 30);
        setFontSize(header, 22);
        setTextColor(header, new Color(28, 55, 90));

        JLabel subheader = createLabel("View payment records and revenue reports.", 40, 52, 320, 20);
        setFontSize(subheader, 13);
        setTextColor(subheader, new Color(100, 116, 139));

        // Detail / Filter fields
        JLabel payLbl = createLabel("Payment ID:", 40, 95, 100, 25);
        setFontStyle(payLbl, Font.BOLD);
        setFontSize(payLbl, 13);
        setTextColor(payLbl, new Color(55, 65, 81));
        paymentIdField = createTextField(150, 95, 200, 30);
        paymentIdField.setEditable(false);

        JLabel bookLbl = createLabel("Booking ID:", 40, 135, 100, 25);
        setFontStyle(bookLbl, Font.BOLD);
        setFontSize(bookLbl, 13);
        setTextColor(bookLbl, new Color(55, 65, 81));
        bookingIdField = createTextField(150, 135, 200, 30);
        bookingIdField.setEditable(false);

        JLabel amtLbl = createLabel("Amount (PHP):", 40, 175, 100, 25);
        setFontStyle(amtLbl, Font.BOLD);
        setFontSize(amtLbl, 13);
        setTextColor(amtLbl, new Color(55, 65, 81));
        amountField = createTextField(150, 175, 200, 30);
        amountField.setEditable(false);

        JLabel methodLbl = createLabel("Method:", 40, 215, 100, 25);
        setFontStyle(methodLbl, Font.BOLD);
        setFontSize(methodLbl, 13);
        setTextColor(methodLbl, new Color(55, 65, 81));
        methodField = createTextField(150, 215, 200, 30);
        methodField.setEditable(false);

        JLabel srcLbl = createLabel("Source:", 40, 255, 100, 25);
        setFontStyle(srcLbl, Font.BOLD);
        setFontSize(srcLbl, 13);
        setTextColor(srcLbl, new Color(55, 65, 81));
        sourceField = createTextField(150, 255, 200, 30);
        sourceField.setEditable(false);

        JLabel dateLbl = createLabel("Date:", 40, 295, 100, 25);
        setFontStyle(dateLbl, Font.BOLD);
        setFontSize(dateLbl, 13);
        setTextColor(dateLbl, new Color(55, 65, 81));
        dateField = createTextField(150, 295, 200, 30);
        dateField.setEditable(false);

        // Action Buttons
        JButton clearBtn = createButton("Clear Selection", 40, 355, 310, 36);
        setFontSize(clearBtn, 13);
        setBackground(clearBtn, new Color(229, 231, 235));
        setTextColor(clearBtn, new Color(55, 65, 81));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> clearForm());

        JButton backBtn = createButton("Back to Dashboard", 40, 405, 310, 36);
        setFontSize(backBtn, 13);
        setBackground(backBtn, new Color(220, 225, 235));
        setTextColor(backBtn, new Color(51, 65, 85));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

        // Search Section using Screen helper (no DAO function attached)
        searchField = createSearchBar(370, 18, 590, 30, () -> {}, () -> {});

        // Read-only Table with columns
        String[] columns = {"Payment ID", "Booking ID", "Date", "Amount", "Method", "Source"};
        tableModel = createReadOnlyTableModel(columns);
        salesTable = createTable(tableModel, 370, 52, 590, 480);
        salesTable.setRowHeight(24);
        if (salesTable.getColumnModel().getColumnCount() >= 6) {
            salesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            salesTable.getColumnModel().getColumn(1).setPreferredWidth(80);
            salesTable.getColumnModel().getColumn(2).setPreferredWidth(120);
            salesTable.getColumnModel().getColumn(3).setPreferredWidth(110);
            salesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
            salesTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        }
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