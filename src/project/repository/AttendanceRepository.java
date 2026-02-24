package project.repository;

import project.config.DbConfig;
import project.modal.Attendance;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
  JDBC repository for the attendance table.
  Used for check-in/out flow and payroll calculation.
 */
public class AttendanceRepository {

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance att = new Attendance();
        att.setAttendanceId(rs.getInt("attendance_id"));
        att.setEmployeeId(rs.getInt("employee_id"));
        att.setDate(rs.getDate("date").toLocalDate());

        Timestamp ci = rs.getTimestamp("check_in");
        if (ci != null) att.setCheckIn(ci.toLocalDateTime());
        Timestamp co = rs.getTimestamp("check_out");
        if (co != null) att.setCheckOut(co.toLocalDateTime());

        att.setStatus(rs.getString("status"));
        att.setWorkHours(rs.getBigDecimal("work_hours"));
        att.setOvertimeHours(rs.getBigDecimal("overtime_hours"));
        att.setLateMinutes(rs.getInt("late_minutes"));
        att.setEarlyLeaveMinutes(rs.getInt("early_leave_minutes"));
        att.setLeaveType(rs.getString("leave_type"));
        att.setNote(rs.getString("note"));
        return att;
    }

    //  Check In

    /**
      Creates today's attendance record with check-in time.
     */
    public boolean checkIn(int employeeId) {
        String sql = """
            INSERT INTO attendance (employee_id, date, check_in, status)
            VALUES (?, CURRENT_DATE, NOW(), 'PRESENT')
            ON CONFLICT (employee_id, date) DO NOTHING
            """;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AttendanceRepository] checkIn error: " + e.getMessage());
        }
        return false;
    }

    //  Check Out (with work hours calculation)

    public boolean checkOut(int employeeId) {
        String sql = """
            UPDATE attendance
            SET check_out = NOW(),
                work_hours = ROUND(EXTRACT(EPOCH FROM (NOW() - check_in)) / 3600.0, 2),
                overtime_hours = GREATEST(
                    ROUND(EXTRACT(EPOCH FROM (NOW() - check_in)) / 3600.0, 2) - 8, 0
                )
            WHERE employee_id = ? AND date = CURRENT_DATE AND check_out IS NULL
            """;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AttendanceRepository] checkOut error: " + e.getMessage());
        }
        return false;
    }

    // Find by Employee + Date Range

    public List<Attendance> findByEmployeeAndPeriod(int employeeId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT * FROM attendance
            WHERE employee_id = ? AND date BETWEEN ? AND ?
            ORDER BY date
            """;
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[AttendanceRepository] findByEmployeeAndPeriod error: " + e.getMessage());
        }
        return list;
    }

    //  Today's Record

    public Attendance findTodayRecord(int employeeId) {
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND date = CURRENT_DATE";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[AttendanceRepository] findTodayRecord error: " + e.getMessage());
        }
        return null;
    }

    //  Paginated list for Admin view

    public List<Attendance> findAll(int page, int size) {
        String sql = "SELECT * FROM attendance ORDER BY date DESC, attendance_id LIMIT ? OFFSET ?";
        List<Attendance> list = new ArrayList<>();
        int offset = (page - 1) * size;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[AttendanceRepository] findAll error: " + e.getMessage());
        }
        return list;
    }

    // Update status/note manually

    public boolean updateStatus(int attendanceId, String status, String note) {
        String sql = "UPDATE attendance SET status = ?, note = ? WHERE attendance_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setInt(3, attendanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AttendanceRepository] updateStatus error: " + e.getMessage());
        }
        return false;
    }
}
