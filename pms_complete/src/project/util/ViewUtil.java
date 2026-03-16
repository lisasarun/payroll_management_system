package project.util;

import project.dto.AttendanceDTO;
import project.dto.EmployeeDTO;
import project.dto.PayrollDTO;
import project.dto.PerformanceDTO;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ViewUtil {

    private static final int    WIDTH = 60;
    private static final String LINE  = "─".repeat(WIDTH);
    private static final String DLINE = "═".repeat(WIDTH);
    private static final String TLINE = "━".repeat(WIDTH);


    public static void printAppHeader() {
        System.out.println();
        System.out.println("""
                        ██████╗ ███╗   ███╗███████╗
                        ██╔══██╗████╗ ████║██╔════╝
                        ██████╔╝██╔████╔██║███████╗
                        ██╔═══╝ ██║╚██╔╝██║╚════██║
                        ██║     ██║ ╚═╝ ██║███████║
                        ╚═╝     ╚═╝     ╚═╝╚══════╝
                """);
        System.out.println("  " + TLINE);
        System.out.println("  " + center("Payroll Management System"));
        System.out.println("  " + TLINE);
        System.out.println();
    }

    public static void printRoleSelection() {
        System.out.println();
        System.out.println("╭" + LINE + "╮");
        System.out.println("│" + center("🔐  LOGIN PORTAL") + "│");
        System.out.println("├" + LINE + "┤");
        System.out.println("│  Who are you logging in as?" + padRight("", WIDTH - 28) + "│");
        System.out.println("│" + padRight("", WIDTH) + "│");
        System.out.println("│  1️⃣   Login as Admin"         + padRight("", WIDTH - 21) + "│");
        System.out.println("│  2️⃣   Login as Employee"      + padRight("", WIDTH - 24) + "│");
        System.out.println("│" + padRight("", WIDTH) + "│");
        System.out.println("│  0️⃣   Exit"                   + padRight("", WIDTH - 11) + "│");
        System.out.println("╰" + LINE + "╯");
        System.out.print("👉 Select option: ");
    }

    public static void printAdminLoginBox() {
        System.out.println();
        System.out.println("╔" + DLINE + "╗");
        System.out.println("║" + center("👑  ADMIN LOGIN") + "║");
        System.out.println("╚" + DLINE + "╝");
    }

    public static void printEmployeeLoginBox() {
        System.out.println();
        System.out.println("╔" + DLINE + "╗");
        System.out.println("║" + center("👤  EMPLOYEE LOGIN") + "║");
        System.out.println("╚" + DLINE + "╝");
    }

    public static void printLoginDivider() {
        System.out.println("  " + TLINE);
    }

    public static void printAccessGranted(String name, String role) {
        System.out.println();
        System.out.println("╔" + DLINE + "╗");
        System.out.println("║" + center("✅  ACCESS GRANTED") + "║");
        System.out.println("║" + center("Welcome, " + name + "  |  " + role) + "║");
        System.out.println("╚" + DLINE + "╝");
    }

    public static void printAccessGrantedEmp(String fullName) {
        System.out.println();
        System.out.println("╔" + DLINE + "╗");
        System.out.println("║" + center("✅  ACCESS GRANTED") + "║");
        System.out.println("║" + center("Welcome, " + fullName) + "║");
        System.out.println("╚" + DLINE + "╝");
    }

    public static void printLoginFailed(int attemptsLeft) {
        System.out.println("  ❌  Invalid credentials — " + attemptsLeft + " attempt(s) remaining.");
    }

    public static void printLoginLocked() {
        System.out.println("  🔒  Too many failed attempts. Access locked.");
    }

    public static void printAdminMenu(String username, String roleLabel) {
        System.out.println();
        System.out.println("╔" + DLINE + "╗");
        System.out.println("║" + center("⚙   ADMIN DASHBOARD") + "║");
        System.out.println("╠" + DLINE + "╣");
        System.out.println("║  👤 " + padRight(username + "   │   🏷  " + roleLabel, WIDTH - 5) + "║");
        System.out.println("╠" + DLINE + "╣");

        System.out.println("║  👥  EMPLOYEE & HR"              + padRight("", WIDTH - 18) + "║");
        System.out.println("║     [1] Manage Employees"         + padRight("", WIDTH - 25) + "║");
        System.out.println("║     [7] Review Leave Requests"    + padRight("", WIDTH - 30) + "║");
        System.out.println("╠" + LINE + "╣");

        System.out.println("║  🕐  ATTENDANCE"                  + padRight("", WIDTH - 15) + "║");
        System.out.println("║     [2] View Attendance Records"   + padRight("", WIDTH - 32) + "║");
        System.out.println("╠" + LINE + "╣");

        System.out.println("║  💰  PERFORMANCE & PAYROLL"       + padRight("", WIDTH - 26) + "║");
        System.out.println("║     [3] Performance Reviews"       + padRight("", WIDTH - 28) + "║");
        System.out.println("║     [4] Calculate Payroll"         + padRight("", WIDTH - 26) + "║");
        System.out.println("║     [5] Generate Payslip (PDF)"    + padRight("", WIDTH - 31) + "║");
        System.out.println("║     [6] Manage Bonuses"            + padRight("", WIDTH - 23) + "║");
        System.out.println("╠" + LINE + "╣");

        System.out.println("║  🚪  SYSTEM"                      + padRight("", WIDTH - 12) + "║");
        System.out.println("║     [0] Logout"                    + padRight("", WIDTH - 15) + "║");
        System.out.println("╚" + DLINE + "╝");
        System.out.print("👉 Select option: ");
    }

    public static void printEmployeeMenu(String fullName) {
        System.out.println();
        System.out.println("╔" + DLINE + "╗");
        System.out.println("║" + center("🧑‍💼  EMPLOYEE DASHBOARD") + "║");
        System.out.println("╠" + DLINE + "╣");
        System.out.println("║  👤 " + padRight(fullName, WIDTH - 5) + "║");
        System.out.println("╠" + DLINE + "╣");

        System.out.println("║  🕐  ATTENDANCE"                  + padRight("", WIDTH - 15) + "║");
        System.out.println("║     [1] Check In"                  + padRight("", WIDTH - 17) + "║");
        System.out.println("║     [2] Check Out"                 + padRight("", WIDTH - 18) + "║");
        System.out.println("║     [3] View My Attendance"        + padRight("", WIDTH - 27) + "║");
        System.out.println("╠" + LINE + "╣");

        System.out.println("║  💰  PERFORMANCE & PAY"           + padRight("", WIDTH - 22) + "║");
        System.out.println("║     [4] View My Performance"       + padRight("", WIDTH - 28) + "║");
        System.out.println("║     [5] View My Payslip (PDF)"     + padRight("", WIDTH - 30) + "║");
        System.out.println("╠" + LINE + "╣");

        System.out.println("║  🗂   MY ACCOUNT"                  + padRight("", WIDTH - 15) + "║");
        System.out.println("║     [6] Change Password"           + padRight("", WIDTH - 24) + "║");
        System.out.println("║     [7] Submit Leave Request"      + padRight("", WIDTH - 29) + "║");
        System.out.println("║     [8] View My Leave Requests"    + padRight("", WIDTH - 31) + "║");
        System.out.println("╠" + LINE + "╣");

        System.out.println("║  🚪  SYSTEM"                      + padRight("", WIDTH - 11) + "║");
        System.out.println("║     [0] Logout"                    + padRight("", WIDTH - 15) + "║");
        System.out.println("╚" + DLINE + "╝");
        System.out.print("👉 Select option: ");
    }

    public static void printManageEmployeeMenu() {
        System.out.println();
        System.out.println("╔" + DLINE + "╗");
        System.out.println("║" + center("👥  MANAGE EMPLOYEES") + "║");
        System.out.println("╠" + DLINE + "╣");
        System.out.println("║     [1] Add Employee"              + padRight("", WIDTH - 21) + "║");
        System.out.println("║     [2] Update Employee"           + padRight("", WIDTH - 24) + "║");
        System.out.println("║     [3] Search Employee"           + padRight("", WIDTH - 24) + "║");
        System.out.println("║     [4] Disable Employee"          + padRight("", WIDTH - 25) + "║");
        System.out.println("║     [5] List All Employees"        + padRight("", WIDTH - 26) + "║");
        System.out.println("╠" + LINE + "╣");
        System.out.println("║     [0] Back"                      + padRight("", WIDTH - 13) + "║");
        System.out.println("╚" + DLINE + "╝");
        System.out.print("👉 Select option: ");
    }

    public static void printTitle(String title) {
        System.out.println();
        System.out.println("  " + TLINE);
        System.out.printf("  %-" + WIDTH + "s%n", title.toUpperCase());
        System.out.println("  " + TLINE);
    }

    public static void printEmployeeTable(List<EmployeeDTO> list, int page, int total) {
        printTitle("EMPLOYEE LIST — Page " + page + " of " + total);
        System.out.printf("  %-4s  %-22s  %-26s  %-10s%n", "ID", "Full Name", "Email", "Base Salary");
        System.out.println("  " + TLINE);
        for (EmployeeDTO e : list) {
            System.out.printf("  %-4d  %-22s  %-26s  %-10s%n",
                    e.getEmployeeId(),
                    trunc(e.getFullName(), 22),
                    trunc(e.getEmail(), 26),
                    money(e.getBaseSalary()));
        }
        System.out.println("  " + TLINE);
        printPaginationHint(page, total);
    }

    public static void printAttendanceTable(List<AttendanceDTO> list) {
        printTitle("ATTENDANCE RECORDS");
        System.out.printf("  %-10s  %-8s  %-19s  %-19s  %-6s  %-6s%n",
                "Date", "Status", "Check In", "Check Out", "Hours", "OT Hrs");
        System.out.println("  " + TLINE);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (AttendanceDTO a : list) {
            System.out.printf("  %-10s  %-8s  %-19s  %-19s  %-6s  %-6s%n",
                    a.getDate(),
                    a.getStatus()       != null ? a.getStatus()               : "—",
                    a.getCheckIn()      != null ? a.getCheckIn().format(fmt)  : "—",
                    a.getCheckOut()     != null ? a.getCheckOut().format(fmt) : "—",
                    a.getWorkHours()    != null ? a.getWorkHours()            : "—",
                    a.getOvertimeHours()!= null ? a.getOvertimeHours()        : "—");
        }
        System.out.println("  " + TLINE);
    }

    public static void printPayrollSummary(PayrollDTO p) {
        printTitle("PAYROLL SUMMARY — " + p.getEmployeeName());
        System.out.println("  Period       : " + p.getPayPeriodStart() + "  →  " + p.getPayPeriodEnd());
        System.out.println("  Payment Date : " + p.getPaymentDate());
        System.out.println("  " + TLINE);
        System.out.printf("  %-22s  %12s%n", "Base Salary",   money(p.getBaseSalary()));
        System.out.printf("  %-22s  %12s%n", "Bonus",         money(p.getBonus()));
        System.out.printf("  %-22s  %12s%n", "Deductions",  "- " + money(p.getDeductions()));
        System.out.println("  " + TLINE);
        System.out.printf("  %-22s  %12s%n", "TOTAL PAID",    money(p.getTotalPaid()));
        System.out.println("  " + TLINE);
    }

    public static void printPerformanceRecord(PerformanceDTO p) {
        System.out.printf("  %-12s  Score: %-6s  %s%n",
                p.getReviewDate(),
                p.getScore()    != null ? p.getScore().toPlainString() : "N/A",
                p.getComments() != null ? p.getComments()              : "N/A");
    }

    public static void printPaginationHint(int page, int total) {
        if (total > 1) System.out.printf("  [N] Next   [P] Prev   [0] Back   (Page %d / %d)%n%n", page, total);
        else           System.out.println("  [0] Back\n");
    }


    public static void printSuccess(String msg) { System.out.println("  ✅  " + msg); }
    public static void printError(String msg)   { System.err.println("  ❌  " + msg); }
    public static void printInfo(String msg)    { System.out.println("  ℹ️   " + msg); }

    public static void printGoodbye(String name) {
        System.out.println();
        System.out.println("  " + TLINE);
        System.out.println("  " + center("👋  Goodbye, " + name + ". See you soon!"));
        System.out.println("  " + TLINE);
        System.out.println();
    }

    public static void printSystemExit() {
        System.out.println();
        System.out.println("  " + TLINE);
        System.out.println("  " + center("Thank you for using PMS. Goodbye!"));
        System.out.println("  " + TLINE);
        System.out.println();
    }

    private static String money(BigDecimal v) {
        return v == null ? "$0.00" : String.format("$%,.2f", v);
    }

    private static String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    static String center(String text) {
        if (text == null) text = "";
        if (text.length() >= ViewUtil.WIDTH) return text.substring(0, ViewUtil.WIDTH);
        int left  = (ViewUtil.WIDTH - text.length()) / 2;
        int right = ViewUtil.WIDTH - text.length() - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    static String padRight(String text, int width) {
        if (text == null) text = "";
        if (width <= 0) return text;
        if (text.length() >= width) return text.substring(0, width);
        return text + " ".repeat(width - text.length());
    }
}