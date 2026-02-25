package project.controller;



import project.dto.AttendanceDTO;
import project.dto.EmployeeDTO;
import project.dto.PerformanceDTO;
import project.dto.PayrollDTO;
import project.model.Payslip;
import project.service.*;
import project.util.InputUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EmployeeController {
    private final AuthService authService;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final PayrollService payrollService;
    private final PerformanceService performanceService;
    private final InputUtil inputUtil;
    private EmployeeDTO currentEmployee;

    public EmployeeController(AuthService authService) {
        this.authService = authService;
        this.employeeService = new EmployeeService();
        this.attendanceService = new AttendanceService();
        this.payrollService = new PayrollService();
        this.performanceService = new PerformanceService();
        this.inputUtil = new InputUtil();
        loadCurrentEmployee();
    }

    private void loadCurrentEmployee() {
        // Assuming the current user is linked to an employee record
        // You might need to modify this based on your user-employee relationship
        String username = authService.getCurrentUser().getUsername();
        // For now, we'll search by name or you can add a method to find by username
        List<EmployeeDTO> employees = employeeService.searchEmployees(username);
        if (employees != null && !employees.isEmpty()) {
            currentEmployee = employees.get(0);
        }
    }

    public void showEmployeeMenu() {
        while (true) {
            System.out.println("\n=== EMPLOYEE DASHBOARD ===");
            System.out.println("Welcome, " + currentEmployee.getFirstName() + " " + currentEmployee.getLastName());
            System.out.println("1. View My Profile");
            System.out.println("2. Mark Attendance");
            System.out.println("3. View My Attendance");
            System.out.println("4. View My Payroll");
            System.out.println("5. View My Performance Reviews");
            System.out.println("6. Generate My Payslip");
            System.out.println("7. Logout");

            int choice = inputUtil.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    viewMyProfile();
                    break;
                case 2:
                    markAttendance();
                    break;
                case 3:
                    viewMyAttendance();
                    break;
                case 4:
                    viewMyPayroll();
                    break;
                case 5:
                    viewMyPerformances();
                    break;
                case 6:
                    generateMyPayslip();
                    break;
                case 7:
                    authService.logout();
                    System.out.println("Logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private void viewMyProfile() {
        System.out.println("\n=== MY PROFILE ===");
        System.out.println("Employee ID: " + currentEmployee.getEmployeeId());
        System.out.println("Name: " + currentEmployee.getFirstName() + " " + currentEmployee.getLastName());
        System.out.println("Email: " + currentEmployee.getEmail());
        System.out.println("Phone: " + currentEmployee.getPhone());
        System.out.println("Department: " + currentEmployee.getDepartment());
        System.out.println("Position: " + currentEmployee.getPosition());
        System.out.println("Salary: $" + String.format("%.2f", currentEmployee.getSalary()));
        System.out.println("Hire Date: " + currentEmployee.getHireDate());
    }

    private void markAttendance() {
        System.out.println("\n=== MARK ATTENDANCE ===");
        System.out.println("Current Date: " + LocalDate.now());
        System.out.println("Current Time: " + LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

        System.out.println("1. Check In");
        System.out.println("2. Check Out");

        int choice = inputUtil.readInt("Enter your choice: ");

        AttendanceDTO attendance = new AttendanceDTO();
        attendance.setEmployeeId(currentEmployee.getEmployeeId());
        attendance.setDate(LocalDate.now());

        if (choice == 1) {
            attendance.setCheckIn(LocalTime.now());
            attendance.setStatus("PRESENT");
            attendance.setRemarks("Regular check-in");
        } else if (choice == 2) {
            attendance.setCheckOut(LocalTime.now());
            attendance.setStatus("COMPLETED");
            attendance.setRemarks("Regular check-out");
        } else {
            System.out.println("Invalid choice!");
            return;
        }

        if (attendanceService.markAttendance(attendance)) {
            System.out.println("Attendance marked successfully!");
        } else {
            System.out.println("Failed to mark attendance!");
        }
    }

    private void viewMyAttendance() {
        System.out.println("\n=== MY ATTENDANCE RECORDS ===");

        List<AttendanceDTO> attendanceList = attendanceService.getAttendanceByEmployee(currentEmployee.getEmployeeId());

        if (attendanceList != null && !attendanceList.isEmpty()) {
            System.out.println("Date\t\tStatus\t\tCheck In\tCheck Out\tRemarks");
            System.out.println("----------------------------------------------------------------");

            attendanceList.forEach(a -> {
                String checkIn = a.getCheckIn() != null ? a.getCheckIn().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "-";
                String checkOut = a.getCheckOut() != null ? a.getCheckOut().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "-";

                System.out.printf("%s\t%s\t%s\t\t%s\t\t%s\n",
                        a.getDate(),
                        a.getStatus(),
                        checkIn,
                        checkOut,
                        a.getRemarks() != null ? a.getRemarks() : "-");
            });

            // Calculate attendance statistics for current month
            LocalDate now = LocalDate.now();
            double attendancePercentage = attendanceService.getAttendancePercentage(
                    currentEmployee.getEmployeeId(),
                    now.getMonthValue(),
                    now.getYear()
            );

            System.out.println("\nCurrent Month Attendance: " + String.format("%.2f", attendancePercentage) + "%");
        } else {
            System.out.println("No attendance records found!");
        }
    }

    private void viewMyPayroll() {
        System.out.println("\n=== MY PAYROLL HISTORY ===");

        List<PayrollDTO> payrolls = payrollService.getEmployeePayrolls(currentEmployee.getEmployeeId());

        if (payrolls != null && !payrolls.isEmpty()) {
            System.out.println("ID\tMonth/Year\tBasic Salary\tNet Salary\tStatus");
            System.out.println("--------------------------------------------------------");

            payrolls.forEach(p -> {
                System.out.printf("%d\t%d/%d\t\t$%.2f\t\t$%.2f\t%s\n",
                        p.getPayrollId(),
                        p.getMonth(),
                        p.getYear(),
                        p.getBasicSalary(),
                        p.getNetSalary(),
                        p.getStatus());
            });
        } else {
            System.out.println("No payroll records found!");
        }
    }

    private void viewMyPerformances() {
        System.out.println("\n=== MY PERFORMANCE REVIEWS ===");

        List<PerformanceDTO> performances = performanceService.getEmployeePerformances(currentEmployee.getEmployeeId());

        if (performances != null && !performances.isEmpty()) {
            System.out.println("Date\t\tRating\tReviewer\tComments");
            System.out.println("------------------------------------------------");

            performances.forEach(p -> {
                String stars = getStarRating(p.getRating());
                System.out.printf("%s\t%d %s\t%s\t%s\n",
                        p.getReviewDate(),
                        p.getRating(),
                        stars,
                        p.getReviewer(),
                        p.getComments() != null ? p.getComments() : "-");
            });

            double avgRating = performanceService.getAverageRating(currentEmployee.getEmployeeId());
            System.out.println("\nAverage Rating: " + String.format("%.2f", avgRating) + " " + getStarRating((int)Math.round(avgRating)));
        } else {
            System.out.println("No performance reviews found!");
        }
    }

    private String getStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("⭐");
        }
        return stars.toString();
    }

    private void generateMyPayslip() {
        System.out.println("\n=== GENERATE PAYSLIP ===");

        List<PayrollDTO> payrolls = payrollService.getEmployeePayrolls(currentEmployee.getEmployeeId());

        if (payrolls != null && !payrolls.isEmpty()) {
            System.out.println("Select Payroll ID to generate payslip:");
            payrolls.forEach(p -> {
                System.out.printf("%d - %d/%d (%s)\n",
                        p.getPayrollId(),
                        p.getMonth(),
                        p.getYear(),
                        p.getStatus());
            });

            int payrollId = inputUtil.readInt("Enter Payroll ID: ");

            Payslip payslip = payrollService.generatePayslip(currentEmployee.getEmployeeId(), payrollId);

            if (payslip != null) {
                System.out.println("\n" + payslip.toString());
            } else {
                System.out.println("Failed to generate payslip!");
            }
        } else {
            System.out.println("No payroll records available!");
        }
    }
}
