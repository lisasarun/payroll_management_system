package project.modal;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performance {
    private Integer reviewId;
    private Integer employeeId;
    private Employee employee;
    private LocalDate reviewDate;
    private String reviewerName;
    private Integer rating;
    private String comments;
    private String goals;
    private LocalDate nextReviewDate;
    private LocalDateTime createdAt;
}

