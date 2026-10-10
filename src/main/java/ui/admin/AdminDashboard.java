package ui.admin;

import common.Screen;
import common.Session;
import ui.Homepage;

import javax.swing.JButton;

public class AdminDashboard extends Screen {

    public AdminDashboard() {
        super("Admin Dashboard");

        // Header & welcome subtitle
        createHeader("Admin Dashboard", 80, 40, 600, 35);
        String welcome = "Welcome to the Admin Portal";
        if (Session.getUsername() != null) {
            welcome += ", " + Session.getUsername() + " (" + Session.getUserRole() + ")";
        }
        createLabel(welcome, 80, 80, 600, 25);

        // Core Management Navigation Buttons (Column 1)
        JButton destBtn = createButton("Manage Destinations", 80, 130, 230, 40);
        destBtn.addActionListener(e -> navigateTo(new DestinationManagement()));

        JButton pkgBtn = createButton("Manage Packages", 80, 185, 230, 40);
        pkgBtn.addActionListener(e -> navigateTo(new PackageManagement()));

        JButton bookBtn = createButton("Manage Bookings", 80, 240, 230, 40);
        bookBtn.addActionListener(e -> navigateTo(new BookingManagement()));

        // Core Management Navigation Buttons (Column 2)
        JButton custBtn = createButton("Manage Customers", 330, 130, 230, 40);
        custBtn.addActionListener(e -> navigateTo(new CustomerManagement()));

        JButton salesBtn = createButton("Sales & Reports", 330, 185, 230, 40);
        salesBtn.addActionListener(e -> navigateTo(new SalesManagement()));

        JButton adminMgmtBtn = createButton("Admin Accounts", 330, 240, 230, 40);
        adminMgmtBtn.addActionListener(e -> navigateTo(new AdminManagement()));

        // Logout Button
        JButton logoutBtn = createButton("Log Out", 80, 310, 230, 40);
        logoutBtn.addActionListener(e -> {
            if (confirm("Are you sure you want to log out?")) {
                Session.clear();
                navigateTo(new Homepage());
            }
        });
    }
}
