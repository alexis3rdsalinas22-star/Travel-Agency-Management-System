package ui;

import common.Screen;

public class Homepage extends Screen {
    public Homepage() {
        super("Travel Agency");

        createLabel("UserLogin", 20, 300);
        
        createLabel("UserRegister", 20, 500);
        
        createButton("Button", 200, 300).addActionListener(e -> {
            navigateTo(new UserLogin());
        });
        
        createButton("Button", 200, 500).addActionListener(e -> {
            navigateTo(new UserRegister());
        });
        
        createButton("DAP UP", 500, 500).addActionListener(e -> {
            showInfo("NICE ONE GANG!");
        });

    }
}
