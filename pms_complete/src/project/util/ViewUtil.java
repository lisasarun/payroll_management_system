package project.util;

import project.dto.AttendanceDTO;
import project.dto.EmployeeDTO;
import project.dto.PayrollDTO;
import project.dto.PerformanceDTO;

import java.math.BigDecimal;
import java.util.List;

public class ViewUtil {

    private static final String LINE  = "─".repeat(72);
    private static final String TLINE = "-".repeat(72);

    public static void printAppHeader() {
        System.out.println("\n" + "═".repeat(72));
        System.out.println("         PAYROLL MANAGEMENT SYSTEM  |  PMS v1.0");
        System.out.println("═".repeat(72));
    }

    public static void printTitle(String title) {
        System.out.println("\n" + LINE);
        System.out.printf("  %s%n", title.toUpperCase());
        System.out.println(LINE);
    }

    public static void printSuccess(String msg) { System.out.println("  ✔ " + msg); }
    public static void printError(String msg)   { System.err.println("  ✘ " + msg); }
    public static void printInfo(String msg)    { System.out.println("  ℹ " + msg); }

    public static void printAdminMenu() {
        printTitle("ADMIN DASHBOARD");
        System.out.println("  [1] Manage Employees");
        System.out.println("  [2] View Attendance Records");
        System.out.println("  [3] Performance Reviews");
        System.out.println("  [4] Calculate Payroll");
        System.out.println("  [5] Generate Payslip (PDF)");
        System.out.println("  [6] Manage Bonuses");
        System.out.println("  [0] Logout");
        System.out.println(LINE);
        System.out.print("  Select option: ");
    }

    public static void printEmployeeMenu() {
        printTitle("EMPLOYEE DASHBOARD");
        System.out.println("  [1] Check In");
        System.out.println("  [2] Check Out");
        System.out.println("  [3] View My Attendance");
        System.out.println("  [4] View My Performance");
        System.out.println("  [5] View My Payslip");
        System.out.println("  [0] Logout");
        System.out.println(LINE);
        System.out.print("  Select option: ");
    }

    public static void printManageEmployeeMenu() {
        printTitle("MANAGE EMPLOYEES");
        System.out.println("  [1] Add Employee");
        System.out.println("  [2] Update Employee");
        System.out.println("  [3] Search Employee");
        System.out.println("  [4] Disable Employee");
        System.out.println("  [5] List All Employees");
        System.out.println("  [0] Back");
        System.out.println(LINE);
        System.out.print("  Select option: ");
    }

    public static void printEmployeeTable(List<EmployeeDTO> list, int page, int total) {
        printTitle("EMPLOYEE LIST — Page " + page + " of " + total);
        System.out.printf("  %-4s %-25s %-28s %-12s%n", "ID", "Full Name", "Email", "Base Salary");
        System.out.println(TLINE);
        for (EmployeeDTO e : list) {
            System.out.printf("  %-4d %-25s %-28s %-12s%n",
                    e.getEmployeeId(), trunc(e.getFullName(), 25),
                    trunc(e.getEmail(), 28), money(e.getBaseSalary()));
        }
        System.out.println(TLINE);
        printPaginationHint(page, total);
    }

    public static void printAttendanceTable(List<AttendanceDTO> list) {
        printTitle("ATTENDANCE RECORDS");
        System.out.printf("  %-10s %-8s %-19s %-19s %-7s %-7s%n",
                "Date", "Status", "Check In", "Check Out", "Hours", "OT Hrs");
        System.out.println(TLINE);
        for (AttendanceDTO a : list) {
            System.out.printf("  %-10s %-8s %-19s %-19s %-7s %-7s%n",
                    a.getDate(),
                    a.getStatus() != null ? a.getStatus() : "—",
                    a.getCheckIn()  != null ? a.getCheckIn().toString().substring(0,19)  : "—",
                    a.getCheckOut() != null ? a.getCheckOut().toString().substring(0,19) : "—",
                    a.getWorkHours()     != null ? a.getWorkHours()     : "—",
                    a.getOvertimeHours() != null ? a.getOvertimeHours() : "—");
        }
        System.out.println(TLINE);
    }

    public static void printPayrollSummary(PayrollDTO p) {
        printTitle("PAYROLL SUMMARY — " + p.getEmployeeName());
        System.out.println("  Period      : " + p.getPayPeriodStart() + " to " + p.getPayPeriodEnd());
        System.out.println("  Payment Date: " + p.getPaymentDate());
        System.out.println(TLINE);
        System.out.printf("  %-22s %15s%n", "Base Salary:",  money(p.getBaseSalary()));
        System.out.printf("  %-22s %15s%n", "Overtime Pay:", money(p.getOvertimePay()));
        System.out.printf("  %-22s %15s%n", "Bonus:",        money(p.getBonus()));
        System.out.printf("  %-22s %15s%n", "Deductions:", "- " + money(p.getDeductions()));
        System.out.println(TLINE);
        System.out.printf("  %-22s %15s%n", "TOTAL PAID:", money(p.getTotalPaid()));
        System.out.println(LINE);
    }

    public static void printPerformanceRecord(PerformanceDTO p) {
        System.out.printf("  Date: %-12s  Score: %-6s  Comments: %s%n",
                p.getReviewDate(),
                p.getScore() != null ? p.getScore().toPlainString() : "N/A",
                p.getComments() != null ? p.getComments() : "N/A");
    }

    public static void printPaginationHint(int page, int total) {
        System.out.printf("  [N] Next  [P] Prev  [0] Back   (Page %d / %d)%n%n", page, total);
    }

    private static String money(BigDecimal v) {
        return v == null ? "$0.00" : String.format("$%,.2f", v);
    }

    private static String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
