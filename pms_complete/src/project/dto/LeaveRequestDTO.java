package project.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {

    private int leaveRequestId;

    private int employeeId;

    private String employeeName;

    private LocalDate startDate;

    private LocalDate endDate;

    private int daysRequested;

    private String leaveType;

    private String reason;

    private String status;

    private String reviewerName;

    private String reviewNote;

    private LocalDate requestDate;

    private LocalDate reviewDate;
}
