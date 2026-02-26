package project.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SalaryReportDTO {

    private int payrollId;

    private int employeeId;

    private String employeeName;

    private LocalDate payPeriodStart;

    private LocalDate payPeriodEnd;

    private BigDecimal baseSalary;

    private BigDecimal overtimePay;

    private BigDecimal bonus;

    private BigDecimal deductions;

    private BigDecimal totalPaid;

    private LocalDate paymentDate;
}
