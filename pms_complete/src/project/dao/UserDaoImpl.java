package project.dao;

import project.config.DbConfig;
import project.model.Employee;
import project.model.User;
import project.util.PasswordUtil;

import java.sql.*;

public class UserDaoImpl implements UserDao {

    @Override
    public User adminLogin(String username, String password) {
        String sql = "SELECT admin_id, username, password, permission_level, last_login FROM admins WHERE username = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordUtil.verifyAny(password, storedHash)) {
                    int adminId = rs.getInt("admin_id");

                    if (PasswordUtil.needsUpgrade(storedHash)) {
                        String newHash = PasswordUtil.hashSecure(password);
                        try (PreparedStatement up = conn.prepareStatement(
                                "UPDATE admins SET password = ? WHERE admin_id = ?")) {
                            up.setString(1, newHash);
                            up.setInt(2, adminId);
                            up.executeUpdate();
                        }
                    }

                    updateAdminLastLogin(adminId);
                    return mapUser(rs);
                }
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
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordUtil.verifyAny(password, storedHash)) {
                    Employee emp = mapEmployee(rs);

                    if (PasswordUtil.needsUpgrade(storedHash)) {
                        String newHash = PasswordUtil.hashSecure(password);
                        try (PreparedStatement up = conn.prepareStatement(
                                "UPDATE employees SET password = ? WHERE employee_id = ?")) {
                            up.setString(1, newHash);
                            up.setInt(2, emp.getEmployeeId());
                            up.executeUpdate();
                        }
                    }

                    updateEmployeeLastLogin(emp.getEmployeeId());
                    return emp;
                }
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

    public User findAdminById(int adminId) {
        String sql = "SELECT admin_id, username, password, permission_level FROM admins WHERE admin_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) {
            System.err.println("[UserDao] findAdminById: " + e.getMessage());
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
        } catch (SQLException e) {
            System.err.println("[UserDao] updateAdminLastLogin: " + e.getMessage());
        }
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
}
