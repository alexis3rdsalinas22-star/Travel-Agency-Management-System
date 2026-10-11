package dao;

import model.AdminAccount;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminAccountDAO {

    private String hash(String password) {
        return common.SecurityUtil.hashPassword(password);
    }

    public boolean usernameExists(Connection con, String username) throws SQLException {
        String sql = "SELECT username FROM adminAccount WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Logs an admin in.
     * @return the matching AdminAccount (password field is left null), or null if the
     *         username/password is wrong.
     */
    public AdminAccount login(Connection con, String username, String password) throws SQLException {
        String sql = "SELECT adminAccountID, username, role FROM adminAccount "
                   + "WHERE username = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AdminAccount(
                            rs.getInt("adminAccountID"),
                            rs.getString("username"),
                            null,
                            rs.getString("role"));
                }
                return null;
            }
        }
    }

    /**
     * Creates a new admin account.
     * @return the generated adminAccountID, or -1 if the username is already taken.
     */
    public int createAccount(Connection con, String username, String password, String role)
            throws SQLException {
        if (usernameExists(con, username)) {
            return -1;
        }
        String sql = "INSERT INTO adminAccount (username, password, role) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            ps.setString(3, role);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /**
     * Deletes an admin account by ID.
     * @return true if a row was deleted, false if no such account exists.
     */
    public boolean deleteAccount(Connection con, int adminAccountId) throws SQLException {
        String sql = "DELETE FROM adminAccount WHERE adminAccountID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminAccountId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Edits an admin account's username and role. The password is only changed when
     * newPassword is non-null and non-empty; otherwise the existing password is kept.
     * @return true if the account was updated, false if it does not exist or the new
     *         username is already used by a different admin.
     */
    public boolean editAccount(Connection con, int adminAccountId, String username,
                               String newPassword, String role) throws SQLException {
        // Block renaming to a username that belongs to another admin.
        String check = "SELECT adminAccountID FROM adminAccount "
                     + "WHERE username = ? AND adminAccountID <> ?";
        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setString(1, username);
            ps.setInt(2, adminAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return false;
                }
            }
        }

        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        String sql = changePassword
                ? "UPDATE adminAccount SET username = ?, role = ?, password = ? WHERE adminAccountID = ?"
                : "UPDATE adminAccount SET username = ?, role = ? WHERE adminAccountID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, role);
            if (changePassword) {
                ps.setString(3, hash(newPassword));
                ps.setInt(4, adminAccountId);
            } else {
                ps.setInt(3, adminAccountId);
            }
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all admin accounts.
     */
    public List<AdminAccount> getAllAccounts(Connection con) throws SQLException {
        List<AdminAccount> list = new ArrayList<>();
        String sql = "SELECT adminAccountID, username, role FROM adminAccount ORDER BY adminAccountID";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AdminAccount(
                        rs.getInt("adminAccountID"),
                        rs.getString("username"),
                        null,
                        rs.getString("role")));
            }
        }
        return list;
    }

    /**
     * Searches admin accounts by username or role.
     */
    public List<AdminAccount> searchAccounts(Connection con, String keyword) throws SQLException {
        List<AdminAccount> list = new ArrayList<>();
        String sql = "SELECT adminAccountID, username, role FROM adminAccount "
                   + "WHERE LOWER(username) LIKE ? OR LOWER(role) LIKE ? "
                   + "ORDER BY adminAccountID";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AdminAccount(
                            rs.getInt("adminAccountID"),
                            rs.getString("username"),
                            null,
                            rs.getString("role")));
                }
            }
        }
        return list;
    }
}