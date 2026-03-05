package project.repository;

import project.config.DbConfig;
import project.model.Attendance;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRepository {

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttendanceId(rs.getInt("attendance_id"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setDate(rs.getDate("date").toLocalDate());
        Timestamp ci = rs.getTimestamp("check_in");
        if (ci != null) a.setCheckIn(ci.toLocalDateTime());
        Timestamp co = rs.getTimestamp("check_out");
        if (co != null) a.setCheckOut(co.toLocalDateTime());
        a.setStatus(rs.getString("status"));
        a.setWorkHours(rs.getBigDecimal("work_hours"));
        a.setOvertimeHours(rs.getBigDecimal("overtime_hours"));
        a.setLateMinutes(rs.getInt("late_minutes"));
        a.setEarlyLeaveMinutes(rs.getInt("early_leave_minutes"));
        a.setLeaveType(rs.getString("leave_type"));
        a.setNote(rs.getString("note"));
        return a;
    }

    public boolean checkIn(int employeeId) {
        String sql = "INSERT INTO attendance (employee_id, date, check_in, status) " +
                "VALUES (?, CURRENT_DATE, NOW(), 'PRESENT') " +
                "ON CONFLICT (employee_id, date) DO NOTHING";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[AttRepo] checkIn: " + e.getMessage()); }
        return false;
    }

    public boolean checkOut(int employeeId) {
        String sql = "UPDATE attendance SET check_out = NOW(), " +
                "work_hours = ROUND(EXTRACT(EPOCH FROM (NOW()-check_in))/3600.0,2), " +
                "overtime_hours = GREATEST(ROUND(EXTRACT(EPOCH FROM (NOW()-check_in))/3600.0,2)-8,0) " +
                "WHERE employee_id = ? AND date = CURRENT_DATE AND check_out IS NULL";
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("[AttRepo] checkOut: " + e.getMessage()); }
        return false;
    }

    public Attendance findTodayRecord(int employeeId) {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM attendance WHERE employee_id = ? AND date = CURRENT_DATE")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { System.err.println("[AttRepo] findToday: " + e.getMessage()); }
        return null;
    }

    public List<Attendance> findByEmployee(int employeeId) {
        List<Attendance> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM attendance WHERE employee_id = ? ORDER BY date DESC")) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[AttRepo] findByEmployee: " + e.getMessage()); }
        return list;
    }

    public List<Attendance> findByEmployeeAndPeriod(int employeeId, LocalDate from, LocalDate to) {
        List<Attendance> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM attendance WHERE employee_id=? AND date BETWEEN ? AND ? ORDER BY date")) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[AttRepo] findByPeriod: " + e.getMessage()); }
        return list;
    }

    public List<Attendance> findAll(int page, int size) {
        List<Attendance> list = new ArrayList<>();
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM attendance ORDER BY date DESC LIMIT ? OFFSET ?")) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("[AttRepo] findAll: " + e.getMessage()); }
        return list;
    }

    public int countAll() {
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM attendance")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[AttRepo] countAll: " + e.getMessage()); }
        return 0;
    }
}
