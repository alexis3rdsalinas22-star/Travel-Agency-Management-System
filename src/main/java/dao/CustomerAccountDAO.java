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
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
}