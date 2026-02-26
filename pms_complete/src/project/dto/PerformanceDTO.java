package project.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PerformanceDTO {

    private int performanceId;

    private int employeeId;

    private String employeeName;

    private LocalDate reviewDate;

    private BigDecimal score;

    private String comments;

    private int reviewerId;

    private String reviewerName;
}
