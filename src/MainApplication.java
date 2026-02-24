import project.controller.*;
import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.modal.Employee;
import project.modal.User;
import project.util.InputUtil;
import project.util.ViewUtil;


public class MainApplication {

    //  Constants

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    //  Shared DAO

    private static final UserDao userDao = new UserDaoImpl();

    //  Controllers (instantiated once, reused per session)

    private static final AdminController       adminController       = new AdminController();
    private static final AttendanceController  attendanceController  = new AttendanceController();
    private static final EmployeeController    employeeController    = new EmployeeController();
    private static final PayrollController     payrollController     = new PayrollController();
    private static final PerformanceController performanceController = new PerformanceController();
    private static final LoginController       loginController       = new LoginController();

    //  MAIN

    public static void main(String[] args) {
        ViewUtil.printAppHeader();
        System.out.println("  Welcome to PMS — Payroll Management System");
        System.out.println("  Press Ctrl+C at any time to force-quit.\n");

        // Main application loop — restart at login after each logout
        while (true) {
            String role = selectRole();

            if (role == null) {
                // User chose to exit from role selection
                printGoodbye();
                break;
            }

            boolean loggedIn = false;

            switch (role) {
                case "ADMIN" -> loggedIn = handleAdminLogin();
                case "EMPLOYEE" -> loggedIn = handleEmployeeLogin();
            }

            // If login failed (all attempts exhausted), loop back to role selection
            if (!loggedIn) {
                ViewUtil.printError("Too many failed attempts. Returning to main menu.\n");
            }
            // After logout, the while(true) loop brings user back to role selection
        }
    }

    //  ROLE SELECTION

    /**
      Displays role selection menu.
      Returns "ADMIN", "EMPLOYEE", or null (exit).
     */
    private static String selectRole() {
        while (true) {
            System.out.println("─".repeat(50));
            System.out.println("  Who are you logging in as?");
            System.out.println("  [1] Admin");
            System.out.println("  [2] Employee");
            System.out.println("  [0] Exit");
            System.out.println("─".repeat(50));

            String choice = InputUtil.readMenuChoice("  Select: ");

            switch (choice) {
                case "1" -> { return "ADMIN"; }
                case "2" -> { return "EMPLOYEE"; }
                case "0" -> { return null; }
                default  -> System.out.println("  ⚠ Invalid choice. Enter 1, 2, or 0.\n");
            }
        }
    }

    //  ADMIN LOGIN + DASHBOARD

    /**
      Handles admin login loop (up to MAX_ATTEMPTS).
      On success, launches the admin dashboard loop.

      @return true if login succeeded and session completed normally (logout)
     */
    private static boolean handleAdminLogin() {
        int attempts = 0;

        while (attempts < MAX_LOGIN_ATTEMPTS) {
            System.out.println("\n  --- ADMIN LOGIN ---");
            String username = InputUtil.readString("  Username: ");
            String password = InputUtil.readPassword("  Password");

            User admin = userDao.adminLogin(username, password);

            if (admin != null) {
                System.out.printf("%n  Welcome, %s! [%s]%n", admin.getUsername(), admin.getPermissionLevel());
                runAdminDashboard(admin);
                return true; // normal logout
            } else {
                attempts++;
                int remaining = MAX_LOGIN_ATTEMPTS - attempts;
                if (remaining > 0) {
                    System.out.printf(" Invalid credentials. %d attempt(s) remaining.%n%n", remaining);
                }
            }
        }
        return false; // all attempts exhausted
    }

    /**
      Admin dashboard loop.
      Runs until the admin selects logout [0].
     */
    private static void runAdminDashboard(User admin) {
        boolean running = true;

        while (running) {
            ViewUtil.printAdminMenu();
            String choice = InputUtil.readMenuChoice("");

            switch (choice) {

                case "1" -> {
                    //  Manage Employees
                    adminController.manageEmployees();
                }

                case "2" -> {
                    //  Attendance Records
                    attendanceController.viewAllAttendance();
                }

                case "3" -> {
                    //  Performance Reviews
                    performanceController.managePerformance(admin.getAdminId());
                }

                case "4" -> {
                    //  Calculate Payroll
                    payrollController.calculatePayroll();
                }

                case "5" -> {
                    //  Generate Payslips
                    payrollController.generatePayslip();
                }

                case "6" -> {
                    //  Manage Bonuses
                    adminController.manageBonuses();
                }

                case "0" -> {
                    //  Logout
                    System.out.printf("%n  Goodbye, %s. Session ended.%n", admin.getUsername());
                    running = false;
                }

                default -> System.out.println("  ⚠ Invalid option. Please try again.");
            }
        }
    }

    //  EMPLOYEE LOGIN + DASHBOARD

    /**
      Handles employee login loop (up to MAX_ATTEMPTS).
      On success, launches the employee dashboard loop.
     */
    private static boolean handleEmployeeLogin() {
        int attempts = 0;

        while (attempts < MAX_LOGIN_ATTEMPTS) {
            System.out.println("\n  --- EMPLOYEE LOGIN ---");
            String email    = InputUtil.readEmail("  Email: ");
            String password = InputUtil.readPassword("  Password");

            Employee employee = userDao.employeeLogin(email, password);

            if (employee != null) {
                System.out.printf("%n   Welcome, %s!%n", employee.getFullName());
                runEmployeeDashboard(employee);
                return true;
            } else {
                attempts++;
                int remaining = MAX_LOGIN_ATTEMPTS - attempts;
                if (remaining > 0) {
                    System.out.printf(" Invalid credentials or inactive account. %d attempt(s) remaining.%n%n",
                            remaining);
                }
            }
        }
        return false;
    }

    /**
      Employee dashboard loop.
      Runs until the employee selects logout [0].
     */
    private static void runEmployeeDashboard(Employee employee) {
        boolean running = true;

        while (running) {
            ViewUtil.printEmployeeMenu();
            String choice = InputUtil.readMenuChoice("");

            switch (choice) {

                case "1" -> {
                    // ── Check In ──────────────────────────────────────────
                    attendanceController.checkIn(employee.getEmployeeId());
                }

                case "2" -> {
                    // ── Check Out ─────────────────────────────────────────
                    attendanceController.checkOut(employee.getEmployeeId());
                }

                case "3" -> {
                    // ── View My Attendance ────────────────────────────────
                    attendanceController.viewMyAttendance(employee.getEmployeeId());
                }

                case "4" -> {
                    // View My Performance
                    performanceController.viewMyPerformance(employee.getEmployeeId());
                }

                case "5" -> {
                    //  View My Payslip
                    payrollController.viewMyPayslip(employee.getEmployeeId());
                }

                case "0" -> {
                    // Logout
                    System.out.printf("%n  Goodbye, %s. Stay productive!%n", employee.getFullName());
                    running = false;
                }

                default -> System.out.println(" Invalid option. Please try again.");
            }
        }
    }

    //  HELPER

    private static void printGoodbye() {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("  Thank you for using PMS. Goodbye!");
        System.out.println("═".repeat(50) + "\n");
    }
}
