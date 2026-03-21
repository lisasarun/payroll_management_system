package project.service;

import project.model.Bonus;
import project.repository.BonusRepository;
import project.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BonusService {

    private static final int MAX_MANUAL_BONUSES_PER_YEAR = 1;
    private static final BigDecimal MAX_MANUAL_BONUS_TOTAL_PER_YEAR = BigDecimal.valueOf(6000);

    private final BonusRepository  bonusRepo = new BonusRepository();
    private final EmployeeRepository empRepo = new EmployeeRepository();

    public boolean addBonus(int employeeId, BigDecimal amount, String reason) {
        if (empRepo.findById(employeeId) == null) {
            System.out.println("  Employee not found."); return false;
        }
        if (amount == null || amount.compareTo(BigDecimal.valueOf(10)) < 0 || amount.compareTo(BigDecimal.valueOf(5000)) > 0) {
            System.out.println("  Bonus amount must be between 10 and 5000.");
            return false;
        }
        if (reason == null || reason.trim().length() < 10 || reason.trim().length() > 120) {
            System.out.println("  Reason must be 10-120 characters.");
            return false;
        }
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int countYear = bonusRepo.countByEmployeeAndYear(employeeId, currentYear);
        if (countYear >= MAX_MANUAL_BONUSES_PER_YEAR) {
            System.out.println("  Yearly bonus add limit reached (max 1 manual bonus per employee per year).");
            return false;
        }
        BigDecimal totalYear = bonusRepo.sumByEmployeeAndYear(employeeId, currentYear);
        if (totalYear.add(amount).compareTo(MAX_MANUAL_BONUS_TOTAL_PER_YEAR) > 0) {
            System.out.println("  Yearly bonus total limit exceeded (max 6000 per employee per year).");
            return false;
        }
        Bonus b = new Bonus();
        b.setEmployeeId(employeeId);
        b.setAmount(amount);
        b.setReason(reason.trim());
        b.setAwardedDate(today);
        return bonusRepo.save(b);
    }

    public List<Bonus> getByEmployee(int employeeId) {
        return bonusRepo.findByEmployee(employeeId);
    }

    public BigDecimal getTotalByPayroll(int payrollId) {
        return bonusRepo.getTotalByPayroll(payrollId);
    }
}
