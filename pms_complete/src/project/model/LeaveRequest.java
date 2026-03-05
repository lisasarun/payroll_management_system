package project.model;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    private int leaveRequestId;
    private int employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;       // SICK, VACATION, PERSONAL, EMERGENCY
    private String reason;
    private String status;          // PENDING, APPROVED, REJECTED
    private Integer reviewerId;     // admin who approved/rejected
    private String reviewNote;
    private LocalDate requestDate;
    private LocalDate reviewDate;
}
