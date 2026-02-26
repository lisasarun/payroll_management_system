package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Performance {

    private int performanceId;

    private int employeeId;

    private LocalDate reviewDate;

    private BigDecimal score;

    private String comments;

    private int reviewerId;

}
