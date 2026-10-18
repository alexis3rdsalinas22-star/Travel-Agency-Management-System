package dao;

import common.DBConnection;
import model.customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

   
    public static class CustomerRecord {
        public int customerID;
        public int customerAccountID;
        public String username;
        public String firstName;
        public String middleName;
        public String lastName;
        public String email;
        public String sex;
        public int age;
        public String status;
    }

    private static final String BASE_SELECT =
            "SELECT c.customerID, c.firstName, c.middleName, c.lastName, c.email, c.sex, c.age, "
          + "ca.customerAccountID, ca.username, ca.status "
          + "FROM customer c "
          + "LEFT JOIN customerAccount ca ON c.customerID = ca.customerID ";

    public int insertCustomer(Connection con, String first, String middle, String last,
                              String email, int age) throws SQLException {
        return insertCustomer(con, first, middle, last, email, null, age);
    }

    public int insertCustomer(Connection con, String first, String middle, String last,
                              String email, String sex, int age) throws SQLException {
        String sql = "INSERT INTO customer (firstName, lastName, middleName, email, sex, age) "
                   + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, middle);
            ps.setString(4, email);
            ps.setString(5, sex);
            ps.setInt(6, age);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                return -1;
            }
        }
    }

    public boolean emailExists(Connection con, String email) throws SQLException {
        if (email == null) {
            return false;
        }
        String sql = "SELECT email FROM customer WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean emailExistsForOther(Connection con, String email, int customerId) throws SQLException {
        if (email == null) {
            return false;
        }
        String sql = "SELECT email FROM customer WHERE LOWER(email) = LOWER(?) AND customerID <> ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


   
    public CustomerRecord getCustomerById(Connection con, int customerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE c.customerID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public CustomerRecord getCustomerById(int customerId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return getCustomerById(con, customerId);
        }
    }

   
    public List<CustomerRecord> getAllCustomers(Connection con) throws SQLException {
        String sql = BASE_SELECT + "ORDER BY c.customerID";
        List<CustomerRecord> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean updateCustomer(Connection con, int customerId, String first, String middle,
                                  String last, String email, String sex, int age)
            throws SQLException {
        String sql = "UPDATE customer SET firstName = ?, middleName = ?, lastName = ?, "
                   + "email = ?, sex = ?, age = ? WHERE customerID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, first);
            ps.setString(2, middle);
            ps.setString(3, last);
            ps.setString(4, email);
            ps.setString(5, sex);
            ps.setInt(6, age);
            ps.setInt(7, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCustomer(Connection con, int customerId) throws SQLException {
        String sql = "DELETE FROM customer WHERE customerID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<CustomerRecord> searchCustomer(Connection con, String keyword)
            throws SQLException {
        String sql = BASE_SELECT
                   + "WHERE c.customerID = ? "
                   + "OR LOWER(c.firstName) LIKE ? "
                   + "OR LOWER(c.middleName) LIKE ? "
                   + "OR LOWER(c.lastName) LIKE ? "
                   + "OR LOWER(c.email) LIKE ? "
                   + "OR LOWER(ca.username) LIKE ? "
                   + "OR LOWER(ca.status) LIKE ? "
                   + "ORDER BY c.customerID";

        String like = "%" + keyword.trim().toLowerCase() + "%";

        int id = common.ValidationUtil.parseSearchId(keyword);

        List<CustomerRecord> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private CustomerRecord mapRow(ResultSet rs) throws SQLException {
        CustomerRecord c = new CustomerRecord();
        c.customerID = rs.getInt("customerID");
        c.firstName = rs.getString("firstName");
        c.middleName = rs.getString("middleName");
        c.lastName = rs.getString("lastName");
        c.email = rs.getString("email");
        c.sex = rs.getString("sex");
        c.age = rs.getInt("age");
        c.customerAccountID = rs.getInt("customerAccountID");
        c.username = rs.getString("username");
        c.status = rs.getString("status");
        return c;
    }
}