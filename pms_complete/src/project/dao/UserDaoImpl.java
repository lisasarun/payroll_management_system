package project.dao;

import project.config.DbConfig;
import project.model.Employee;
import project.model.User;

import java.sql.*;

public class UserDaoImpl implements UserDao {

    @Override
    public User adminLogin(String username, String password) {
        String sql = "SELECT admin_id, username, password, permission_level FROM admins WHERE username = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString("password").equals(password)) {
                User user = mapUser(rs);
                updateAdminLastLogin(user.getAdminId());
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] adminLogin: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Employee employeeLogin(String email, String password) {
        String sql = "SELECT * FROM employees WHERE email = ? AND is_active = TRUE";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString("password").equals(password)) {
                Employee emp = mapEmployee(rs);
                updateEmployeeLastLogin(emp.getEmployeeId());
                return emp;
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] employeeLogin: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findAdminByUsername(String username) {
        String sql = "SELECT admin_id, username, password, permission_level FROM admins WHERE username = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) {
            System.err.println("[UserDao] findAdminByUsername: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Employee findEmployeeByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapEmployee(rs);
        } catch (SQLException e) {
            System.err.println("[UserDao] findEmployeeByEmail: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void updateAdminLastLogin(int adminId) {
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE admins SET last_login = NOW() WHERE admin_id = ?")) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    @Override
    public void updateEmployeeLastLogin(int employeeId) {
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE employees SET last_login = NOW() WHERE employee_id = ?")) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDao] updateEmployeeLastLogin: " + e.getMessage());
        }
    }

    @Override
    public boolean adminUsernameExists(String username) {
        return exists("SELECT 1 FROM admins WHERE username = ?", username);
    }

    @Override
    public boolean employeeEmailExists(String email) {
        return exists("SELECT 1 FROM employees WHERE email = ?", email);
    }

    private boolean exists(String sql, String param) {
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setAdminId(rs.getInt("admin_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setPermissionLevel(rs.getString("permission_level"));
        return u;
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
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
}
