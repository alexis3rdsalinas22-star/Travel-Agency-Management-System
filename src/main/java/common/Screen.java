package common;

import java.awt.Color;
import java.awt.Font;
import javax.swing.*;

public class Screen extends JFrame {
    public Screen(String title, int width, int height) {
        setTitle(title);
        setSize(width, height);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }
    
    public Screen(String title){
        setTitle(title);
        setSize(960, 540);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void showScreen() {
        setVisible(true);
    }

    public void hideScreen() {
        setVisible(false);
    }

    public void closeScreen() {
        dispose();
    }
    
    public void addComponent(JComponent component, int x, int y, int width, int height) {
        component.setBounds(x, y, width, height);
        add(component);
    }
    
    public void centerScreen() {
        setLocationRelativeTo(null);
    }
    
    public void setFullscreen(boolean choice){
        if(choice){
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setExtendedState(JFrame.NORMAL);
        }
    }
    
    public void toggleFullscreen() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
    
    public void setBackgroundColor(Color color) {
        getContentPane().setBackground(color);
    }

    public void navigateTo(Screen nextScreen) {
        nextScreen.showScreen();
        closeScreen();
    }
    
    // Swing components
    
    public JLabel createLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, width, height);
        add(label);
        return label;
    }
    
    public JLabel createLabel(String text, int x, int y) {
        return createLabel(text, x, y, 200, 30);
    }

    public JLabel createHeader(String text, int x, int y, int width, int height) {
        JLabel header = new JLabel(text);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBounds(x, y, width, height);
        add(header);
        return header;
    }
    
    public JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        add(button);
        return button;
    }
    
    public JButton createButton(String text, int x, int y) {
        return createButton(text, x, y, 200, 30);
    }

    public JTextField createTextField(int x, int y, int width, int height) {
        JTextField textField = new JTextField();
        textField.setBounds(x, y, width, height);
        add(textField);
        return textField;
    }

    public JTextField createTextField(int x, int y) {
        return createTextField(x, y, 200, 30);
    }

    public JPasswordField createPasswordField(int x, int y, int width, int height) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(x, y, width, height);
        add(passwordField);
        return passwordField;
    }

    public JPasswordField createPasswordField(int x, int y) {
        return createPasswordField(x, y, 200, 30);
    }

    public <T> JComboBox<T> createComboBox(T[] items, int x, int y, int width, int height) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setBounds(x, y, width, height);
        add(comboBox);
        return comboBox;
    }

    public <T> JComboBox<T> createComboBox(T[] items, int x, int y) {
        return createComboBox(items, x, y, 200, 30);
    }

    public JTextArea createTextArea(int x, int y, int width, int height) {
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(x, y, width, height);
        add(scrollPane);
        return textArea;
    }
    
    // DIalog Screens
    
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
}