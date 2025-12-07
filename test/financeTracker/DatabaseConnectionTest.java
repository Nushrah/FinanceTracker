package financeTracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseConnection
 * Tests database connectivity and driver loading
 */
public class DatabaseConnectionTest {
    
    @Test
    @DisplayName("Test SQLite JDBC driver is loaded")
    public void testDriverLoaded() {
        try {
            Class.forName("org.sqlite.JDBC");
            assertTrue(true, "SQLite driver loaded successfully");
        } catch (ClassNotFoundException e) {
            fail("SQLite driver not found: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test database connection can be established")
    public void testDatabaseConnection() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
            conn.close();
            assertTrue(conn.isClosed(), "Connection should be closed after close()");
        } catch (SQLException e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test DatabaseConnection singleton instance")
    public void testDatabaseConnectionSingleton() {
        try {
            DatabaseConnection instance1 = DatabaseConnection.getInstance();
            DatabaseConnection instance2 = DatabaseConnection.getInstance();
            
            assertNotNull(instance1, "DatabaseConnection instance should not be null");
            assertSame(instance1, instance2, "getInstance should return the same instance");
            
            Connection conn = instance1.getConnection();
            assertNotNull(conn, "Connection from singleton should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
        } catch (Exception e) {
            fail("DatabaseConnection singleton test failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test database initialization creates tables")
    public void testDatabaseInitialization() {
        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            Connection conn = dbConn.getConnection();
            
            // Check if users table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, "users", null);
            assertTrue(rs.next(), "users table should exist");
            
            // Check if accounts table exists
            rs = meta.getTables(null, null, "accounts", null);
            assertTrue(rs.next(), "accounts table should exist");
            
            // Check if transactions table exists
            rs = meta.getTables(null, null, "transactions", null);
            assertTrue(rs.next(), "transactions table should exist");
            
        } catch (Exception e) {
            fail("Database initialization test failed: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test connection is not null")
    public void testConnectionNotNull() {
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        assertNotNull(dbConn.getConnection(), "Connection should not be null");
    }
    
    @Test
    @DisplayName("Test connection is open")
    public void testConnectionIsOpen() {
        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            Connection conn = dbConn.getConnection();
            assertFalse(conn.isClosed(), "Connection should be open");
        } catch (SQLException e) {
            fail("Failed to check connection status: " + e.getMessage());
        }
    }
}