package project.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserDTO {

    private int adminId;

    private String username;

    private String permissionLevel;

}
