package project.service;

import project.dto.PayrollDTO;
import project.mapper.EntityMapper;
import project.model.*;
import project.procedure.PayrollProcedure;
import project.repository.*;
import project.util.DateUtil;
import project.util.SalaryCalculator;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


public class PayrollService {

    private final PayrollRepository     payrollRepo = new PayrollRepository();
    private final EmployeeRepository    empRepo     = new EmployeeRepository();
    private final PerformanceRepository perfRepo    = new PerformanceRepository();
    private final AttendanceRepository  attRepo     = new AttendanceRepository();
    private final BonusRepository       bonusRepo   = new BonusRepository();


    public boolean calculatePayroll(int employeeId, int month, int year) {
        Employee emp = empRepo.findById(employeeId);
        if (emp == null) { System.out.println("  Employee not found."); return false; }

        LocalDate from = DateUtil.firstDayOfMonth(month, year);
        LocalDate to   = DateUtil.lastDayOfMonth(month, year);


        for (Payroll p : payrollRepo.findByEmployee(employeeId)) {
            if (!(p.getPayPeriodEnd().isBefore(from) || p.getPayPeriodStart().isAfter(to))) {
                System.out.println("  Payroll period overlaps with existing period: " +
                        p.getPayPeriodStart() + " to " + p.getPayPeriodEnd());
                return false;
            }
        }


        List<Attendance> attList = attRepo.findByEmployeeAndPeriod(employeeId, from, to);
        BigDecimal base = emp.getBaseSalary() != null ? emp.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal ot   = SalaryCalculator.calculateOvertimePay(base, attList);


        double avgScore  = perfRepo.getAverageScore(employeeId);
        BigDecimal bonus = SalaryCalculator.calculateBonus(base, avgScore);


        BigDecimal gross      = base.add(ot).add(bonus);
        BigDecimal deductions = SalaryCalculator.totalDeductions(base, gross);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    project.config.DbConfig.getUrl(),
                    project.config.DbConfig.getDbUser(),
                    project.config.DbConfig.getDbPass());
            conn.setAutoCommit(false);


            boolean saved = PayrollProcedure.calculateWithConnection(
                    conn, employeeId, from, to, bonus, deductions
            );

            if (!saved) {
                conn.rollback();
                System.out.println("  Stored procedure failed — check PostgreSQL logs.");
                return false;
            }


            if (bonus.compareTo(BigDecimal.ZERO) > 0) {
                Payroll latest = payrollRepo.findLatestWithConnection(conn, employeeId);
                if (latest != null) {
                    Bonus b = new Bonus();
                    b.setEmployeeId(employeeId);
                    b.setPayrollId(latest.getPayrollId());
                    b.setAmount(bonus);
                    b.setReason("Performance bonus — avg score: " + String.format("%.2f", avgScore));
                    b.setAwardedDate(LocalDate.now());

                    if (!bonusRepo.saveWithConnection(conn, b)) {
                        conn.rollback();
                        System.out.println("  Bonus save failed — transaction rolled back.");
                        return false;
                    }
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("  Transaction rolled back: " + e.getMessage());
                } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public Payslip buildPayslip(int employeeId, int payrollId) {
        Employee emp = empRepo.findById(employeeId);
        Payroll  pay = payrollRepo.findById(payrollId);
        if (emp == null || pay == null) return null;
        if (pay.getEmployeeId() != employeeId) return null;

        List<Attendance> attList = attRepo.findByEmployeeAndPeriod(
                employeeId, pay.getPayPeriodStart(), pay.getPayPeriodEnd());

        BigDecimal base  = emp.getBaseSalary() != null ? emp.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal ot    = SalaryCalculator.calculateOvertimePay(base, attList);
        BigDecimal bonus = pay.getBonus() != null ? pay.getBonus() : BigDecimal.ZERO;
        BigDecimal tax   = SalaryCalculator.calculateTax(base.add(ot).add(bonus));
        BigDecimal ss    = SalaryCalculator.calculateSocialSecurity(base);

        Payslip ps = new Payslip();
        ps.setEmployeeId(employeeId);
        ps.setEmployeeName(emp.getFullName());
        ps.setEmail(emp.getEmail());
        ps.setPayPeriodStart(pay.getPayPeriodStart());
        ps.setPayPeriodEnd(pay.getPayPeriodEnd());


        LocalDate minAllowed = LocalDate.of(2026, 1, 1);
        LocalDate maxAllowed = LocalDate.of(2026, 12, 31);
        LocalDate paymentDate = pay.getPaymentDate() != null ? pay.getPaymentDate() : LocalDate.now();
        if (paymentDate.isBefore(minAllowed)) {
            paymentDate = minAllowed;
        } else if (paymentDate.isAfter(maxAllowed)) {
            paymentDate = maxAllowed;
        }
        ps.setPaymentDate(paymentDate);
        ps.setBaseSalary(base);
        ps.setOvertimePay(ot);
        ps.setBonus(bonus);
        ps.setTax(tax);
        ps.setSocialSecurity(ss);
        ps.setTotalDeductions(tax.add(ss));
        ps.setTotalPaid(pay.getTotalPaid());
        return ps;
    }

    public List<PayrollDTO> getByEmployee(int employeeId) {
        Employee emp = empRepo.findById(employeeId);
        return payrollRepo.findByEmployee(employeeId).stream()
                .map(p -> {
                    PayrollDTO dto = EntityMapper.toPayrollDTO(p);
                    if (emp != null) dto.setEmployeeName(emp.getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public PayrollDTO getLatest(int employeeId) {
        Payroll p = payrollRepo.findLatest(employeeId);
        if (p == null) return null;
        PayrollDTO dto = EntityMapper.toPayrollDTO(p);
        Employee emp = empRepo.findById(employeeId);
        if (emp != null) dto.setEmployeeName(emp.getFullName());
        return dto;
    }
}