package dao;

import common.DBConnection;
import model.customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerDAO {
    
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
}