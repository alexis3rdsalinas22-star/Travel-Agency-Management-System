package ui;

import common.Screen;

public class UserRegister extends Screen{
    public UserRegister(){
        super("Account Register");
        
        createButton("Go Back", 100, 10).addActionListener(e -> {
            navigateTo(new Homepage());
        });
    }
}
