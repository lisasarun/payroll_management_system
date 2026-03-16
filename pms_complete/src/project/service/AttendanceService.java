package project.service;

import project.dto.AttendanceDTO;
import project.mapper.EntityMapper;
import project.model.Attendance;
import project.model.Employee;
import project.repository.AttendanceRepository;
import project.repository.EmployeeRepository;
import project.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceService {

    private final AttendanceRepository attendanceRepo = new AttendanceRepository();
    private final EmployeeRepository   employeeRepo   = new EmployeeRepository();

    public boolean checkIn(int employeeId) {
        java.time.LocalTime now = java.time.LocalTime.now();
        if (now.isBefore(java.time.LocalTime.of(7, 0)) || now.isAfter(java.time.LocalTime.of(23, 0))) {
            System.out.println("  Check-in allowed only between 7:00 AM and 11:00 PM.");
            return false;
        }

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
        Employee emp = employeeRepo.findById(employeeId);
        String empName = emp != null ? emp.getFullName() : "";
        return attendanceRepo.findByEmployee(employeeId).stream()
                .map(a -> {
                    AttendanceDTO dto = EntityMapper.toAttendanceDTO(a);
                    dto.setEmployeeName(empName);
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
        List<Attendance> records = attendanceRepo.findAll(page, size);

        Map<Integer, String> empNames = records.stream()
                .map(Attendance::getEmployeeId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            Employee e = employeeRepo.findById(id);
                            return e != null ? e.getFullName() : "Unknown";
                        }
                ));

        return records.stream()
                .map(a -> {
                    AttendanceDTO dto = EntityMapper.toAttendanceDTO(a);
                    dto.setEmployeeName(empNames.getOrDefault(a.getEmployeeId(), "Unknown"));
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
