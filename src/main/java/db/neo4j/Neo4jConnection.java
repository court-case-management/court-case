package db.neo4j;

import org.neo4j.driver.*;

/**
 * Neo4jConnection - Centralized configuration and driver manager for Neo4j 5.x Graph Database.
 * Connects via the official Neo4j Java Driver using the binary Bolt protocol.
 * Supports local Neo4j Community/Enterprise, Docker containers, and Neo4j AuraDB Free cloud instances.
 */
public class Neo4jConnection {

    // Configurable connection parameters
    private static String uri = "neo4j://127.0.0.1:7687";
    private static String user = "neo4j";
    private static String password = "Oceans@8888";

    private static Driver driver;

    /**
     * Retrieves or lazily initializes the singleton Neo4j Driver instance.
     * @return Driver instance
     */
    public static synchronized Driver getDriver() {
        if (driver == null || isClosed()) {
            Config config = Config.builder()
                .withMaxConnectionLifetime(30, java.util.concurrent.TimeUnit.MINUTES)
                .withMaxConnectionPoolSize(50)
                .withConnectionTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
        }
        return driver;
    }

    /**
     * Opens a new session from the driver. Always close sessions using try-with-resources.
     * @return Session object
     */
    public static Session getSession() {
        return getDriver().session();
    }

    /**
     * Checks if the driver instance is closed.
     */
    private static boolean isClosed() {
        try {
            return driver == null;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Tests connectivity to the Neo4j instance.
     * @return true if connectivity verification succeeds, false otherwise
     */
    public static boolean testConnection() {
        try {
            getDriver().verifyConnectivity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the driver and releases socket resources.
     */
    public static synchronized void close() {
        if (driver != null) {
            try {
                driver.close();
            } catch (Exception ignored) {}
            driver = null;
        }
    }

    // Getters and Setters for configuration
    public static String getUri() { return uri; }
    public static synchronized void setUri(String newUri) {
        if (!newUri.equals(uri)) {
            uri = newUri;
            close();
        }
    }

    public static String getUser() { return user; }
    public static synchronized void setUser(String newUser) {
        if (!newUser.equals(user)) {
            user = newUser;
            close();
        }
    }

    public static String getPassword() { return password; }
    public static synchronized void setPassword(String newPassword) {
        if (!newPassword.equals(password)) {
            password = newPassword;
            close();
        }
    }
}
