package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Bonus {
    private int bonusId;

    private int employeeId;

    private int payrollId;

    private BigDecimal amount;

    private String reason;

    private LocalDate awardedDate;

}
