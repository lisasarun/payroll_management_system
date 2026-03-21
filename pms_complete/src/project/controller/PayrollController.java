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

import java.io.File;
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
        
        if (id <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }
        
        EmployeeDTO emp = empService.getById(id);
        if (emp == null) { ViewUtil.printError("Employee not found or disabled."); return; }

        System.out.println("  Employee: " + emp.getFullName());
        java.time.LocalDate date = InputUtil.readDateInYear("  Pay Period Date (yyyy-MM-dd, year must be 2026)", 2026);
        int month = date.getMonthValue();
        int year  = date.getYear();

        System.out.printf("  Working days this month: %d%n",
                DateUtil.getWorkingDaysInMonth(month, year));

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
        java.time.LocalDate date = InputUtil.readDateInYear("  Pay Period Date (yyyy-MM-dd, year must be 2026)", 2026);
        int month = date.getMonthValue();
        int year  = date.getYear();

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


        int total = empService.countAll();
        if (total == 0) { ViewUtil.printInfo("No employees found."); return; }
        int size = 5;
        project.model.Pagination pg = new project.model.Pagination(1, size, total);
        while (true) {
            List<EmployeeDTO> page = empService.getAllPaged(pg.getPage(), size);
            ViewUtil.printEmployeeTable(page, pg.getPage(), pg.getTotalPages());
            if (pg.getTotalPages() > 1)
                System.out.print("  Navigate [N/P] or press Enter to select: ");
            else
                System.out.print("  Press Enter to continue: ");
            String nav = InputUtil.readMenuChoice("").toUpperCase();
            if (nav.equals("N")) { if (pg.hasNext()) pg.next(); else ViewUtil.printInfo("Already on last page."); }
            else if (nav.equals("P")) { if (pg.hasPrev()) pg.prev(); else ViewUtil.printInfo("Already on first page."); }
            else break;
        }

        int empId = InputUtil.readInt("  Employee ID: ");
        
        if (empId <= 0) {
            ViewUtil.printError("Invalid Employee ID. ID must be a positive number.");
            return;
        }
        
        EmployeeDTO emp = empService.getById(empId);
        if (emp == null) { ViewUtil.printError("Employee not found or disabled."); return; }

        List<PayrollDTO> payrolls = payrollService.getByEmployee(empId);
        if (payrolls.isEmpty()) { ViewUtil.printInfo("No payroll records found."); return; }

        System.out.println("\n  Payroll records for " + emp.getFullName() + ":");

        printPayrollTable(payrolls);

        int payrollId = readExistingPayrollId(payrolls, "\n  Enter Payroll ID: ");
        Payslip slip = payrollService.buildPayslip(empId, payrollId);
        if (slip == null) { ViewUtil.printError("Could not build payslip."); return; }

        System.out.println(slip);

        String pdfPath = reportGen.generatePayslip(slip);
        if (pdfPath != null) {
            ViewUtil.printSuccess("PDF saved to: " + pdfPath);

            if (openPdf(pdfPath)) {
                ViewUtil.printInfo("Payslip PDF opened successfully.");
            } else {
                ViewUtil.printInfo("Could not auto-open PDF. Please open manually: " + pdfPath);
            }
            
            InputUtil.readMenuChoice("  Press Enter to continue...");
        } else {
            ViewUtil.printError("PDF generation failed. If you had the PDF open, close it and try again.");
            InputUtil.readMenuChoice("  Press Enter to continue...");
        }
    }

    public void viewMyPayslip(int employeeId) {
        System.out.println();
        ViewUtil.printTitle("MY PAYSLIP");
        List<PayrollDTO> payrolls = payrollService.getByEmployee(employeeId);
        if (payrolls.isEmpty()) { ViewUtil.printInfo("No payroll records yet."); return; }

        System.out.println("  Your payroll records:");

        printPayrollTable(payrolls);


        int payrollId = readExistingPayrollId(payrolls, "\n  Enter Payroll ID: ");
        Payslip slip = payrollService.buildPayslip(employeeId, payrollId);
        if (slip == null) { ViewUtil.printError("Payslip not available."); return; }

        System.out.println(slip);

        String pdfPath = reportGen.generatePayslip(slip);
        if (pdfPath != null) {
            ViewUtil.printSuccess("PDF saved to: " + pdfPath);

            if (openPdf(pdfPath)) {
                ViewUtil.printInfo("Payslip PDF opened successfully.");
            } else {
                ViewUtil.printInfo("Could not auto-open PDF. Please open manually: " + pdfPath);
            }
            
            InputUtil.readMenuChoice("  Press Enter to continue...");
        } else {
            ViewUtil.printError("PDF generation failed. If you had the PDF open, close it and try again.");
            InputUtil.readMenuChoice("  Press Enter to continue...");
        }
    }

    /**
     * Opens a PDF file using the system's default PDF viewer.
     * Returns true if successful, false otherwise.
     */
    private boolean openPdf(String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                return false;
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(pdfFile);
                    return true;
                }
            }

            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {


                pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", pdfPath);
            } else if (os.contains("mac")) {


                pb = new ProcessBuilder("open", pdfPath);
            } else {


                pb = new ProcessBuilder("xdg-open", pdfPath);
            }
            
            pb.start();
            return true;
            
        } catch (Exception e) {
            System.err.println("[PayrollCtrl] Failed to open PDF: " + e.getMessage());
            return false;
        }
    }


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
