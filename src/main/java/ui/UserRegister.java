package ui;

import common.Screen;
import dao.CustomerDAO;

import javax.swing.*;

public class UserRegister extends Screen{
    public UserRegister(){
        super("Account Register", 600, 800);
        
        createHeader("ACCOUNT REGISTER", 125, 50, 400, 50);
        
        createLabel("First Name: ", 50, 100);
        JTextField firstName = createTextField(200, 100);
        
        createLabel("Middle Name: ", 50, 150);
        JTextField middleName = createTextField(200, 150);
        
        createLabel("Last Name: ", 50, 200);
        JTextField lastName = createTextField(200, 200);
        
        createLabel("Email: ", 50, 250);
        JTextField email = createTextField(200, 250);

        createLabel("Age: ", 50, 300);
        JTextField age = createTextField(200, 300);
        
        createLabel("Username: ", 50, 400);
        JTextField username = createTextField(200, 400);

        createLabel("Password: ", 50, 450);
        JPasswordField password = createPasswordField(200, 450);
        
        createButton("Register", 125, 550).addActionListener(e -> {
            
        });
        
        createButton("Go Back", 50, 700).addActionListener(e -> {
            navigateTo(new Homepage());
        });
    }
}
