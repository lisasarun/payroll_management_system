package project.controller;

import project.dto.EmployeeDTO;
import project.model.Bonus;
import project.model.Employee;
import project.model.Pagination;
import project.service.BonusService;
import project.service.EmployeeService;
import project.util.InputUtil;
import project.util.PasswordUtil;
import project.util.ViewUtil;

import java.math.BigDecimal;
import java.util.List;

public class AdminController {

    private final EmployeeService empService   = new EmployeeService();
    private final BonusService    bonusService = new BonusService();

    //  MANAGE EMPLOYEES

    public void manageEmployees() {
        boolean running = true;
        while (running) {
            ViewUtil.printManageEmployeeMenu();
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> addEmployee();
                case "2" -> {
                    showAllEmployeesForSelection();
                    updateEmployee();
                }
                case "3" -> {
                    showAllEmployeesForSelection();
                    searchEmployee();
                }
                case "4" -> {
                    showAllEmployeesForSelection();
                    disableEmployee();
                }
                case "5" -> listEmployees();
                case "0" -> running = false;
                default  -> ViewUtil.printError("Invalid option.");
            }
        }
    }

    private void addEmployee() {
        ViewUtil.printTitle("ADD EMPLOYEE");
        String fullName   = InputUtil.readFullName("  Full Name       : ");
        String email      = InputUtil.readEmailMatchingName("  Email           : ", fullName);
        String password   = InputUtil.readStrongPassword("  Password        ");
        BigDecimal salary = InputUtil.readBigDecimal("  Base Salary ($) : ");

        Employee emp = new Employee();
        emp.setFullName(fullName);
        emp.setEmail(email);
        emp.setPassword(password);   // EmployeeRepository.save() hashes this
        emp.setBaseSalary(salary);
        emp.setActive(true);

        if (empService.addEmployee(emp)) {
            ViewUtil.printSuccess("Employee added successfully.");
            // After successful add, show current employees so admin can see the new record
            showAllEmployeesForSelection();
        } else {
            ViewUtil.printError("Failed to add employee (email may already exist).");
        }
    }

    private void updateEmployee() {
        ViewUtil.printTitle("UPDATE EMPLOYEE");
        int id = InputUtil.readInt("  Employee ID: ");

        EmployeeDTO dto = empService.getById(id);
        if (dto == null) { ViewUtil.printError("Employee not found."); return; }

        System.out.println("  Employee : " + dto.getFullName());
        System.out.println("  Leave blank to keep current value.");

        // Full Name — validate if provided (same rules as addEmployee)
        System.out.printf("  Full Name  [%s]: ", dto.getFullName());
        String nameInput = InputUtil.readOptionalString("");
        String name = dto.getFullName();
        if (!nameInput.isEmpty()) {
            name = InputUtil.validateFullName(nameInput);
            if (name == null) return; // validation printed the error
        }

        // Email — validate if provided (must match the (possibly updated) full name)
        System.out.printf("  Email      [%s]: ", dto.getEmail());
        String emailInput = InputUtil.readOptionalString("");
        String email = dto.getEmail();
        if (!emailInput.isEmpty()) {
            email = InputUtil.validateEmailMatchingName(emailInput, name);
            if (email == null) return; // validation printed the error
        }

        // Salary — validate if provided (same rules as addEmployee)
        System.out.printf("  Salary     [%s]: ", dto.getBaseSalary());
        String salStr = InputUtil.readOptionalString("");
        BigDecimal salary = dto.getBaseSalary();
        if (!salStr.isEmpty()) {
            salary = InputUtil.validateSalary(salStr);
            if (salary == null) return; // validation printed the error
        }

        // Optional password reset
        System.out.print("  New Password (leave blank to keep): ");
        String newPass = InputUtil.readOptionalString("");

        Employee emp = new Employee();
        emp.setEmployeeId(id);
        emp.setFullName(name);
        emp.setEmail(email);
        emp.setBaseSalary(salary);

        // Hash new password if provided, then update
        if (!newPass.isEmpty()) {
            emp.setPassword(PasswordUtil.hash(newPass));
            if (empService.updateEmployeeWithPasswordChange(emp, dto.getEmail()))
                ViewUtil.printSuccess("Employee updated (password changed).");
            else
                ViewUtil.printError("Update failed (email may already be in use).");
        } else {
            if (empService.updateEmployeeWithEmailCheck(emp, dto.getEmail()))
                ViewUtil.printSuccess("Employee updated.");
            else
                ViewUtil.printError("Update failed (email may already be in use).");
        }
    }

    private void searchEmployee() {
        ViewUtil.printTitle("SEARCH EMPLOYEE");
        String kw = InputUtil.readString("  Keyword (name): ");
        List<EmployeeDTO> list = empService.search(kw, 1, 20);
        if (list.isEmpty()) { ViewUtil.printInfo("No results found."); return; }
        ViewUtil.printEmployeeTable(list, 1, 1);
    }

    private void disableEmployee() {
        ViewUtil.printTitle("DISABLE EMPLOYEE");
        int id = InputUtil.readInt("  Employee ID: ");
        EmployeeDTO dto = empService.getById(id);
        if (dto == null) { ViewUtil.printError("Employee not found."); return; }
        System.out.println("  Employee: " + dto.getFullName());
        if (!InputUtil.readConfirm("  Confirm disable?")) { ViewUtil.printInfo("Cancelled."); return; }
        if (empService.disableEmployee(id)) ViewUtil.printSuccess("Employee disabled.");
        else                                ViewUtil.printError("Failed to disable employee.");
    }

    private void listEmployees() {
        int total = empService.countAll();
        if (total == 0) { ViewUtil.printInfo("No employees found."); return; }

        int size = 5;
        Pagination pg = new Pagination(1, size, total);
        boolean running = true;
        String sortKey = selectEmployeeSortOption();

        while (running) {
            List<EmployeeDTO> list = empService.getAllPagedSorted(pg.getPage(), size, sortKey);
            ViewUtil.printEmployeeTable(list, pg.getPage(), pg.getTotalPages());

            if (pg.getTotalPages() > 1) {
                System.out.print("  Option [N/P/S/0]: ");
            } else {
                System.out.print("  Option [S/0]: ");
            }

            String ch = InputUtil.readMenuChoice("").toUpperCase();
            switch (ch) {
                case "N" -> { if (pg.hasNext()) pg.next(); else ViewUtil.printInfo("Already on last page."); }
                case "P" -> { if (pg.hasPrev()) pg.prev(); else ViewUtil.printInfo("Already on first page."); }
                case "S" -> {
                    sortKey = selectEmployeeSortOption();
                    pg = new Pagination(1, size, total); // reset to first page when sort changes
                }
                case "0" -> running = false;
                default  -> ViewUtil.printInfo("Enter N, P, S or 0.");
            }
        }
    }

    /**
     * Ask admin how to sort the employee list.
     * 1 = Salary (Highest), 2 = Name, 3 = ID (default).
     */
    private String selectEmployeeSortOption() {
        System.out.println();
        System.out.println("  EMPLOYEE LIST SORT OPTIONS");
        System.out.println("  [1] Sort by Salary (Highest)");
        System.out.println("  [2] Sort by Name");
        System.out.println("  [3] Sort by ID (default)");
        int choice = InputUtil.readIntInRange("  Select sort option: ", 1, 3);
        return switch (choice) {
            case 1 -> "SALARY_DESC";
            case 2 -> "NAME";
            case 3 -> "ID";
            default -> "ID";
        };
    }

    /**
     * Paginated employee list (5 per page) for admin to pick an ID before update/search/disable.
     */
    private void showAllEmployeesForSelection() {
        int total = empService.countAll();
        if (total == 0) {
            ViewUtil.printInfo("No employees found.");
            return;
        }
        int size = 5;
        Pagination pg = new Pagination(1, size, total);
        while (true) {
            List<EmployeeDTO> list = empService.getAllPaged(pg.getPage(), size);
            ViewUtil.printEmployeeTable(list, pg.getPage(), pg.getTotalPages());

            if (pg.getTotalPages() > 1) {
                System.out.print("  Navigate [N/P] or press Enter to continue: ");
            } else {
                System.out.print("  Press Enter to continue: ");
            }
            String ch = InputUtil.readMenuChoice("").toUpperCase();
            if (ch.equals("N")) {
                if (pg.hasNext()) pg.next(); else ViewUtil.printInfo("Already on last page.");
            } else if (ch.equals("P")) {
                if (pg.hasPrev()) pg.prev(); else ViewUtil.printInfo("Already on first page.");
            } else {
                break; // Enter or anything else → proceed
            }
        }
    }

    //  MANAGE BONUSES

    public void manageBonuses() {
        boolean running = true;
        while (running) {
            ViewUtil.printTitle("BONUS MANAGEMENT");
            System.out.println("  [1] Add Manual Bonus");
            System.out.println("  [2] View Employee Bonuses");
            System.out.println("  [0] Back");
            System.out.print("  Select: ");
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> addBonus();
                case "2" -> viewBonuses();
                case "0" -> running = false;
                default  -> ViewUtil.printError("Invalid option.");
            }
        }
    }

    private void addBonus() {
        int id = InputUtil.readInt("  Employee ID     : ");
        EmployeeDTO dto = empService.getById(id);
        if (dto == null) { ViewUtil.printError("Employee not found."); return; }
        System.out.println("  Employee: " + dto.getFullName());
        BigDecimal amount = InputUtil.readBigDecimal("  Bonus Amount ($): ");
        String reason     = InputUtil.readString("  Reason          : ");

        if (bonusService.addBonus(id, amount, reason))
            ViewUtil.printSuccess("Bonus added successfully.");
        else
            ViewUtil.printError("Failed to add bonus.");
    }

    private void viewBonuses() {
        int id = InputUtil.readInt("  Employee ID: ");
        EmployeeDTO dto = empService.getById(id);
        if (dto == null) { ViewUtil.printError("Employee not found."); return; }

        List<Bonus> bonuses = bonusService.getByEmployee(id);
        if (bonuses.isEmpty()) { ViewUtil.printInfo("No bonuses found for this employee."); return; }

        ViewUtil.printTitle("BONUSES — " + dto.getFullName());
        System.out.printf("  %-12s %-12s %-30s%n", "Date", "Amount", "Reason");
        System.out.println("  " + "-".repeat(56));
        bonuses.forEach(b -> System.out.printf("  %-12s $%-11.2f %-30s%n",
                b.getAwardedDate(), b.getAmount(), b.getReason()));
        System.out.println();
    }
}
