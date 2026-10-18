package common;

import dao.AdminAccountDAO;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import dao.CustomerDAO.CustomerRecord;
import dao.DestinationDAO;
import dao.DestinationDAO.DestinationRecord;
import dao.TravelPackageDAO;
import dao.TravelPackageDAO.PackageRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the database with essential foundational data:
 * - Admin and Manager accounts
 * - Sample customer accounts
 * - Curated tourist destinations
 * - Tour packages with pricing and capacities
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
                seedPackages(destMap);

                System.out.println("=================================================");
                System.out.println("Essential database seeding completed successfully!");
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

        List<CustomerRecord> existing = customerDAO.getAllCustomers(con);
        Map<String, Integer> existingUsernames = new HashMap<>();
        for (CustomerRecord c : existing) {
            if (c.username != null) {
                existingUsernames.put(c.username.toLowerCase(), c.customerID);
            }
        }

        // { username, password, firstName, middleName, lastName, email, sex, age }
        Object[][] customers = {
            {"juan",  "juan123",  "Juan",  "Santos", "Dela Cruz", "juan@example.com",  "Male",   28},
            {"maria", "maria123", "Maria", "Reyes",  "Clara",     "maria@example.com", "Female", 25},
        };

        for (Object[] c : customers) {
            String username = (String) c[0];
            if (existingUsernames.containsKey(username.toLowerCase())) {
                System.out.println("Customer account '" + username + "' already exists, skipped.");
                continue;
            }

            int custId = customerDAO.insertCustomer(con, (String) c[2], (String) c[3], (String) c[4],
                    (String) c[5], (String) c[6], (Integer) c[7]);
            if (custId > 0) {
                accountDAO.insertAccount(con, custId, username, (String) c[1]);
                System.out.println("Customer account '" + username + "' created (Customer ID " + custId + ").");
            }
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
            {"Boracay Island", "Philippines", "World-famous white sand beaches, turquoise waters, vibrant nightlife, and water sports.", "dest_boracay.jpg"},
            {"El Nido, Palawan", "Philippines", "Limestone cliffs, crystal-clear hidden lagoons, and UNESCO-recognized marine ecosystems.", "dest_palawan.jpg"},
            {"Banaue Rice Terraces", "Philippines", "Hand-carved mountain terraces built 2,000 years ago by ancestral Ifugao tribes.", "dest_banaue.jpg"},
            {"Tokyo", "Japan", "Dynamic metropolis blending futuristic skyscrapers, neon-lit districts, historic shrines, and world-class culinary scenes.", "dest_tokyo.jpg"},
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

    private static void seedPackages(Map<String, Integer> destMap) throws Exception {
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
    }
}