package ui;

import common.Screen;

import javax.swing.*;

public class UserLogin extends Screen{
    public UserLogin(){
        super("Account Login", 600, 800);
        
        createHeader("ACCOUNT LOGIN", 150, 100, 200, 50);
        
        createLabel("Username: ", 50, 200);
        JTextField username = createTextField(200, 200);

        createLabel("Password: ", 50, 250);
        JPasswordField password = createPasswordField(200, 250);
        
        createButton("Login", 125, 350).addActionListener(e -> {
            
        });
        
        createButton("Go Back", 50, 500).addActionListener(e -> {
            navigateTo(new Homepage());
        });
    }
}
