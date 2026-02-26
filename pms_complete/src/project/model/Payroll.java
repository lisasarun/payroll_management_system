package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Payroll {

    private int payrollId;

    private int employeeId;

    private LocalDate payPeriodStart;

    private LocalDate payPeriodEnd;

    private BigDecimal baseSalary;

    private BigDecimal bonus;

    private BigDecimal deductions;

    private BigDecimal totalPaid;

    private LocalDate paymentDate;

}
