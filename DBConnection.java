package com.lostfound.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * ------------------------------------------------------------
 * Provides a single shared database Connection object.
 * Uses the Singleton pattern so the entire app reuses one
 * connection instead of opening a new one every request.
 *
 * HOW TO USE in any DAO class:
 *   Connection conn = DBConnection.getConnection();
 *
 * Place: src/main/java/com/lostfound/utils/DBConnection.java
 * ------------------------------------------------------------
 */
public class DBConnection {

    // The single shared connection instance
    private static Connection connection = null;

    /**
     * Returns the active database connection.
     * Creates a new one only if none exists or the current one is closed.
     *
     * @return active Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {

        try {
            // Check if connection is null OR has been closed/dropped
            if (connection == null || connection.isClosed()) {

                // Step 1: Load the MySQL JDBC driver into memory
                Class.forName(DBConfig.DRIVER);

                // Step 2: Create the connection using URL + credentials
                connection = DriverManager.getConnection(
                    DBConfig.URL,
                    DBConfig.USERNAME,
                    DBConfig.PASSWORD
                );

                System.out.println("[DB] Connection established to: "
                    + DBConfig.DATABASE);
            }

        } catch (ClassNotFoundException e) {
            // This means mysql-connector-j JAR is missing from WEB-INF/lib
            System.err.println("[DB] ERROR: MySQL JDBC Driver not found!");
            System.err.println("     → Make sure mysql-connector-j.jar is in WEB-INF/lib");
            throw new SQLException("JDBC Driver not found: " + e.getMessage());

        } catch (SQLException e) {
            System.err.println("[DB] ERROR: Cannot connect to database!");
            System.err.println("     → Check host, port, username, password in DBConfig.java");
            System.err.println("     → Error: " + e.getMessage());
            throw e;
        }

        return connection;
    }

    /**
     * Safely closes the database connection.
     * Call this when the application shuts down (not after every query).
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Warning: Error while closing connection: "
                    + e.getMessage());
            }
        }
    }

    // Private constructor — prevent instantiation
    private DBConnection() {}
}