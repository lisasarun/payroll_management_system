package project.repository;

import project.config.DbConfig;
import project.modal.Payroll;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
  JDBC repository for the payroll table.
  Used by PayrollService to save and retrieve payroll records.
 */
public class PayrollRepository {

    private Payroll mapRow(ResultSet rs) throws SQLException {
        Payroll p = new Payroll();
        p.setPayrollId(rs.getInt("payroll_id"));
        p.setEmployeeId(rs.getInt("employee_id"));
        p.setPayPeriodStart(rs.getDate("pay_period_start").toLocalDate());
        p.setPayPeriodEnd(rs.getDate("pay_period_end").toLocalDate());
        p.setBaseSalary(rs.getBigDecimal("base_salary"));
        p.setBonus(rs.getBigDecimal("bonus"));
        p.setDeductions(rs.getBigDecimal("deductions"));
        p.setTotalPaid(rs.getBigDecimal("total_paid"));
        Date pd = rs.getDate("payment_date");
        if (pd != null) p.setPaymentDate(pd.toLocalDate());
        return p;
    }

    //  Save

    public boolean save(Payroll payroll) {
        String sql = """
            INSERT INTO payroll
                (employee_id, pay_period_start, pay_period_end, base_salary, bonus, deductions, total_paid, payment_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payroll.getEmployeeId());
            ps.setDate(2, Date.valueOf(payroll.getPayPeriodStart()));
            ps.setDate(3, Date.valueOf(payroll.getPayPeriodEnd()));
            ps.setBigDecimal(4, payroll.getBaseSalary());
            ps.setBigDecimal(5, payroll.getBonus());
            ps.setBigDecimal(6, payroll.getDeductions());
            ps.setBigDecimal(7, payroll.getTotalPaid());
            ps.setDate(8, Date.valueOf(payroll.getPaymentDate() != null
                    ? payroll.getPaymentDate() : LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] save error: " + e.getMessage());
        }
        return false;
    }

    //  Find by Employee

    public List<Payroll> findByEmployee(int employeeId) {
        String sql = "SELECT * FROM payroll WHERE employee_id = ? ORDER BY pay_period_start DESC";
        List<Payroll> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] findByEmployee error: " + e.getMessage());
        }
        return list;
    }

    // Find Latest for an Employee

    public Payroll findLatest(int employeeId) {
        String sql = "SELECT * FROM payroll WHERE employee_id = ? ORDER BY pay_period_start DESC LIMIT 1";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] findLatest error: " + e.getMessage());
        }
        return null;
    }

    //  Find All (Paginated)

    public List<Payroll> findAll(int page, int size) {
        String sql = "SELECT * FROM payroll ORDER BY payment_date DESC LIMIT ? OFFSET ?";
        List<Payroll> list = new ArrayList<>();
        int offset = (page - 1) * size;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] findAll error: " + e.getMessage());
        }
        return list;
    }

    // Find by ID

    public Payroll findById(int payrollId) {
        String sql = "SELECT * FROM payroll WHERE payroll_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payrollId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] findById error: " + e.getMessage());
        }
        return null;
    }
}