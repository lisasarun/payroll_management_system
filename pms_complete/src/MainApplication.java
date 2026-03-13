import project.config.DbConfig;
import project.controller.*;
import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.model.Employee;
import project.model.User;
import project.util.InputUtil;
import project.util.ViewUtil;

/**
  PMS — Payroll Management System
  Entry point. Initialises DB connection, drives role-based login,
 and routes to Admin or Employee dashboard.
 */
public class MainApplication {

    private static final int MAX_ATTEMPTS = 3;

    private static final UserDao               userDao      = new UserDaoImpl();
    private static final AdminController       adminCtrl    = new AdminController();
    private static final AttendanceController  attCtrl      = new AttendanceController();
    private static final PerformanceController perfCtrl     = new PerformanceController();
    private static final PayrollController     payrollCtrl  = new PayrollController();
    private static final EmployeeController    empCtrl      = new EmployeeController();
    private static final LeaveRequestController leaveCtrl   = new LeaveRequestController();

    public static void main(String[] args) {

        // Connect to Database - MUST be first
        DbConfig.init();

        ViewUtil.printAppHeader();
        System.out.println("  Welcome to PMS — Payroll Management System");
        System.out.println("  Press 0 at any time to exit.\n");

        // Main loop
        while (true) {
            String role = selectRole();
            if (role == null) break;

            boolean ok = switch (role) {
                case "ADMIN"    -> handleAdminLogin();
                case "EMPLOYEE" -> handleEmployeeLogin();
                default         -> false;
            };

            if (!ok) ViewUtil.printError("Too many failed attempts. Returning to main menu.\n");
        }

        // Clean shutdown
        System.out.println("\n" + "═".repeat(72));
        System.out.println("  Thank you for using PMS. Goodbye!");
        System.out.println("═".repeat(72) + "\n");
        DbConfig.close();
    }

    //  Role selection

    private static String selectRole() {
        while (true) {
            System.out.println("─".repeat(50));
            System.out.println("  Who are you logging in as?");
            System.out.println("  [1] Admin");
            System.out.println("  [2] Employee");
            System.out.println("  [0] Exit");
            System.out.println("─".repeat(50));
            switch (InputUtil.readMenuChoice("  Select: ")) {
                case "1" -> { return "ADMIN"; }
                case "2" -> { return "EMPLOYEE"; }
                case "0" -> { return null; }
                default  -> System.out.println("  Enter 1, 2, or 0.\n");
            }
        }
    }

    //  Admin login + dashboard

    private static boolean handleAdminLogin() {
        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            System.out.println("\n  --- ADMIN LOGIN ---");
            String username = InputUtil.readString("  Username : ");
            String password = InputUtil.readPassword("  Password");

            User admin = userDao.adminLogin(username, password);
            if (admin != null) {
                System.out.printf("%n  Welcome, %s! [%s]%n", admin.getUsername(), admin.getPermissionLevel());
                runAdminDashboard(admin);
                return true;
            }
            int left = MAX_ATTEMPTS - attempts - 1;
            if (left > 0) System.out.printf("  Invalid credentials. %d attempt(s) left.%n%n", left);
        }
        return false;
    }

    private static void runAdminDashboard(User admin) {
        boolean running = true;
        while (running) {
            ViewUtil.printAdminMenu();
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> adminCtrl.manageEmployees();
                case "2" -> attCtrl.viewAllAttendance();
                case "3" -> perfCtrl.managePerformance(admin.getAdminId());
                case "4" -> payrollCtrl.calculatePayroll();
                case "5" -> payrollCtrl.generatePayslip();
                case "6" -> adminCtrl.manageBonuses();
                case "7" -> leaveCtrl.reviewLeaveRequests(admin.getAdminId());
                case "0" -> {
                    System.out.printf("%n  Goodbye, %s.%n", admin.getUsername());
                    running = false;
                }
                default -> System.out.println("  Invalid option.");
            }
        }
    }

    // Employee login + dashboard

    private static boolean handleEmployeeLogin() {
        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            System.out.println("\n  --- EMPLOYEE LOGIN ---");
            String email    = InputUtil.readEmail("  Email    : ");
            String password = InputUtil.readPassword("  Password");

            Employee emp = userDao.employeeLogin(email, password);
            if (emp != null) {
                System.out.printf("%n  Welcome, %s!%n", emp.getFullName());
                runEmployeeDashboard(emp);
                return true;
            }
            int left = MAX_ATTEMPTS - attempts - 1;
            if (left > 0) System.out.printf("  Invalid credentials. %d attempt(s) left.%n%n", left);
        }
        return false;
    }

    private static void runEmployeeDashboard(Employee emp) {
        boolean running = true;
        while (running) {
            ViewUtil.printEmployeeMenu();
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> attCtrl.checkIn(emp.getEmployeeId());
                case "2" -> attCtrl.checkOut(emp.getEmployeeId());
                case "3" -> attCtrl.viewMyAttendance(emp.getEmployeeId());
                case "4" -> perfCtrl.viewMyPerformance(emp.getEmployeeId());
                case "5" -> payrollCtrl.viewMyPayslip(emp.getEmployeeId());
                case "6" -> empCtrl.changePassword(emp.getEmployeeId());
                case "7" -> leaveCtrl.submitLeaveRequest(emp.getEmployeeId());
                case "8" -> leaveCtrl.viewMyLeaveRequests(emp.getEmployeeId());
                case "0" -> {
                    System.out.printf("%n  Goodbye, %s!%n", emp.getFullName());
                    running = false;
                }
                default -> System.out.println("  Invalid option.");
            }
        }
    }
}
