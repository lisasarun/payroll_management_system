package project.repository;

import project.config.DbConfig;
import project.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("employee_id"));
        e.setFullName(rs.getString("full_name"));
        e.setEmail(rs.getString("email"));
        e.setPassword(rs.getString("password"));
        e.setActive(rs.getBoolean("is_active"));
        e.setBaseSalary(rs.getBigDecimal("base_salary"));
        Timestamp li = rs.getTimestamp("last_login");
        if (li != null) e.setLastLogin(li.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) e.setCreatedAt(ca.toLocalDateTime());
        return e;
    }

    public Employee findById(int id) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM employees WHERE employee_id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[EmpRepo] findById: " + e.getMessage()); }
        return null;
    }

    public Employee findByEmail(String email) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM employees WHERE email = ? AND is_active = TRUE")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[EmpRepo] findByEmail: " + e.getMessage()); }
        return null;
    }

    public List<Employee> findAll(int page, int size) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE is_active = TRUE ORDER BY employee_id LIMIT ? OFFSET ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[EmpRepo] findAll: " + e.getMessage()); }
        return list;
    }

    public int countAll() {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM employees WHERE is_active = TRUE")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[EmpRepo] countAll: " + e.getMessage()); }
        return 0;
    }

    public List<Employee> searchByName(String keyword, int page, int size) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE full_name ILIKE ? AND is_active = TRUE ORDER BY employee_id LIMIT ? OFFSET ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setInt(2, size);
            ps.setInt(3, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[EmpRepo] search: " + e.getMessage()); }
        return list;
    }

    public boolean save(Employee emp) {
        String sql = "INSERT INTO employees (full_name, email, password, is_active, base_salary, created_at) VALUES (?,?,?,TRUE,?,NOW())";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            ps.setString(3, emp.getPassword());
            ps.setBigDecimal(4, emp.getBaseSalary());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] save: " + e.getMessage()); }
        return false;
    }

    public boolean update(Employee emp) {
        String sql = "UPDATE employees SET full_name=?, email=?, base_salary=? WHERE employee_id=?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            ps.setBigDecimal(3, emp.getBaseSalary());
            ps.setInt(4, emp.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] update: " + e.getMessage()); }
        return false;
    }

    public boolean disable(int employeeId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE employees SET is_active = FALSE WHERE employee_id = ?")) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] disable: " + e.getMessage()); }
        return false;
    }
}
