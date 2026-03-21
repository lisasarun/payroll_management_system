package project.util;

import project.model.Attendance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalaryCalculator {

    private static final int    WORK_DAYS      = 22;
    private static final int    HOURS_PER_DAY  = 8;
    private static final double OT_MULTIPLIER  = 1.5;
    private static final double BONUS_THRESHOLD = 75.0;
    private static final double TAX_RATE        = 0.10;
    private static final double SOC_SEC_RATE    = 0.02;


    private static final Map<String, int[]> SALARY_RULES = new HashMap<>();

    static {
        if (WORK_DAYS <= 0 || HOURS_PER_DAY <= 0) {
            throw new IllegalStateException("Invalid salary calculation constants: WORK_DAYS and HOURS_PER_DAY must be positive");
        }


        SALARY_RULES.put("Junior Developer", new int[]{500, 1500});
        SALARY_RULES.put("Senior Developer", new int[]{2000, 5000});
        SALARY_RULES.put("Software Engineer", new int[]{1000, 3000});
        SALARY_RULES.put("HR Coordinator", new int[]{800, 2000});
        SALARY_RULES.put("Marketing Specialist", new int[]{700, 1800});
        SALARY_RULES.put("Financial Analyst", new int[]{1200, 3000});
        SALARY_RULES.put("Manager", new int[]{3000, 8000});
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


    public static boolean isValidSalaryForPosition(String position, BigDecimal salary) {
        if (position == null || salary == null) return false;
        
        int[] range = SALARY_RULES.get(position);
        if (range == null) return true; // No rules for this position, allow any salary
        
        BigDecimal minSalary = BigDecimal.valueOf(range[0]);
        BigDecimal maxSalary = BigDecimal.valueOf(range[1]);
        
        return salary.compareTo(minSalary) >= 0 && salary.compareTo(maxSalary) <= 0;
    }


    public static String getSalaryRange(String position) {
        if (position == null) return null;
        
        int[] range = SALARY_RULES.get(position);
        if (range == null) return null;
        
        return String.format("$%,d – $%,d", range[0], range[1]);
    }


    public static Map<String, int[]> getAllPositionRules() {
        return new HashMap<>(SALARY_RULES);
    }
}
