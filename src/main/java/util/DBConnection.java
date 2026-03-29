package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Provides a shared MySQL JDBC connection.
 * Uses singleton-style connection pooling via a single static connection.
 * For production, replace with a proper connection pool (e.g., HikariCP or DBCP).
 */
public class DBConnection {

    // ── Database configuration ────────────────────────────────────────────────
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/live_auction_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "root123";
    private static final String DRIVER      = "com.mysql.cj.jdbc.Driver";

    /** Obtain a fresh JDBC connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /** Silently close a connection (null-safe). */
    public static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
