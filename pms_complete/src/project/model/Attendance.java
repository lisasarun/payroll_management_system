package project.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance entity representing employee check-in/out records
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    private int attendanceId;

    private int employeeId;

    private LocalDate date;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private String status;

    private BigDecimal workHours;

    private BigDecimal overtimeHours;

    private int lateMinutes;

    private int earlyLeaveMinutes;

    private String leaveType;

    private String note;

    private LocalDateTime createdAt;

}
