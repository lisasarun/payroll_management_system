package project.mapper;

import project.dto.*;
import project.model.*;

import java.math.BigDecimal;

/**
 Manual mapper between Model <=> DTO.
 No MapStruct — pure setter/getter mapping as required by project rules.
 */
public class EntityMapper {

    //  Employee

    public static EmployeeDTO toEmployeeDTO(Employee e) {
        if (e == null) return null;
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(e.getEmployeeId());
        dto.setFullName(e.getFullName());
        dto.setEmail(e.getEmail());
        dto.setActive(e.isActive());
        dto.setBaseSalary(e.getBaseSalary());
        dto.setPosition(e.getPosition());
        dto.setDepartment(e.getDepartment());
        dto.setHireDate(e.getHireDate());
        dto.setLastLogin(e.getLastLogin());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    public static Employee toEmployee(EmployeeDTO dto) {
        if (dto == null) return null;
        Employee e = new Employee();
        e.setEmployeeId(dto.getEmployeeId());
        e.setFullName(dto.getFullName());
        e.setEmail(dto.getEmail());
        e.setActive(dto.isActive());
        e.setBaseSalary(dto.getBaseSalary());
        e.setPosition(dto.getPosition());
        e.setDepartment(dto.getDepartment());
        e.setHireDate(dto.getHireDate());
        return e;
    }

    //  Attendance

    public static AttendanceDTO toAttendanceDTO(Attendance a) {
        if (a == null) return null;
        AttendanceDTO dto = new AttendanceDTO();
        dto.setAttendanceId(a.getAttendanceId());
        dto.setEmployeeId(a.getEmployeeId());
        dto.setDate(a.getDate());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setStatus(a.getStatus());
        dto.setWorkHours(a.getWorkHours());
        dto.setOvertimeHours(a.getOvertimeHours());
        dto.setLateMinutes(a.getLateMinutes());
        dto.setEarlyLeaveMinutes(a.getEarlyLeaveMinutes());
        dto.setLeaveType(a.getLeaveType());
        dto.setNote(a.getNote());
        return dto;
    }

    // Performance

    public static PerformanceDTO toPerformanceDTO(Performance p) {
        if (p == null) return null;
        PerformanceDTO dto = new PerformanceDTO();
        dto.setPerformanceId(p.getPerformanceId());
        dto.setEmployeeId(p.getEmployeeId());
        dto.setReviewDate(p.getReviewDate());
        dto.setScore(p.getScore());
        dto.setComments(p.getComments());
        dto.setReviewerId(p.getReviewerId());
        return dto;
    }

    public static Performance toPerformance(PerformanceDTO dto) {
        if (dto == null) return null;
        Performance p = new Performance();
        p.setEmployeeId(dto.getEmployeeId());
        p.setReviewDate(dto.getReviewDate());
        p.setScore(dto.getScore());
        p.setComments(dto.getComments());
        p.setReviewerId(dto.getReviewerId());
        return p;
    }

    //  Payroll

    public static PayrollDTO toPayrollDTO(Payroll p) {
        if (p == null) return null;
        PayrollDTO dto = new PayrollDTO();
        dto.setPayrollId(p.getPayrollId());
        dto.setEmployeeId(p.getEmployeeId());
        dto.setPayPeriodStart(p.getPayPeriodStart());
        dto.setPayPeriodEnd(p.getPayPeriodEnd());
        dto.setBaseSalary(p.getBaseSalary());
        dto.setBonus(p.getBonus());
        dto.setDeductions(p.getDeductions());
        dto.setTotalPaid(p.getTotalPaid());
        dto.setPaymentDate(p.getPaymentDate());
        return dto;
    }

    //  User

    public static UserDTO toUserDTO(User u) {
        if (u == null) return null;
        UserDTO dto = new UserDTO();
        dto.setAdminId(u.getAdminId());
        dto.setUsername(u.getUsername());
        dto.setPermissionLevel(u.getPermissionLevel());
        return dto;
    }
}
