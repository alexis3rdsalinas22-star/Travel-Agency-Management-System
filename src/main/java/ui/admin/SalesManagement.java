package ui.admin;

import common.Screen;
import dao.PaymentDAO;
import dao.PaymentDAO.PaymentRecord;
import dao.PaymentDAO.SalesSummary;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class SalesManagement extends Screen {

    private final PaymentDAO paymentDAO = new PaymentDAO();
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

    // Filters and Table
    private JComboBox<String> methodFilterCombo;
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
        loadPayments();
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

        // Hidden ID fields to preserve programmatic access
        paymentIdField = new JTextField();
        bookingIdField = new JTextField();

        // Read-only Details
        dateField = addFormField(formCard, "Date:", 40, false);
        amountField = addFormField(formCard, "Amount (PHP):", 76, false);
        methodField = addFormField(formCard, "Method:", 112, false);
        sourceField = addFormField(formCard, "Source:", 148, false);

        // Action Button inside Card
        createSecondaryButton(formCard, "Clear Selection", 16, 192, 313, 30, this::clearForm);

        // Mini Card: Sales Summary KPI Panel
        JPanel summaryBox = new JPanel(null);
        summaryBox.setBounds(16, 238, 313, 195);
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

        JLabel helperHint = createLabel(formCard, "Click any payment row in table to view breakdown", 16, 450, 313, 16);
        setFontStyle(helperHint, Font.ITALIC);
        setFontSize(helperHint, 10);
        setTextColor(helperHint, COLOR_TEXT_MUTED);

        // Right Column: Advanced Filter & Search Bar
        JLabel methodLbl = createLabel("Method:", 410, 88, 55, 34);
        setFontStyle(methodLbl, Font.BOLD);
        setFontSize(methodLbl, 11);

        methodFilterCombo = new JComboBox<>(new String[]{"All Methods", "Cash", "Credit Card", "GCash", "Bank Transfer", "PayPal"});
        methodFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        methodFilterCombo.setBounds(468, 88, 120, 34);
        methodFilterCombo.addActionListener(e -> applyFilters());
        add(methodFilterCombo);

        JLabel searchLbl = createLabel("Search:", 596, 88, 48, 34);
        setFontStyle(searchLbl, Font.BOLD);
        setFontSize(searchLbl, 11);

        searchField = createTextField(648, 88, 170, 34);
        searchField.addActionListener(e -> applyFilters());

        JButton searchBtn = createButton("Search", 824, 88, 74, 34);
        searchBtn.addActionListener(e -> applyFilters());

        JButton showAllBtn = createButton("Show All", 904, 88, 82, 34);
        showAllBtn.addActionListener(e -> {
            methodFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            applyFilters();
        });

        JButton exportBtn = createSuccessButton(this, "⬇ Export CSV", 992, 88, 163, 34, this::handleExportCSV);

        String[] columns = {"Date", "Amount", "Method", "Source"};
        tableModel = createReadOnlyTableModel(columns);
        salesTable = createTable(tableModel, 410, 132, 745, 511);
        setColumnWidths(salesTable, 175, 180, 195, 195);

        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });
    }

    private void loadPayments() {
        try {
            allPayments = paymentDAO.getAllPayments();
            applyFilters();
        } catch (SQLException ex) {
            showError("Database error loading sales records: " + ex.getMessage());
        }
    }

    private void applyFilters() {
        tableModel.setRowCount(0);
        displayedPayments.clear();

        String selectedMethod = (String) methodFilterCombo.getSelectedItem();
        boolean filterMethod = selectedMethod != null && !selectedMethod.equals("All Methods");

        String kw = getTrimmedText(searchField).toLowerCase();

        BigDecimal runningTotal = BigDecimal.ZERO;

        for (PaymentRecord p : allPayments) {
            if (filterMethod && (p.method == null || !p.method.equalsIgnoreCase(selectedMethod))) {
                continue;
            }

            if (!kw.isEmpty()) {
                boolean match = (p.method != null && p.method.toLowerCase().contains(kw))
                        || (p.source != null && p.source.toLowerCase().contains(kw))
                        || (p.amount != null && p.amount.toPlainString().contains(kw));
                if (!match) {
                    continue;
                }
            }

            displayedPayments.add(p);
            if (p.amount != null) {
                runningTotal = runningTotal.add(p.amount);
            }

            tableModel.addRow(new Object[]{
                p.paymentDate != null ? dateFormat.format(p.paymentDate) : "N/A",
                p.amount != null ? String.format("PHP %,.2f", p.amount) : "PHP 0.00",
                p.method != null ? p.method : "N/A",
                p.source != null ? p.source : "N/A"
            });
        }

        // Update Summary KPI
        totalRevenueLbl.setText(String.format("PHP %,.2f", runningTotal));
        totalPaymentsLbl.setText(displayedPayments.size() + " transactions");
        if (!displayedPayments.isEmpty()) {
            BigDecimal avg = runningTotal.divide(BigDecimal.valueOf(displayedPayments.size()), 2, RoundingMode.HALF_UP);
            avgPaymentLbl.setText(String.format("PHP %,.2f", avg));
        } else {
            avgPaymentLbl.setText("PHP 0.00");
        }
    }

    private void handleExportCSV() {
        if (displayedPayments.isEmpty()) {
            showError("No payment records currently displayed to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Sales Report to CSV");
        chooser.setSelectedFile(new File("sales_report_" + System.currentTimeMillis() + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                pw.println("Payment ID,Booking ID,Date,Amount (PHP),Method,Source");
                for (PaymentRecord p : displayedPayments) {
                    String d = p.paymentDate != null ? dateFormat.format(p.paymentDate) : "";
                    String amt = p.amount != null ? p.amount.toPlainString() : "0.00";
                    String meth = p.method != null ? "\"" + p.method.replace("\"", "\"\"") + "\"" : "";
                    String src = p.source != null ? "\"" + p.source.replace("\"", "\"\"") + "\"" : "";
                    pw.println(p.paymentID + "," + p.bookingID + ",\"" + d + "\"," + amt + "," + meth + "," + src);
                }
                showInfo("Successfully exported " + displayedPayments.size() + " transactions to " + file.getName());
            } catch (Exception ex) {
                showError("Failed to export CSV: " + ex.getMessage());
            }
        }
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