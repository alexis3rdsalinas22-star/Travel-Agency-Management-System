package ui.customer;

import common.Screen;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MyBookings extends Screen {

    
    private static final Color COLOR_WARNING = new Color(217, 119, 6);

    private DefaultTableModel tableModel;
    private JTable bookingsTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JLabel countLabel;
    private JButton cancelBtn;

    public MyBookings() {
        super("My Bookings");
        initUI();
    }

    private void initUI() {
 
        createTopHeader(
                "CUSTOMER",
                "My Bookings",
                "Review your travel reservations and booking status.",
                () -> navigateTo(new CustomerDashboard())
        );

      
        searchField = createSearchBar(45, 100, 1110, 32, this::applySearch, this::applySearch);
        searchField.addActionListener(e -> applySearch()); 

      
        countLabel = createLabel("0 bookings", 45, 594, 400, 20);
        setFontSize(countLabel, 12);
        setTextColor(countLabel, COLOR_TEXT_MUTED);

        
        String[] columns = {"Booking ID", "Package Name", "Status", "Travel Date", "Booking Date"};
        tableModel = createReadOnlyTableModel(columns);
        bookingsTable = createTable(tableModel, 45, 148, 1110, 420);
        setColumnWidths(bookingsTable, 110, 420, 160, 210, 210);

        sorter = new TableRowSorter<>(tableModel);
        bookingsTable.setRowSorter(sorter);
        bookingsTable.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());

      
        tableModel.addTableModelListener(e -> refreshCount());
        bookingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateCancelButton();
            }
        });

     
        cancelBtn = createDangerButton("Cancel Booking", 940, 585, 215, 38, null);
        cancelBtn.setEnabled(false);

        refreshCount();
    }

  

    private void applySearch() {
        String text = getTrimmedText(searchField);
        sorter.setRowFilter(text.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        refreshCount();
    }

    private void refreshCount() {
        int shown = bookingsTable.getRowCount();
        countLabel.setText(shown == 0
                ? "No bookings found"
                : shown + (shown == 1 ? " booking" : " bookings"));
    }

    private void updateCancelButton() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            cancelBtn.setEnabled(false);
            return;
        }
        Object status = bookingsTable.getValueAt(row, 2);
        String s = status == null ? "" : status.toString();
        cancelBtn.setEnabled(s.equalsIgnoreCase("Pending") || s.equalsIgnoreCase("Confirmed"));
    }


    private static Color statusColor(String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
            case "completed":
                return COLOR_SUCCESS;
            case "pending":
                return COLOR_WARNING;
            case "cancelled":
                return COLOR_DANGER;
            default:
                return COLOR_TEXT_MAIN;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            c.setFont(c.getFont().deriveFont(Font.BOLD));
            if (!isSelected) {
                c.setForeground(statusColor(value == null ? "" : value.toString()));
            }
            return c;
        }
    }
}