package dao;

import common.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TravelerDAO {

    
    public static class TravelerRecord {
        public int travelerID;
        public int bookingID;
        public String firstName;
        public String lastName;
        public Date dateOfBirth;
        public String contactNumber;
    }

    private static final String BASE_SELECT =
            "SELECT travelerID, bookingID, firstName, lastName, dateOfBirth, contactNumber "
          + "FROM traveler ";

    
    public int addTraveler(int bookingId, String firstName, String lastName,
                           Date dateOfBirth, String contactNumber) throws SQLException {
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

   
    public TravelerRecord getTravelerById(int travelerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE travelerID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, travelerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    
    public List<TravelerRecord> getTravelersByBooking(int bookingId) throws SQLException {
        String sql = BASE_SELECT + "WHERE bookingID = ? ORDER BY lastName, firstName";
        List<TravelerRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

 
    public List<TravelerRecord> getAllTravelers() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY lastName, firstName";
        List<TravelerRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    
    public int countTravelers(int bookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM traveler WHERE bookingID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

 
    public List<TravelerRecord> searchTraveler(String keyword) throws SQLException {
        String sql = BASE_SELECT
                   + "WHERE travelerID = ? OR bookingID = ? "
                   + "OR LOWER(firstName) LIKE ? "
                   + "OR LOWER(lastName) LIKE ? "
                   + "OR LOWER(contactNumber) LIKE ? "
                   + "ORDER BY lastName, firstName";

        String like = "%" + keyword.trim().toLowerCase() + "%";

 
        int id = -1;
        try {
            id = Integer.parseInt(keyword.trim());
        } catch (NumberFormatException ignored) { }

        List<TravelerRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    
    public boolean updateTraveler(int travelerId, String firstName, String lastName,
                                  Date dateOfBirth, String contactNumber) throws SQLException {
        String sql = "UPDATE traveler SET firstName = ?, lastName = ?, dateOfBirth = ?, "
                   + "contactNumber = ? WHERE travelerID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setDate(3, dateOfBirth);
            ps.setString(4, contactNumber);
            ps.setInt(5, travelerId);
            return ps.executeUpdate() > 0;
        }
    }

    
    public boolean deleteTraveler(int travelerId) throws SQLException {
        String sql = "DELETE FROM traveler WHERE travelerID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, travelerId);
            return ps.executeUpdate() > 0;
        }
    }

    private TravelerRecord mapRow(ResultSet rs) throws SQLException {
        TravelerRecord t = new TravelerRecord();
        t.travelerID = rs.getInt("travelerID");
        t.bookingID = rs.getInt("bookingID");
        t.firstName = rs.getString("firstName");
        t.lastName = rs.getString("lastName");
        t.dateOfBirth = rs.getDate("dateOfBirth");
        t.contactNumber = rs.getString("contactNumber");
        return t;
    }
}