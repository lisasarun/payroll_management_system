package project.model;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Admin/User entity representing admin users in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private int adminId;
    private String username;
    private String password;
    private String permissionLevel;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean isActive;

}
