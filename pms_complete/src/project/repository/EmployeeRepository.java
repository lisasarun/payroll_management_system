package project.repository;

import project.config.DbConfig;
import project.model.Employee;
import project.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for employee CRUD operations.
 *
 * NEW passwords are hashed with PBKDF2 + salt via {@link PasswordUtil#hashSecure(String)}.
 * Legacy SHA-256 hashes are still supported for existing rows via PasswordUtil.verifyAny().
 */
public class EmployeeRepository {

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("employee_id"));
        e.setFullName(rs.getString("full_name"));
        e.setEmail(rs.getString("email"));
        e.setPassword(rs.getString("password"));
        e.setActive(rs.getBoolean("is_active"));
        e.setBaseSalary(rs.getBigDecimal("base_salary"));

        // New fields
        e.setPosition(rs.getString("position"));
        e.setDepartment(rs.getString("department"));

        Date hd = rs.getDate("hire_date");
        if (hd != null) e.setHireDate(hd.toLocalDate());

        Timestamp li = rs.getTimestamp("last_login");
        if (li != null) e.setLastLogin(li.toLocalDateTime());

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) e.setCreatedAt(ca.toLocalDateTime());

        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) e.setUpdatedAt(ua.toLocalDateTime());

        return e;
    }

    public Employee findById(int id) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM employees WHERE employee_id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[EmpRepo] findById: " + e.getMessage()); }
        return null;
    }

    public Employee findByEmail(String email) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM employees WHERE email = ? AND is_active = TRUE")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[EmpRepo] findByEmail: " + e.getMessage()); }
        return null;
    }

    public boolean emailExists(String email) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM employees WHERE email = ?")) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) { System.err.println("[EmpRepo] emailExists: " + e.getMessage()); }
        return false;
    }

    public List<Employee> findAll(int page, int size) {
        // Default ordering by ID (ascending)
        return findAllOrdered(page, size, "ID");
    }

    public List<Employee> findAllOrdered(int page, int size, String sortKey) {
        List<Employee> list = new ArrayList<>();
        String orderBy;
        switch (sortKey) {
            case "SALARY_DESC"      -> orderBy = "base_salary DESC";
            case "NAME"             -> orderBy = "full_name ASC";
            case "ID" -> orderBy = "employee_id ASC";
            default -> orderBy = "employee_id ASC";
        }

        String sql = "SELECT * FROM employees WHERE is_active = TRUE ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[EmpRepo] findAllOrdered: " + e.getMessage()); }
        return list;
    }

    public int countAll() {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM employees WHERE is_active = TRUE")) {
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

    /**
     * Find all active employees who DO NOT have any payroll in the given period.
     */
    public List<Employee> findActiveWithoutPayroll(java.time.LocalDate from, java.time.LocalDate to) {
        List<Employee> list = new ArrayList<>();
        String sql = """
                    SELECT e.*
                    FROM employees e
                    WHERE e.is_active = TRUE
                      AND NOT EXISTS (
                          SELECT 1 FROM payroll p
                          WHERE p.employee_id = e.employee_id
                            AND p.pay_period_start <= ?
                            AND p.pay_period_end   >= ?
                      )
                    ORDER BY e.employee_id
                    """;
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(to));
            ps.setDate(2, Date.valueOf(from));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[EmpRepo] findActiveWithoutPayroll: " + e.getMessage()); }
        return list;
    }

    public boolean save(Employee emp) {
        String sql = "INSERT INTO employees (full_name, email, password, is_active, base_salary, position, department, hire_date, created_at, updated_at) " +
                "VALUES (?,?,?,TRUE,?,?,?,?,NOW(),NOW())";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            // Hash password using secure PBKDF2 for new employees
            ps.setString(3, PasswordUtil.hashSecure(emp.getPassword()));
            ps.setBigDecimal(4, emp.getBaseSalary());
            ps.setString(5, emp.getPosition());
            ps.setString(6, emp.getDepartment());
            ps.setDate(7, emp.getHireDate() != null ? Date.valueOf(emp.getHireDate()) : Date.valueOf(java.time.LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] save: " + e.getMessage()); }
        return false;
    }

    public boolean update(Employee emp) {
        String sql = "UPDATE employees SET full_name=?, email=?, base_salary=?, position=?, department=?, updated_at=NOW() WHERE employee_id=?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            ps.setBigDecimal(3, emp.getBaseSalary());
            ps.setString(4, emp.getPosition());
            ps.setString(5, emp.getDepartment());
            ps.setInt(6, emp.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] update: " + e.getMessage()); }
        return false;
    }

    /** Update name, email,salary. And password in one query (password already hashed by caller) */
    public boolean updateWithPassword(Employee emp) {
        String sql = "UPDATE employees SET full_name=?, email=?, base_salary=?, position=?, department=?, password=?, updated_at=NOW() WHERE employee_id=?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            ps.setBigDecimal(3, emp.getBaseSalary());
            ps.setString(4, emp.getPosition());
            ps.setString(5, emp.getDepartment());
            ps.setString(6, emp.getPassword()); // already hashed by caller
            ps.setInt(7, emp.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] updateWithPassword: " + e.getMessage()); }
        return false;
    }

    public boolean updatePassword(int employeeId, String newPassword) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE employees SET password = ? WHERE employee_id = ?")) {
            // Use secure PBKDF2 hashing for password updates
            ps.setString(1, PasswordUtil.hashSecure(newPassword));
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] updatePassword: " + e.getMessage()); }
        return false;
    }

    public boolean disable(int employeeId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE employees SET is_active = FALSE WHERE employee_id = ?")) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[EmpRepo] disable: " + e.getMessage()); }
        return false;
    }
}
