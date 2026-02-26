package project.config;

import java.sql.Connection;

public class DbConfig {

    public static Connection getConnection() {
        return DatabaseConfig.getConnection();
    }
}
