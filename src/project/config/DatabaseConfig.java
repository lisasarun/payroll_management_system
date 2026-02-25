package project.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

        // Create singleton object
        private static Connection conn;

        // Invoke singleton object
        public static Connection getInstance() {
            return conn;
        }

        // Initialize singleton object (only 1 time)
        public static void init() {
            if (conn == null) {
                try {
                    // Step 1: Load driver
                    Class.forName("org.postgresql.Driver");

                    // Step 2: Establish connection
                    String url = "jdbc:postgresql://localhost:5432/postgres";
                    String user = "postgres";
                    String password = "qwer";

                    conn = DriverManager.getConnection(url, user, password);
                    conn.setAutoCommit(true);
                } catch (ClassNotFoundException e) {
                    System.out.println("class not found: " + e.getMessage());
                } catch (SQLException e) {
                    System.out.println("SQL error: " + e.getMessage());
                }
            } else {
                System.out.println("Connection already initialized");
            }
        }
}
