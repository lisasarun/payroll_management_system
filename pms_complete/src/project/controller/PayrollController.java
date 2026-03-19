package project.controller;

import project.dto.EmployeeDTO;
import project.dto.PayrollDTO;
import project.model.Payslip;
import project.report.JasperReportGenerator;
import project.service.EmployeeService;
import project.service.PayrollService;
import project.util.DateUtil;
import project.util.InputUtil;
import project.util.ViewUtil;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PayrollController {

    private final PayrollService       payrollService = new PayrollService();
    private final EmployeeService      empService     = new EmployeeService();
    private final JasperReportGenerator reportGen     = new JasperReportGenerator();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void calculatePayroll() {
        boolean running = true;
        while (running) {
            ViewUtil.printTitle("CALCULATE PAYROLL");
            System.out.println("  [1] Calculate for one employee");
            System.out.println("  [2] Calculate for all employees");
            System.out.println("  [0] Back");
            System.out.print("  Select: ");
            switch (InputUtil.readMenuChoice("")) {
                case "1" -> calcForOne();
                case "2" -> calcForAll();
                case "0" -> running = false;
                default  -> ViewUtil.printError("Invalid option.");
            }
        }
    }

    private void calcForOne() {
        int id = InputUtil.readInt("  Employee ID: ");
        EmployeeDTO emp = empService.getById(id);
        if (emp == null) { ViewUtil.printError("Employee not found."); return; }

        System.out.println("  Employee: " + emp.getFullName());
        // Restrict payroll calculation to year 2026 only (any month in 2026)
        int month  = InputUtil.readIntInRange("  Month (1-12, year  2026): ", 1, 12);
        int year;
        while (true) {
            year = InputUtil.readInt("  Year  (must be 2026): ");
            if (year == 2026) {
                break;
            }
            ViewUtil.printError("Payroll can only be calculated for the year 2026.");
        }

        System.out.printf("  Working days this month: %d%n",
                DateUtil.getWorkingDaysInMonth(month, year));

        // Show employees WITHOUT payroll for this period (useful if admin wants to see who still needs payroll)
        java.time.LocalDate from = DateUtil.firstDayOfMonth(month, year);
        java.time.LocalDate to   = DateUtil.lastDayOfMonth(month, year);
        List<EmployeeDTO> noPayroll = empService.getWithoutPayroll(from, to);
        if (!noPayroll.isEmpty()) {
            System.out.println("\n  Employees without payroll for this period:");
            ViewUtil.printEmployeeTable(noPayroll, 1, 1);
        }

        if (!InputUtil.readConfirm("  Proceed with payroll calculation?")) {
            ViewUtil.printInfo("Cancelled."); return;
        }

        if (payrollService.calculatePayroll(id, month, year)) {
            ViewUtil.printSuccess("Payroll calculated.");
            PayrollDTO dto = payrollService.getLatest(id);
            if (dto != null) ViewUtil.printPayrollSummary(dto);
        } else {
            ViewUtil.printError("Payroll calculation failed.");
        }
    }

    private void calcForAll() {
        // Restrict payroll calculation to year 2026 only (any month in 2026)
        int month  = InputUtil.readIntInRange("  Month (1-12, year  2026): ", 1, 12);
        int year;
        while (true) {
            year = InputUtil.readInt("  Year  (must be 2026): ");
            if (year == 2026) {
                break;
            }
            ViewUtil.printError("Payroll can only be calculated for the year 2026.");
        }

        // Show which employees still don't have payroll for this period
        java.time.LocalDate from = DateUtil.firstDayOfMonth(month, year);
        java.time.LocalDate to   = DateUtil.lastDayOfMonth(month, year);
        List<EmployeeDTO> noPayroll = empService.getWithoutPayroll(from, to);
        if (noPayroll.isEmpty()) {
            ViewUtil.printInfo("All employees already have payroll for this period.");
        } else {
            System.out.println("\n  Employees without payroll for this period:");
            ViewUtil.printEmployeeTable(noPayroll, 1, 1);
        }

        if (!InputUtil.readConfirm("  Calculate payroll for ALL active employees?")) {
            ViewUtil.printInfo("Cancelled."); return;
        }

        int total = empService.countAll();
        int success = 0;
        int page = 1, size = 50;
        while (true) {
            List<EmployeeDTO> list = empService.getAllPaged(page, size);
            if (list.isEmpty()) break;
            for (EmployeeDTO e : list) {
                if (payrollService.calculatePayroll(e.getEmployeeId(), month, year)) success++;
            }
            if (list.size() < size) break;
            page++;
        }
        ViewUtil.printSuccess("Payroll calculated for " + success + " / " + total + " employees.");
    }

    public void generatePayslip() {
        ViewUtil.printTitle("GENERATE PAYSLIP");
        int empId = InputUtil.readInt("  Employee ID: ");
        EmployeeDTO emp = empService.getById(empId);
        if (emp == null) { ViewUtil.printError("Employee not found."); return; }

        List<PayrollDTO> payrolls = payrollService.getByEmployee(empId);
        if (payrolls.isEmpty()) { ViewUtil.printInfo("No payroll records found."); return; }

        System.out.println("\n  Payroll records for " + emp.getFullName() + ":");
        // Payroll ID is displayed here (READ-ONLY): user can select it, but never edit it.
        printPayrollTable(payrolls);

        // Payroll ID is used here for report generation.
        // We validate the ID exists in the list before building the payslip.
        int payrollId = readExistingPayrollId(payrolls, "\n  Enter Payroll ID: ");
        Payslip slip = payrollService.buildPayslip(empId, payrollId);
        if (slip == null) { ViewUtil.printError("Could not build payslip."); return; }

        System.out.println(slip);

        String pdfPath = reportGen.generatePayslip(slip);
        if (pdfPath != null) {
            ViewUtil.printSuccess("PDF saved to: " + pdfPath);
            ViewUtil.printInfo("Payslip is ready. You can open the PDF now.");
            InputUtil.readMenuChoice("  Press Enter to continue...");
        } else {
            ViewUtil.printError("PDF generation failed. If you had the PDF open, close it and try again.");
            InputUtil.readMenuChoice("  Press Enter to continue...");
        }
    }

    public void viewMyPayslip(int employeeId) {
        ViewUtil.printTitle("MY PAYSLIP");
        List<PayrollDTO> payrolls = payrollService.getByEmployee(employeeId);
        if (payrolls.isEmpty()) { ViewUtil.printInfo("No payroll records yet."); return; }

        System.out.println("  Your payroll records:");
        // Payroll ID is displayed here (READ-ONLY): user can select it, but never edit it.
        printPayrollTable(payrolls);

        // Payroll ID is used here for report generation.
        // We validate the ID exists in the list before building the payslip.
        int payrollId = readExistingPayrollId(payrolls, "\n  Enter Payroll ID: ");
        Payslip slip = payrollService.buildPayslip(employeeId, payrollId);
        if (slip == null) { ViewUtil.printError("Payslip not available."); return; }

        System.out.println(slip);

        String pdfPath = reportGen.generatePayslip(slip);
        if (pdfPath != null) {
            ViewUtil.printSuccess("PDF saved to: " + pdfPath);
            ViewUtil.printInfo("Payslip is ready. You can open the PDF now.");
            InputUtil.readMenuChoice("  Press Enter to continue...");
        } else {
            ViewUtil.printError("PDF generation failed. If you had the PDF open, close it and try again.");
            InputUtil.readMenuChoice("  Press Enter to continue...");
        }
    }

    /**
     * UI helper: prints payroll records in a clean aligned table.
     * Columns required by spec:
     * - Payroll ID (READ-ONLY)
     * - Employee ID
     * - Salary (we display Total Paid)
     * - Date (Payment Date)
     */
    private void printPayrollTable(List<PayrollDTO> payrolls) {
        System.out.printf("%n  %-10s %-10s %-14s %-12s%n", "PayrollID", "EmpID", "Salary", "Pay Date");
        System.out.println("  " + "-".repeat(52));
        for (PayrollDTO p : payrolls) {
            System.out.printf("  %-10d %-10d %-14s %-12s%n",
                    p.getPayrollId(),
                    p.getEmployeeId(),
                    money(p.getTotalPaid()),
                    p.getPaymentDate() != null ? p.getPaymentDate().format(DATE_FMT) : "—");
        }
        System.out.println("  " + "-".repeat(52));
    }

    /**
     * Reads a Payroll ID from the user and validates it exists in the given list.
     * Used for actions that require a payroll record (payslip generation / future update/delete).
     */
    private int readExistingPayrollId(List<PayrollDTO> payrolls, String prompt) {
        while (true) {
            int id = InputUtil.readInt(prompt);
            boolean exists = payrolls.stream().anyMatch(p -> p.getPayrollId() == id);
            if (exists) return id;
            ViewUtil.printError("Payroll ID not found in the list above. Please enter a valid Payroll ID.");
        }
    }

    private String money(BigDecimal v) {
        return v == null ? "$0.00" : String.format("$%,.2f", v);
    }
}