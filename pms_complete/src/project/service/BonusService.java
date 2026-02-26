package project.service;

import project.model.Bonus;
import project.repository.BonusRepository;
import project.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BonusService {

    private final BonusRepository  bonusRepo = new BonusRepository();
    private final EmployeeRepository empRepo = new EmployeeRepository();

    public boolean addBonus(int employeeId, BigDecimal amount, String reason) {
        if (empRepo.findById(employeeId) == null) {
            System.out.println("  Employee not found."); return false;
        }
        Bonus b = new Bonus();
        b.setEmployeeId(employeeId);
        b.setAmount(amount);
        b.setReason(reason);
        b.setAwardedDate(LocalDate.now());
        return bonusRepo.save(b);
    }

    public List<Bonus> getByEmployee(int employeeId) {
        return bonusRepo.findByEmployee(employeeId);
    }

    public BigDecimal getTotalByPayroll(int payrollId) {
        return bonusRepo.getTotalByPayroll(payrollId);
    }
}
