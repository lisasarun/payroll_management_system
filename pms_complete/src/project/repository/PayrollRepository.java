package project.repository;

import project.config.DbConfig;
import project.model.Payroll;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public boolean save(Payroll p) {
        String sql = "INSERT INTO payroll (employee_id, pay_period_start, pay_period_end, " +
                "base_salary, bonus, deductions, total_paid, payment_date) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getEmployeeId());
            ps.setDate(2, Date.valueOf(p.getPayPeriodStart()));
            ps.setDate(3, Date.valueOf(p.getPayPeriodEnd()));
            ps.setBigDecimal(4, p.getBaseSalary());
            ps.setBigDecimal(5, p.getBonus());
            ps.setBigDecimal(6, p.getDeductions());
            ps.setBigDecimal(7, p.getTotalPaid());
            ps.setDate(8, Date.valueOf(p.getPaymentDate() != null ? p.getPaymentDate() : LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[PayRepo] save: " + e.getMessage()); }
        return false;
    }

    public List<Payroll> findByEmployee(int employeeId) {
        List<Payroll> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM payroll WHERE employee_id = ? ORDER BY pay_period_start DESC")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[PayRepo] findByEmployee: " + e.getMessage()); }
        return list;
    }

    public Payroll findLatest(int employeeId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM payroll WHERE employee_id = ? ORDER BY pay_period_start DESC LIMIT 1")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[PayRepo] findLatest: " + e.getMessage()); }
        return null;
    }


    public Payroll findLatestWithConnection(Connection conn, int employeeId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM payroll WHERE employee_id = ? ORDER BY pay_period_start DESC LIMIT 1")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[PayRepo] findLatestWithConnection: " + e.getMessage()); }
        return null;
    }

    public Payroll findById(int payrollId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM payroll WHERE payroll_id = ?")) {
            ps.setInt(1, payrollId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[PayRepo] findById: " + e.getMessage()); }
        return null;
    }

    public List<Payroll> findAll(int page, int size) {
        List<Payroll> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM payroll ORDER BY payment_date DESC LIMIT ? OFFSET ?")) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[PayRepo] findAll: " + e.getMessage()); }
        return list;
    }
}