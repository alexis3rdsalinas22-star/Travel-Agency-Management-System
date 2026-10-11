package ui.admin;

import common.Screen;
import javax.swing.JButton;

public class CustomerManagement extends Screen {
    public CustomerManagement(){
        super("Customer Management", 960, 560);
        
        JButton backBtn = createButton("Back to Dashboard", 40, 375, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));
    }
}