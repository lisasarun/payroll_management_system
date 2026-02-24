package project.modal;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    private String username;
    private String passwordHash;
    private Integer employeeId;
    private Employee employee;
    private String role;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}

