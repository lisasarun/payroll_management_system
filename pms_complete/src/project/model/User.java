package project.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class User {

    private int adminId;

    private String username;

    private String password;

    private String permissionLevel;

}
