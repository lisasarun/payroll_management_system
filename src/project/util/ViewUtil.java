package project.util;

import project.dto.AttendanceDTO;
import project.dto.EmployeeDTO;
import project.dto.PayrollDTO;
import project.modal.Performance;

import java.math.BigDecimal;
import java.util.List;

/**
  Console view formatting utility.
  Provides table-style output for lists, payslips, and menus.
  Uses text-table-formatter-1.1.2 if available, or simple ASCII fallback.
 */
public class ViewUtil {

    //  Borders & Styles

    private static final String LINE      = "─".repeat(72);
    private static final String THIN_LINE = "-".repeat(72);
    private static final String HEADER_FMT = "│ %-4s │ %-20s │ %-25s │ %-12s │%n";
    private static final String ROW_FMT    = "│ %-4d │ %-20s │ %-25s │ %-12s │%n";

    //  App Header

    public static void printAppHeader() {
        System.out.println("\n" + "═".repeat(72));
        System.out.println("        PAYROLL MANAGEMENT SYSTEM  |  PMS v1.0");
        System.out.println("═".repeat(72));
    }

    //  Section Title

    public static void printTitle(String title) {
        System.out.println("\n" + LINE);
        System.out.printf("  %s%n", title.toUpperCase());
        System.out.println(LINE);
    }

    //  Success / Error / Info Messages

    public static void printSuccess(String msg) {
        System.out.println("  ✔ " + msg);
    }

    public static void printError(String msg) {
        System.err.println("  ✘ ERROR: " + msg);
    }

    public static void printInfo(String msg) {
        System.out.println("  ℹ " + msg);
    }

    //  Employee Table

    /**
      Prints a paginated table of employees.
     */
    public static void printEmployeeTable(List<EmployeeDTO> employees, int page, int totalPages) {
        printTitle("EMPLOYEE LIST  |  Page " + page + " of " + totalPages);
        System.out.printf(HEADER_FMT, "ID", "Full Name", "Email", "Base Salary");
        System.out.println(THIN_LINE);
        for (EmployeeDTO e : employees) {
            System.out.printf(ROW_FMT,
                    e.getEmployeeId(),
                    truncate(e.getFullName(), 20),
                    truncate(e.getEmail(), 25),
                    formatMoney(e.getBaseSalary()));
        }
        System.out.println(THIN_LINE);
        System.out.printf("  Total: %d employees on this page%n", employees.size());
        printPaginationHint(page, totalPages);
    }

    //  Attendance Table

    public static void printAttendanceTable(List<AttendanceDTO> records) {
        printTitle("ATTENDANCE RECORDS");
        System.out.printf("│ %-10s │ %-19s │ %-19s │ %-8s │ %-8s │ %-8s │%n",
                "Date", "Check In", "Check Out", "Hours", "OT Hrs", "Status");
        System.out.println(THIN_LINE);
        for (AttendanceDTO a : records) {
            System.out.printf("│ %-10s │ %-19s │ %-19s │ %-8s │ %-8s │ %-8s │%n",
                    a.getDate(),
                    a.getCheckIn() != null ? a.getCheckIn().toString().substring(0, 19) : "—",
                    a.getCheckOut() != null ? a.getCheckOut().toString().substring(0, 19) : "—",
                    a.getWorkHours() != null ? a.getWorkHours() : "—",
                    a.getOvertimeHours() != null ? a.getOvertimeHours() : "—",
                    a.getStatus() != null ? a.getStatus() : "—");
        }
        System.out.println(THIN_LINE);
    }

    //  Payroll Summary

    public static void printPayrollSummary(PayrollDTO p) {
        printTitle("PAYROLL SUMMARY — " + p.getEmployeeName());
        System.out.println("  Pay Period : " + p.getPayPeriodStart() + "  to  " + p.getPayPeriodEnd());
        System.out.println("  Payment Date : " + p.getPaymentDate());
        System.out.println(THIN_LINE);
        System.out.printf("  %-25s %15s%n", "Base Salary:", formatMoney(p.getBaseSalary()));
        System.out.printf("  %-25s %15s%n", "Overtime Pay:", formatMoney(p.getOvertimePay()));
        System.out.printf("  %-25s %15s%n", "Bonus:", formatMoney(p.getBonus()));
        System.out.printf("  %-25s %15s%n", "Deductions:", "- " + formatMoney(p.getDeductions()));
        System.out.println(THIN_LINE);
        System.out.printf("  %-25s %15s%n", "TOTAL PAID:", formatMoney(p.getTotalPaid()));
        System.out.println(LINE);
    }

    //  Performance Record

    public static void printPerformanceRecord(Performance p) {
        System.out.printf("  Review Date: %-15s  Score: %-6s  Comments: %s%n",
                p.getReviewDate(), p.getScore(), p.getComments() != null ? p.getComments() : "N/A");
    }

    // Admin Menu

    public static void printAdminMenu() {
        printTitle("ADMIN DASHBOARD");
        System.out.println("  [1] Manage Employees");
        System.out.println("  [2] Attendance Records");
        System.out.println("  [3] Performance Reviews");
        System.out.println("  [4] Calculate Payroll");
        System.out.println("  [5] Generate Payslip (PDF)");
        System.out.println("  [6] Manage Bonuses");
        System.out.println("  [0] Logout");
        System.out.println(LINE);
        System.out.print("  Select option: ");
    }

    // Employee Menu

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

    //  Manage Employee Sub-Menu

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

    //  Pagination Hint

    public static void printPaginationHint(int page, int totalPages) {
        System.out.printf("  [ Page %d / %d ]   [N] Next   [P] Prev   [0] Back%n%n", page, totalPages);
    }

    //  Helpers

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%,.2f", amount);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

}