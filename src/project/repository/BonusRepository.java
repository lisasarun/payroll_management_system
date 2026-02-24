package project.repository;

import project.config.DbConfig;
import project.modal.Bonus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
  JDBC repository for the bonus table (if separate from payroll).
  Stores bonus amounts tied to performance scores per employee.

  Assumed bonus table (if team creates it):
    bonus_id (serial pk), employee_id (int fk), payroll_id (int fk),
    amount (decimal), reason (text), awarded_date (date)
 */
public class BonusRepository {

    private Bonus mapRow(ResultSet rs) throws SQLException {
        Bonus b = new Bonus();
        b.setBonusId(rs.getInt("bonus_id"));
        b.setEmployeeId(rs.getInt("employee_id"));
        b.setPayrollId(rs.getInt("payroll_id"));
        b.setAmount(rs.getBigDecimal("amount"));
        b.setReason(rs.getString("reason"));
        b.setAwardedDate(rs.getDate("awarded_date").toLocalDate());
        return b;
    }

    // Save Bonus

    public boolean save(Bonus bonus) {
        String sql = """
            INSERT INTO bonus (employee_id, payroll_id, amount, reason, awarded_date)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bonus.getEmployeeId());
            ps.setInt(2, bonus.getPayrollId());
            ps.setBigDecimal(3, bonus.getAmount());
            ps.setString(4, bonus.getReason());
            ps.setDate(5, Date.valueOf(bonus.getAwardedDate()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BonusRepository] save error: " + e.getMessage());
        }
        return false;
    }

    // Find by Employee

    public List<Bonus> findByEmployee(int employeeId) {
        String sql = "SELECT * FROM bonus WHERE employee_id = ? ORDER BY awarded_date DESC";
        List<Bonus> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[BonusRepository] findByEmployee error: " + e.getMessage());
        }
        return list;
    }

    // Total bonus for employee in a payroll period

    public java.math.BigDecimal getTotalByPayroll(int payrollId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM bonus WHERE payroll_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payrollId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("[BonusRepository] getTotalByPayroll error: " + e.getMessage());
        }
        return java.math.BigDecimal.ZERO;
    }
}