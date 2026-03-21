package project.repository;

import project.config.DbConfig;
import project.model.LeaveRequest;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class LeaveRequestRepository {

    public boolean save(LeaveRequest leave) {
        String sql = "INSERT INTO leave_request (employee_id, start_date, end_date, leave_type, " +
                "reason, status, request_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, leave.getEmployeeId());
            ps.setDate(2, Date.valueOf(leave.getStartDate()));
            ps.setDate(3, Date.valueOf(leave.getEndDate()));
            ps.setString(4, leave.getLeaveType());
            ps.setString(5, leave.getReason());
            ps.setString(6, leave.getStatus());
            ps.setDate(7, Date.valueOf(leave.getRequestDate()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] save: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int leaveRequestId, String status, int reviewerId, String reviewNote) {
        String sql = "UPDATE leave_request SET status = ?, reviewer_id = ?, review_note = ?, " +
                "review_date = ? WHERE leave_request_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reviewerId);
            ps.setString(3, reviewNote);
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setInt(5, leaveRequestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] updateStatus: " + e.getMessage());
            return false;
        }
    }

    public LeaveRequest findById(int id) {
        String sql = "SELECT * FROM leave_request WHERE leave_request_id = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] findById: " + e.getMessage());
        }
        return null;
    }

    public List<LeaveRequest> findByEmployee(int employeeId) {
        String sql = "SELECT * FROM leave_request WHERE employee_id = ? ORDER BY request_date DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] findByEmployee: " + e.getMessage());
        }
        return list;
    }

    public List<LeaveRequest> findByStatus(String status) {
        String sql = "SELECT * FROM leave_request WHERE status = ? ORDER BY request_date ASC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] findByStatus: " + e.getMessage());
        }
        return list;
    }

    public List<LeaveRequest> findAll() {
        String sql = "SELECT * FROM leave_request ORDER BY request_date DESC";
        List<LeaveRequest> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] findAll: " + e.getMessage());
        }
        return list;
    }

    private LeaveRequest mapRow(ResultSet rs) throws SQLException {
        LeaveRequest lr = new LeaveRequest();
        lr.setLeaveRequestId(rs.getInt("leave_request_id"));
        lr.setEmployeeId(rs.getInt("employee_id"));
        lr.setStartDate(rs.getDate("start_date").toLocalDate());
        lr.setEndDate(rs.getDate("end_date").toLocalDate());
        lr.setLeaveType(rs.getString("leave_type"));
        lr.setReason(rs.getString("reason"));
        lr.setStatus(rs.getString("status"));

        int reviewerId = rs.getInt("reviewer_id");
        lr.setReviewerId(rs.wasNull() ? null : reviewerId);

        lr.setReviewNote(rs.getString("review_note"));

        Date reqDate = rs.getDate("request_date");
        lr.setRequestDate(reqDate != null ? reqDate.toLocalDate() : null);

        Date revDate = rs.getDate("review_date");
        lr.setReviewDate(revDate != null ? revDate.toLocalDate() : null);

        return lr;
    }


    public boolean hasOverlappingLeave(int employeeId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM leave_request " +
                "WHERE employee_id = ? AND status IN ('PENDING', 'APPROVED') " +
                "AND ((start_date <= ? AND end_date >= ?) " +  // New request overlaps existing
                "OR (start_date <= ? AND end_date >= ?) " +     // Existing overlaps new start
                "OR (start_date >= ? AND end_date <= ?))";      // Existing within new range
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(endDate));
            ps.setDate(3, Date.valueOf(startDate));
            ps.setDate(4, Date.valueOf(startDate));
            ps.setDate(5, Date.valueOf(startDate));
            ps.setDate(6, Date.valueOf(startDate));
            ps.setDate(7, Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] hasOverlappingLeave: " + e.getMessage());
        }
        return false;
    }


    public boolean hasDuplicateRequest(int employeeId, LocalDate startDate, LocalDate endDate, String leaveType) {
        String sql = "SELECT COUNT(*) FROM leave_request " +
                "WHERE employee_id = ? AND start_date = ? AND end_date = ? " +
                "AND leave_type = ? AND status = 'PENDING'";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            ps.setString(4, leaveType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] hasDuplicateRequest: " + e.getMessage());
        }
        return false;
    }


    public int countPendingRequests(int employeeId) {
        String sql = "SELECT COUNT(*) FROM leave_request WHERE employee_id = ? AND status = 'PENDING'";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] countPendingRequests: " + e.getMessage());
        }
        return 0;
    }


    public int getTotalLeaveDaysInMonth(int employeeId, int year, int month) {
        String sql = "SELECT SUM(end_date - start_date + 1) AS total_days " +
                "FROM leave_request " +
                "WHERE employee_id = ? AND status = 'APPROVED' " +
                "AND EXTRACT(YEAR FROM start_date) = ? " +
                "AND EXTRACT(MONTH FROM start_date) = ?";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int days = rs.getInt("total_days");
                return rs.wasNull() ? 0 : days;
            }
        } catch (SQLException e) {
            System.err.println("[LeaveRequestRepo] getTotalLeaveDaysInMonth: " + e.getMessage());
        }
        return 0;
    }
}
