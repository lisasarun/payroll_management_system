package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Performance entity representing employee performance reviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performance {

    private int performanceId;

    private int employeeId;

    private LocalDate reviewDate;

    private BigDecimal score; // 0.01 to 100.00

    private String comments;

    private int reviewerId;

    private LocalDateTime createdAt;

}
