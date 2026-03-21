import project.config.DbConfig;
import project.controller.*;
import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.model.Employee;
import project.model.User;
import project.util.InputUtil;
import project.util.ViewUtil;


public class MainApplication {

    private static final UserDao               userDao      = new UserDaoImpl();
    private static final AdminController       adminCtrl    = new AdminController();
    private static final AttendanceController  attCtrl      = new AttendanceController();
    private static final PerformanceController perfCtrl     = new PerformanceController();
    private static final PayrollController     payrollCtrl  = new PayrollController();
    private static final EmployeeController    empCtrl      = new EmployeeController();
    private static final LeaveRequestController leaveCtrl   = new LeaveRequestController();

    public static void main(String[] args) {


        DbConfig.init();

        ViewUtil.printAppHeader();
        System.out.println("  Welcome to PMS — Payroll Management System");
        System.out.println("  Press 0 at any time to exit.\n");


        while (true) {
            String role = selectRole();
            if (role == null) break;

            switch (role) {
                case "ADMIN"    -> handleAdminLogin();
                case "EMPLOYEE" -> handleEmployeeLogin();
                default         -> { }
            };
        }


        System.out.println("\n" + "═".repeat(72));
        System.out.println("  Thank you for using PMS. Goodbye!");
        System.out.println("═".repeat(72) + "\n");
        DbConfig.close();
    }



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


    private static boolean handleAdminLogin() {
        System.out.println("\n  --- ADMIN LOGIN ---");
        String username = InputUtil.readAdminUsername("  Username : ");
        String password = InputUtil.readPassword("  Password");

        User admin = userDao.adminLogin(username, password);
        if (admin != null) {
            System.out.printf("%n  Welcome, %s! [%s]%n", admin.getUsername(), admin.getPermissionLevel());
            runAdminDashboard(admin);
            return true;
        }
        System.out.println("  Invalid credentials. Returning to role selection.\n");
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



    private static boolean handleEmployeeLogin() {
        System.out.println("\n  --- EMPLOYEE LOGIN ---");
        String email    = InputUtil.readEmail("  Email    : ");
        String password = InputUtil.readPassword("  Password");

        Employee emp = userDao.employeeLogin(email, password);
        if (emp != null) {
            System.out.printf("%n  Welcome, %s!%n", emp.getFullName());
            runEmployeeDashboard(emp);
            return true;
        }

        Employee byEmail = userDao.findEmployeeByEmail(email);
        if (byEmail != null && !byEmail.isActive()) {
            System.out.println("  Employee account is disabled.\n");
            return false;
        }

        System.out.println("  Invalid credentials. Returning to role selection.\n");
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
                case "9" -> ViewUtil.printEmployeeProfile(emp);
                case "0" -> {
                    System.out.printf("%n  Goodbye, %s!%n", emp.getFullName());
                    running = false;
                }
                default -> System.out.println("  Invalid option.");
            }
        }
    }
}
