package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Centralized configuration for Oracle Database access via JDBC.
 * Configured for Oracle XE 21c (Service Name: XEPDB1).
 */
public class DBConnection {

    // Configurable Database Connection Parameters
    private static String dbUrl = "jdbc:oracle:thin:@localhost:1521/FREEPDB1";
    private static String dbUser = "system";
    private static String dbPassword = "Oceans@8888";

    static {
        try {
            // Explicitly load Oracle JDBC Driver
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found! Ensure ojdbc11.jar is in classpath.");
        }
    }

    /**
     * Establishes and returns a new JDBC connection to the Oracle Database.
     * @return Connection object
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Test connection health.
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Getters and Setters for configuration
    public static String getDbUrl() { return dbUrl; }
    public static void setDbUrl(String url) { dbUrl = url; }

    public static String getDbUser() { return dbUser; }
    public static void setDbUser(String user) { dbUser = user; }

    public static String getDbPassword() { return dbPassword; }
    public static void setDbPassword(String password) { dbPassword = password; }
}
