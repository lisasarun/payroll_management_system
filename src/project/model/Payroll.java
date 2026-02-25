package project.model;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {
    private Integer payrollId;
    private Integer employeeId;
    private Employee employee;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private BigDecimal regularPay;
    private BigDecimal overtimePay;
    private BigDecimal bonusAmount;
    private BigDecimal grossPay;
    private BigDecimal deductions;
    private BigDecimal netPay;
    private LocalDate paymentDate;
    private String status;
    private LocalDateTime createdAt;
}
