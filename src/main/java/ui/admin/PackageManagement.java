package ui.admin;

import common.Screen;
import javax.swing.JButton;

public class PackageManagement extends Screen {
    public PackageManagement(){
        super("Package Management", 960, 560);
        
        JButton backBtn = createButton("Back to Dashboard", 40, 375, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));
    }
}