package project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
  DTO for payroll data used in calculation, display, and report generation.
  Mirrors payroll table + adds computed fields for payslip.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDTO {
    private int payrollId;
    private int employeeId;
    private String employeeName;   // joined from employees for display

    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;

    private BigDecimal baseSalary;
    private BigDecimal overtimePay;  // calculated, not directly in ERD payroll table
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal totalPaid;

    private LocalDate paymentDate;

}