package ui;

import common.Screen;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Homepage extends Screen {
    public Homepage() {
        super("Travel Agency");

        getContentPane().setBackground(new Color(173, 216, 230));

       
        JLabel title = createHeader("Travel Agency", 50, 40, 400, 40);
        setFont(title, "Arial", Font.BOLD, 28);
        setTextColor(title, new Color(30, 60, 90));

       
        JLabel loginLabel = createLabel("MagLogin", 50, 150, 150, 40);
        setFont(loginLabel, "Arial", Font.BOLD, 16);

        JButton loginBtn = createButton("Login", 220, 150, 200, 40);
        styleButton(loginBtn, new Color(0, 120, 215));
        loginBtn.addActionListener(e -> {
            navigateTo(new UserLogin());
        });

      
        JLabel registerLabel = createLabel("MagRegister", 50, 230, 150, 40);
        setFont(registerLabel, "Arial", Font.BOLD, 16);

        JButton registerBtn = createButton("Register", 220, 230, 200, 40);
        styleButton(registerBtn, new Color(40, 167, 69));
        registerBtn.addActionListener(e -> {
            navigateTo(new UserRegister());
        });

     
        JButton dapUpBtn = createButton("DAP UP NIGGAAA", 220, 310, 200, 40);
        styleButton(dapUpBtn, new Color(120, 120, 120));
        dapUpBtn.addActionListener(e -> {
            showInfo("NICE ONE GANG! NIGGAA CUHH");
        });
    }

    private void styleButton(JButton button, Color borderColor) {
        setFont(button, "Arial", Font.BOLD, 14);
        button.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        button.setFocusPainted(false);
    }
}