package project.repository;

import project.config.DbConfig;
import project.modal.Performance;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
  JDBC repository for the performance table.
  Used by PerformanceService for reviews and bonus eligibility checks.
 */
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

    // ─── Save Review ──────────────────────────────────────────────────────────

    public boolean save(Performance perf) {
        String sql = """
            INSERT INTO performance (employee_id, review_date, score, comments, reviewer_id)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, perf.getEmployeeId());
            ps.setDate(2, Date.valueOf(perf.getReviewDate()));
            ps.setBigDecimal(3, perf.getScore());
            ps.setString(4, perf.getComments());
            ps.setInt(5, perf.getReviewerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PerformanceRepository] save error: " + e.getMessage());
        }
        return false;
    }

    // ─── Latest Score for Employee ────────────────────────────────────────────

    public Performance findLatestByEmployee(int employeeId) {
        String sql = "SELECT * FROM performance WHERE employee_id = ? ORDER BY review_date DESC LIMIT 1";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[PerformanceRepository] findLatest error: " + e.getMessage());
        }
        return null;
    }

    // ─── All Reviews for Employee (paginated) ─────────────────────────────────

    public List<Performance> findByEmployee(int employeeId, int page, int size) {
        String sql = "SELECT * FROM performance WHERE employee_id = ? ORDER BY review_date DESC LIMIT ? OFFSET ?";
        List<Performance> list = new ArrayList<>();
        int offset = (page - 1) * size;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, size);
            ps.setInt(3, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PerformanceRepository] findByEmployee error: " + e.getMessage());
        }
        return list;
    }

    // ─── Average score for period ─────────────────────────────────────────────

    public double getAverageScore(int employeeId, LocalDate from, LocalDate to) {
        String sql = "SELECT AVG(score) FROM performance WHERE employee_id = ? AND review_date BETWEEN ? AND ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[PerformanceRepository] getAverageScore error: " + e.getMessage());
        }
        return 0.0;
    }
}