package project.controller;

import project.dto.LeaveRequestDTO;
import project.service.LeaveRequestService;
import project.util.InputUtil;
import project.util.ViewUtil;

import java.time.LocalDate;
import java.util.List;

public class LeaveRequestController {

    private final LeaveRequestService leaveService = new LeaveRequestService();


    public void submitLeaveRequest(int employeeId) {
        System.out.println(); // Add newline before showing leave request screen
        ViewUtil.printTitle("SUBMIT LEAVE REQUEST");

        System.out.println("  Leave Types: SICK, VACATION, PERSONAL, EMERGENCY");
        String leaveType = InputUtil.readString("  Leave Type     : ").toUpperCase();

        if (!List.of("SICK", "VACATION", "PERSONAL", "EMERGENCY").contains(leaveType)) {
            ViewUtil.printError("Invalid leave type.");
            return;
        }

        // Only allow dates within year 2026 (LocalDate.parse also blocks invalid dates like 2026-02-31)
        LocalDate startDate = InputUtil.readDateInYear("  Start Date     : ", 2026);
        LocalDate endDate   = InputUtil.readDateInYear("  End Date       : ", 2026);
        String reason = InputUtil.readLeaveReason("  Reason         ");

        if (leaveService.submitLeaveRequest(employeeId, startDate, endDate, leaveType, reason)) {
            ViewUtil.printSuccess("Leave request submitted successfully.");
        } else {
            ViewUtil.printError("Failed to submit leave request.");
        }
    }

    public void viewMyLeaveRequests(int employeeId) {
        ViewUtil.printTitle("MY LEAVE REQUESTS");
        List<LeaveRequestDTO> list = leaveService.getMyLeaveRequests(employeeId);

        if (list.isEmpty()) {
            ViewUtil.printInfo("No leave requests found.");
            return;
        }

        list.forEach(this::printLeaveRequest);
    }

    public void reviewLeaveRequests(int adminId) {
        boolean running = true;
        while (running) {
            ViewUtil.printTitle("REVIEW LEAVE REQUESTS");
            System.out.println("  [1] View Pending Requests");
            System.out.println("  [2] View All Requests");
            System.out.println("  [3] Approve/Reject Request");
            System.out.println("  [0] Back");
            System.out.print("  Select: ");

            switch (InputUtil.readMenuChoice("")) {
                case "1" -> viewPendingRequests();
                case "2" -> viewAllRequests();
                case "3" -> reviewRequest(adminId);
                case "0" -> running = false;
                default -> ViewUtil.printError("Invalid option.");
            }
        }
    }

    private void viewPendingRequests() {
        ViewUtil.printTitle("PENDING LEAVE REQUESTS");
        List<LeaveRequestDTO> list = leaveService.getPendingRequests();

        if (list.isEmpty()) {
            ViewUtil.printInfo("No pending leave requests.");
            return;
        }

        list.forEach(this::printLeaveRequest);
    }

    private void viewAllRequests() {
        ViewUtil.printTitle("ALL LEAVE REQUESTS");
        List<LeaveRequestDTO> list = leaveService.getAllRequests();

        if (list.isEmpty()) {
            ViewUtil.printInfo("No leave requests found.");
            return;
        }

        list.forEach(this::printLeaveRequest);
    }

    private void reviewRequest(int adminId) {
        int requestId = InputUtil.readInt("  Leave Request ID: ");

        System.out.println("  [1] Approve");
        System.out.println("  [2] Reject");
        String choice = InputUtil.readMenuChoice("  Select: ");

        String status = switch (choice) {
            case "1" -> "APPROVED";
            case "2" -> "REJECTED";
            default -> {
                ViewUtil.printError("Invalid choice.");
                yield null;
            }
        };

        if (status == null) return;

        String reviewNote = InputUtil.readString("  Review Note    : ");

        if (leaveService.reviewLeaveRequest(requestId, status, adminId, reviewNote)) {
            ViewUtil.printSuccess("Leave request " + status.toLowerCase() + ".");
        } else {
            ViewUtil.printError("Failed to update leave request.");
        }
    }

    private void printLeaveRequest(LeaveRequestDTO dto) {
        System.out.println("\n" + "─".repeat(70));
        System.out.printf("  ID: %d  |  Employee: %s  |  Status: %s%n",
                dto.getLeaveRequestId(), dto.getEmployeeName(), dto.getStatus());
        System.out.printf("  Type: %s  |  Days: %d%n", dto.getLeaveType(), dto.getDaysRequested());
        System.out.printf("  Period: %s to %s%n", dto.getStartDate(), dto.getEndDate());
        System.out.printf("  Reason: %s%n", dto.getReason());
        System.out.printf("  Requested: %s%n", dto.getRequestDate());

        if (dto.getReviewerName() != null) {
            System.out.printf("  Reviewed by: %s on %s%n", dto.getReviewerName(), dto.getReviewDate());
            if (dto.getReviewNote() != null) {
                System.out.printf("  Review Note: %s%n", dto.getReviewNote());
            }
        }
    }
}
