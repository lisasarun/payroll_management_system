package project.controller;


import project.dto.LoginRequest;
import project.dto.UserDTO;
import project.service.AuthService;
import project.util.InputUtil;

public class LoginController {
    private final AuthService authService;
    private final InputUtil inputUtil;

    public LoginController() {
        this.authService = new AuthService();
        this.inputUtil = new InputUtil();
    }

    public void showLoginMenu() {
        System.out.println("\n=== EMPLOYEE MANAGEMENT SYSTEM ===");
        System.out.println("1. Login");
        System.out.println("2. Exit");

        int choice = inputUtil.readInt("Enter your choice: ");

        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                System.out.println("Thank you for using the system!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice!");
                showLoginMenu();
        }
    }

    private void handleLogin() {
        System.out.println("\n=== LOGIN ===");
        String username = inputUtil.readString("Username: ");
        String password = inputUtil.readString("Password: ");

        LoginRequest loginRequest = new LoginRequest(username, password);

        if (authService.login(loginRequest)) {
            UserDTO user = authService.getCurrentUser();
            System.out.println("Welcome, " + user.getUsername() + "!");

            if (authService.isAdmin()) {
                AdminController adminController = new AdminController(authService);
                adminController.showAdminMenu();
            } else {
                EmployeeController employeeController = new EmployeeController(authService);
                employeeController.showEmployeeMenu();
            }
        } else {
            System.out.println("Invalid username or password!");
            showLoginMenu();
        }
    }
}