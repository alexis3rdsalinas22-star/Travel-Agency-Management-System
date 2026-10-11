package ui.admin;

import common.Screen;
import javax.swing.JButton;

public class BookingManagement extends Screen {
    public BookingManagement(){
        super("Booking Management", 960, 560);
        
        JButton backBtn = createButton("Back to Dashboard", 40, 375, 310, 35);
        backBtn.addActionListener(e -> navigateTo(new AdminDashboard()));

    }
}