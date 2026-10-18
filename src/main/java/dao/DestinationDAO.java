package dao;

import common.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DestinationDAO {

    // Holds one row of the destination table
    public static class DestinationRecord {
        public int destinationID;
        public String name;
        public String country;
        public String description;
        public String imagePath;
        public String status;
    }

    // Adds a destination with image and returns the new destinationID
    public int addDestination(String name, String country, String description, String imagePath)
            throws SQLException {
        String sql = "INSERT INTO destination (name, country, description, imagePath, status) "
                   + "VALUES (?,?,?,?,'Active')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, country);
            ps.setString(3, description);
            ps.setString(4, imagePath);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    // Adds a destination (status defaults to Active) and returns the new destinationID
    public int addDestination(String name, String country, String description)
            throws SQLException {
        return addDestination(name, country, description, null);
    }

    // Returns all destinations (handy for filling a table on your screen)
    public List<DestinationRecord> getAllDestinations() throws SQLException {
        String sql = "SELECT destinationID, name, country, description, imagePath, status "
                   + "FROM destination ORDER BY name";
        List<DestinationRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Returns a single destination by its ID
    public DestinationRecord getDestinationById(int destinationId) throws SQLException {
        String sql = "SELECT destinationID, name, country, description, imagePath, status "
                   + "FROM destination WHERE destinationID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // Searches by destination ID, name, country, description or status
    public List<DestinationRecord> search(String keyword) throws SQLException {
        String sql = "SELECT destinationID, name, country, description, imagePath, status "
                   + "FROM destination "
                   + "WHERE destinationID = ? "
                   + "OR LOWER(name) LIKE ? "
                   + "OR LOWER(country) LIKE ? "
                   + "OR LOWER(description) LIKE ? "
                   + "OR LOWER(status) LIKE ? "
                   + "ORDER BY name";

        String like = "%" + keyword.trim().toLowerCase() + "%";

        // If the keyword isn't a number, use -1 so the ID match is skipped
        int id = -1;
        try {
            id = Integer.parseInt(keyword.trim());
        } catch (NumberFormatException ignored) { }

        List<DestinationRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, like);
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

    // Searches destinations strictly by name
    public List<DestinationRecord> searchByName(String keyword) throws SQLException {
        String sql = "SELECT destinationID, name, country, description, imagePath, status "
                   + "FROM destination "
                   + "WHERE LOWER(name) LIKE ? "
                   + "ORDER BY name";
        String like = "%" + keyword.trim().toLowerCase() + "%";
        List<DestinationRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // Edits a destination with imagePath. Status must be Active or Inactive.
    public boolean editDestination(int destinationId, String name, String country,
                                   String description, String imagePath, String status) throws SQLException {
        if (!"Active".equals(status) && !"Inactive".equals(status)) {
            throw new IllegalArgumentException("Status must be Active or Inactive.");
        }
        String sql = "UPDATE destination "
                   + "SET name = ?, country = ?, description = ?, imagePath = ?, status = ? "
                   + "WHERE destinationID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, country);
            ps.setString(3, description);
            ps.setString(4, imagePath);
            ps.setString(5, status);
            ps.setInt(6, destinationId);
            return ps.executeUpdate() > 0;
        }
    }

    // Edits a destination without altering imagePath (or setting null).
    public boolean editDestination(int destinationId, String name, String country,
                                   String description, String status) throws SQLException {
        return editDestination(destinationId, name, country, description, null, status);
    }

    // Deletes a destination. Fails with a clear message if packages still use it.
    public boolean deleteDestination(int destinationId) throws SQLException {
        String sql = "DELETE FROM destination WHERE destinationID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            // Derby's SQLState for a foreign key violation
            if ("23503".equals(ex.getSQLState())) {
                throw new SQLException(
                        "Cannot delete: this destination is used by one or more packages.", ex);
            }
            throw ex;
        }
    }

    // Converts one result row into a DestinationRecord
    private DestinationRecord mapRow(ResultSet rs) throws SQLException {
        DestinationRecord d = new DestinationRecord();
        d.destinationID = rs.getInt("destinationID");
        d.name = rs.getString("name");
        d.country = rs.getString("country");
        d.description = rs.getString("description");
        try {
            d.imagePath = rs.getString("imagePath");
        } catch (SQLException ignored) {}
        d.status = rs.getString("status");
        return d;
    }
}