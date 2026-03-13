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


public class LeaveRequestService {

    private final LeaveRequestRepository leaveRepo = new LeaveRequestRepository();
    private final EmployeeRepository empRepo = new EmployeeRepository();
    private final UserDaoImpl userDao = new UserDaoImpl();


    public boolean submitLeaveRequest(int employeeId, LocalDate startDate, LocalDate endDate,
                                      String leaveType, String reason) {

        if (startDate.isAfter(endDate)) {
            System.out.println("  Start date must be before end date.");
            return false;
        }

        if (startDate.isBefore(LocalDate.now().plusDays(1))) {
            System.out.println("  Cannot request leave for today or past dates. Request at least 1 day in advance.");
            return false;
        }

        if (startDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
            startDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY ||
            endDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
            endDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            System.out.println("  Cannot request leave for weekends (Saturdays/Sundays are non-working days).");
            return false;
        }

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


    public boolean reviewLeaveRequest(int leaveRequestId, String status, int reviewerId, String reviewNote) {
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            System.out.println("  Invalid status. Use APPROVED or REJECTED.");
            return false;
        }
        return leaveRepo.updateStatus(leaveRequestId, status, reviewerId, reviewNote);
    }


    public List<LeaveRequestDTO> getMyLeaveRequests(int employeeId) {
        return leaveRepo.findByEmployee(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<LeaveRequestDTO> getPendingRequests() {
        return leaveRepo.findByStatus("PENDING").stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<LeaveRequestDTO> getAllRequests() {
        return leaveRepo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


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


        long days = ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1;
        dto.setDaysRequested((int) days);


        Employee emp = empRepo.findById(lr.getEmployeeId());
        dto.setEmployeeName(emp != null ? emp.getFullName() : "Unknown");


        if (lr.getReviewerId() != null) {
            User admin = userDao.findAdminById(lr.getReviewerId());
            dto.setReviewerName(admin != null ? admin.getUsername() : "Unknown");
        }

        return dto;
    }
}
