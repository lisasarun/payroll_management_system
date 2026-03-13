package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing an employee in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private int employeeId;

    private String fullName;

    private String email;

    private String password;

    private boolean isActive;

    private BigDecimal baseSalary;

    private String position;

    private String department;

    private LocalDate hireDate;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
