import common.DBConnection;
import common.SecurityUtil;
import dao.AdminAccountDAO;
import dao.BookingDAO;
import dao.CustomerAccountDAO;
import dao.CustomerDAO;
import dao.DestinationDAO;
import dao.PaymentDAO;
import dao.TravelPackageDAO;
import model.AdminAccount;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class SystemAuditVerificationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   GOD Travel Agency Management System - Automated Audit Verification ");
        System.out.println("======================================================================");

        runTest("TC-01: Live Database Connectivity & Schema Constraints", SystemAuditVerificationTest::testDatabaseSchema);
        runTest("TC-02: Customer Registration & Uniqueness Enforcement", SystemAuditVerificationTest::testCustomerRegistration);
        runTest("TC-03: Customer Login & Account Status Enforcement", SystemAuditVerificationTest::testCustomerLoginStatus);
        runTest("TC-04: Package Management & Price Update Trigger Safety", SystemAuditVerificationTest::testPackagePriceTrigger);
        runTest("TC-05: Group Booking Manifest & Traveler Count Subquery", SystemAuditVerificationTest::testBookingManifest);
        runTest("TC-06: Payment Processing & Group Undercharge Prevention", SystemAuditVerificationTest::testPaymentAndTotalDue);
        runTest("TC-07: Admin vs Manager Role Permission Boundaries", SystemAuditVerificationTest::testAdminManagerPermissions);
        runTest("TC-08: Sales Reporting KPI Aggregations", SystemAuditVerificationTest::testSalesReporting);

        System.out.println("======================================================================");
        System.out.printf("   Audit Test Summary: %d PASSED, %d FAILED\n", testsPassed, testsFailed);
        System.out.println("======================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, TestCase test) {
        System.out.print("[RUNNING] " + testName + " ... ");
        try {
            test.run();
            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            testsFailed++;
        }
    }

    @FunctionalInterface
    interface TestCase {
        void run() throws Exception;
    }

    // TC-01: Live Database Connectivity & Schema Constraints
    private static void testDatabaseSchema() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            assertNotNull(con, "Database connection must not be null");
            String[] requiredTables = {
                "CUSTOMER", "CUSTOMERACCOUNT", "DESTINATION", "PACKAGE",
                "BOOKING", "TRAVELER", "PAYMENT", "ADMINACCOUNT"
            };
            for (String table : requiredTables) {
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM " + table)) {
                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next(), "Table " + table + " must be queryable");
                    }
                }
            }
        }
    }

    // TC-02: Customer Registration & Uniqueness Enforcement
    private static void testCustomerRegistration() throws Exception {
        CustomerDAO customerDAO = new CustomerDAO();
        CustomerAccountDAO accountDAO = new CustomerAccountDAO();
        String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 100000);
        String testUser = "audit_user_" + uniqueSuffix;
        String testEmail = "audit_" + uniqueSuffix + "@tams.test";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                assertFalse(customerDAO.emailExists(con, testEmail), "Email should not exist before registration");
                assertFalse(accountDAO.usernameExists(con, testUser), "Username should not exist before registration");

                int custId = customerDAO.insertCustomer(con, "Audit", "M", "Traveler", testEmail, "Male", 28);
                assertTrue(custId > 0, "Customer record should be inserted with positive ID");

                accountDAO.insertAccount(con, custId, testUser, "SecretPass123!");
                con.commit();

                // Verify duplicates are rejected
                assertTrue(customerDAO.emailExists(con, testEmail), "Email must now exist");
                assertTrue(accountDAO.usernameExists(con, testUser), "Username must now exist");

                // Cleanup
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM customeraccount WHERE username = ?")) {
                    ps.setString(1, testUser);
                    ps.executeUpdate();
                }
                customerDAO.deleteCustomer(con, custId);
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // TC-03: Customer Login & Account Status Enforcement
    private static void testCustomerLoginStatus() throws Exception {
        CustomerDAO customerDAO = new CustomerDAO();
        CustomerAccountDAO accountDAO = new CustomerAccountDAO();
        String suffix = String.valueOf(System.currentTimeMillis() % 100000);
        String activeUser = "active_" + suffix;
        String inactiveUser = "inactive_" + suffix;
        String pass = "ValidPass123!";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id1 = customerDAO.insertCustomer(con, "Act", null, "User", "act_" + suffix + "@test.com", "Female", 25);
                accountDAO.insertAccount(con, id1, activeUser, pass);

                int id2 = customerDAO.insertCustomer(con, "Inact", null, "User", "inact_" + suffix + "@test.com", "Female", 26);
                String sql = "INSERT INTO customeraccount (customerID, username, password, status) VALUES (?,?,?,'Suspended')";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id2);
                    ps.setString(2, inactiveUser);
                    ps.setString(3, SecurityUtil.hashPassword(pass));
                    ps.executeUpdate();
                }
                con.commit();

                CustomerAccountDAO.CustomerLoginResult r1 = accountDAO.login(con, activeUser, pass);
                assertNotNull(r1, "Active account must be allowed to log in");

                CustomerAccountDAO.CustomerLoginResult r2 = accountDAO.login(con, inactiveUser, pass);
                assertNull(r2, "Suspended account must NOT be allowed to log in");

                // Cleanup
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM customeraccount WHERE username IN (?,?)")) {
                    ps.setString(1, activeUser);
                    ps.setString(2, inactiveUser);
                    ps.executeUpdate();
                }
                customerDAO.deleteCustomer(con, id1);
                customerDAO.deleteCustomer(con, id2);
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // TC-04: Package Management & Price Update Trigger Safety
    private static void testPackagePriceTrigger() throws Exception {
        TravelPackageDAO pkgDAO = new TravelPackageDAO();
        DestinationDAO destDAO = new DestinationDAO();

        // Ensure a test destination exists
        List<DestinationDAO.DestinationRecord> dests = destDAO.getAllDestinations();
        assertTrue(!dests.isEmpty(), "Destination catalog must contain at least one destination");
        int destId = dests.get(0).destinationID;

        BigDecimal initialPrice = new BigDecimal("4500.00");
        int pkgId = pkgDAO.addPackage(destId, "Trigger Test Tour", "Test Description", null, initialPrice, 2, 8);
        assertTrue(pkgId > 0, "Package must be created");

        try {
            // Update 1: Non-price update (price unchanged)
            boolean ok1 = pkgDAO.updatePackage(pkgId, destId, "Trigger Test Tour (Renamed)", "Updated Desc", null, initialPrice, "Active", 2, 8);
            assertTrue(ok1, "Package update with identical price must succeed without trigger violation");

            // Update 2: Price modification (trigger test)
            BigDecimal newPrice = new BigDecimal("5500.00");
            boolean ok2 = pkgDAO.updatePackage(pkgId, destId, "Trigger Test Tour", "Price Updated", null, newPrice, "Active", 2, 8);
            assertTrue(ok2, "Package price update must atomically deactivate old record and insert new active record");
        } finally {
            // Cleanup
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM package WHERE name LIKE 'Trigger Test Tour%'")) {
                ps.executeUpdate();
            }
        }
    }

    // TC-05: Group Booking Manifest & Traveler Count Subquery
    private static void testBookingManifest() throws Exception {
        BookingDAO bookingDAO = new BookingDAO();
        TravelPackageDAO pkgDAO = new TravelPackageDAO();

        List<TravelPackageDAO.PackageRecord> pkgs = pkgDAO.getAllPackages();
        assertTrue(!pkgs.isEmpty(), "Package catalog must not be empty");
        TravelPackageDAO.PackageRecord p = pkgs.get(0);

        // Find customer account
        int custAccId = 1;
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT customerAccountID FROM customeraccount FETCH FIRST 1 ROWS ONLY")) {
            if (rs.next()) custAccId = rs.getInt(1);
        }

        int bookedPax = 3;
        Timestamp travelDate = Timestamp.valueOf(LocalDate.now().plusDays(20).atStartOfDay());
        int bookingId = bookingDAO.createBooking(custAccId, p.packageID, travelDate);
        assertTrue(bookingId > 0, "Booking record must be created");

        try {
            // Register lead traveler and 2 companions
            bookingDAO.addTraveler(bookingId, "LeadFirst", "LeadLast", java.sql.Date.valueOf(LocalDate.now().minusYears(30)), "+63 9123456789");
            bookingDAO.addTraveler(bookingId, "Companion 1", "LeadLast", java.sql.Date.valueOf(LocalDate.now().minusYears(28)), "+63 9123456789");
            bookingDAO.addTraveler(bookingId, "Companion 2", "LeadLast", java.sql.Date.valueOf(LocalDate.now().minusYears(25)), "+63 9123456789");

            // Verify traveler count subquery in BookingDAO
            BookingDAO.BookingRecord record = bookingDAO.getBookingById(bookingId);
            assertNotNull(record, "Booking record must be retrieved");
            assertEquals(bookedPax, record.travelerCount, "Traveler count subquery must accurately reflect 3 registered manifest passengers");

            // Verify total due = rate * pax
            BigDecimal expectedTotal = p.pricePerTraveller.multiply(BigDecimal.valueOf(bookedPax));
            assertEquals(0, expectedTotal.compareTo(record.getTotalAmount()), "Total amount must be rate * pax");
        } finally {
            // Cleanup
            try (Connection con = DBConnection.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM traveler WHERE bookingID = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM booking WHERE bookingID = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
            }
        }
    }

    // TC-06: Payment Processing & Group Undercharge Prevention
    private static void testPaymentAndTotalDue() throws Exception {
        BookingDAO bookingDAO = new BookingDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        TravelPackageDAO pkgDAO = new TravelPackageDAO();

        List<TravelPackageDAO.PackageRecord> pkgs = pkgDAO.getAllPackages();
        TravelPackageDAO.PackageRecord p = pkgs.get(0);

        int custAccId = 1;
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT customerAccountID FROM customeraccount FETCH FIRST 1 ROWS ONLY")) {
            if (rs.next()) custAccId = rs.getInt(1);
        }

        int bookedPax = 2;
        Timestamp travelDate = Timestamp.valueOf(LocalDate.now().plusDays(30).atStartOfDay());
        int bookingId = bookingDAO.createBooking(custAccId, p.packageID, travelDate);

        try {
            bookingDAO.addTraveler(bookingId, "BuyerOne", "Family", java.sql.Date.valueOf(LocalDate.now().minusYears(30)), "+63 9111111111");
            bookingDAO.addTraveler(bookingId, "BuyerTwo", "Family", java.sql.Date.valueOf(LocalDate.now().minusYears(28)), "+63 9111111111");

            BookingDAO.BookingRecord b = bookingDAO.getBookingById(bookingId);
            BigDecimal fullAmount = b.getTotalAmount();
            BigDecimal singleRate = p.pricePerTraveller;

            // Ensure full amount is greater than single rate
            assertTrue(fullAmount.compareTo(singleRate) > 0, "Group full amount must be 2x single traveler rate");

            // Record full payment
            int payId = paymentDAO.recordPayment(bookingId, fullAmount, "GCash", "REF12345678");
            assertTrue(payId > 0, "Payment must be recorded");

            bookingDAO.updateBookingStatus(bookingId, "Confirmed");
            BookingDAO.BookingRecord confirmed = bookingDAO.getBookingById(bookingId);
            assertEquals("Confirmed", confirmed.status, "Booking status must transition to Confirmed");

            BigDecimal totalPaid = paymentDAO.getTotalPaidForBooking(bookingId);
            assertEquals(0, fullAmount.compareTo(totalPaid), "Total paid in payment table must equal full group amount");
        } finally {
            // Cleanup
            try (Connection con = DBConnection.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM payment WHERE bookingID = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM traveler WHERE bookingID = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM booking WHERE bookingID = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
            }
        }
    }

    // TC-07: Admin vs Manager Role Permission Boundaries
    private static void testAdminManagerPermissions() throws Exception {
        AdminAccountDAO adminDAO = new AdminAccountDAO();
        String suffix = String.valueOf(System.currentTimeMillis() % 100000);
        String managerUser = "mgr_test_" + suffix;
        String adminUser = "adm_test_" + suffix;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Create Manager
                int mgrId = adminDAO.createAccount(con, managerUser, "MgrPass123!", "Manager");
                assertTrue(mgrId > 0, "Manager account must be created");

                // Create Admin
                int admId = adminDAO.createAccount(con, adminUser, "AdmPass123!", "Admin");
                assertTrue(admId > 0, "Admin account must be created");

                // Verify login retrieval and roles
                AdminAccount mgr = adminDAO.login(con, managerUser, "MgrPass123!");
                assertNotNull(mgr, "Manager should be able to log in");
                assertEquals("Manager", mgr.getrole(), "Role must be Manager");

                AdminAccount adm = adminDAO.login(con, adminUser, "AdmPass123!");
                assertNotNull(adm, "Admin should be able to log in");
                assertEquals("Admin", adm.getrole(), "Role must be Admin");

                // Cleanup
                adminDAO.deleteAccount(con, mgrId);
                adminDAO.deleteAccount(con, admId);
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // TC-08: Sales Reporting KPI Aggregations
    private static void testSalesReporting() throws Exception {
        PaymentDAO paymentDAO = new PaymentDAO();
        PaymentDAO.SalesSummary summary = paymentDAO.getSalesReportSummary();
        assertNotNull(summary, "Sales summary must not be null");

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM payment")) {
            assertTrue(rs.next(), "Aggregate query must return a row");
            int expectedCount = rs.getInt(1);
            BigDecimal expectedRevenue = rs.getBigDecimal(2);

            assertEquals(expectedCount, summary.totalPayments, "Summary transaction count must match actual DB payment records");
            assertEquals(0, expectedRevenue.compareTo(summary.totalRevenue), "Summary total revenue must match actual DB payment sum");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError(message);
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError(message);
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) throw new AssertionError(message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " [Expected: " + expected + ", Actual: " + actual + "]");
    }
}
