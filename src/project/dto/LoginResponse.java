package project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  DTO returned after successful authentication.
  Contains session identity — no raw password exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private int userId;
    private String username;
    private String role;           // "ADMIN" or "EMPLOYEE"
    private String permissionLevel; // e.g. "SUPER_ADMIN", "VIEWER" (for admins)
    private boolean authenticated;
    private String message;

}