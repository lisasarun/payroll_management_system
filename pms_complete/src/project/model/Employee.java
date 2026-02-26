package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

}
