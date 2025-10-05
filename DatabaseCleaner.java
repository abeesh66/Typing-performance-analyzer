package org.example.util;

import org.example.database.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for cleaning up database data.
 */
public class DatabaseCleaner {
    private static final Logger LOGGER = Logger.getLogger(DatabaseCleaner.class.getName());

    /**
     * Clears all user data from the database.
     * This will delete all users and their associated test sessions.
     */
    public static void clearAllUserData() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Disable foreign key constraints temporarily
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            // Clear test sessions first due to foreign key constraint
            stmt.execute("DELETE FROM TEST_SESSION");
            
            // Clear users
            stmt.execute("DELETE FROM USER");
            
            // Reset auto-increment counters
            stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('USER', 'TEST_SESSION')");
            
            // Re-enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Commit the transaction
            conn.commit();
            
            LOGGER.info("Successfully cleared all user data from the database");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to clear user data: " + e.getMessage(), e);
            throw new RuntimeException("Failed to clear user data", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("Clearing all user data...");
        clearAllUserData();
        System.out.println("User data has been cleared successfully!");
    }
}
