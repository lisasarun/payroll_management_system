package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Payslip {

    private int employeeId;

    private String employeeName;

    private String email;

    private LocalDate payPeriodStart;

    private LocalDate payPeriodEnd;

    private LocalDate paymentDate;

    private BigDecimal baseSalary;

    private BigDecimal overtimePay;

    private BigDecimal bonus;

    private BigDecimal tax;

    private BigDecimal socialSecurity;

    private BigDecimal totalDeductions;

    private BigDecimal totalPaid;

    @Override
    public String toString() {
        String line = "─".repeat(50);
        return "\n" + line + "\n" +
                "              PAYSLIP\n" +
                line + "\n" +
                String.format("  Employee  : %s (ID: %d)%n", employeeName, employeeId) +
                String.format("  Email     : %s%n", email) +
                String.format("  Period    : %s to %s%n", payPeriodStart, payPeriodEnd) +
                String.format("  Pay Date  : %s%n", paymentDate) +
                line + "\n" +
                String.format("  Base Salary    : $%,.2f%n", baseSalary) +
                String.format("  Overtime Pay   : $%,.2f%n", overtimePay != null ? overtimePay : BigDecimal.ZERO) +
                String.format("  Bonus          : $%,.2f%n", bonus != null ? bonus : BigDecimal.ZERO) +
                line + "\n" +
                String.format("  Tax (10%%)      : -$%,.2f%n", tax != null ? tax : BigDecimal.ZERO) +
                String.format("  Social Sec(2%%): -$%,.2f%n", socialSecurity != null ? socialSecurity : BigDecimal.ZERO) +
                line + "\n" +
                String.format("  NET PAY        : $%,.2f%n", totalPaid) +
                line + "\n";
    }
}
