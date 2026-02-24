package project.repository;

import project.config.DbConfig;
import project.modal.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
  JDBC repository for the employees table.
  Supports CRUD + pagination + soft-delete (is_active).
 */
public class EmployeeRepository {

    // Mapping Helper

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setEmployeeId(rs.getInt("employee_id"));
        emp.setFullName(rs.getString("full_name"));
        emp.setEmail(rs.getString("email"));
        emp.setPassword(rs.getString("password"));
        emp.setActive(rs.getBoolean("is_active"));
        emp.setBaseSalary(rs.getBigDecimal("base_salary"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) emp.setLastLogin(lastLogin.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) emp.setCreatedAt(createdAt.toLocalDateTime());
        return emp;
    }

    // Find by ID

    public Employee findById(int employeeId) {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] findById error: " + e.getMessage());
        }
        return null;
    }

    //  Find by Email (for employee login)

    public Employee findByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ? AND is_active = TRUE";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] findByEmail error: " + e.getMessage());
        }
        return null;
    }

    //  Find All (paginated)

    /**
      Returns one page of active employees.
      @param page  1-based page number
      @param size  rows per page
     */
    public List<Employee> findAll(int page, int size) {
        String sql = "SELECT * FROM employees WHERE is_active = TRUE ORDER BY employee_id LIMIT ? OFFSET ?";
        List<Employee> list = new ArrayList<>();
        int offset = (page - 1) * size;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] findAll error: " + e.getMessage());
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM employees WHERE is_active = TRUE";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] countAll error: " + e.getMessage());
        }
        return 0;
    }

    // Save (Insert)

    public boolean save(Employee emp) {
        String sql = """
            INSERT INTO employees (full_name, email, password, is_active, base_salary, created_at)
            VALUES (?, ?, ?, TRUE, ?, NOW())
            """;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            ps.setString(3, emp.getPassword()); // should be hashed before calling
            ps.setBigDecimal(4, emp.getBaseSalary());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] save error: " + e.getMessage());
        }
        return false;
    }

    //  Update

    public boolean update(Employee emp) {
        String sql = """
            UPDATE employees SET full_name=?, email=?, base_salary=?
            WHERE employee_id=?
            """;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            ps.setBigDecimal(3, emp.getBaseSalary());
            ps.setInt(4, emp.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] update error: " + e.getMessage());
        }
        return false;
    }

    //  Soft Delete (Disable)

    public boolean disable(int employeeId) {
        String sql = "UPDATE employees SET is_active = FALSE WHERE employee_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] disable error: " + e.getMessage());
        }
        return false;
    }

    // Search by name

    public List<Employee> searchByName(String keyword, int page, int size) {
        String sql = "SELECT * FROM employees WHERE full_name ILIKE ? AND is_active = TRUE ORDER BY employee_id LIMIT ? OFFSET ?";
        List<Employee> list = new ArrayList<>();
        int offset = (page - 1) * size;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setInt(2, size);
            ps.setInt(3, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] searchByName error: " + e.getMessage());
        }
        return list;
    }

    // Update last login

    public void updateLastLogin(int employeeId) {
        String sql = "UPDATE employees SET last_login = NOW() WHERE employee_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[EmployeeRepository] updateLastLogin error: " + e.getMessage());
        }
    }
}