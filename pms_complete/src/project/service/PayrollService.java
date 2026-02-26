package project.service;

import project.dto.PayrollDTO;
import project.mapper.EntityMapper;
import project.model.*;
import project.repository.*;
import project.util.DateUtil;
import project.util.SalaryCalculator;

import java.math.BigDecimal;
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

        // Check for duplicate
        List<Payroll> existing = payrollRepo.findByEmployee(employeeId);
        for (Payroll p : existing) {
            if (!p.getPayPeriodStart().isBefore(from) && !p.getPayPeriodStart().isAfter(to)) {
                System.out.println("  Payroll already calculated for this period.");
                return false;
            }
        }

        List<Attendance> attList = attRepo.findByEmployeeAndPeriod(employeeId, from, to);
        BigDecimal base  = emp.getBaseSalary() != null ? emp.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal ot    = SalaryCalculator.calculateOvertimePay(base, attList);

        // Bonus from latest performance score
        double avgScore = perfRepo.getAverageScore(employeeId);
        BigDecimal bonus = SalaryCalculator.calculateBonus(base, avgScore);

        BigDecimal gross = base.add(ot).add(bonus);
        BigDecimal deductions = SalaryCalculator.totalDeductions(base, gross);
        BigDecimal totalPaid  = SalaryCalculator.calculateTotalPay(base, ot, bonus, deductions);

        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employeeId);
        payroll.setPayPeriodStart(from);
        payroll.setPayPeriodEnd(to);
        payroll.setBaseSalary(base);
        payroll.setBonus(bonus);
        payroll.setDeductions(deductions);
        payroll.setTotalPaid(totalPaid);
        payroll.setPaymentDate(LocalDate.now());

        boolean saved = payrollRepo.save(payroll);

        // Save bonus record separately if bonus > 0
        if (saved && bonus.compareTo(BigDecimal.ZERO) > 0) {
            Payroll latest = payrollRepo.findLatest(employeeId);
            if (latest != null) {
                Bonus b = new Bonus();
                b.setEmployeeId(employeeId);
                b.setPayrollId(latest.getPayrollId());
                b.setAmount(bonus);
                b.setReason("Performance bonus (score: " + String.format("%.2f", avgScore) + ")");
                b.setAwardedDate(LocalDate.now());
                bonusRepo.save(b);
            }
        }
        return saved;
    }

    public Payslip buildPayslip(int employeeId, int payrollId) {
        Employee emp = empRepo.findById(employeeId);
        Payroll  pay = payrollRepo.findById(payrollId);
        if (emp == null || pay == null) return null;
        if (pay.getEmployeeId() != employeeId) return null;

        List<Attendance> attList = attRepo.findByEmployeeAndPeriod(
                employeeId, pay.getPayPeriodStart(), pay.getPayPeriodEnd());

        BigDecimal base = emp.getBaseSalary() != null ? emp.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal ot   = SalaryCalculator.calculateOvertimePay(base, attList);
        BigDecimal tax  = SalaryCalculator.calculateTax(base.add(ot).add(
                pay.getBonus() != null ? pay.getBonus() : BigDecimal.ZERO));
        BigDecimal ss   = SalaryCalculator.calculateSocialSecurity(base);

        Payslip ps = new Payslip();
        ps.setEmployeeId(employeeId);
        ps.setEmployeeName(emp.getFullName());
        ps.setEmail(emp.getEmail());
        ps.setPayPeriodStart(pay.getPayPeriodStart());
        ps.setPayPeriodEnd(pay.getPayPeriodEnd());
        ps.setPaymentDate(pay.getPaymentDate() != null ? pay.getPaymentDate() : LocalDate.now());
        ps.setBaseSalary(base);
        ps.setOvertimePay(ot);
        ps.setBonus(pay.getBonus() != null ? pay.getBonus() : BigDecimal.ZERO);
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
