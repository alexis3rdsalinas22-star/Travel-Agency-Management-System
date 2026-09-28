package ui;

import common.Screen;

public class UserLogin extends Screen{
    public UserLogin(){
        super("Account Login");
        
        createButton("Go Back", 100, 10).addActionListener(e -> {
            navigateTo(new Homepage());
        });
    }
}
