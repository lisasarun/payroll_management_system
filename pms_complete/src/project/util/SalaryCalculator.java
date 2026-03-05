package project.util;

import project.model.Attendance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SalaryCalculator {

    private static final int    WORK_DAYS      = 22;
    private static final int    HOURS_PER_DAY  = 8;
    private static final double OT_MULTIPLIER  = 1.5;
    private static final double BONUS_THRESHOLD = 75.0;
    private static final double TAX_RATE        = 0.10;
    private static final double SOC_SEC_RATE    = 0.02;

    // FIX Issue #7: Validate constants at class load time
    static {
        if (WORK_DAYS <= 0 || HOURS_PER_DAY <= 0) {
            throw new IllegalStateException("Invalid salary calculation constants: WORK_DAYS and HOURS_PER_DAY must be positive");
        }
    }

    public static BigDecimal hourlyRate(BigDecimal baseSalary) {
        return baseSalary.divide(BigDecimal.valueOf((long) WORK_DAYS * HOURS_PER_DAY), 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateOvertimePay(BigDecimal baseSalary, List<Attendance> records) {
        BigDecimal rate = hourlyRate(baseSalary).multiply(BigDecimal.valueOf(OT_MULTIPLIER));
        BigDecimal totalOT = records.stream()
                .filter(a -> a.getOvertimeHours() != null)
                .map(Attendance::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return rate.multiply(totalOT).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateBonus(BigDecimal baseSalary, double score) {
        double pct = 0.0;
        if      (score >= 90.0)            pct = 0.15;
        else if (score >= 80.0)            pct = 0.10;
        else if (score >= BONUS_THRESHOLD) pct = 0.05;
        if (pct == 0.0) return BigDecimal.ZERO;
        return baseSalary.multiply(BigDecimal.valueOf(pct)).setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isBonusEligible(double score) {
        return score >= BONUS_THRESHOLD;
    }

    public static BigDecimal calculateTax(BigDecimal gross) {
        return gross.multiply(BigDecimal.valueOf(TAX_RATE)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateSocialSecurity(BigDecimal base) {
        return base.multiply(BigDecimal.valueOf(SOC_SEC_RATE)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal totalDeductions(BigDecimal base, BigDecimal gross) {
        return calculateTax(gross).add(calculateSocialSecurity(base)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotalPay(BigDecimal base, BigDecimal ot, BigDecimal bonus, BigDecimal deductions) {
        return base.add(ot).add(bonus).subtract(deductions).setScale(2, RoundingMode.HALF_UP);
    }
}
