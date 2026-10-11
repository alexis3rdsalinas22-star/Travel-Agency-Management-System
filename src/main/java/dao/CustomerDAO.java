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
        public String firstName;
        public String middleName;
        public String lastName;
        public String email;
        public String sex;
        public int age;
    }

    private static final String BASE_SELECT =
            "SELECT customerID, firstName, middleName, lastName, email, sex, age FROM customer ";

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
                keys.next();
                return keys.getInt(1);
            }
        }
    }

   
    public CustomerRecord getCustomerById(Connection con, int customerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE customerID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

   
    public List<CustomerRecord> getAllCustomers(Connection con) throws SQLException {
        String sql = BASE_SELECT + "ORDER BY lastName, firstName";
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

   
    public List<CustomerRecord> searchCustomer(Connection con, String keyword)
            throws SQLException {
        String sql = BASE_SELECT
                   + "WHERE customerID = ? "
                   + "OR LOWER(firstName) LIKE ? "
                   + "OR LOWER(middleName) LIKE ? "
                   + "OR LOWER(lastName) LIKE ? "
                   + "OR LOWER(email) LIKE ? "
                   + "ORDER BY lastName, firstName";

        String like = "%" + keyword.trim().toLowerCase() + "%";

       
        int id = -1;
        try {
            id = Integer.parseInt(keyword.trim());
        } catch (NumberFormatException ignored) { }

        List<CustomerRecord> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
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

    private CustomerRecord mapRow(ResultSet rs) throws SQLException {
        CustomerRecord c = new CustomerRecord();
        c.customerID = rs.getInt("customerID");
        c.firstName = rs.getString("firstName");
        c.middleName = rs.getString("middleName");
        c.lastName = rs.getString("lastName");
        c.email = rs.getString("email");
        c.sex = rs.getString("sex");
        c.age = rs.getInt("age");
        return c;
    }
}