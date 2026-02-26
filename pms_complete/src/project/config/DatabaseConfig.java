package project.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String URL     = "jdbc:postgresql://localhost:5432/payroll_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "maneth23/4/2006"; // ← change to your password

    private static Connection conn;

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
            System.out.println("[DB] Connected to PostgreSQL successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver not found — add postgresql JAR to libraries.");
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
        }
    }

    public static void close() {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
            conn = null;
        }
    }
}
