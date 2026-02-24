package project.util;

import project.modal.Attendance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
  Utility class for all payroll calculation logic.
  Inspired by Gusto/ADP: accurate overtime, bonus thresholds, deductions.

  Rules used:
    - Standard work hours/day: 8h
    - Overtime rate: 1.5x hourly rate
    - Hourly rate = base_salary / (workDaysPerMonth * 8)
    - Bonus eligibility: performance score >= target (default 75.0)
    - Bonus percentage tiers based on score range
    - Tax deduction: flat 10% of gross (simplified)
    - Social security: 2% of base salary
 */
public class SalaryCalculator {

    //  Constants

    private static final int    WORK_DAYS_PER_MONTH = 22;
    private static final int    STANDARD_HOURS_PER_DAY = 8;
    private static final double OVERTIME_RATE_MULTIPLIER = 1.5;
    private static final double BONUS_TARGET_SCORE = 75.0;
    private static final double TAX_RATE = 0.10;
    private static final double SOCIAL_SECURITY_RATE = 0.02;

    //  Hourly Rate

    /**
      Calculates hourly rate from monthly base salary.
     */
    public static BigDecimal hourlyRate(BigDecimal baseSalary) {
        int totalHours = WORK_DAYS_PER_MONTH * STANDARD_HOURS_PER_DAY;
        return baseSalary.divide(BigDecimal.valueOf(totalHours), 4, RoundingMode.HALF_UP);
    }

    //  Overtime Pay

    /**
      Total overtime pay from a list of attendance records.
      overtime_hours field is already computed during check-out.
     */
    public static BigDecimal calculateOvertimePay(BigDecimal baseSalary, List<Attendance> records) {
        BigDecimal rate = hourlyRate(baseSalary);
        BigDecimal overtimeRate = rate.multiply(BigDecimal.valueOf(OVERTIME_RATE_MULTIPLIER));

        BigDecimal totalOvertimeHours = records.stream()
                .filter(a -> a.getOvertimeHours() != null)
                .map(Attendance::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return overtimeRate.multiply(totalOvertimeHours).setScale(2, RoundingMode.HALF_UP);
    }

    //  Bonus Calculation

    /**
      Calculates bonus based on performance score tiers.

      Score tiers:
        >= 90  → 15% of base salary
        >= 80  → 10% of base salary
        >= 75  → 5%  of base salary
        < 75   → No bonus
     */
    public static BigDecimal calculateBonus(BigDecimal baseSalary, double performanceScore) {
        double pct = 0.0;
        if (performanceScore >= 90.0)      pct = 0.15;
        else if (performanceScore >= 80.0) pct = 0.10;
        else if (performanceScore >= BONUS_TARGET_SCORE) pct = 0.05;

        if (pct == 0.0) return BigDecimal.ZERO;
        return baseSalary.multiply(BigDecimal.valueOf(pct)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
      Returns true if the employee qualifies for a bonus.
     */
    public static boolean isBonusEligible(double performanceScore) {
        return performanceScore >= BONUS_TARGET_SCORE;
    }

    //  Deductions

    /**
      Tax deduction = 10% of gross (baseSalary + overtime + bonus).
     */
    public static BigDecimal calculateTax(BigDecimal grossSalary) {
        return grossSalary.multiply(BigDecimal.valueOf(TAX_RATE)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
      Social security deduction = 2% of base salary.
     */
    public static BigDecimal calculateSocialSecurity(BigDecimal baseSalary) {
        return baseSalary.multiply(BigDecimal.valueOf(SOCIAL_SECURITY_RATE)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
      Total deductions = tax + social security.
     */
    public static BigDecimal totalDeductions(BigDecimal baseSalary, BigDecimal grossSalary) {
        return calculateTax(grossSalary).add(calculateSocialSecurity(baseSalary))
                .setScale(2, RoundingMode.HALF_UP);
    }

    //  Total Pay

    /**
      Net pay = base + overtime + bonus - deductions.

      @param baseSalary     from employees.base_salary
      @param overtimePay    from calculateOvertimePay()
      @param bonus          from calculateBonus()
      @param deductions     from totalDeductions()
     */
    public static BigDecimal calculateTotalPay(BigDecimal baseSalary,
                                               BigDecimal overtimePay,
                                               BigDecimal bonus,
                                               BigDecimal deductions) {
        return baseSalary.add(overtimePay).add(bonus).subtract(deductions)
                .setScale(2, RoundingMode.HALF_UP);
    }

}