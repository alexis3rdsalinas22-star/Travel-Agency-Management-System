package common;

import java.awt.Color;
import java.awt.Font;
import javax.swing.*;

public class Screen extends JFrame {

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
    
    public Screen(String title){
        InitScreen(title, 960, 540);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        centerScreen();
    }
    
    public Screen(){
        InitScreen("Screen", 960, 540);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        centerScreen();
    }
    
    public final void InitScreen(String title, int width, int height){
        setTitle(title);
        setSize(width, height);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
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
    
    public final void setFullscreen(boolean choice){
        if(choice){
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
    
    // CREATE SWING COMPONENT METHODS
    
    public void addComponent(JComponent component, int x, int y, int width, int height) {
        component.setBounds(x, y, width, height);
        add(component);
    }
    
    public JLabel createLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(defaultFont);
        label.setBounds(x, y, width, height);
        add(label);
        return label;
    }
    
    public JLabel createLabel(String text, int x, int y) {
        return createLabel(text, x, y, 200, 30);
    }

    public JLabel createHeader(String text, int x, int y, int width, int height) {
        JLabel header = new JLabel(text);
        header.setFont(defaultHeaderFont);
        header.setBounds(x, y, width, height);
        add(header);
        return header;
    }
    
    public JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(defaultFont);
        button.setBounds(x, y, width, height);
        add(button);
        return button;
    }
    
    public JButton createButton(String text, int x, int y) {
        return createButton(text, x, y, 200, 30);
    }

    public JTextField createTextField(int x, int y, int width, int height) {
        JTextField textField = new JTextField();
        textField.setFont(defaultFont);
        textField.setBounds(x, y, width, height);
        add(textField);
        return textField;
    }

    public JTextField createTextField(int x, int y) {
        return createTextField(x, y, 200, 30);
    }

    public JPasswordField createPasswordField(int x, int y, int width, int height) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(defaultFont);
        passwordField.setBounds(x, y, width, height);
        add(passwordField);
        return passwordField;
    }

    public JPasswordField createPasswordField(int x, int y) {
        return createPasswordField(x, y, 200, 30);
    }

    public <T> JComboBox<T> createComboBox(T[] items, int x, int y, int width, int height) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(defaultFont);
        comboBox.setBounds(x, y, width, height);
        add(comboBox);
        return comboBox;
    }

    public <T> JComboBox<T> createComboBox(T[] items, int x, int y) {
        return createComboBox(items, x, y, 200, 30);
    }

    public JTextArea createTextArea(int x, int y, int width, int height) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(defaultFont);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(x, y, width, height);
        add(scrollPane);
        return textArea;
    }
    
    // DIALOG SCREEN METHODS
    
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
    
    // DESIGN METHODS FOR COMPONENTS
    
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
}