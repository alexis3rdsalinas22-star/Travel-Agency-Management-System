package dao;

import common.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TravelPackageDAO {

    
    public static class PackageRecord {
        public int packageID;
        public int destinationID;
        public String destinationName;
        public String country;
        public String name;
        public String description;
        public String imagePath;
        public BigDecimal pricePerTraveller;
        public String status;
        public int minTraveller;
        public int maxTraveller;
    }

    private static final String BASE_SELECT =
            "SELECT p.packageID, p.destinationID, p.name, p.description, p.imagePath, "
          + "p.pricePerTraveller, p.status, p.minTraveller, p.maxTraveller, "
          + "d.name AS destinationName, d.country "
          + "FROM package p "
          + "JOIN destination d ON p.destinationID = d.destinationID ";

    public int addPackage(int destinationId, String name, String description, String imagePath,
                          BigDecimal pricePerTraveller, int minTraveller, int maxTraveller)
            throws SQLException {
        validate(pricePerTraveller, minTraveller, maxTraveller);

        String sql = "INSERT INTO package (destinationID, name, description, imagePath, "
                   + "pricePerTraveller, status, minTraveller, maxTraveller) "
                   + "VALUES (?,?,?,?,?,'Active',?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, destinationId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, imagePath);
            ps.setBigDecimal(5, pricePerTraveller);
            ps.setInt(6, minTraveller);
            ps.setInt(7, maxTraveller);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public int addPackage(int destinationId, String name, String description,
                          BigDecimal pricePerTraveller, int minTraveller, int maxTraveller)
            throws SQLException {
        return addPackage(destinationId, name, description, null, pricePerTraveller, minTraveller, maxTraveller);
    }

  
    public List<PackageRecord> getAllPackages() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY d.name, p.name";
        List<PackageRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

   
    public PackageRecord getPackageById(int packageId) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.packageID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    
    public List<PackageRecord> searchPackage(String keyword) throws SQLException {
        String sql = BASE_SELECT
                   + "WHERE p.packageID = ? "
                   + "OR LOWER(p.name) LIKE ? "
                   + "OR LOWER(p.description) LIKE ? "
                   + "OR LOWER(d.name) LIKE ? "
                   + "OR LOWER(d.country) LIKE ? "
                   + "OR LOWER(p.status) LIKE ? "
                   + "ORDER BY d.name, p.name";

        String like = "%" + keyword.trim().toLowerCase() + "%";

       
        int id = -1;
        try {
            id = Integer.parseInt(keyword.trim());
        } catch (NumberFormatException ignored) { }

        List<PackageRecord> list = new ArrayList<>();
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

    public List<PackageRecord> searchByName(String keyword) throws SQLException {
        String sql = BASE_SELECT
                   + "WHERE LOWER(p.name) LIKE ? "
                   + "ORDER BY d.name, p.name";
        String like = "%" + keyword.trim().toLowerCase() + "%";
        List<PackageRecord> list = new ArrayList<>();
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

    public boolean updatePackage(int packageId, int destinationId, String name,
                                 String description, String imagePath, BigDecimal pricePerTraveller,
                                 String status, int minTraveller, int maxTraveller)
            throws SQLException {
        validate(pricePerTraveller, minTraveller, maxTraveller);
        if (!"Active".equals(status) && !"Inactive".equals(status)) {
            throw new IllegalArgumentException("Status must be Active or Inactive.");
        }

        String sql = "UPDATE package SET destinationID = ?, name = ?, description = ?, imagePath = ?, "
                   + "pricePerTraveller = ?, status = ?, minTraveller = ?, maxTraveller = ? "
                   + "WHERE packageID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, imagePath);
            ps.setBigDecimal(5, pricePerTraveller);
            ps.setString(6, status);
            ps.setInt(7, minTraveller);
            ps.setInt(8, maxTraveller);
            ps.setInt(9, packageId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePackage(int packageId, int destinationId, String name,
                                 String description, BigDecimal pricePerTraveller,
                                 String status, int minTraveller, int maxTraveller)
            throws SQLException {
        return updatePackage(packageId, destinationId, name, description, null, pricePerTraveller, status, minTraveller, maxTraveller);
    }

    public boolean deletePackage(int packageId) throws SQLException {
        String sql = "DELETE FROM package WHERE packageID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, packageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
           
            if ("23503".equals(ex.getSQLState())) {
                throw new SQLException(
                        "Cannot delete: this package has bookings. Set it to Inactive instead.", ex);
            }
            throw ex;
        }
    }

    private void validate(BigDecimal price, int min, int max) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (min < 1) {
            throw new IllegalArgumentException("Minimum travellers must be at least 1.");
        }
        if (max < min) {
            throw new IllegalArgumentException(
                    "Maximum travellers cannot be less than the minimum.");
        }
    }

    private PackageRecord mapRow(ResultSet rs) throws SQLException {
        PackageRecord r = new PackageRecord();
        r.packageID = rs.getInt("packageID");
        r.destinationID = rs.getInt("destinationID");
        r.destinationName = rs.getString("destinationName");
        r.country = rs.getString("country");
        r.name = rs.getString("name");
        r.description = rs.getString("description");
        try {
            r.imagePath = rs.getString("imagePath");
        } catch (SQLException ignored) {}
        r.pricePerTraveller = rs.getBigDecimal("pricePerTraveller");
        r.status = rs.getString("status");
        r.minTraveller = rs.getInt("minTraveller");
        r.maxTraveller = rs.getInt("maxTraveller");
        return r;
    }
}