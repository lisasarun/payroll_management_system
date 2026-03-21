package project.controller;

import project.repository.EmployeeRepository;
import project.util.InputUtil;
import project.util.PasswordUtil;
import project.util.ViewUtil;


public class EmployeeController {

    private final EmployeeRepository empRepo = new EmployeeRepository();

    public void changePassword(int employeeId) {
        System.out.println();
        ViewUtil.printTitle("CHANGE PASSWORD");


        var emp = empRepo.findById(employeeId);
        if (emp == null) {
            ViewUtil.printError("Employee not found.");
            return;
        }

        String current = InputUtil.readPassword("  Current Password");
        if (!PasswordUtil.verifyAny(current, emp.getPassword())) {
            ViewUtil.printError("Current password is incorrect.");
            return;
        }


        String newPass  = InputUtil.readStrongPassword("  New Password    ");
        String confirm  = InputUtil.readStrongPassword("  Confirm New     ");

        if (!newPass.equals(confirm)) {
            ViewUtil.printError("Passwords do not match.");
            return;
        }

        if (empRepo.updatePassword(employeeId, newPass))
            ViewUtil.printSuccess("Password changed successfully.");
        else
            ViewUtil.printError("Failed to update password.");
    }
}
