package project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusDTO {

    private int bonusId;

    private int employeeId;

    private String employeeName;

    private BigDecimal amount;

    private String reason;

    private LocalDate awardedDate;
}