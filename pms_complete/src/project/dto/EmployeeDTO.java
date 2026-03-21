package project.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private int employeeId;

    private String fullName;

    private String email;

    private boolean isActive;

    private BigDecimal baseSalary;

    private String position;

    private String department;

    private LocalDate hireDate;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

}
