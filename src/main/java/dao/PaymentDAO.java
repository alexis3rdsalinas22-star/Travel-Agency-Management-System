package dao;

import common.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaymentDAO {

    
    public static class PaymentRecord {
        public int paymentID;
        public int bookingID;
        public Timestamp paymentDate;
        public BigDecimal amount;
        public String method;
        public String source;
    }

    
    public static class SalesSummary {
        public int totalPayments;
        public BigDecimal totalRevenue = BigDecimal.ZERO;
        public BigDecimal averagePayment = BigDecimal.ZERO;
        public Map<String, BigDecimal> revenueByMethod = new LinkedHashMap<>();
        public Map<String, BigDecimal> revenueByPackage = new LinkedHashMap<>();
    }

    private static final List<String> VALID_METHODS =
            Arrays.asList("Cash", "Credit Card", "Debit Card", "GCash", "Bank Transfer");

   
    public int recordPayment(int bookingId, BigDecimal amount, String method, String source)
            throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (!VALID_METHODS.contains(method)) {
            throw new IllegalArgumentException(
                    "Method must be Cash, Credit Card, Debit Card, GCash or Bank Transfer.");
        }
        String sql = "INSERT INTO payment (bookingID, paymentDate, amount, method, source) "
                   + "VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setBigDecimal(3, amount);
            ps.setString(4, method);
            ps.setString(5, source);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public List<PaymentRecord> getAllPayments() throws SQLException {
        String sql = "SELECT paymentID, bookingID, paymentDate, amount, method, source "
                   + "FROM payment ORDER BY paymentDate DESC";
        List<PaymentRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PaymentRecord p = new PaymentRecord();
                p.paymentID = rs.getInt("paymentID");
                p.bookingID = rs.getInt("bookingID");
                p.paymentDate = rs.getTimestamp("paymentDate");
                p.amount = rs.getBigDecimal("amount");
                p.method = rs.getString("method");
                p.source = rs.getString("source");
                list.add(p);
            }
        }
        return list;
    }

    public List<PaymentRecord> getPaymentsByBooking(int bookingId) throws SQLException {
        String sql = "SELECT paymentID, bookingID, paymentDate, amount, method, source "
                   + "FROM payment WHERE bookingID = ? ORDER BY paymentDate DESC";
        List<PaymentRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentRecord p = new PaymentRecord();
                    p.paymentID = rs.getInt("paymentID");
                    p.bookingID = rs.getInt("bookingID");
                    p.paymentDate = rs.getTimestamp("paymentDate");
                    p.amount = rs.getBigDecimal("amount");
                    p.method = rs.getString("method");
                    p.source = rs.getString("source");
                    list.add(p);
                }
            }
        }
        return list;
    }

    
    public BigDecimal getTotalPaidForBooking(int bookingId) throws SQLException {
        String sql = "SELECT SUM(amount) FROM payment WHERE bookingID = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal total = rs.next() ? rs.getBigDecimal(1) : null;
                return total == null ? BigDecimal.ZERO : total;
            }
        }
    }

    
    public SalesSummary getSalesReportSummary() throws SQLException {
        return getSalesReportSummary(null, null);
    }

   
    public SalesSummary getSalesReportSummary(Timestamp from, Timestamp to) throws SQLException {
        SalesSummary s = new SalesSummary();

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Timestamp> params = new ArrayList<>();
        if (from != null) {
            where.append("AND p.paymentDate >= ? ");
            params.add(from);
        }
        if (to != null) {
            where.append("AND p.paymentDate <= ? ");
            params.add(to);
        }

        try (Connection con = DBConnection.getConnection()) {

            
            String totals = "SELECT COUNT(*), SUM(p.amount) FROM payment p" + where;
            try (PreparedStatement ps = con.prepareStatement(totals)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        s.totalPayments = rs.getInt(1);
                        BigDecimal sum = rs.getBigDecimal(2);
                        s.totalRevenue = sum == null ? BigDecimal.ZERO : sum;
                    }
                }
            }
            if (s.totalPayments > 0) {
                s.averagePayment = s.totalRevenue.divide(
                        BigDecimal.valueOf(s.totalPayments), 2, java.math.RoundingMode.HALF_UP);
            }

            String byMethod = "SELECT p.method, SUM(p.amount) FROM payment p" + where
                            + " GROUP BY p.method ORDER BY 2 DESC";
            try (PreparedStatement ps = con.prepareStatement(byMethod)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        s.revenueByMethod.put(rs.getString(1), rs.getBigDecimal(2));
                    }
                }
            }

            
            String byPackage = "SELECT pk.name, SUM(p.amount) FROM payment p "
                             + "JOIN booking b ON p.bookingID = b.bookingID "
                             + "JOIN package pk ON b.packageID = pk.packageID"
                             + where
                             + " GROUP BY pk.name ORDER BY 2 DESC";
            try (PreparedStatement ps = con.prepareStatement(byPackage)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        s.revenueByPackage.put(rs.getString(1), rs.getBigDecimal(2));
                    }
                }
            }
        }
        return s;
    }

    private void bind(PreparedStatement ps, List<Timestamp> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setTimestamp(i + 1, params.get(i));
        }
    }
}