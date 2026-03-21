package project.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AttendanceDTO {

    private int attendanceId;

    private int employeeId;

    private String employeeName;

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
}
