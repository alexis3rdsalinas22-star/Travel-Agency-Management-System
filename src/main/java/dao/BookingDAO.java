package dao;

import common.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookingDAO {

    // Holds one booking row together with its customer and package details
    public static class BookingRecord {
        public int bookingID;
        public int customerAccountID;
        public String customerName;
        public int packageID;
        public String packageName;
        public String destinationName;
        public java.math.BigDecimal pricePerTraveller;
        public String status;
        public Timestamp bookingDate;
        public Timestamp travelDate;
    }

    private static final List<String> VALID_STATUSES =
            Arrays.asList("Pending", "Confirmed", "Cancelled", "Completed");

    // Shared SELECT used by getAllBookings() and searchBooking()
    private static final String BASE_SELECT =
            "SELECT b.bookingID, b.customerAccountID, b.packageID, b.status, "
          + "b.bookingDate, b.travelDate, "
          + "c.firstName, c.middleName, c.lastName, "
          + "p.name AS packageName, p.pricePerTraveller, d.name AS destinationName "
          + "FROM booking b "
          + "JOIN customeraccount ca ON b.customerAccountID = ca.customerAccountID "
          + "JOIN customer c ON ca.customerID = c.customerID "
          + "JOIN package p ON b.packageID = p.packageID "
          + "LEFT JOIN destination d ON p.destinationID = d.destinationID ";

    // Creates a booking with status 'Pending' and returns the new bookingID
    public int createBooking(int customerAccountId, int packageId, Timestamp travelDate)
            throws SQLException {
        String sql = "INSERT INTO booking (customerAccountID, packageID, status, bookingDate, travelDate) "
                   + "VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerAccountId);
            ps.setInt(2, packageId);
            ps.setString(3, "Pending");
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(5, travelDate);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // Returns every booking with customer name and package name (JOIN)
    public List<BookingRecord> getAllBookings() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY b.bookingDate DESC";
        List<BookingRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Changes a booking's status: Pending, Confirmed, Cancelled or Completed
    public boolean updateBookingStatus(int bookingId, String newStatus) throws SQLException {
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        String sql = "UPDATE booking SET status = ? WHERE bookingID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    // Searches by booking ID, customer name, package name or status
    public List<BookingRecord> searchBooking(String keyword) throws SQLException {
        String sql = BASE_SELECT
                   + "WHERE b.bookingID = ? "
                   + "OR LOWER(c.firstName) LIKE ? "
                   + "OR LOWER(c.middleName) LIKE ? "
                   + "OR LOWER(c.lastName) LIKE ? "
                   + "OR LOWER(p.name) LIKE ? "
                   + "OR LOWER(b.status) LIKE ? "
                   + "ORDER BY b.bookingDate DESC";

        String like = "%" + keyword.trim().toLowerCase() + "%";

        // If the keyword isn't a number, use -1 so the bookingID match is skipped
        int id = -1;
        try {
            id = Integer.parseInt(keyword.trim());
        } catch (NumberFormatException ignored) { }

        List<BookingRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public static class TravelerRecord {
        public int travelerID;
        public int bookingID;
        public String firstName;
        public String lastName;
        public java.sql.Date dateOfBirth;
        public String contactNumber;
    }

    // Creates a booking with custom status (e.g. for seeder)
    public int createBooking(int customerAccountId, int packageId, Timestamp travelDate, String status)
            throws SQLException {
        String sql = "INSERT INTO booking (customerAccountID, packageID, status, bookingDate, travelDate) "
                   + "VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerAccountId);
            ps.setInt(2, packageId);
            ps.setString(3, status != null ? status : "Pending");
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(5, travelDate);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<TravelerRecord> getTravelersByBooking(int bookingId) throws SQLException {
        String sql = "SELECT travelerID, bookingID, firstName, lastName, dateOfBirth, contactNumber "
                   + "FROM traveler WHERE bookingID = ? ORDER BY travelerID";
        List<TravelerRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TravelerRecord t = new TravelerRecord();
                    t.travelerID = rs.getInt("travelerID");
                    t.bookingID = rs.getInt("bookingID");
                    t.firstName = rs.getString("firstName");
                    t.lastName = rs.getString("lastName");
                    t.dateOfBirth = rs.getDate("dateOfBirth");
                    t.contactNumber = rs.getString("contactNumber");
                    list.add(t);
                }
            }
        }
        return list;
    }

    public int addTraveler(int bookingId, String firstName, String lastName, java.sql.Date dateOfBirth, String contactNumber)
            throws SQLException {
        String sql = "INSERT INTO traveler (bookingID, firstName, lastName, dateOfBirth, contactNumber) "
                   + "VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setDate(4, dateOfBirth);
            ps.setString(5, contactNumber);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<BookingRecord> getBookingsByCustomer(int customerAccountId, int customerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.customerAccountID = ? OR ca.customerID = ? ORDER BY b.bookingDate DESC";
        List<BookingRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerAccountId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // Converts one result row into a BookingRecord
    private BookingRecord mapRow(ResultSet rs) throws SQLException {
        BookingRecord r = new BookingRecord();
        r.bookingID = rs.getInt("bookingID");
        r.customerAccountID = rs.getInt("customerAccountID");
        r.packageID = rs.getInt("packageID");
        r.status = rs.getString("status");
        r.bookingDate = rs.getTimestamp("bookingDate");
        r.travelDate = rs.getTimestamp("travelDate");
        r.packageName = rs.getString("packageName");
        try {
            r.destinationName = rs.getString("destinationName");
        } catch (SQLException ignored) {}
        try {
            r.pricePerTraveller = rs.getBigDecimal("pricePerTraveller");
        } catch (SQLException ignored) {}

        String middle = rs.getString("middleName");
        r.customerName = rs.getString("firstName")
                + (middle == null || middle.isEmpty() ? " " : " " + middle + " ")
                + rs.getString("lastName");
        return r;
    }
}