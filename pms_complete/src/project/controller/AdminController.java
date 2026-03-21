package project.controller;

import project.dto.EmployeeDTO;
import project.model.Bonus;
import project.model.Employee;
import project.model.Pagination;
import project.repository.ReferenceDataRepository;
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
    private final ReferenceDataRepository refDataRepo = new ReferenceDataRepository();

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
        
        // Load positions and departments dynamically from database
        List<String> positionsList = refDataRepo.getAllPositions();
        List<String> departmentsList = refDataRepo.getAllDepartments();
        
        // Fallback to hardcoded values if database tables not ready
        String[] positions = positionsList.isEmpty() 
            ? new String[]{"Junior Developer", "Senior Developer", "Software Engineer", 
                          "HR Coordinator", "Marketing Specialist", "Financial Analyst", "Manager"}
            : positionsList.toArray(new String[0]);
        
        String[] departments = departmentsList.isEmpty()
            ? new String[]{"Engineering", "Human Resources", "Marketing", 
                          "Finance", "Operations", "Sales", "IT Support"}
            : departmentsList.toArray(new String[0]);
        
        String position   = InputUtil.readChoice("  Position        :", positions);
        String department = InputUtil.readChoice("  Department      :", departments);
        
        // Show salary range for the position
        String salaryRange = project.util.SalaryCalculator.getSalaryRange(position);
        if (salaryRange != null) {
            System.out.println("  Allowed salary range for " + position + ": " + salaryRange);
        }
        BigDecimal salary = InputUtil.readBigDecimal("  Base Salary ($) : ");

        Employee emp = new Employee();
        emp.setFullName(fullName);
        emp.setEmail(email);
        emp.setPassword(password);   // EmployeeRepository.save() hashes this
        emp.setBaseSalary(salary);
        emp.setPosition(position);
        emp.setDepartment(department);
        emp.setActive(true);

        if (empService.addEmployee(emp)) {
            ViewUtil.printSuccess("Employee added successfully.");
            // After successful add, show current employees so admin can see the new record
            showAllEmployeesForSelection();
        }
        // Error messages already displayed by service/repository layer
    }

    private void updateEmployee() {
        ViewUtil.printTitle("UPDATE EMPLOYEE");
        int id = InputUtil.readInt("  Employee ID: ");
        
        if (id <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }

        EmployeeDTO dto = empService.getById(id);
        if (dto == null) { ViewUtil.printError("Employee not found or disabled."); return; }

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

        // Position - use selection menu with option to keep current
        String position = dto.getPosition();
        System.out.printf("  Position   [%s]%n", position != null ? position : "N/A");
        System.out.println("    [K] Keep current");
        System.out.println("    [C] Change position");
        String posChoice = InputUtil.readMenuChoice("  Select: ");
        if (!posChoice.equalsIgnoreCase("K") && !posChoice.equalsIgnoreCase("C")) {
            ViewUtil.printError("Invalid option. Please select K or C.");
            return;
        }
        if (posChoice.equalsIgnoreCase("C")) {
            // Load positions from database
            List<String> positionsList = refDataRepo.getAllPositions();
            String[] positions = positionsList.isEmpty() 
                ? new String[]{"Junior Developer", "Senior Developer", "Software Engineer", 
                              "HR Coordinator", "Marketing Specialist", "Financial Analyst", "Manager"}
                : positionsList.toArray(new String[0]);
            position = InputUtil.readChoice("  Select new position:", positions);
        }

        // Department - use selection menu with option to keep current
        String department = dto.getDepartment();
        System.out.printf("  Department [%s]%n", department != null ? department : "N/A");
        System.out.println("    [K] Keep current");
        System.out.println("    [C] Change department");
        String deptChoice = InputUtil.readMenuChoice("  Select: ");
        if (!deptChoice.equalsIgnoreCase("K") && !deptChoice.equalsIgnoreCase("C")) {
            ViewUtil.printError("Invalid option. Please select K or C.");
            return;
        }
        if (deptChoice.equalsIgnoreCase("C")) {
            // Load departments from database
            List<String> departmentsList = refDataRepo.getAllDepartments();
            String[] departments = departmentsList.isEmpty()
                ? new String[]{"Engineering", "Human Resources", "Marketing", 
                              "Finance", "Operations", "Sales", "IT Support"}
                : departmentsList.toArray(new String[0]);
            department = InputUtil.readChoice("  Select new department:", departments);
        }

        // Show salary range if position is set or changed
        if (position != null && !position.isEmpty()) {
            String salaryRange = project.util.SalaryCalculator.getSalaryRange(position);
            if (salaryRange != null) {
                System.out.println("  Allowed salary range for " + position + ": " + salaryRange);
            }
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
        emp.setPosition(position);
        emp.setDepartment(department);

        // Hash new password if provided, then update
        if (!newPass.isEmpty()) {
            emp.setPassword(PasswordUtil.hash(newPass));
            if (empService.updateEmployeeWithPasswordChange(emp, dto.getEmail())) {
                ViewUtil.printSuccess("Employee updated (password changed).");
            }
            // Error messages already displayed by service/repository layer
        } else {
            if (empService.updateEmployeeWithEmailCheck(emp, dto.getEmail())) {
                ViewUtil.printSuccess("Employee updated.");
            }
            // Error messages already displayed by service/repository layer
        }
    }

    private void searchEmployee() {
        ViewUtil.printTitle("SEARCH EMPLOYEE");
        System.out.println("  [1] Search by Name");
        System.out.println("  [2] Search by Position");
        System.out.println("  [3] Search by Department");
        System.out.print("  Select: ");
        String choice = InputUtil.readMenuChoice("");
        
        List<EmployeeDTO> list;
        if (choice.equals("2")) {
            // Search by Position - use selection menu
            List<String> positionsList = refDataRepo.getAllPositions();
            
            if (positionsList.isEmpty()) {
                ViewUtil.printError("No positions found in database.");
                return;
            }
            
            String[] positions = positionsList.toArray(new String[0]);
            String selectedPosition = InputUtil.readChoice("  Select position to search:", positions);
            list = empService.searchByPositionOrDepartment(selectedPosition, 1, 20);
            
        } else if (choice.equals("3")) {
            // Search by Department - use selection menu
            List<String> departmentsList = refDataRepo.getAllDepartments();
            
            if (departmentsList.isEmpty()) {
                ViewUtil.printError("No departments found in database.");
                return;
            }
            
            String[] departments = departmentsList.toArray(new String[0]);
            String selectedDepartment = InputUtil.readChoice("  Select department to search:", departments);
            list = empService.searchByPositionOrDepartment(selectedDepartment, 1, 20);
            
        } else {
            // Search by Name - free text is okay here
            String kw = InputUtil.readString("  Keyword (name): ");
            list = empService.search(kw, 1, 20);
        }
        
        if (list.isEmpty()) { ViewUtil.printInfo("No results found."); return; }
        ViewUtil.printEmployeeTable(list, 1, 1);
    }

    private void disableEmployee() {
        boolean running = true;
        while (running) {
            ViewUtil.printTitle("DISABLE EMPLOYEE");
            System.out.println("  [1] Active Employees");
            System.out.println("  [2] Disabled Employees");
            System.out.println("  [3] Enable Employee");
            System.out.println("  [0] Back");
            String choice = InputUtil.readMenuChoice("  Select: ");

            switch (choice) {
                case "1" -> disableFromActiveList();
                case "2" -> showEmployeesByStatus(false);
                case "3" -> enableFromDisabledList();
                case "0" -> running = false;
                default  -> ViewUtil.printError("Invalid option.");
            }
        }
    }

    private void disableFromActiveList() {
        if (!showEmployeesByStatus(true)) return;
        int id = InputUtil.readInt("  Employee ID: ");
        
        if (id <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }
        
        Employee model = empService.getModelByIdIgnoreStatus(id);
        if (model == null) {
            ViewUtil.printError("Employee ID not found.");
            return;
        }
        if (!model.isActive()) {
            ViewUtil.printError("Employee is already disabled.");
            return;
        }
        System.out.println("  Employee: " + model.getFullName());
        if (!InputUtil.readConfirm("  Confirm disable?")) {
            ViewUtil.printInfo("Cancelled.");
            return;
        }
        if (empService.disableEmployee(id)) ViewUtil.printSuccess("Employee disabled.");
        else                                ViewUtil.printError("Failed to disable employee. Please try again.");
    }

    private void enableFromDisabledList() {
        List<EmployeeDTO> disabled = empService.getByActiveStatus(false);
        if (disabled.isEmpty()) {
            ViewUtil.printInfo("No disabled employees found.");
            return;
        }
        ViewUtil.printEmployeeTable(disabled, 1, 1);
        int id = InputUtil.readInt("  Employee ID: ");
        
        if (id <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }
        
        Employee full = empService.getModelByIdIgnoreStatus(id);
        if (full == null) {
            ViewUtil.printError("Employee ID not found.");
            return;
        }
        if (full.isActive()) {
            ViewUtil.printError("Employee is already active.");
            return;
        }
        System.out.println("  Employee: " + full.getFullName());
        if (!InputUtil.readConfirm("  Confirm enable?")) {
            ViewUtil.printInfo("Cancelled.");
            return;
        }
        if (empService.enableEmployee(id)) ViewUtil.printSuccess("Employee enabled.");
        else                               ViewUtil.printError("Failed to enable employee. Please try again.");
    }

    private boolean showEmployeesByStatus(boolean active) {
        List<EmployeeDTO> list = empService.getByActiveStatus(active);
        if (list.isEmpty()) {
            ViewUtil.printInfo(active ? "No active employees found." : "No disabled employees found.");
            return false;
        }
        ViewUtil.printEmployeeTable(list, 1, 1);
        return true;
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
        
        if (id <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }
        
        EmployeeDTO dto = empService.getById(id);
        if (dto == null) { ViewUtil.printError("Employee not found or disabled."); return; }
        System.out.println("  Employee: " + dto.getFullName());
        BigDecimal amount = InputUtil.readBigDecimalInRange(
                "  Bonus Amount ($): ",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5000)
        );
        String reason     = InputUtil.readBonusReason("  Reason          ");

        if (bonusService.addBonus(id, amount, reason))
            ViewUtil.printSuccess("Bonus added successfully.");
        // Specific failure messages are already printed by service/repository layer
    }

    private void viewBonuses() {
        int id = InputUtil.readInt("  Employee ID: ");
        
        if (id <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }
        
        EmployeeDTO dto = empService.getById(id);
        if (dto == null) { ViewUtil.printError("Employee not found or disabled."); return; }

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
