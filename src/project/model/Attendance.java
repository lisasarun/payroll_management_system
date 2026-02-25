package project.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    private Integer attendanceId;
    private Integer employeeId;
    private Employee employee;
    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private BigDecimal hoursWorked;
    private BigDecimal overtimeHours;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
