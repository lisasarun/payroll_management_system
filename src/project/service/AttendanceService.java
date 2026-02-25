package project.service;

import project.dao.EmployeeDao;
import project.dao.EmlpoyeeDaoImpl;
import project.dto.AttendanceDTO;
import project.mapper.EntityMapper;
import project.model.Attendance;
import project.model.Employee;
import project.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceService {
    private final EmployeeDao employeeDao;

    public AttendanceService() {
        this.employeeDao = new EmlpoyeeDaoImpl();
    }

    public boolean markAttendance(AttendanceDTO attendanceDTO) {
        try {
            Attendance attendance = EntityMapper.toAttendance(attendanceDTO);
            attendance.setDate(LocalDate.now());

            Employee employee = employeeDao.findById(attendanceDTO.getEmployeeId());
            if (employee != null) {
                employee.addAttendance(attendance);
                return employeeDao.update(employee);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            return false;
        }
    }

    public List<AttendanceDTO> getAttendanceByEmployee(int employeeId) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            if (employee != null) {
                return employee.getAttendances().stream()
                        .map(EntityMapper::toAttendanceDTO)
                        .collect(Collectors.toList());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching attendance: " + e.getMessage());
            return null;
        }
    }

    public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
        try {
            List<Employee> employees = employeeDao.findAll(null);
            return employees.stream()
                    .flatMap(e -> e.getAttendances().stream())
                    .filter(a -> a.getDate().equals(date))
                    .map(EntityMapper::toAttendanceDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching attendance by date: " + e.getMessage());
            return null;
        }
    }

    public double getAttendancePercentage(int employeeId, int month, int year) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            if (employee != null) {
                long presentDays = employee.getAttendances().stream()
                        .filter(a -> a.getDate().getMonthValue() == month &&
                                a.getDate().getYear() == year &&
                                "PRESENT".equals(a.getStatus()))
                        .count();

                int totalWorkingDays = DateUtil.getWorkingDaysInMonth(month, year);
                return (presentDays * 100.0) / totalWorkingDays;
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating attendance percentage: " + e.getMessage());
            return 0.0;
        }
    }
}