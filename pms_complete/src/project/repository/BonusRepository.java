package project.repository;

import project.config.DbConfig;
import project.model.Bonus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BonusRepository {


    public boolean saveWithConnection(Connection c, Bonus b) {
        String sql = "INSERT INTO bonus (employee_id, payroll_id, amount, reason, awarded_date) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getEmployeeId());
            if (b.getPayrollId() > 0) {
                ps.setInt(2, b.getPayrollId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setBigDecimal(3, b.getAmount());
            ps.setString(4, b.getReason());
            ps.setDate(5, Date.valueOf(b.getAwardedDate()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BonusRepo] save: " + e.getMessage());
            return false;
        }
    }


    public boolean save(Bonus b) {
        try (Connection c = DbConfig.getConnection()) {
            return saveWithConnection(c, b);
        } catch (SQLException e) {
            System.err.println("[BonusRepo] save: " + e.getMessage());
            return false;
        }
    }

    public List<Bonus> findByEmployee(int employeeId) {
        List<Bonus> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM bonus WHERE employee_id = ? ORDER BY awarded_date DESC")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bonus b = new Bonus();
                b.setBonusId(rs.getInt("bonus_id"));
                b.setEmployeeId(rs.getInt("employee_id"));
                int payrollId = rs.getInt("payroll_id");
                b.setPayrollId(rs.wasNull() ? 0 : payrollId);
                b.setAmount(rs.getBigDecimal("amount"));
                b.setReason(rs.getString("reason"));
                Date ad = rs.getDate("awarded_date");
                if (ad != null) b.setAwardedDate(ad.toLocalDate());
                list.add(b);
            }
        } catch (SQLException e) { System.err.println("[BonusRepo] findByEmployee: " + e.getMessage()); }
        return list;
    }

    public BigDecimal getTotalByPayroll(int payrollId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(amount),0) FROM bonus WHERE payroll_id = ?")) {
            ps.setInt(1, payrollId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) { System.err.println("[BonusRepo] getTotal: " + e.getMessage()); }
        return BigDecimal.ZERO;
    }


    public int countByEmployeeAndYear(int employeeId, int year) {
        String sql = "SELECT COUNT(*) FROM bonus WHERE employee_id = ? AND EXTRACT(YEAR FROM awarded_date) = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[BonusRepo] countByEmployeeAndYear: " + e.getMessage());
        }
        return 0;
    }


    public BigDecimal sumByEmployeeAndYear(int employeeId, int year) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM bonus WHERE employee_id = ? AND EXTRACT(YEAR FROM awarded_date) = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("[BonusRepo] sumByEmployeeAndYear: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
