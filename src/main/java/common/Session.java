package common;

import model.AdminAccount;

public class Session {
    private static AdminAccount currentAdmin;
    private static int customerId = -1;
    private static int customerAccountId = -1;
    private static String username;
    private static String userRole;

    private Session() {}

    public static void setAdminSession(AdminAccount admin) {
        currentAdmin = admin;
        customerId = -1;
        customerAccountId = -1;
        if (admin != null) {
            username = admin.getusername();
            userRole = admin.getrole();
        } else {
            username = null;
            userRole = null;
        }
    }

    public static void setCustomerSession(int custId, int custAccountId, String user) {
        currentAdmin = null;
        customerId = custId;
        customerAccountId = custAccountId;
        username = user;
        userRole = "Customer";
    }

    public static void setCustomerSession(int custId, String user) {
        setCustomerSession(custId, -1, user);
    }

    public static AdminAccount getCurrentAdmin() {
        return currentAdmin;
    }

    public static int getCustomerId() {
        return customerId;
    }

    public static int getCustomerAccountId() {
        return customerAccountId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static boolean isLoggedIn() {
        return currentAdmin != null || customerId != -1;
    }

    public static boolean isAdmin() {
        return currentAdmin != null;
    }

    public static boolean isCustomer() {
        return "Customer".equalsIgnoreCase(userRole);
    }

    public static void clear() {
        currentAdmin = null;
        customerId = -1;
        customerAccountId = -1;
        username = null;
        userRole = null;
    }
}
