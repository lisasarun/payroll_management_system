package project.dao;

import project.config.DbConfig;
import project.modal.Employee;
import project.modal.User;

import java.sql.*;

/**
  JDBC implementation of UserDao.

  Security notes:
    - Passwords should be BCrypt-hashed before storage.
    - This impl does a direct hash comparison; the actual hashing
      belongs in AuthService (not here), but we accept the hash string
      to compare with the stored value.
    - Never logs or returns raw passwords.

  Tables used:  admins, employees  (per ERD)
 */
public class UserDaoImpl implements UserDao {

    //  Admin Login

    /**
      Verifies admin credentials.
      Compares the provided password against the stored password hash.
      NOTE: In production, replace direct comparison with BCrypt.checkpw().
     */
    @Override
    public User adminLogin(String username, String password) {
        // Query by username first, then verify password in Java
        // (avoids timing attacks from DB-level password comparison)
        String sql = "SELECT admin_id, username, password, permission_level " +
                "FROM admins WHERE username = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // Password check — swap for BCrypt.checkpw(password, storedHash) if hashing is used
                if (storedHash != null && storedHash.equals(password)) {
                    User user = mapAdminRow(rs);
                    updateAdminLastLogin(user.getAdminId()); // track login time
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] adminLogin error: " + e.getMessage());
        }
        return null; // authentication failed
    }

    // ─── Employee Login ───────────────────────────────────────────────────────

    /**
      Verifies employee credentials.
      Only active employees (is_active = TRUE) can log in.
     */
    @Override
    public Employee employeeLogin(String email, String password) {
        String sql = "SELECT employee_id, full_name, email, password, is_active, " +
                "last_login, created_at, base_salary " +
                "FROM employees WHERE email = ? AND is_active = TRUE";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (storedHash != null && storedHash.equals(password)) {
                    Employee emp = mapEmployeeRow(rs);
                    updateEmployeeLastLogin(emp.getEmployeeId());
                    return emp;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] employeeLogin error: " + e.getMessage());
        }
        return null;
    }

    //  Find Admin by Username

    @Override
    public User findAdminByUsername(String username) {
        String sql = "SELECT admin_id, username, password, permission_level " +
                "FROM admins WHERE username = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapAdminRow(rs);

        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] findAdminByUsername error: " + e.getMessage());
        }
        return null;
    }

    //  Find Employee by Email

    @Override
    public Employee findEmployeeByEmail(String email) {
        String sql = "SELECT employee_id, full_name, email, password, is_active, " +
                "last_login, created_at, base_salary " +
                "FROM employees WHERE email = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapEmployeeRow(rs);

        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] findEmployeeByEmail error: " + e.getMessage());
        }
        return null;
    }

    //  Update Last Login — Admin

    @Override
    public void updateAdminLastLogin(int adminId) {
        // admins table may not have last_login per ERD — adapt if team adds it
        // Included here for completeness; remove if column doesn't exist
        String sql = "UPDATE admins SET last_login = NOW() WHERE admin_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.executeUpdate();

        } catch (SQLException e) {
            // Silently ignore if column doesn't exist yet
            // System.err.println("[UserDaoImpl] updateAdminLastLogin: " + e.getMessage());
        }
    }

    // Update Last Login — Employee

    @Override
    public void updateEmployeeLastLogin(int employeeId) {
        String sql = "UPDATE employees SET last_login = NOW() WHERE employee_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] updateEmployeeLastLogin error: " + e.getMessage());
        }
    }

    //  Existence Checks

    @Override
    public boolean adminUsernameExists(String username) {
        String sql = "SELECT 1 FROM admins WHERE username = ?";
        return existsCheck(sql, username);
    }

    @Override
    public boolean employeeEmailExists(String email) {
        String sql = "SELECT 1 FROM employees WHERE email = ?";
        return existsCheck(sql, email);
    }

    //  Private Helpers

    /** Reusable existence check for single-string parameter queries. */
    private boolean existsCheck(String sql, String param) {
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("[UserDaoImpl] existsCheck error: " + e.getMessage());
        }
        return false;
    }

    /**
      Maps a ResultSet row → User (admin) model.
      Manual mapping — no ORM.
     */
    private User mapAdminRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setAdminId(rs.getInt("admin_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // hashed; only used internally
        user.setPermissionLevel(rs.getString("permission_level"));
        return user;
    }

    /**
      Maps a ResultSet row → Employee model.
     */
    private Employee mapEmployeeRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setEmployeeId(rs.getInt("employee_id"));
        emp.setFullName(rs.getString("full_name"));
        emp.setEmail(rs.getString("email"));
        emp.setPassword(rs.getString("password")); // hashed
        emp.setActive(rs.getBoolean("is_active"));
        emp.setBaseSalary(rs.getBigDecimal("base_salary"));

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) emp.setLastLogin(lastLogin.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) emp.setCreatedAt(createdAt.toLocalDateTime());

        return emp;
    }
}