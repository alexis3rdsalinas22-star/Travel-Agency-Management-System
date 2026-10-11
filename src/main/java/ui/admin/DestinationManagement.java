package ui.admin;

import common.Screen;
import javax.swing.JButton;

public class DestinationManagement extends Screen {
    public DestinationManagement(){
        super("Destination Management", 960, 560);
        
        JButton backBtn = createButton("Back to Dashboard", 40, 375, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));
    }
}