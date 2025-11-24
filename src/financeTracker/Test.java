package financeTracker;

import java.sql.*;

public class Test {
    public static void main(String[] args) {
        try {
            // Test if driver is loaded
            Class.forName("org.sqlite.JDBC");
            System.out.println("✓ SQLite driver found!");
            
            // Test connection
            Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("✓ Database connected!");
            conn.close();
            
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }
}