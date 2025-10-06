package org.example;

import org.example.database.DatabaseManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try {
            // Test connection
            Connection conn = DatabaseManager.getInstance().connect();
            System.out.println("✓ Successfully connected to database");
            
            // Test if tables exist
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table'")) {
                
                System.out.println("\nDatabase tables:");
                System.out.println("-----------------");
                while (rs.next()) {
                    System.out.println("- " + rs.getString("name"));
                }
                System.out.println("-----------------");
            }
            
            System.out.println("\n✓ Database test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("✗ Database test failed:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}