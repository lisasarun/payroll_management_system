package project.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbConfig {

    private static final String URL     = getEnvOrDefault("DB_URL",  "jdbc:postgresql://202.178.125.77:3297/postgres");
    private static final String DB_USER = getEnvOrDefault("DB_USER", "postgres");
    private static final String DB_PASS = getEnvOrDefault("DB_PASS", "pmqwerqwer");

    private static Connection conn;
    private static boolean firstConnection = true;  // Track first connection


    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        // Also check system properties (for -D flags)
        value = System.getProperty(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static void init() {
        if (conn == null) openConnection();
    }

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(2)) openConnection();
        } catch (SQLException e) {
            openConnection();
        }
        return conn;
    }

    private static void openConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
            conn.setAutoCommit(true);
            
            // Only show messages on first connection
            if (firstConnection) {
                if ("dev_password".equals(DB_PASS)) {
                    System.err.println("[DB] WARNING: Using development placeholder password 'dev_password'.");
                    System.err.println("[DB]          Set DB_PASS environment variable for your real PostgreSQL password.");
                }
                System.out.println("[DB] Connected to PostgreSQL successfully (" + URL + ").");
                firstConnection = false;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver not found — add postgresql JAR to libraries.");
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            System.err.println("[DB] Check: DB_PASS environment variable is correct.");
        }
    }

    public static void close() {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
            conn = null;
        }
    }

    public static String getUrl()    { return URL; }

    public static String getDbUser() { return DB_USER; }

    public static String getDbPass() { return DB_PASS; }
}
