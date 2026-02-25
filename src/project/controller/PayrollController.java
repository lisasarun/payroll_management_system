package project.controller;



import project.dto.PayrollDTO;
import project.dto.SalaryReportDTO;
import project.model.Payslip;
import project.service.EmployeeService;
import project.service.PayrollService;
import project.service.ReportService;
import project.util.InputUtil;

import java.util.List;

public class PayrollController {
    private final PayrollService payrollService;
    private final EmployeeService employeeService;
    private final ReportService reportService;
    private final InputUtil inputUtil;

    public PayrollController() {
        this.payrollService = new PayrollService();
        this.employeeService = new EmployeeService();
        this.reportService = new ReportService();
        this.inputUtil = new InputUtil();
    }

    public void showPayrollMenu() {
        while (true) {
            System.out.println("\n=== PAYROLL MANAGEMENT SYSTEM ===");
            System.out.println("1. Generate Monthly Payroll");
            System.out.println("2. View All Payrolls");
            System.out.println("3. View Employee Payrolls");
            System.out.println("4. Generate Payslip");
            System.out.println("5. Process Payment");
            System.out.println("6. Salary Report");
            System.out.println("7. Back to Main Menu");

            int choice = inputUtil.readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    generateMonthlyPayroll();
                    break;
                case 2:
                    viewAllPayrolls();
                    break;
                case 3:
                    viewEmployeePayrolls();
                    break;
                case 4:
                    generatePayslip();
                    break;
                case 5:
                    processPayment();
                    break;
                case 6:
                    generateSalaryReport();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private void generateMonthlyPayroll() {
        System.out.println("\n=== GENERATE MONTHLY PAYROLL ===");

        int month = inputUtil.readInt("Enter month (1-12): ");
        int year = inputUtil.readInt("Enter year: ");

        // Validate month
        if (month < 1 || month > 12) {
            System.out.println("Invalid month!");
            return;
        }

        // Check if payroll already exists for this month
        System.out.println("Checking for existing payroll records...");

        String confirm = inputUtil.readString("Generate payroll for " + month + "/" + year + "? (yes/no): ");

        if (!confirm.equalsIgnoreCase("yes")) {
            return;
        }

        var employees = employeeService.getAllEmployees(null);

        if (employees == null || employees.isEmpty()) {
            System.out.println("No employees found!");
            return;
        }

        System.out.println("Generating payroll for " + employees.size() + " employees...");

        int successCount = 0;
        for (var emp : employees) {
            if (payrollService.generatePayroll(emp.getEmployeeId(), month, year)) {
                successCount++;
                System.out.print(".");
            }
        }

        System.out.println("\nPayroll generated successfully for " + successCount + " employees!");
    }

    private void viewAllPayrolls() {
        System.out.println("\n=== ALL PAYROLLS ===");

        var employees = employeeService.getAllEmployees(null);

        if (employees == null || employees.isEmpty()) {
            System.out.println("No employees found!");
            return;
        }

        System.out.println("Employee ID\tName\t\tMonth/Year\tNet Salary\tStatus");
        System.out.println("----------------------------------------------------------------");

        for (var emp : employees) {
            List<PayrollDTO> payrolls = payrollService.getEmployeePayrolls(emp.getEmployeeId());

            if (payrolls != null && !payrolls.isEmpty()) {
                for (PayrollDTO p : payrolls) {
                    System.out.printf("%d\t\t%s %s\t%d/%d\t\t$%.2f\t%s\n",
                            emp.getEmployeeId(),
                            emp.getFirstName(),
                            emp.getLastName().charAt(0) + ".",
                            p.getMonth(),
                            p.getYear(),
                            p.getNetSalary(),
                            p.getStatus());
                }
            }
        }
    }

    private void viewEmployeePayrolls() {
        System.out.println("\n=== VIEW EMPLOYEE PAYROLLS ===");

        int employeeId = inputUtil.readInt("Enter Employee ID: ");

        var employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            System.out.println("Employee not found!");
            return;
        }

        System.out.println("Employee: " + employee.getFirstName() + " " + employee.getLastName());

        List<PayrollDTO> payrolls = payrollService.getEmployeePayrolls(employeeId);

        if (payrolls != null && !payrolls.isEmpty()) {
            System.out.println("\nPayroll History:");
            System.out.println("ID\tMonth/Year\tBasic Salary\tAllowances\tDeductions\tNet Salary\tStatus");
            System.out.println("--------------------------------------------------------------------------------");

            for (PayrollDTO p : payrolls) {
                System.out.printf("%d\t%d/%d\t\t$%.2f\t\t$%.2f\t\t$%.2f\t\t$%.2f\t%s\n",
                        p.getPayrollId(),
                        p.getMonth(),
                        p.getYear(),
                        p.getBasicSalary(),
                        p.getAllowances(),
                        p.getDeductions(),
                        p.getNetSalary(),
                        p.getStatus());
            }

            // Calculate totals
            double totalNet = payrolls.stream().mapToDouble(PayrollDTO::getNetSalary).sum();
            System.out.println("\nTotal Net Salary: $" + String.format("%.2f", totalNet));

        } else {
            System.out.println("No payroll records found for this employee!");
        }
    }

    private void generatePayslip() {
        System.out.println("\n=== GENERATE PAYSLIP ===");

        int employeeId = inputUtil.readInt("Enter Employee ID: ");

        var employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            System.out.println("Employee not found!");
            return;
        }

        List<PayrollDTO> payrolls = payrollService.getEmployeePayrolls(employeeId);

        if (payrolls == null || payrolls.isEmpty()) {
            System.out.println("No payroll records found for this employee!");
            return;
        }

        System.out.println("\nAvailable Payrolls:");
        for (PayrollDTO p : payrolls) {
            System.out.printf("%d - %d/%d (%s)\n",
                    p.getPayrollId(),
                    p.getMonth(),
                    p.getYear(),
                    p.getStatus());
        }

        int payrollId = inputUtil.readInt("Enter Payroll ID: ");

        Payslip payslip = payrollService.generatePayslip(employeeId, payrollId);

        if (payslip != null) {
            System.out.println("\n" + payslip.toString());

            // Option to save to file
            String saveOption = inputUtil.readString("Save payslip to file? (yes/no): ");
            if (saveOption.equalsIgnoreCase("yes")) {
                String filename = "payslip_" + employeeId + "_" + payrollId + ".txt";
                try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
                    writer.print(payslip.toString());
                    System.out.println("Payslip saved to: " + filename);
                } catch (Exception e) {
                    System.out.println("Error saving payslip: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Failed to generate payslip!");
        }
    }

    private void processPayment() {
        System.out.println("\n=== PROCESS PAYMENT ===");

        int payrollId = inputUtil.readInt("Enter Payroll ID: ");

        // First, find and display payroll details
        PayrollDTO targetPayroll = null;
        var employees = employeeService.getAllEmployees(null);

        for (var emp : employees) {
            List<PayrollDTO> payrolls = payrollService.getEmployeePayrolls(emp.getEmployeeId());
            if (payrolls != null) {
                targetPayroll = payrolls.stream()
                        .filter(p -> p.getPayrollId() == payrollId)
                        .findFirst()
                        .orElse(null);
                if (targetPayroll != null) break;
            }
        }

        if (targetPayroll == null) {
            System.out.println("Payroll record not found!");
            return;
        }

        System.out.println("\nPayroll Details:");
        System.out.println("Payroll ID: " + targetPayroll.getPayrollId());
        System.out.println("Period: " + targetPayroll.getMonth() + "/" + targetPayroll.getYear());
        System.out.println("Net Salary: $" + String.format("%.2f", targetPayroll.getNetSalary()));
        System.out.println("Current Status: " + targetPayroll.getStatus());

        if ("PAID".equals(targetPayroll.getStatus())) {
            System.out.println("This payroll has already been paid!");
            return;
        }

        String confirm = inputUtil.readString("Process payment for this payroll? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            if (payrollService.processPayment(payrollId)) {
                System.out.println("Payment processed successfully!");

                // Generate final payslip
                Payslip payslip = payrollService.generatePayslip(
                        targetPayroll.getEmployeeId(),
                        payrollId
                );

                if (payslip != null) {
                    System.out.println("\nFinal Payslip generated:");
                    System.out.println("----------------------");
                    System.out.println(payslip.toString());
                }
            } else {
                System.out.println("Failed to process payment!");
            }
        }
    }

    private void generateSalaryReport() {
        System.out.println("\n=== GENERATE SALARY REPORT ===");

        int month = inputUtil.readInt("Enter month (1-12): ");
        int year = inputUtil.readInt("Enter year: ");

        String filename = "salary_report_" + month + "_" + year + ".pdf";

        if (reportService.generateSalaryReport(month, year, filename)) {
            System.out.println("Salary report generated successfully: " + filename);
        } else {
            System.out.println("Failed to generate salary report!");
        }
    }
}
