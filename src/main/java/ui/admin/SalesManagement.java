package ui.admin;

import common.Screen;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;
import dao.PaymentDAO;
import dao.PaymentDAO.PaymentRecord;
import dao.PaymentDAO.SalesSummary;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SalesManagement extends Screen {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // Form / Detail fields
    private JTextField paymentIdField;
    private JTextField bookingIdField;
    private JTextField amountField;
    private JTextField methodField;
    private JTextField dateField;
    private JTextField sourceField;

    // Sales Summary Metrics
    private JLabel totalRevenueLbl;
    private JLabel totalPaymentsLbl;
    private JLabel avgPaymentLbl;

    // Search and Table
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable salesTable;

    // In-memory payment records
    private List<PaymentRecord> allPayments = new ArrayList<>();
    private List<PaymentRecord> displayedPayments = new ArrayList<>();

    public SalesManagement() {
        super("Sales & Revenue Management - Admin Console", STD_WIDTH, STD_HEIGHT);

        getContentPane().setBackground(COLOR_BG);

        initUI();
        loadPayments(null);
    }

    private void initUI() {
        createTopHeader(
            "ADMIN CONSOLE  •  REVENUE & TRANSACTIONS",
            "Sales & Revenue Management",
            "View payment records, booking transactions, and revenue reports.",
            () -> navigateTo(new AdminDashboard())
        );

        // Left Column: Modern Card Container for Transaction Details
        JPanel formCard = createCard(45, 88, 345, 555);
        createCardTag(formCard, "TRANSACTION DETAILS");

        // Read-only Details
        paymentIdField = addFormField(formCard, "Payment ID:", 40, false);
        bookingIdField = addFormField(formCard, "Booking ID:", 80, false);
        amountField = addFormField(formCard, "Amount (PHP):", 120, false);
        methodField = addFormField(formCard, "Method:", 160, false);
        sourceField = addFormField(formCard, "Source:", 200, false);
        dateField = addFormField(formCard, "Date:", 240, false);

        // Action Button inside Card
        createSecondaryButton(formCard, "Clear Selection", 16, 282, 313, 32, this::clearForm);

        // Mini Card: Sales Summary KPI Panel
        JPanel summaryBox = new JPanel(null);
        summaryBox.setBounds(16, 330, 313, 185);
        summaryBox.setBackground(COLOR_BG);
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel sumTitle = createLabel(summaryBox, "REVENUE OVERVIEW", 12, 10, 280, 16);
        setFontStyle(sumTitle, Font.BOLD);
        setFontSize(sumTitle, 11);
        setTextColor(sumTitle, COLOR_SUCCESS);

        JLabel revHeader = createLabel(summaryBox, "Total Revenue:", 12, 34, 130, 18);
        setFontSize(revHeader, 11);
        setTextColor(revHeader, COLOR_TEXT_MUTED);

        totalRevenueLbl = createLabel(summaryBox, "PHP 0.00", 12, 54, 280, 22);
        setFontStyle(totalRevenueLbl, Font.BOLD);
        setFontSize(totalRevenueLbl, 14);
        setTextColor(totalRevenueLbl, COLOR_SUCCESS);

        JLabel payHeader = createLabel(summaryBox, "Total Transactions:", 12, 84, 130, 18);
        setFontSize(payHeader, 11);
        setTextColor(payHeader, COLOR_TEXT_MUTED);

        totalPaymentsLbl = createLabel(summaryBox, "0 records", 12, 102, 280, 20);
        setFontStyle(totalPaymentsLbl, Font.BOLD);
        setFontSize(totalPaymentsLbl, 13);
        setTextColor(totalPaymentsLbl, COLOR_TEXT_MAIN);

        JLabel avgHeader = createLabel(summaryBox, "Average Transaction:", 12, 130, 150, 18);
        setFontSize(avgHeader, 11);
        setTextColor(avgHeader, COLOR_TEXT_MUTED);

        avgPaymentLbl = createLabel(summaryBox, "PHP 0.00", 12, 148, 280, 20);
        setFontStyle(avgPaymentLbl, Font.BOLD);
        setFontSize(avgPaymentLbl, 13);
        setTextColor(avgPaymentLbl, COLOR_PRIMARY);

        formCard.add(summaryBox);

        JLabel helperHint = createLabel(formCard, "Click any payment row in table to view breakdown", 16, 525, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Search Bar & Table
        searchField = createSearchBar(410, 88, 745, 34, this::handleSearch, () -> loadPayments(null));
        searchField.addActionListener(e -> handleSearch());

        String[] columns = {"Payment ID", "Booking ID", "Date", "Amount", "Method", "Source"};
        tableModel = createReadOnlyTableModel(columns);
        salesTable = createTable(tableModel, 410, 132, 745, 511);
        setColumnWidths(salesTable, 95, 95, 140, 140, 130, 130);

        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadPayments(String keyword) {
        tableModel.setRowCount(0);
        allPayments.clear();
        displayedPayments.clear();

        try {
            // Retrieve bookings to aggregate payments via current DAO methods
            List<BookingRecord> bookings = bookingDAO.getAllBookings();
            for (BookingRecord b : bookings) {
                List<PaymentRecord> payments = paymentDAO.getPaymentsByBooking(b.bookingID);
                if (payments != null) {
                    allPayments.addAll(payments);
                }
            }

            // Sort payments by date descending
            allPayments.sort((a, b) -> {
                if (a.paymentDate == null && b.paymentDate == null) return 0;
                if (a.paymentDate == null) return 1;
                if (b.paymentDate == null) return -1;
                return b.paymentDate.compareTo(a.paymentDate);
            });

            // Filter by keyword if provided
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                for (PaymentRecord p : allPayments) {
                    boolean match = String.valueOf(p.paymentID).contains(kw)
                            || String.valueOf(p.bookingID).contains(kw)
                            || (p.method != null && p.method.toLowerCase().contains(kw))
                            || (p.source != null && p.source.toLowerCase().contains(kw))
                            || (p.amount != null && p.amount.toPlainString().contains(kw));
                    if (match) {
                        displayedPayments.add(p);
                    }
                }
            } else {
                displayedPayments.addAll(allPayments);
            }

            // Populate table
            for (PaymentRecord p : displayedPayments) {
                tableModel.addRow(new Object[]{
                    p.paymentID,
                    p.bookingID,
                    p.paymentDate != null ? dateFormat.format(p.paymentDate) : "N/A",
                    p.amount != null ? String.format("PHP %,.2f", p.amount) : "PHP 0.00",
                    p.method != null ? p.method : "N/A",
                    p.source != null ? p.source : "N/A"
                });
            }

            // Load aggregate summary from PaymentDAO
            SalesSummary summary = paymentDAO.getSalesReportSummary();
            if (summary != null) {
                totalRevenueLbl.setText(String.format("PHP %,.2f", summary.totalRevenue));
                totalPaymentsLbl.setText(summary.totalPayments + " transactions");
                totalPaymentAvg(summary.averagePayment);
            }

        } catch (SQLException ex) {
            showError("Database error loading sales records: " + ex.getMessage());
        }
    }

    private void totalPaymentAvg(BigDecimal avg) {
        if (avg != null) {
            avgPaymentLbl.setText(String.format("PHP %,.2f", avg));
        } else {
            avgPaymentLbl.setText("PHP 0.00");
        }
    }

    private void handleSearch() {
        String keyword = getTrimmedText(searchField);
        loadPayments(keyword.isEmpty() ? null : keyword);
    }

    private void populateFormFromSelectedRow() {
        int row = salesTable.getSelectedRow();
        if (row >= 0 && row < displayedPayments.size()) {
            PaymentRecord p = displayedPayments.get(row);
            paymentIdField.setText(String.valueOf(p.paymentID));
            bookingIdField.setText(String.valueOf(p.bookingID));
            dateField.setText(p.paymentDate != null ? dateFormat.format(p.paymentDate) : "");
            amountField.setText(p.amount != null ? String.format("PHP %,.2f", p.amount) : "");
            methodField.setText(p.method != null ? p.method : "");
            sourceField.setText(p.source != null ? p.source : "");
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