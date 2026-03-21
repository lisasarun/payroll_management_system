package project.service;

import project.dto.LeaveRequestDTO;
import project.model.Employee;
import project.model.LeaveRequest;
import project.model.User;
import project.repository.EmployeeRepository;
import project.repository.LeaveRequestRepository;
import project.dao.UserDaoImpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for leave request management.
 */
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRepo = new LeaveRequestRepository();
    private final EmployeeRepository empRepo = new EmployeeRepository();
    private final UserDaoImpl userDao = new UserDaoImpl();

    /**
     * Employee submits a leave request with comprehensive validation.
     */
    public boolean submitLeaveRequest(int employeeId, LocalDate startDate, LocalDate endDate,
                                      String leaveType, String reason) {
        // Validation #1: Start date must be >= today
        if (startDate.isBefore(LocalDate.now())) {
            System.out.println("  Start date cannot be in the past.");
            return false;
        }

        // Validation #2: End date must be >= start date
        if (startDate.isAfter(endDate)) {
            System.out.println("  End date must be on or after start date.");
            return false;
        }

        // Validation #3: Only allow leave inside year 2026
        if (startDate.getYear() != 2026 || endDate.getYear() != 2026) {
            System.out.println("  Leave requests are only allowed in the year 2026.");
            return false;
        }

        // Validation #4: Max duration = 7 days (1 week) per request
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysBetween > 7) {
            System.out.println("  Leave request cannot exceed 7 days.");
            return false;
        }

        // Validation #5: Cannot request leave for weekends
        if (startDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                startDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY ||
                endDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                endDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            System.out.println("  Cannot request leave for weekends (Saturdays/Sundays are non-working days).");
            return false;
        }

        // Validation #6: Check for overlapping leave requests
        if (leaveRepo.hasOverlappingLeave(employeeId, startDate, endDate)) {
            System.out.println("  You already have a leave request that overlaps with these dates.");
            return false;
        }

        // Validation #7: Check for duplicate request (same dates + same type)
        if (leaveRepo.hasDuplicateRequest(employeeId, startDate, endDate, leaveType)) {
            System.out.println("  You already have a pending request with the same dates and type.");
            return false;
        }

        // Validation #8: Max 3 pending requests per employee
        int pendingCount = leaveRepo.countPendingRequests(employeeId);
        if (pendingCount >= 3) {
            System.out.println("  You have reached the maximum of 3 pending leave requests.");
            System.out.println("  Please wait for approval or cancellation before submitting more.");
            return false;
        }

        // Validation #9: Max 7 total leave days per month
        int month = startDate.getMonthValue();
        int year = startDate.getYear();
        int existingDays = leaveRepo.getTotalLeaveDaysInMonth(employeeId, year, month);
        int requestedDays = (int) daysBetween;
        
        if (existingDays + requestedDays > 7) {
            System.out.println("  Monthly leave limit exceeded.");
            System.out.printf("  You have used %d days in %s %d. Requesting %d more would exceed the 7-day limit.%n",
                    existingDays, startDate.getMonth(), year, requestedDays);
            return false;
        }

        // All validations passed - create and save leave request
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(employeeId);
        lr.setStartDate(startDate);
        lr.setEndDate(endDate);
        lr.setLeaveType(leaveType);
        lr.setReason(reason);
        lr.setStatus("PENDING");
        lr.setRequestDate(LocalDate.now());

        return leaveRepo.save(lr);
    }

    /**
     * Admin approves or rejects a leave request.
     */
    public boolean reviewLeaveRequest(int leaveRequestId, String status, int reviewerId, String reviewNote) {
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            System.out.println("  Invalid status. Use APPROVED or REJECTED.");
            return false;
        }
        LeaveRequest existing = leaveRepo.findById(leaveRequestId);
        if (existing == null) {
            System.out.println("  Leave request not found.");
            return false;
        }
        Employee employee = empRepo.findById(existing.getEmployeeId());
        if (employee == null || !employee.isActive()) {
            System.out.println("  Cannot review request for a disabled or missing employee.");
            return false;
        }
        return leaveRepo.updateStatus(leaveRequestId, status, reviewerId, reviewNote);
    }

    /**
     * Get all leave requests for a specific employee.
     */
    public List<LeaveRequestDTO> getMyLeaveRequests(int employeeId) {
        return leaveRepo.findByEmployee(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending leave requests (for admin review).
     */
    public List<LeaveRequestDTO> getPendingRequests() {
        return leaveRepo.findByStatus("PENDING").stream()
                .filter(this::isForActiveEmployee)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all leave requests (for admin view).
     */
    public List<LeaveRequestDTO> getAllRequests() {
        return leaveRepo.findAll().stream()
                .filter(this::isForActiveEmployee)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private boolean isForActiveEmployee(LeaveRequest lr) {
        Employee employee = empRepo.findById(lr.getEmployeeId());
        return employee != null && employee.isActive();
    }

    /**
     * Convert LeaveRequest Entity to DTO with employee/reviewer names.
     */
    private LeaveRequestDTO toDTO(LeaveRequest lr) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveRequestId(lr.getLeaveRequestId());
        dto.setEmployeeId(lr.getEmployeeId());
        dto.setStartDate(lr.getStartDate());
        dto.setEndDate(lr.getEndDate());
        dto.setLeaveType(lr.getLeaveType());
        dto.setReason(lr.getReason());
        dto.setStatus(lr.getStatus());
        dto.setReviewNote(lr.getReviewNote());
        dto.setRequestDate(lr.getRequestDate());
        dto.setReviewDate(lr.getReviewDate());

        // Calculate days
        long days = ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1;
        dto.setDaysRequested((int) days);

        // Employee name
        Employee emp = empRepo.findById(lr.getEmployeeId());
        dto.setEmployeeName(emp != null ? emp.getFullName() : "Unknown");

        // Reviewer name
        if (lr.getReviewerId() != null) {
            User admin = userDao.findAdminById(lr.getReviewerId());
            dto.setReviewerName(admin != null ? admin.getUsername() : "Unknown");
        }

        return dto;
    }
}
