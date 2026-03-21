package project.repository;

import project.config.DbConfig;
import project.model.Performance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerformanceRepository {

    private Performance mapRow(ResultSet rs) throws SQLException {
        Performance p = new Performance();
        p.setPerformanceId(rs.getInt("performance_id"));
        p.setEmployeeId(rs.getInt("employee_id"));
        p.setReviewDate(rs.getDate("review_date").toLocalDate());
        p.setScore(rs.getBigDecimal("score"));
        p.setComments(rs.getString("comments"));
        p.setReviewerId(rs.getInt("reviewer_id"));
        return p;
    }

    public boolean save(Performance p) {
        String sql = "INSERT INTO performance (employee_id, review_date, score, comments, reviewer_id) VALUES (?,?,?,?,?)";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getEmployeeId());
            ps.setDate(2, Date.valueOf(p.getReviewDate()));
            ps.setBigDecimal(3, p.getScore());
            ps.setString(4, p.getComments());
            ps.setInt(5, p.getReviewerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("[PerfRepo] save: " + e.getMessage());

            if (e.getMessage().contains("check constraint")) {
                if (e.getMessage().contains("score")) {
                    System.out.println("  ✘ Score must be greater than 0 and up to 100.");

                }
            }
        }
        return false;
    }

    public List<Performance> findByEmployee(int employeeId) {
        List<Performance> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM performance WHERE employee_id = ? ORDER BY review_date DESC")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[PerfRepo] findByEmployee: " + e.getMessage()); }
        return list;
    }

    public Performance findLatestByEmployee(int employeeId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM performance WHERE employee_id = ? ORDER BY review_date DESC LIMIT 1")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[PerfRepo] findLatest: " + e.getMessage()); }
        return null;
    }

    public double getAverageScore(int employeeId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT AVG(score) FROM performance WHERE employee_id = ?")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println("[PerfRepo] avgScore: " + e.getMessage()); }
        return 0.0;
    }


    public java.util.Set<Integer> findEmployeeIdsWithReviews() {
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT DISTINCT employee_id FROM performance")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) { System.err.println("[PerfRepo] findEmployeeIdsWithReviews: " + e.getMessage()); }
        return ids;
    }


    public boolean hasReviewToday(int employeeId) {
        String sql = "SELECT 1 FROM performance WHERE employee_id = ? AND review_date = CURRENT_DATE";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { 
            System.err.println("[PerfRepo] hasReviewToday: " + e.getMessage()); 
        }
        return false;
    }


    public boolean update(int performanceId, double score, String comments) {
        String sql = "UPDATE performance SET score = ?, comments = ? WHERE performance_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(score));
            ps.setString(2, comments);
            ps.setInt(3, performanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("[PerfRepo] update: " + e.getMessage());
            // Check for constraint violations
            if (e.getMessage().contains("check constraint")) {
                if (e.getMessage().contains("score")) {
                    System.out.println("  ✘ Score must be greater than 0 and up to 100.");
                }
            }
        }
        return false;
    }


    public boolean delete(int performanceId) {
        String sql = "DELETE FROM performance WHERE performance_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, performanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("[PerfRepo] delete: " + e.getMessage());
        }
        return false;
    }


    public Performance findById(int performanceId) {
        String sql = "SELECT * FROM performance WHERE performance_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, performanceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { 
            System.err.println("[PerfRepo] findById: " + e.getMessage());
        }
        return null;
    }
}
