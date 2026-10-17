package common;

import dao.AdminAccountDAO;
import dao.BookingDAO;
import dao.BookingDAO.BookingRecord;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;
import dao.PaymentDAO;
import dao.TravelPackageDAO;
import dao.TravelPackageDAO.PackageRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fills the database with comprehensive starting data:
 * - Admin and Manager accounts
 * - Registered customer accounts
 * - Tourist destinations (with image paths)
 * - Tour packages (with pricing, capacities, and image paths)
 * - Sample reservations and manifest travelers
 * - Financial transaction records and revenue entries
 *
 * Safe to run more than once: records that already exist are automatically skipped.
 */
public class DatabaseSeeder {

    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                ensureSchemaCompatibility(con);
                seedAdmins(con);
                seedCustomers(con);
                con.commit(); // commit accounts first

                Map<String, Integer> destMap = seedDestinations();
                Map<String, Integer> pkgMap = seedPackages(destMap);
                seedBookingsAndPayments(pkgMap);

                System.out.println("=================================================");
                System.out.println("Database seeding completed successfully!");
                System.out.println("=================================================");
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ensureSchemaCompatibility(Connection con) {
        try {
            DatabaseMetaData md = con.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, "PAYMENT", "DATE")) {
                if (rs.next()) {
                    try (Statement st = con.createStatement()) {
                        st.executeUpdate("RENAME COLUMN payment.\"DATE\" TO paymentDate");
                        System.out.println("Migrated legacy column payment.DATE to paymentDate.");
                    }
                }
            }
        } catch (Exception ignored) {}
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

    private static Map<String, Integer> seedDestinations() throws Exception {
        DestinationDAO destinationDAO = new DestinationDAO();
        List<DestinationRecord> existing = destinationDAO.getAllDestinations();
        Map<String, Integer> destMap = new HashMap<>();

        for (DestinationRecord d : existing) {
            destMap.put(d.name, d.destinationID);
        }

        // { name, country, description, imagePath }
        String[][] destinations = {
            {"Boracay Island", "Philippines", "World-famous white sand beaches, turquoise waters, vibrant nightlife, and exciting water sports.", "dest_boracay.jpg"},
            {"El Nido, Palawan", "Philippines", "Limestone cliffs, crystal-clear hidden lagoons, and UNESCO-recognized marine sanctuaries.", "dest_palawan.jpg"},
            {"Banaue Rice Terraces", "Philippines", "Hand-carved mountain terraces built 2,000 years ago by ancestors of the indigenous people.", "dest_banaue.jpg"},
            {"Tokyo", "Japan", "Dynamic metropolis blending futuristic skyscrapers, neon-lit streets, and tranquil historic shrines.", "dest_tokyo.jpg"},
            {"Kyoto", "Japan", "The cultural heart of Japan with peaceful bamboo groves, zen rock gardens, and traditional tea houses.", "dest_kyoto.jpg"},
        };

        for (String[] d : destinations) {
            String name = d[0];
            if (destMap.containsKey(name)) {
                System.out.println("Destination '" + name + "' already exists, skipped.");
                continue;
            }
            int id = destinationDAO.addDestination(name, d[1], d[2], d[3]);
            if (id > 0) {
                destMap.put(name, id);
                System.out.println("Destination '" + name + "' created (ID " + id + ").");
            }
        }
        return destMap;
    }

    private static Map<String, Integer> seedPackages(Map<String, Integer> destMap) throws Exception {
        TravelPackageDAO packageDAO = new TravelPackageDAO();
        List<PackageRecord> existing = packageDAO.getAllPackages();
        Map<String, Integer> pkgMap = new HashMap<>();

        for (PackageRecord p : existing) {
            pkgMap.put(p.name, p.packageID);
        }

        // { destinationName, packageName, description, price, min, max, imagePath }
        Object[][] packages = {
            {"Boracay Island", "Boracay 4D3N Sun & Sand Getaway", "All-inclusive beachfront hotel stay with daily island buffet breakfast and sunset paraw sailing.", new BigDecimal("12500.00"), 1, 6, "pkg_boracay.jpg"},
            {"El Nido, Palawan", "El Nido Lagoons & Coral Reef Explorer", "4-day expedition featuring Tour A and Tour C, kayaking in Big Lagoon, and premium seafood beach dinner.", new BigDecimal("16800.00"), 2, 10, "pkg_palawan.jpg"},
            {"Banaue Rice Terraces", "Ifugao Cultural Heritage & Mountain Trek", "Guided trek through Batad amphitheater terraces with homestay experience and native weaving tour.", new BigDecimal("8900.00"), 2, 8, "pkg_banaue.jpg"},
            {"Tokyo", "Tokyo Neon Highlights & Modern Culture", "5-day city pass, Shibuya sky observatory, Akihabara tech tour, and authentic Tsukiji food crawl.", new BigDecimal("45000.00"), 1, 5, "pkg_tokyo.jpg"},
            {"Kyoto", "Kyoto Ancient Temples & Bamboo Forest", "Full-day kimono experience, Fushimi Inari shrine sunrise tour, and traditional matcha ceremony.", new BigDecimal("38500.00"), 1, 6, "pkg_kyoto.jpg"},
        };

        for (Object[] p : packages) {
            String destName = (String) p[0];
            String pkgName = (String) p[1];
            String desc = (String) p[2];
            BigDecimal price = (BigDecimal) p[3];
            int min = (Integer) p[4];
            int max = (Integer) p[5];
            String img = (String) p[6];

            if (pkgMap.containsKey(pkgName)) {
                System.out.println("Package '" + pkgName + "' already exists, skipped.");
                continue;
            }

            Integer destId = destMap.get(destName);
            if (destId != null) {
                int id = packageDAO.addPackage(destId, pkgName, desc, img, price, min, max);
                if (id > 0) {
                    pkgMap.put(pkgName, id);
                    System.out.println("Package '" + pkgName + "' created (ID " + id + ").");
                }
            }
        }
        return pkgMap;
    }

    private static void seedBookingsAndPayments(Map<String, Integer> pkgMap) throws Exception {
        BookingDAO bookingDAO = new BookingDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        CustomerDAO customerDAO = new CustomerDAO();

        // Get customerAccount IDs for juan and maria
        int juanAccId = -1;
        int mariaAccId = -1;

        try (Connection con = DBConnection.getConnection()) {
            List<CustomerRecord> customers = customerDAO.getAllCustomers(con);
            for (CustomerRecord c : customers) {
                if ("juan".equalsIgnoreCase(c.username)) {
                    juanAccId = c.customerAccountID;
                } else if ("maria".equalsIgnoreCase(c.username)) {
                    mariaAccId = c.customerAccountID;
                }
            }
        }

        if (juanAccId == -1 && mariaAccId == -1) {
            System.out.println("No customer accounts available to link sample bookings.");
            return;
        }

        List<BookingRecord> existingBookings = bookingDAO.getAllBookings();
        if (!existingBookings.isEmpty()) {
            System.out.println("Sample bookings already present (" + existingBookings.size() + " records), skipped.");
            return;
        }

        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;

        // 1. Juan's Booking for Boracay (Confirmed)
        Integer boracayPkgId = pkgMap.get("Boracay 4D3N Sun & Sand Getaway");
        if (juanAccId > 0 && boracayPkgId != null) {
            Timestamp travelDate = new Timestamp(now + (14 * day));
            int bId = bookingDAO.createBooking(juanAccId, boracayPkgId, travelDate, "Confirmed");
            if (bId > 0) {
                System.out.println("Booking # " + bId + " (Boracay) created for Juan.");
                // Add Travelers
                bookingDAO.addTraveler(bId, "Juan", "Dela Cruz", Date.valueOf("1999-05-12"), "09171234567");
                bookingDAO.addTraveler(bId, "Ana", "Dela Cruz", Date.valueOf("2001-08-23"), "09187654321");
                // Add Payment
                paymentDAO.recordPayment(bId, new BigDecimal("25000.00"), "GCash", "GCASH-REF-894120");
                System.out.println("Recorded GCash payment PHP 25,000.00 for Booking # " + bId);
            }
        }

        // 2. Maria's Booking for Palawan (Confirmed)
        Integer palawanPkgId = pkgMap.get("El Nido Lagoons & Coral Reef Explorer");
        if (mariaAccId > 0 && palawanPkgId != null) {
            Timestamp travelDate = new Timestamp(now + (30 * day));
            int bId = bookingDAO.createBooking(mariaAccId, palawanPkgId, travelDate, "Confirmed");
            if (bId > 0) {
                System.out.println("Booking # " + bId + " (Palawan) created for Maria.");
                // Add Traveler
                bookingDAO.addTraveler(bId, "Maria", "Clara", Date.valueOf("1996-11-04"), "09209876543");
                // Add Payment
                paymentDAO.recordPayment(bId, new BigDecimal("16800.00"), "Credit Card", "VISA-AUTH-441290");
                System.out.println("Recorded Credit Card payment PHP 16,800.00 for Booking # " + bId);
            }
        }

        // 3. Juan's Booking for Tokyo (Pending)
        Integer tokyoPkgId = pkgMap.get("Tokyo Neon Highlights & Modern Culture");
        if (juanAccId > 0 && tokyoPkgId != null) {
            Timestamp travelDate = new Timestamp(now + (60 * day));
            int bId = bookingDAO.createBooking(juanAccId, tokyoPkgId, travelDate, "Pending");
            if (bId > 0) {
                System.out.println("Booking # " + bId + " (Tokyo) created for Juan.");
                bookingDAO.addTraveler(bId, "Juan", "Dela Cruz", Date.valueOf("1999-05-12"), "09171234567");
            }
        }
    }
}