package com.internshala.hms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles the connection to the MySQL database.
 * Update DB_URL, DB_USER and DB_PASSWORD to match your local MySQL setup.
 */
public class DatabaseConnector {

    // ---- Update these to match your MySQL Workbench setup ----
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hospitality_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Saurabh@2005";
    // ------------------------------------------------------------

    private static Connection connection;

    private DatabaseConnector() {
        // utility class, prevent instantiation
    }

    /**
     * Returns a single shared Connection instance (created on first call).
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connected successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add mysql-connector-j to your build path.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Closes the shared connection, if open.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
