package project.modal;

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
public class Bonus {
    private Integer bonusId;
    private Integer employeeId;
    private Employee employee;
    private String bonusType;
    private BigDecimal amount;
    private LocalDate bonusDate;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}

