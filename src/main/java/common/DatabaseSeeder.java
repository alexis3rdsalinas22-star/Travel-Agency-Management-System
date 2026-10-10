package common;

import dao.AdminAccountDAO;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;

import java.sql.Connection;

/**
 * Fills the database with starting data (admin + sample customer accounts).
 *
 * Everything goes through the DAOs, so passwords are stored hashed (SHA-256) exactly the
 * way the login code expects. Never insert accounts by hand in SQL, or the plain-text
 * password will not match the hash during login.
 *
 * Safe to run more than once: rows that already exist are skipped.
 * Run this file directly (Run File / Shift+F6) after the tables are created.
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                seedAdmins(con);
                seedCustomers(con);
                con.commit();
                System.out.println("Seeding finished.");
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Seeding failed, nothing was saved: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedAdmins(Connection con) throws Exception {
        AdminAccountDAO adminDAO = new AdminAccountDAO();

        // { username, password, role }
        String[][] admins = {
            {"admin",   "admin123",   "Admin"},
            {"manager", "manager123", "Manager"},
        };

        for (String[] a : admins) {
            int id = adminDAO.createAccount(con, a[0], a[1], a[2]);
            System.out.println(id == -1
                    ? "Admin '" + a[0] + "' already exists, skipped."
                    : "Admin '" + a[0] + "' created (ID " + id + ").");
        }
    }

    private static void seedCustomers(Connection con) throws Exception {
        CustomerDAO customerDAO = new CustomerDAO();
        CustomerAccountDAO accountDAO = new CustomerAccountDAO();

        // { first, middle, last, email, sex, age, username, password }
        String[][] customers = {
            {"Juan",  "Santos", "Dela Cruz", "juan@example.com",  "Male",   "25", "juan",  "juan123"},
            {"Maria", "Reyes",  "Clara",     "maria@example.com", "Female", "30", "maria", "maria123"},
        };

        for (String[] c : customers) {
            String username = c[6];
            if (accountDAO.usernameExists(con, username)) {
                System.out.println("Customer account '" + username + "' already exists, skipped.");
                continue;
            }
            int customerId = customerDAO.insertCustomer(
                    con, c[0], c[1], c[2], c[3], c[4], Integer.parseInt(c[5]));
            accountDAO.insertAccount(con, customerId, username, c[7]);
            System.out.println("Customer '" + username + "' created (ID " + customerId + ").");
        }
    }
}