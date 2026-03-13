package project.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LeaveRequest entity representing employee leave/time-off requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    private int leaveRequestId;

    private int employeeId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String leaveType;

    private String reason;

    private String status;

    private Integer reviewerId;

    private String reviewNote;

    private LocalDate requestDate;

    private LocalDate reviewDate;

    private LocalDateTime createdAt;

}
