package project.repository;

import project.config.DbConfig;
import project.modal.User; // team's model

import java.sql.*;

/**
 Handles DB access for the admins table.
 Called by AuthService for admin login.
 */
public class UserRepository {

    /**
      Find admin by username for login verification.
     */
    public User findAdminByUsername(String username) {
        String sql = "SELECT admin_id, username, password, permission_level FROM admins WHERE username = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                // Manual mapping ResultSet → User model
                user.setAdminId(rs.getInt("admin_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password")); // hashed
                user.setPermissionLevel(rs.getString("permission_level"));
                return user;
            }

        } catch (SQLException e) {
            System.err.println("[UserRepository] Error finding admin: " + e.getMessage());
        }
        return null;
    }

    /**
      Update last login timestamp for admin (optional tracking).
     */
    public void updateLastLogin(int adminId) {
        String sql = "UPDATE admins SET last_login = NOW() WHERE admin_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserRepository] Error updating last login: " + e.getMessage());
        }
    }
}
