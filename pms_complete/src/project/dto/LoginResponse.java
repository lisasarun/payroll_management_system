package project.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LoginResponse {

    private int userId;

    private String username;

    private String role;

    private String permissionLevel;

    private boolean authenticated;

    private String message;
}
