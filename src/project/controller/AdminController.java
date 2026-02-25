package project.controller;

import project.dto.EmployeeDTO;
import project.dto.PayrollDTO;
import project.dto.PerformanceDTO;
import project.model.Pagination;
import project.service.*;
import project.util.InputUtil;

import java.time.LocalDate;
import java.util.List;

    public class AdminController {
        private final AuthService authService;
        private final EmployeeService employeeService;
        private final AttendanceService attendanceService;
        private final PayrollService payrollService;
        private final PerformanceService performanceService;
        private final ReportService reportService;
        private final InputUtil inputUtil;

        public AdminController(AuthService authService) {
            this.authService = authService;
            this.employeeService = new EmployeeService();
            this.attendanceService = new AttendanceService();
            this.payrollService = new PayrollService();
            this.performanceService = new PerformanceService();
            this.reportService = new ReportService();
            this.inputUtil = new InputUtil();
        }

        public void showAdminMenu() {
            while (true) {
                System.out.println("\n=== ADMIN DASHBOARD ===");
                System.out.println("1. Employee Management");
                System.out.println("2. Attendance Management");
                System.out.println("3. Payroll Management");
                System.out.println("4. Performance Management");
                System.out.println("5. Generate Reports");
                System.out.println("6. Logout");

                int choice = inputUtil.readInt("Enter your choice: ");

                switch (choice) {                    case 1:

                        employeeManagementMenu();
                        break;
                    case 2:
                        attendanceManagementMenu();
                        break;
                    case 3:
                        payrollManagementMenu();
                        break;
                    case 4:
                        performanceManagementMenu();
                        break;
                    case 5:
                        reportGenerationMenu();
                        break;
                    case 6:
                        authService.logout();
                        System.out.println("Logged out successfully!");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }
        }

        private void employeeManagementMenu() {
            while (true) {
                System.out.println("\n=== EMPLOYEE MANAGEMENT ===");
                System.out.println("1. Add Employee");
                System.out.println("2. View All Employees");
                System.out.println("3. Search Employee");
                System.out.println("4. Update Employee");
                System.out.println("5. Delete Employee");
                System.out.println("6. Back to Main Menu");

                int choice = inputUtil.readInt("Enter your choice: ");

                switch (choice) {
                    case 1:
                        addEmployee();
                        break;
                    case 2:
                        viewAllEmployees();
                        break;
                    case 3:
                        searchEmployee();
                        break;
                    case 4:
                        updateEmployee();
                        break;
                    case 5:
                        deleteEmployee();
                        break;
                    case 6:
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }
        }

        private void addEmployee() {
            System.out.println("\n=== ADD NEW EMPLOYEE ===");

            EmployeeDTO employee = new EmployeeDTO();
            employee.setFirstName(inputUtil.readString("First Name: "));
            employee.setLastName(inputUtil.readString("Last Name: "));
            employee.setEmail(inputUtil.readString("Email: "));
            employee.setPhone(inputUtil.readString("Phone: "));
            employee.setDepartment(inputUtil.readString("Department: "));
            employee.setPosition(inputUtil.readString("Position: "));
            employee.setSalary(inputUtil.readDouble("Salary: "));
            employee.setHireDate(LocalDate.now());

            if (employeeService.addEmployee(employee)) {
                System.out.println("Employee added successfully!");
            } else {
                System.out.println("Failed to add employee!");
            }
        }

        private void viewAllEmployees() {
            System.out.println("\n=== ALL EMPLOYEES ===");

            int page = 1;
            int pageSize = 10;
            int totalEmployees = employeeService.getTotalEmployeeCount();
            int totalPages = (int) Math.ceil((double) totalEmployees / pageSize);

            while (true) {
                Pagination pagination = new Pagination(page, pageSize);
                List<EmployeeDTO> employees = employeeService.getAllEmployees(pagination);

                if (employees != null && !employees.isEmpty()) {
                    System.out.println("\nPage " + page + " of " + totalPages);
                    System.out.println("ID\tName\t\tDepartment\tPosition\tSalary");
                    System.out.println("--------------------------------------------------------");

                    for (EmployeeDTO emp : employees) {
                        System.out.printf("%d\t%s %s\t%s\t%s\t%.2f\n",
                                emp.getEmployeeId(),
                                emp.getFirstName(),
                                emp.getLastName(),
                                emp.getDepartment(),
                                emp.getPosition(),
                                emp.getSalary());
                    }

                    System.out.println("\n1. Next Page");
                    System.out.println("2. Previous Page");
                    System.out.println("3. Back");

                    int choice = inputUtil.readInt("Enter your choice: ");

                    switch (choice) {
                        case 1:
                            if (page < totalPages) page++;
                            else System.out.println("You're on the last page!");
                            break;
                        case 2:
                            if (page > 1) page--;
                            else System.out.println("You're on the first page!");
                            break;
                        case 3:
                            return;
                    }
                } else {
                    System.out.println("No employees found!");
                    return;
                }
            }
        }

        private void searchEmployee() {
            System.out.println("\n=== SEARCH EMPLOYEE ===");
            String keyword = inputUtil.readString("Enter search keyword: ");

            List<EmployeeDTO> employees = employeeService.searchEmployees(keyword);

            if (employees != null && !employees.isEmpty()) {
                System.out.println("\nSearch Results:");
                System.out.println("ID\tName\t\tDepartment\tPosition");
                System.out.println("------------------------------------------------");

                for (EmployeeDTO emp : employees) {
                    System.out.printf("%d\t%s %s\t%s\t%s\n",
                            emp.getEmployeeId(),
                            emp.getFirstName(),
                            emp.getLastName(),
                            emp.getDepartment(),
                            emp.getPosition());
                }
            } else {
                System.out.println("No employees found!");
            }
        }

        private void updateEmployee() {
            System.out.println("\n=== UPDATE EMPLOYEE ===");
            int employeeId = inputUtil.readInt("Enter Employee ID: ");

            EmployeeDTO employee = employeeService.getEmployeeById(employeeId);

            if (employee != null) {
                System.out.println("Leave blank to keep current value");

                String firstName = inputUtil.readString("First Name (" + employee.getFirstName() + "): ");
                if (!firstName.isEmpty()) employee.setFirstName(firstName);

                String lastName = inputUtil.readString("Last Name (" + employee.getLastName() + "): ");
                if (!lastName.isEmpty()) employee.setLastName(lastName);

                String email = inputUtil.readString("Email (" + employee.getEmail() + "): ");
                if (!email.isEmpty()) employee.setEmail(email);

                String phone = inputUtil.readString("Phone (" + employee.getPhone() + "): ");
                if (!phone.isEmpty()) employee.setPhone(phone);

                String department = inputUtil.readString("Department (" + employee.getDepartment() + "): ");
                if (!department.isEmpty()) employee.setDepartment(department);

                String position = inputUtil.readString("Position (" + employee.getPosition() + "): ");
                if (!position.isEmpty()) employee.setPosition(position);

                String salaryStr = inputUtil.readString("Salary (" + employee.getSalary() + "): ");
                if (!salaryStr.isEmpty()) {
                    employee.setSalary(Double.parseDouble(salaryStr));
                }

                if (employeeService.updateEmployee(employee)) {
                    System.out.println("Employee updated successfully!");
                } else {
                    System.out.println("Failed to update employee!");
                }
            } else {
                System.out.println("Employee not found!");
            }
        }

        private void deleteEmployee() {
            System.out.println("\n=== DELETE EMPLOYEE ===");
            int employeeId = inputUtil.readInt("Enter Employee ID to delete: ");

            EmployeeDTO employee = employeeService.getEmployeeById(employeeId);

            if (employee != null) {
                System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());
                String confirm = inputUtil.readString("Are you sure? (yes/no): ");

                if (confirm.equalsIgnoreCase("yes")) {
                    if (employeeService.deleteEmployee(employeeId)) {
                        System.out.println("Employee deleted successfully!");
                    } else {
                        System.out.println("Failed to delete employee!");
                    }
                }
            } else {
                System.out.println("Employee not found!");
            }
        }

        private void attendanceManagementMenu() {
            System.out.println("\n=== ATTENDANCE MANAGEMENT ===");
            System.out.println("1. View Today's Attendance");
            System.out.println("2. View Employee Attendance");
            System.out.println("3. View Attendance by Date");
            System.out.println("4. Back");

            int choice = inputUtil.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    viewTodaysAttendance();
                    break;
                case 2:
                    viewEmployeeAttendance();
                    break;
                case 3:
                    viewAttendanceByDate();
                    break;
                case 4:
                    return;
            }
        }

        private void viewTodaysAttendance() {
            System.out.println("\n=== TODAY'S ATTENDANCE ===");
            LocalDate today = LocalDate.now();

            var attendanceList = attendanceService.getAttendanceByDate(today);

            if (attendanceList != null && !attendanceList.isEmpty()) {
                System.out.println("Date: " + today);
                System.out.println("Employee ID\tStatus\tCheck In\tCheck Out");
                System.out.println("----------------------------------------");

                attendanceList.forEach(a -> {
                    System.out.printf("%d\t\t%s\t%s\t%s\n",
                            a.getEmployeeId(),
                            a.getStatus(),
                            a.getCheckIn(),
                            a.getCheckOut());
                });

                long present = attendanceList.stream()
                        .filter(a -> "PRESENT".equals(a.getStatus()))
                        .count();

                System.out.println("\nTotal Present: " + present);
                System.out.println("Total Absent: " + (attendanceList.size() - present));
            } else {
                System.out.println("No attendance records for today!");
            }
        }

        private void viewEmployeeAttendance() {
            int employeeId = inputUtil.readInt("Enter Employee ID: ");

            var attendanceList = attendanceService.getAttendanceByEmployee(employeeId);

            if (attendanceList != null && !attendanceList.isEmpty()) {
                System.out.println("\n=== EMPLOYEE ATTENDANCE RECORDS ===");
                System.out.println("Date\t\tStatus\tCheck In\tCheck Out");
                System.out.println("----------------------------------------");

                attendanceList.forEach(a -> {
                    System.out.printf("%s\t%s\t%s\t%s\n",
                            a.getDate(),
                            a.getStatus(),
                            a.getCheckIn(),
                            a.getCheckOut());
                });
            } else {
                System.out.println("No attendance records found!");
            }
        }

        private void viewAttendanceByDate() {
            LocalDate date = inputUtil.readDate("Enter date (yyyy-mm-dd): ");

            var attendanceList = attendanceService.getAttendanceByDate(date);

            if (attendanceList != null && !attendanceList.isEmpty()) {
                System.out.println("\n=== ATTENDANCE FOR " + date + " ===");
                System.out.println("Employee ID\tStatus\tCheck In\tCheck Out");
                System.out.println("----------------------------------------");

                attendanceList.forEach(a -> {
                    System.out.printf("%d\t\t%s\t%s\t%s\n",
                            a.getEmployeeId(),
                            a.getStatus(),
                            a.getCheckIn(),
                            a.getCheckOut());
                });
            } else {
                System.out.println("No attendance records for this date!");
            }
        }

        private void payrollManagementMenu() {
            while (true) {
                System.out.println("\n=== PAYROLL MANAGEMENT ===");
                System.out.println("1. Generate Monthly Payroll");
                System.out.println("2. View Employee Payrolls");
                System.out.println("3. Generate Payslip");
                System.out.println("4. Process Payment");
                System.out.println("5. Back");

                int choice = inputUtil.readInt("Enter your choice: ");

                switch (choice) {
                    case 1:
                        generateMonthlyPayroll();
                        break;
                    case 2:
                        viewEmployeePayrolls();
                        break;
                    case 3:
                        generatePayslip();
                        break;
                    case 4:
                        processPayment();
                        break;
                    case 5:
                        return;
                }
            }
        }

        private void generateMonthlyPayroll() {
            System.out.println("\n=== GENERATE MONTHLY PAYROLL ===");
            int month = inputUtil.readInt("Enter month (1-12): ");
            int year = inputUtil.readInt("Enter year: ");

            int totalEmployees = employeeService.getTotalEmployeeCount();
            int successCount = 0;

            for (int i = 1; i <= totalEmployees; i++) {
                if (payrollService.generatePayroll(i, month, year)) {
                    successCount++;
                }
            }

            System.out.println("Payroll generated for " + successCount + " employees!");
        }

        private void viewEmployeePayrolls() {
            int employeeId = inputUtil.readInt("Enter Employee ID: ");

            List<PayrollDTO> payrolls = payrollService.getEmployeePayrolls(employeeId);

            if (payrolls != null && !payrolls.isEmpty()) {
                System.out.println("\n=== EMPLOYEE PAYROLLS ===");
                System.out.println("ID\tMonth/Year\tBasic\tNet Salary\tStatus");
                System.out.println("----------------------------------------");

                payrolls.forEach(p -> {
                    System.out.printf("%d\t%d/%d\t%.2f\t%.2f\t%s\n",
                            p.getPayrollId(),
                            p.getMonth(),
                            p.getYear(),
                            p.getBasicSalary(),
                            p.getNetSalary(),
                            p.getStatus());
                });
            } else {
                System.out.println("No payroll records found!");
            }
        }

        private void generatePayslip() {
            int employeeId = inputUtil.readInt("Enter Employee ID: ");
            int payrollId = inputUtil.readInt("Enter Payroll ID: ");

            var payslip = payrollService.generatePayslip(employeeId, payrollId);

            if (payslip != null) {
                System.out.println("\n=== PAYSLIP ===");
                System.out.println(payslip.toString());
            } else {
                System.out.println("Failed to generate payslip!");
            }
        }

        private void processPayment() {
            int payrollId = inputUtil.readInt("Enter Payroll ID: ");

            if (payrollService.processPayment(payrollId)) {
                System.out.println("Payment processed successfully!");
            } else {
                System.out.println("Failed to process payment!");
            }
        }

        private void performanceManagementMenu() {
            while (true) {
                System.out.println("\n=== PERFORMANCE MANAGEMENT ===");
                System.out.println("1. Add Performance Review");
                System.out.println("2. View Employee Performances");
                System.out.println("3. View Top Performers");
                System.out.println("4. Back");

                int choice = inputUtil.readInt("Enter your choice: ");

                switch (choice) {
                    case 1:
                        addPerformanceReview();
                        break;
                    case 2:
                        viewEmployeePerformances();
                        break;
                    case 3:
                        viewTopPerformers();
                        break;
                    case 4:
                        return;
                }
            }
        }

        private void addPerformanceReview() {
            System.out.println("\n=== ADD PERFORMANCE REVIEW ===");

            PerformanceDTO performance = new PerformanceDTO();
            performance.setEmployeeId(inputUtil.readInt("Employee ID: "));
            performance.setRating(inputUtil.readInt("Rating (1-5): "));
            performance.setComments(inputUtil.readString("Comments: "));
            performance.setReviewDate(LocalDate.now());
            performance.setReviewer(authService.getCurrentUser().getUsername());

            if (performanceService.addPerformanceReview(performance)) {
                System.out.println("Performance review added successfully!");
            } else {
                System.out.println("Failed to add performance review!");
            }
        }

        private void viewEmployeePerformances() {
            int employeeId = inputUtil.readInt("Enter Employee ID: ");

            List<PerformanceDTO> performances = performanceService.getEmployeePerformances(employeeId);

            if (performances != null && !performances.isEmpty()) {
                System.out.println("\n=== PERFORMANCE REVIEWS ===");
                System.out.println("Date\t\tRating\tReviewer\tComments");
                System.out.println("----------------------------------------");

                performances.forEach(p -> {
                    System.out.printf("%s\t%d\t%s\t%s\n",
                            p.getReviewDate(),
                            p.getRating(),
                            p.getReviewer(),
                            p.getComments());
                });

                double avgRating = performanceService.getAverageRating(employeeId);
                System.out.println("\nAverage Rating: " + String.format("%.2f", avgRating));
            } else {
                System.out.println("No performance reviews found!");
            }
        }

        private void viewTopPerformers() {
            int year = inputUtil.readInt("Enter year: ");

            List<PerformanceDTO> topPerformers = performanceService.getTopPerformers(year);

            if (topPerformers != null && !topPerformers.isEmpty()) {
                System.out.println("\n=== TOP PERFORMERS " + year + " ===");
                System.out.println("Employee ID\tRating\tDate\t\tComments");
                System.out.println("----------------------------------------");

                topPerformers.forEach(p -> {
                    System.out.printf("%d\t\t%d\t%s\t%s\n",
                            p.getEmployeeId(),
                            p.getRating(),
                            p.getReviewDate(),
                            p.getComments());
                });
            } else {
                System.out.println("No top performers found!");
            }
        }

        private void reportGenerationMenu() {
            System.out.println("\n=== GENERATE REPORTS ===");
            System.out.println("1. Salary Report");
            System.out.println("2. Attendance Report");
            System.out.println("3. Back");

            int choice = inputUtil.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    generateSalaryReport();
                    break;
                case 2:
                    generateAttendanceReport();
                    break;

            }
