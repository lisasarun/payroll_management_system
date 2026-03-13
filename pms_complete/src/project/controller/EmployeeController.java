package project.controller;

import project.repository.EmployeeRepository;
import project.util.InputUtil;
import project.util.PasswordUtil;
import project.util.ViewUtil;


public class EmployeeController {

    private final EmployeeRepository empRepo = new EmployeeRepository();

    public void changePassword(int employeeId) {
        ViewUtil.printTitle("CHANGE PASSWORD");

        String current = InputUtil.readPassword("  Current Password");
        String newPass  = InputUtil.readPassword("  New Password    ");
        String confirm  = InputUtil.readPassword("  Confirm New     ");

        if (!newPass.equals(confirm)) {
            ViewUtil.printError("Passwords do not match.");
            return;
        }
        if (newPass.length() < 6) {
            ViewUtil.printError("Password must be at least 6 characters.");
            return;
        }

        // Verify Current password
        var emp = empRepo.findById(employeeId);
        if (emp == null || !PasswordUtil.verify(current, emp.getPassword())) {
            ViewUtil.printError("Current password is incorrect.");
            return;
        }

        if (empRepo.updatePassword(employeeId, newPass))
            ViewUtil.printSuccess("Password changed successfully.");
        else
            ViewUtil.printError("Failed to update password.");
    }
}
