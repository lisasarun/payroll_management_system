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

import java.util.List;

public class PayrollController {

    private final PayrollService       payrollService = new PayrollService();
    private final EmployeeService      empService     = new EmployeeService();
    private final JasperReportGenerator reportGen     = new JasperReportGenerator();

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

    // Admin: generate and print payslip + save PDF
    public void generatePayslip() {
        ViewUtil.printTitle("GENERATE PAYSLIP");

        // Show paginated employee list so admin can pick an ID
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
        EmployeeDTO emp = empService.getById(empId);
        if (emp == null) { ViewUtil.printError("Employee not found."); return; }

        List<PayrollDTO> payrolls = payrollService.getByEmployee(empId);
        if (payrolls.isEmpty()) { ViewUtil.printInfo("No payroll records found."); return; }

        System.out.println("\n  Payroll records for " + emp.getFullName() + ":");
        payrolls.forEach(p -> System.out.printf("  [%d] %s to %s  —  $%,.2f%n",
                p.getPayrollId(), p.getPayPeriodStart(), p.getPayPeriodEnd(), p.getTotalPaid()));

        int payrollId = InputUtil.readInt("\n  Enter Payroll ID: ");
        Payslip slip = payrollService.buildPayslip(empId, payrollId);
        if (slip == null) { ViewUtil.printError("Could not build payslip."); return; }

        System.out.println(slip);

        String pdfPath = reportGen.generatePayslip(slip);
        if (pdfPath != null)
            ViewUtil.printSuccess("PDF saved to: " + pdfPath);
    }

    // Employee: view own payslip + save PDF
    public void viewMyPayslip(int employeeId) {
        ViewUtil.printTitle("MY PAYSLIP");
        List<PayrollDTO> payrolls = payrollService.getByEmployee(employeeId);
        if (payrolls.isEmpty()) { ViewUtil.printInfo("No payroll records yet."); return; }

        System.out.println("  Your payroll records:");
        payrolls.forEach(p -> System.out.printf("  [%d] %s to %s  —  Total: $%,.2f%n",
                p.getPayrollId(), p.getPayPeriodStart(), p.getPayPeriodEnd(), p.getTotalPaid()));

        int payrollId = InputUtil.readInt("\n  Enter Payroll ID: ");
        Payslip slip = payrollService.buildPayslip(employeeId, payrollId);
        if (slip == null) { ViewUtil.printError("Payslip not available."); return; }

        // Print to console
        System.out.println(slip);

        // Save as PDF
        String pdfPath = reportGen.generatePayslip(slip);
        if (pdfPath != null)
            ViewUtil.printSuccess("PDF saved to: " + pdfPath);
    }
}