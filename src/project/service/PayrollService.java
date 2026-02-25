package project.service;

import project.dao.EmployeeDao;
import project.dao.EmlpoyeeDaoImpl;
import project.dto.PayrollDTO;
import project.mapper.EntityMapper;
import project.model.Employee;
import project.model.Payroll;
import project.model.Payslip;
import project.util.SalaryCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PayrollService {
    private final EmployeeDao employeeDao;
    private final SalaryCalculator salaryCalculator;

    public PayrollService() {
        this.employeeDao = new EmlpoyeeDaoImpl();
        this.salaryCalculator = new SalaryCalculator();
    }

    public boolean generatePayroll(int employeeId, int month, int year) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            if (employee == null) return false;

            // Calculate attendance percentage
            AttendanceService attendanceService = new AttendanceService();
            double attendancePercentage = attendanceService.getAttendancePercentage(employeeId, month, year);

            // Calculate salary
            double basicSalary = employee.getSalary();
            double allowances = salaryCalculator.calculateAllowances(basicSalary);
            double deductions = salaryCalculator.calculateDeductions(basicSalary, attendancePercentage);
            double netSalary = salaryCalculator.calculateNetSalary(basicSalary, allowances, deductions);

            // Create payroll
            Payroll payroll = new Payroll();
            payroll.setEmployeeId(employeeId);
            payroll.setMonth(month);
            payroll.setYear(year);
            payroll.setBasicSalary(basicSalary);
            payroll.setAllowances(allowances);
            payroll.setDeductions(deductions);
            payroll.setNetSalary(netSalary);
            payroll.setStatus("PENDING");

            employee.addPayroll(payroll);
            return employeeDao.update(employee);

        } catch (Exception e) {
            System.err.println("Error generating payroll: " + e.getMessage());
            return false;
        }
    }

    public Payslip generatePayslip(int employeeId, int payrollId) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            if (employee == null) return null;

            Payroll payroll = employee.getPayrolls().stream()
                    .filter(p -> p.getPayrollId() == payrollId)
                    .findFirst()
                    .orElse(null);

            if (payroll == null) return null;

            return salaryCalculator.generatePayslip(employee, payroll);

        } catch (Exception e) {
            System.err.println("Error generating payslip: " + e.getMessage());
            return null;
        }
    }

    public List<PayrollDTO> getEmployeePayrolls(int employeeId) {
        try {
            Employee employee = employeeDao.findById(employeeId);
            if (employee != null) {
                return employee.getPayrolls().stream()
                        .map(EntityMapper::toPayrollDTO)
                        .collect(Collectors.toList());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching payrolls: " + e.getMessage());
            return null;
        }
    }

    public boolean processPayment(int payrollId) {
        try {
            // Find employee containing this payroll
            List<Employee> employees = employeeDao.findAll(null);
            for (Employee employee : employees) {
                for (Payroll payroll : employee.getPayrolls()) {
                    if (payroll.getPayrollId() == payrollId) {
                        payroll.setStatus("PAID");
                        payroll.setPaymentDate(LocalDate.now());
                        return employeeDao.update(employee);
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }
}

