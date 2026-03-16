import project.config.DbConfig;
import project.controller.*;
import project.dao.UserDao;
import project.dao.UserDaoImpl;
import project.model.Employee;
import project.model.User;
import project.util.InputUtil;
import project.util.ViewUtil;

public class MainApplication {

    private static final int MAX_ATTEMPTS = 3;

    private static final UserDao                userDao     = new UserDaoImpl();
    private static final AdminController        adminCtrl   = new AdminController();
    private static final AttendanceController   attCtrl     = new AttendanceController();
    private static final PerformanceController  perfCtrl    = new PerformanceController();
    private static final PayrollController      payrollCtrl = new PayrollController();
    private static final EmployeeController     empCtrl     = new EmployeeController();
    private static final LeaveRequestController leaveCtrl   = new LeaveRequestController();

    public static void main(String[] args) {
        DbConfig.init();
        ViewUtil.printAppHeader();

        while (true) {
            String role = selectRole();
            if (role == null) break;

            boolean ok = switch (role) {
                case "ADMIN"    -> handleAdminLogin();
                case "EMPLOYEE" -> handleEmployeeLogin();
                default         -> false;
            };

            if (!ok) ViewUtil.printError("Access locked. Returning to main menu.");
        }

        ViewUtil.printSystemExit();
        DbConfig.close();
    }

    private static String selectRole() {
        while (true) {
            ViewUtil.printRoleSelection();
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> { return "ADMIN"; }
                case "2" -> { return "EMPLOYEE"; }
                case "0" -> { return null; }
                default  -> ViewUtil.printError("Enter 1, 2, or 0.");
            }
        }
    }


    private static boolean handleAdminLogin() {
        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            ViewUtil.printAdminLoginBox();
            String username = InputUtil.readString(" Username : ");
            String password = InputUtil.readPassword(" Password : ");
            ViewUtil.printLoginDivider();

            User admin = userDao.adminLogin(username, password);
            if (admin != null) {
                ViewUtil.printAccessGranted(admin.getUsername(), admin.getPermissionLevel());
                runAdminDashboard(admin);
                return true;
            }

            int left = MAX_ATTEMPTS - attempts - 1;
            if (left > 0) ViewUtil.printLoginFailed(left);
            else          ViewUtil.printLoginLocked();
        }
        return false;
    }

    private static void runAdminDashboard(User admin) {
        boolean running = true;
        while (running) {
            ViewUtil.printAdminMenu(admin.getUsername(), admin.getPermissionLevel());
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> adminCtrl.manageEmployees();
                case "2" -> attCtrl.viewAllAttendance();
                case "3" -> perfCtrl.managePerformance(admin.getAdminId());
                case "4" -> payrollCtrl.calculatePayroll();
                case "5" -> payrollCtrl.generatePayslip();
                case "6" -> adminCtrl.manageBonuses();
                case "7" -> leaveCtrl.reviewLeaveRequests(admin.getAdminId());
                case "0" -> {
                    ViewUtil.printGoodbye(admin.getUsername());
                    running = false;
                }
                default -> ViewUtil.printError("Invalid option. Please try again.");
            }
        }
    }

    private static boolean handleEmployeeLogin() {
        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            ViewUtil.printEmployeeLoginBox();
            String email    = InputUtil.readEmail(" Email    : ");
            String password = InputUtil.readPassword(" Password : ");
            ViewUtil.printLoginDivider();

            Employee emp = userDao.employeeLogin(email, password);
            if (emp != null) {
                ViewUtil.printAccessGrantedEmp(emp.getFullName());
                runEmployeeDashboard(emp);
                return true;
            }

            int left = MAX_ATTEMPTS - attempts - 1;
            if (left > 0) ViewUtil.printLoginFailed(left);
            else          ViewUtil.printLoginLocked();
        }
        return false;
    }

    private static void runEmployeeDashboard(Employee emp) {
        boolean running = true;
        while (running) {
            ViewUtil.printEmployeeMenu(emp.getFullName());
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
                    ViewUtil.printGoodbye(emp.getFullName());
                    running = false;
                }
                default -> ViewUtil.printError("Invalid option. Please try again.");
            }
        }
    }
}