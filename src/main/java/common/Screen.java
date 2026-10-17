package common;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
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
    public static final Color COLOR_BG         = new Color(248, 250, 252);
    public static final Color COLOR_CARD_BG    = Color.WHITE;
    public static final Color COLOR_BORDER     = new Color(226, 232, 240);
    public static final Color COLOR_PRIMARY    = new Color(37, 99, 235);   // Vibrant Blue
    public static final Color COLOR_SUCCESS    = new Color(16, 149, 93);   // Emerald Green
    public static final Color COLOR_DANGER     = new Color(220, 53, 69);   // Crimson Red
    public static final Color COLOR_SECONDARY  = new Color(241, 245, 249); // Light Slate
    public static final Color COLOR_TEXT_MAIN  = new Color(15, 23, 42);    // Dark Slate
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Muted Slate

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
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setExtendedState(JFrame.NORMAL);
        }
    }
    
    public final void toggleFullscreen() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
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

    public void createTopHeader(String categoryTag, String title, String subtitle, Runnable onBack) {
        JLabel tag = createLabel(categoryTag, 45, 14, 500, 16);
        setFontStyle(tag, Font.BOLD);
        setFontSize(tag, 11);
        setTextColor(tag, COLOR_PRIMARY);

        JLabel header = createHeader(title, 45, 30, 500, 30);
        setFontSize(header, 22);
        setTextColor(header, COLOR_TEXT_MAIN);

        JLabel sub = createLabel(subtitle, 45, 60, 600, 18);
        setFontSize(sub, 12);
        setTextColor(sub, COLOR_TEXT_MUTED);

        if (onBack != null) {
            JButton backBtn = createButton("← Dashboard", 1020, 18, 135, 36);
            setFontStyle(backBtn, Font.BOLD);
            setFontSize(backBtn, 12);
            setBackground(backBtn, Color.WHITE);
            setTextColor(backBtn, new Color(51, 65, 85));
            backBtn.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
            backBtn.setFocusPainted(false);
            backBtn.addActionListener(e -> onBack.run());
        }
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
        card.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        parent.add(card);
        return card;
    }

    public JLabel createCardTag(Container parent, String text) {
        JLabel cardTag = new JLabel(text);
        cardTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cardTag.setForeground(COLOR_PRIMARY);
        cardTag.setBounds(16, 12, parent.getWidth() > 32 ? parent.getWidth() - 32 : 280, 16);
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
        lbl.setForeground(new Color(51, 65, 85));
        lbl.setBounds(16, y, 95, 26);
        parent.add(lbl);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBounds(115, y, 214, 26);
        field.setEditable(editable);
        if (!editable) {
            field.setBackground(COLOR_SECONDARY);
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
            btn.setBorder(BorderFactory.createLineBorder(border, 1));
        }
        if (fontSize > 0) {
            setFontSize(btn, fontSize);
        }
        return btn;
    }

    public JButton createSuccessButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        return createStyledButton(parent, text, x, y, w, h, COLOR_SUCCESS, Color.WHITE, action);
    }

    public JButton createSuccessButton(String text, int x, int y, int w, int h, Runnable action) {
        return createSuccessButton(this, text, x, y, w, h, action);
    }

    public JButton createPrimaryButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        return createStyledButton(parent, text, x, y, w, h, COLOR_PRIMARY, Color.WHITE, action);
    }

    public JButton createPrimaryButton(String text, int x, int y, int w, int h, Runnable action) {
        return createPrimaryButton(this, text, x, y, w, h, action);
    }

    public JButton createDangerButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        return createStyledButton(parent, text, x, y, w, h, COLOR_DANGER, Color.WHITE, action);
    }

    public JButton createDangerButton(String text, int x, int y, int w, int h, Runnable action) {
        return createDangerButton(this, text, x, y, w, h, action);
    }

    public JButton createSecondaryButton(Container parent, String text, int x, int y, int w, int h, Runnable action) {
        JButton btn = createStyledButton(parent, text, x, y, w, h, COLOR_SECONDARY, new Color(71, 85, 105), action);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
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
        passwordField.setFont(defaultFont);
        passwordField.setBounds(x, y, width, height);
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
        comboBox.setFont(defaultFont);
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
        textArea.setFont(defaultFont);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(x, y, width, height);
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
        table.setRowHeight(26);
        table.setGridColor(COLOR_SECONDARY);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(COLOR_TEXT_MAIN);

        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getTableHeader().setBackground(COLOR_SECONDARY);
            table.getTableHeader().setForeground(new Color(51, 65, 85));
        }

        JScrollPane scrollPane = new JScrollPane(table);
        addComponent(scrollPane, x, y, width, height);
        return table;
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
        int showAllWidth = 90;
        int searchBtnWidth = 85;
        int gap = 8;
        int buttonsTotal = searchBtnWidth + gap + showAllWidth;

        createLabel("Search:", x, y, labelWidth, height);
        JTextField searchField = createTextField(x + labelWidth + 5, y, width - labelWidth - 5 - buttonsTotal - gap, height);

        JButton searchBtn = createButton("Search", x + width - buttonsTotal, y, searchBtnWidth, height);
        searchBtn.setMargin(new Insets(2, 4, 2, 4));
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> {
            if (onSearch != null) {
                onSearch.run();
            }
        });

        JButton showAllBtn = createButton("Show All", x + width - showAllWidth, y, showAllWidth, height);
        showAllBtn.setMargin(new Insets(2, 4, 2, 4));
        showAllBtn.setFocusPainted(false);
        showAllBtn.addActionListener(e -> {
            searchField.setText("");
            if (onShowAll != null) {
                onShowAll.run();
            }
        });
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
}