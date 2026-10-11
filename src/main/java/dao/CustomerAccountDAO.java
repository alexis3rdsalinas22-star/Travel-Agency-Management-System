package dao;

import common.DBConnection;
import model.CustomerAccount;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerAccountDAO {

    private String hash(String password) {
        return common.SecurityUtil.hashPassword(password);
    }

    public boolean usernameExists(Connection con, String username) throws SQLException {
        String sql = "SELECT username FROM customeraccount WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertAccount(Connection con, int customerId, String username,
                              String password) throws SQLException {
        String sql = "INSERT INTO customeraccount (customerID, username, password, status) "
                   + "VALUES (?,?,?,'Active')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, username);
            ps.setString(3, hash(password));
            ps.executeUpdate();
        }
    }

    public int login(Connection con, String username, String password) throws SQLException {
        String sql = "SELECT customerID, status FROM customeraccount "
                   + "WHERE username = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && "Active".equals(rs.getString("status"))) {
                    return rs.getInt("customerID");
                }
                return -1;
            }
        }
    }

  

    // Changes the username. Returns false if the new username is already taken by a different account, or if the account doesn't exist.
     
    public boolean editUsername(Connection con, int customerAccountId, String newUsername)
            throws SQLException {
        String check = "SELECT customerAccountID FROM customeraccount "
                     + "WHERE username = ? AND customerAccountID <> ?";
        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setString(1, newUsername);
            ps.setInt(2, customerAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return false; // username belongs to someone else
                }
            }
        }

        String sql = "UPDATE customeraccount SET username = ? WHERE customerAccountID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setInt(2, customerAccountId);
            return ps.executeUpdate() > 0;
        }
    }

    // Changes the password only if the old password is correct.
    // Returns false if the old password doesn't match.
    public boolean changePassword(Connection con, int customerAccountId,
                                  String oldPassword, String newPassword) throws SQLException {
        String sql = "UPDATE customeraccount SET password = ? "
                   + "WHERE customerAccountID = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash(newPassword));
            ps.setInt(2, customerAccountId);
            ps.setString(3, hash(oldPassword));
            return ps.executeUpdate() > 0;
        }
    }

   

    // Sets the status to Active, Inactive or Suspended (the values from your original design).
    public boolean updateStatus(Connection con, int customerAccountId, String newStatus)
            throws SQLException {
        if (!"Active".equals(newStatus) && !"Inactive".equals(newStatus)
                && !"Suspended".equals(newStatus)) {
            throw new IllegalArgumentException(
                    "Status must be Active, Inactive or Suspended.");
        }
        String sql = "UPDATE customeraccount SET status = ? WHERE customerAccountID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, customerAccountId);
            return ps.executeUpdate() > 0;
        }
    }

    // Shortcut: deactivate an account (login() already blocks non-Active accounts).
    public boolean deactivateAccount(Connection con, int customerAccountId) throws SQLException {
        return updateStatus(con, customerAccountId, "Inactive");
    }

    // Admin reset password without needing old password
    public boolean resetPassword(Connection con, int customerAccountId, String newPassword)
            throws SQLException {
        String sql = "UPDATE customeraccount SET password = ? WHERE customerAccountID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash(newPassword));
            ps.setInt(2, customerAccountId);
            return ps.executeUpdate() > 0;
        }
    }
}