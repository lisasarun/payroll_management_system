package project.service;

import project.dto.AttendanceDTO;
import project.mapper.EntityMapper;
import project.model.Attendance;
import project.repository.AttendanceRepository;
import project.repository.EmployeeRepository;
import project.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceService {

    private final AttendanceRepository attendanceRepo = new AttendanceRepository();
    private final EmployeeRepository   employeeRepo   = new EmployeeRepository();

    public boolean checkIn(int employeeId) {
        Attendance today = attendanceRepo.findTodayRecord(employeeId);
        if (today != null && today.getCheckIn() != null) {
            System.out.println("  You have already checked in today.");
            return false;
        }
        return attendanceRepo.checkIn(employeeId);
    }

    public boolean checkOut(int employeeId) {
        Attendance today = attendanceRepo.findTodayRecord(employeeId);
        if (today == null || today.getCheckIn() == null) {
            System.out.println("  No check-in record found for today.");
            return false;
        }
        if (today.getCheckOut() != null) {
            System.out.println("  You have already checked out today.");
            return false;
        }
        return attendanceRepo.checkOut(employeeId);
    }

    public AttendanceDTO getTodayRecord(int employeeId) {
        return EntityMapper.toAttendanceDTO(attendanceRepo.findTodayRecord(employeeId));
    }

    public List<AttendanceDTO> getByEmployee(int employeeId) {
        return attendanceRepo.findByEmployee(employeeId).stream()
                .map(a -> {
                    AttendanceDTO dto = EntityMapper.toAttendanceDTO(a);
                    var emp = employeeRepo.findById(employeeId);
                    if (emp != null) dto.setEmployeeName(emp.getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getByPeriod(int employeeId, LocalDate from, LocalDate to) {
        return attendanceRepo.findByEmployeeAndPeriod(employeeId, from, to).stream()
                .map(EntityMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAllPaged(int page, int size) {
        return attendanceRepo.findAll(page, size).stream()
                .map(a -> {
                    AttendanceDTO dto = EntityMapper.toAttendanceDTO(a);
                    var emp = employeeRepo.findById(a.getEmployeeId());
                    if (emp != null) dto.setEmployeeName(emp.getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public int countAll() { return attendanceRepo.countAll(); }

    public double getAttendancePercentage(int employeeId, int month, int year) {
        LocalDate from = DateUtil.firstDayOfMonth(month, year);
        LocalDate to   = DateUtil.lastDayOfMonth(month, year);
        List<Attendance> records = attendanceRepo.findByEmployeeAndPeriod(employeeId, from, to);
        int workDays = DateUtil.getWorkingDaysInMonth(month, year);
        if (workDays == 0) return 0.0;
        long present = records.stream()
                .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()))
                .count();
        return (present * 100.0) / workDays;
    }
}
