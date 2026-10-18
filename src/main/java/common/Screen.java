package common;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class Screen extends JFrame {

    // -------------------------------------------------------------
    // Design System Tokens (Standard Colors & Dimensions)
    // -------------------------------------------------------------
    public static final Color COLOR_BG              = new Color(248, 250, 252);
    public static final Color COLOR_CARD_BG         = Color.WHITE;
    public static final Color COLOR_BORDER          = new Color(226, 232, 240);
    public static final Color COLOR_BORDER_INPUT    = new Color(203, 213, 225);
    public static final Color COLOR_PRIMARY         = new Color(37, 99, 235);   // Royal Blue
    public static final Color COLOR_PRIMARY_HOVER   = new Color(29, 78, 216);   // Darker Royal Blue
    public static final Color COLOR_SUCCESS         = new Color(16, 185, 129);  // Emerald Green
    public static final Color COLOR_SUCCESS_HOVER   = new Color(5, 150, 105);   // Darker Emerald
    public static final Color COLOR_DANGER          = new Color(239, 68, 68);   // Crimson Red
    public static final Color COLOR_DANGER_HOVER    = new Color(220, 38, 38);   // Darker Crimson
    public static final Color COLOR_SECONDARY       = new Color(241, 245, 249); // Light Slate
    public static final Color COLOR_SECONDARY_HOVER = new Color(226, 232, 240); // Slightly darker slate
    public static final Color COLOR_TEXT_MAIN       = new Color(15, 23, 42);    // Dark Slate
    public static final Color COLOR_TEXT_MUTED      = new Color(100, 116, 139); // Muted Slate

    static {
        try {
            javax.swing.UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
            javax.swing.UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));
            javax.swing.UIManager.put("OptionPane.messageForeground", COLOR_TEXT_MAIN);
        } catch (Exception ignored) {}
    }

    public static final int STD_WIDTH  = 1200;
    public static final int STD_HEIGHT = 700;

    // Default Font Attributes
    protected String defaultFontFamily = "Segoe UI";
    protected int defaultFontStyle = Font.PLAIN;
    protected int defaultFontSize = 15;
    protected Font defaultFont = new Font(defaultFontFamily, defaultFontStyle, defaultFontSize);

    // Header Font Attributes
    protected String defaultHeaderFontFamily = "Segoe UI";
    protected int defaultHeaderFontStyle = Font.BOLD;
    protected int defaultHeaderFontSize = 20;
    protected Font defaultHeaderFont = new Font(defaultHeaderFontFamily, defaultHeaderFontStyle, defaultHeaderFontSize);

    public Screen(String title, int width, int height) {
        InitScreen(title, width, height);
        centerScreen();
    }
    
    public Screen(String title) {
        InitScreen(title, STD_WIDTH, STD_HEIGHT);
        centerScreen();
    }
    
    public Screen() {
        InitScreen("Screen", STD_WIDTH, STD_HEIGHT);
        centerScreen();
    }
    
    public final void InitScreen(String title, int width, int height) {
        setTitle(title);
        setSize(width, height);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(COLOR_BG);
    }

    public final void showScreen() {
        setVisible(true);
    }

    public final void hideScreen() {
        setVisible(false);
    }

    public final void closeScreen() {
        dispose();
    }
    
    public final void centerScreen() {
        setLocationRelativeTo(null);
    }
    
    public final void setFullscreen(boolean choice) {
        if (choice) {
            setResizable(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setExtendedState(JFrame.NORMAL);
        }
    }
    
    public final void toggleFullscreen() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setResizable(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    public final void navigateTo(Screen nextScreen) {
        nextScreen.showScreen();
        closeScreen();
    }
    
    // -------------------------------------------------------------
    // Header Bar Generator
    // -------------------------------------------------------------

    public JButton createTopHeader(String categoryTag, String title, String subtitle, Runnable onBack) {
        JLabel tag = createLabel(categoryTag, 45, 14, 550, 16);
        setFontStyle(tag, Font.BOLD);
        setFontSize(tag, 10);
        setTextColor(tag, COLOR_PRIMARY);

        JLabel header = createHeader(title, 45, 30, 550, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        JLabel sub = createLabel(subtitle, 45, 60, 650, 18);
        setFontSize(sub, 12);
        setTextColor(sub, COLOR_TEXT_MUTED);

        if (onBack != null) {
            int backX = getWidth() > 45 + 135 ? getWidth() - 45 - 135 : 1020;
            JButton backBtn = createSecondaryButton(this, "← Dashboard", backX, 18, 135, 36, onBack);
            setFontStyle(backBtn, Font.BOLD);
            setFontSize(backBtn, 12);
            setBackground(backBtn, Color.WHITE);
            setTextColor(backBtn, new Color(51, 65, 85));
            return backBtn;
        }
        return null;
    }

    // -------------------------------------------------------------
    // Card & Panel Creators
    // -------------------------------------------------------------

    public JPanel createCard(int x, int y, int width, int height) {
        return createCard(this, x, y, width, height);
    }

    public JPanel createCard(Container parent, int x, int y, int width, int height) {
        JPanel card = new JPanel(null);
        card.setBounds(x, y, width, height);
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        parent.add(card);
        return card;
    }

    public JLabel createCardTag(Container parent, String text) {
        JLabel cardTag = new JLabel("  " + text + "  ");
        cardTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        cardTag.setForeground(COLOR_PRIMARY);
        cardTag.setOpaque(true);
        cardTag.setBackground(new Color(239, 246, 255));
        cardTag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        cardTag.setBounds(16, 12, parent.getWidth() > 32 ? parent.getWidth() - 32 : 280, 22);
        parent.add(cardTag);
        return cardTag;
    }

    // -------------------------------------------------------------
    // Form Field Row Helpers
    // -------------------------------------------------------------

    public JTextField addFormField(Container parent, String labelText, int y, boolean editable) {
        return addFormField(parent, labelText, new JTextField(), y, editable);
    }

    public JTextField addFormField(Container parent, String labelText, JTextField field, int y, boolean editable) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setBounds(16, y, 95, 28);
        parent.add(lbl);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBounds(115, y, 214, 28);
        field.setEditable(editable);
        if (editable) {
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
        } else {
            field.setBackground(new Color(241, 245, 249));
            field.setForeground(new Color(100, 116, 139));
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
        }
        parent.add(field);
        return field;
    }

    // -------------------------------------------------------------
    // Semantic Button Factories
    // -------------------------------------------------------------

    public JButton createStyledButton(Container parent, String text, int x, int y, int w, int h,
                                      Color bg, Color fg, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setBounds(x, y, w, h);
        if (action != null) {
            btn.addActionListener(e -> action.run());
        }
        parent.add(btn);
        return btn;
    }

    public JButton createStyledButton(Container parent, String text, int x, int y, int w, int h,
                                      Color bg, Color fg, Color border, int fontSize, Runnable action) {
        JButton btn = createStyledButton(parent, text, x, y, w, h, bg, fg, action);
        if (border != null) {
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
        }
        if (fontSize > 0) {
            setFontSize(btn, fontSize);
        }
        return btn;
    }

    public JButton createSuccessButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        JButton btn = createStyledButton(parent, text, x, y, w, h, COLOR_SUCCESS, Color.WHITE, action);
        btn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_SUCCESS_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_SUCCESS);
            }
        });
        return btn;
    }

    public JButton createSuccessButton(String text, int x, int y, int w, int h, Runnable action) {
        return createSuccessButton(this, text, x, y, w, h, action);
    }

    public JButton createPrimaryButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        JButton btn = createStyledButton(parent, text, x, y, w, h, COLOR_PRIMARY, Color.WHITE, action);
        btn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY);
            }
        });
        return btn;
    }

    public JButton createPrimaryButton(String text, int x, int y, int w, int h, Runnable action) {
        return createPrimaryButton(this, text, x, y, w, h, action);
    }

    public JButton createDangerButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        JButton btn = createStyledButton(parent, text, x, y, w, h, COLOR_DANGER, Color.WHITE, action);
        btn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_DANGER_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_DANGER);
            }
        });
        return btn;
    }

    public JButton createDangerButton(String text, int x, int y, int w, int h, Runnable action) {
        return createDangerButton(this, text, x, y, w, h, action);
    }

    public JButton createSecondaryButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        JButton btn = createStyledButton(parent, text, x, y, w, h, COLOR_SECONDARY, new Color(71, 85, 105), action);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_SECONDARY_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COLOR_SECONDARY);
            }
        });
        return btn;
    }

    public JButton createSecondaryButton(String text, int x, int y, int w, int h, Runnable action) {
        return createSecondaryButton(this, text, x, y, w, h, action);
    }

    // -------------------------------------------------------------
    // Container-Aware Component Creators
    // -------------------------------------------------------------
    
    public void addComponent(JComponent component, int x, int y, int width, int height) {
        component.setBounds(x, y, width, height);
        add(component);
    }
    
    public JLabel createLabel(Container parent, String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(defaultFont);
        label.setBounds(x, y, width, height);
        parent.add(label);
        return label;
    }

    public JLabel createLabel(String text, int x, int y, int width, int height) {
        return createLabel(this, text, x, y, width, height);
    }
    
    public JLabel createLabel(String text, int x, int y) {
        return createLabel(text, x, y, 200, 30);
    }

    public JLabel createHeader(Container parent, String text, int x, int y, int width, int height) {
        JLabel header = new JLabel(text);
        header.setFont(defaultHeaderFont);
        header.setBounds(x, y, width, height);
        parent.add(header);
        return header;
    }

    public JLabel createHeader(String text, int x, int y, int width, int height) {
        return createHeader(this, text, x, y, width, height);
    }
    
    public JButton createButton(Container parent, String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(defaultFont);
        button.setBounds(x, y, width, height);
        parent.add(button);
        return button;
    }

    public JButton createButton(String text, int x, int y, int width, int height) {
        return createButton(this, text, x, y, width, height);
    }
    
    public JButton createButton(String text, int x, int y) {
        return createButton(text, x, y, 200, 30);
    }

    public JTextField createTextField(Container parent, int x, int y, int width, int height) {
        JTextField textField = new JTextField();
        textField.setFont(defaultFont);
        textField.setBounds(x, y, width, height);
        parent.add(textField);
        return textField;
    }

    public JTextField createTextField(int x, int y, int width, int height) {
        return createTextField(this, x, y, width, height);
    }

    public JTextField createTextField(int x, int y) {
        return createTextField(x, y, 200, 30);
    }

    public JPasswordField createPasswordField(Container parent, int x, int y, int width, int height) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordField.setBounds(x, y, width, height);
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        parent.add(passwordField);
        return passwordField;
    }

    public JPasswordField createPasswordField(int x, int y, int width, int height) {
        return createPasswordField(this, x, y, width, height);
    }

    public JPasswordField createPasswordField(int x, int y) {
        return createPasswordField(x, y, 200, 30);
    }

    public <T> JComboBox<T> createComboBox(Container parent, T[] items, int x, int y, int width, int height) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBounds(x, y, width, height);
        parent.add(comboBox);
        return comboBox;
    }

    public <T> JComboBox<T> createComboBox(T[] items, int x, int y, int width, int height) {
        return createComboBox(this, items, x, y, width, height);
    }

    public <T> JComboBox<T> createComboBox(T[] items, int x, int y) {
        return createComboBox(items, x, y, 200, 30);
    }

    public JTextArea createTextArea(Container parent, int x, int y, int width, int height) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(x, y, width, height);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1));
        parent.add(scrollPane);
        return textArea;
    }

    public JTextArea createTextArea(int x, int y, int width, int height) {
        return createTextArea(this, x, y, width, height);
    }
    
    // -------------------------------------------------------------
    // Dialog Helpers
    // -------------------------------------------------------------
    
    public boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Confirmation", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // -------------------------------------------------------------
    // Design Helpers for Components
    // -------------------------------------------------------------
    
    public final void setFont(JComponent component, String fontName, int style, int size) {
        component.setFont(new Font(fontName, style, size));
    }

    public final void setFontSize(JComponent component, int size) {
        Font currentFont = component.getFont();
        if (currentFont != null) {
            component.setFont(new Font(currentFont.getName(), currentFont.getStyle(), size));
        } else {
            component.setFont(new Font(defaultFontFamily, defaultFontStyle, size));
        }
    }

    public final void setFontStyle(JComponent component, int style) {
        Font currentFont = component.getFont();
        if (currentFont != null) {
            component.setFont(new Font(currentFont.getName(), style, currentFont.getSize()));
        } else {
            component.setFont(new Font(defaultFontFamily, style, defaultFontSize));
        }
    }

    public final void setBackground(JComponent component, Color color) {
        component.setOpaque(true);
        component.setBackground(color);
    }

    public final void setTextColor(JComponent component, Color color) {
        component.setForeground(color);
    }

    // -------------------------------------------------------------
    // Table and Search Helper Methods
    // -------------------------------------------------------------

    public DefaultTableModel createReadOnlyTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public JTable createTable(DefaultTableModel model, int x, int y, int width, int height) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(COLOR_TEXT_MAIN);

        // Custom default cell renderer with padding and zebra striping
        javax.swing.table.DefaultTableCellRenderer defaultRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    lbl.setForeground(COLOR_TEXT_MAIN);
                }
                return lbl;
            }
        };
        table.setDefaultRenderer(Object.class, defaultRenderer);

        if (table.getTableHeader() != null) {
            table.getTableHeader().setPreferredSize(new java.awt.Dimension(width, 36));
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getTableHeader().setBackground(new Color(241, 245, 249));
            table.getTableHeader().setForeground(new Color(71, 85, 105));
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
            table.getTableHeader().setReorderingAllowed(false);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        addComponent(scrollPane, x, y, width, height);
        return table;
    }

    public void setStatusBadgeRenderer(JTable table, int statusCol) {
        if (table == null || statusCol < 0 || statusCol >= table.getColumnModel().getColumnCount()) return;
        table.getColumnModel().getColumn(statusCol).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                c.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String text = value != null ? value.toString() : "";
                c.setText(text);

                if (!isSelected) {
                    if ("Active".equalsIgnoreCase(text) || "Confirmed".equalsIgnoreCase(text)) {
                        c.setBackground(new Color(220, 252, 231));
                        c.setForeground(new Color(22, 101, 52));
                    } else if ("Pending".equalsIgnoreCase(text)) {
                        c.setBackground(new Color(254, 243, 199));
                        c.setForeground(new Color(146, 64, 14));
                    } else if ("Completed".equalsIgnoreCase(text)) {
                        c.setBackground(new Color(224, 242, 254));
                        c.setForeground(new Color(7, 89, 133));
                    } else if ("Inactive".equalsIgnoreCase(text) || "Cancelled".equalsIgnoreCase(text) || "Suspended".equalsIgnoreCase(text)) {
                        c.setBackground(new Color(254, 226, 226));
                        c.setForeground(new Color(153, 27, 27));
                    } else if ("Admin".equalsIgnoreCase(text)) {
                        c.setBackground(new Color(243, 232, 255));
                        c.setForeground(new Color(107, 33, 168));
                    } else if ("Manager".equalsIgnoreCase(text)) {
                        c.setBackground(new Color(224, 231, 255));
                        c.setForeground(new Color(49, 46, 129));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                        c.setForeground(COLOR_TEXT_MAIN);
                    }
                } else {
                    c.setBackground(tbl.getSelectionBackground());
                    c.setForeground(tbl.getSelectionForeground());
                }
                c.setOpaque(true);
                return c;
            }
        });
    }

    public void setColumnWidths(JTable table, int... widths) {
        if (table == null || widths == null) return;
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public JTextField createSearchBar(int x, int y, int width, int height,
                                      Runnable onSearch, Runnable onShowAll) {
        int labelWidth = 55;
        int showAllWidth = 95;
        int searchBtnWidth = 85;
        int gap = 8;
        int buttonsTotal = searchBtnWidth + gap + showAllWidth;

        JLabel sLbl = createLabel("Search:", x, y, labelWidth, height);
        setFontStyle(sLbl, Font.BOLD);
        setFontSize(sLbl, 11);
        setTextColor(sLbl, new Color(71, 85, 105));

        JTextField searchField = createTextField(x + labelWidth + 5, y, width - labelWidth - 5 - buttonsTotal - gap, height);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBackground(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        JButton searchBtn = createPrimaryButton(this, "Search", x + width - buttonsTotal, y, searchBtnWidth, height, onSearch);
        setFontSize(searchBtn, 12);

        JButton showAllBtn = createSecondaryButton(this, "Show All", x + width - showAllWidth, y, showAllWidth, height, () -> {
            searchField.setText("");
            if (onShowAll != null) {
                onShowAll.run();
            }
        });
        setFontSize(showAllBtn, 12);

        return searchField;
    }

    // -------------------------------------------------------------
    // Form Input Parsing & Validation Helpers
    // -------------------------------------------------------------

    public String getTrimmedText(javax.swing.text.JTextComponent component) {
        return component != null && component.getText() != null ? component.getText().trim() : "";
    }

    public Integer getIntOrNull(JTextField field) {
        String text = getTrimmedText(field);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Integer getIntOrNull(JTextField field, String fieldName) {
        String text = getTrimmedText(field);
        if (text.isEmpty()) {
            showError(fieldName + " is required.");
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            showError(fieldName + " must be a valid integer number.");
            return null;
        }
    }

    public BigDecimal getPriceOrNull(JTextField field) {
        String text = getTrimmedText(field).replace(",", "");
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public BigDecimal getPriceOrNull(JTextField field, String fieldName) {
        String text = getTrimmedText(field).replace(",", "");
        if (text.isEmpty()) {
            showError(fieldName + " is required.");
            return null;
        }
        try {
            BigDecimal val = new BigDecimal(text);
            if (val.compareTo(BigDecimal.ZERO) <= 0) {
                showError(fieldName + " must be greater than zero.");
                return null;
            }
            return val;
        } catch (NumberFormatException ex) {
            showError(fieldName + " must be a valid numeric amount.");
            return null;
        }
    }

    // -------------------------------------------------------------
    // Image & Icon Loading Helpers
    // -------------------------------------------------------------

    public ImageIcon loadIcon(String resourcePath, int targetWidth, int targetHeight) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return null;
    }
}