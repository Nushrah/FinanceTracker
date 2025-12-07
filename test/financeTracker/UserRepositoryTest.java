package financeTracker;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import financeTracker.*;

import java.sql.*;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    private static Connection connection;
    private UserRepository userRepository;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_EMAIL = "test@example.com";
    private final Currency TEST_CURRENCY = Currency.getInstance("USD");

    @BeforeAll
    static void setUpBeforeAll() throws SQLException {
        // Get a connection to an in-memory database for testing
        connection = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
        initializeTestDatabase();
    }

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        
        // Use reflection to set the private connection field
        try {
            var field = UserRepository.class.getDeclaredField("connection");
            field.setAccessible(true);
            field.set(userRepository, connection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set test connection", e);
        }
        
        cleanDatabase();
    }

    @AfterAll
    static void tearDownAfterAll() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private static void initializeTestDatabase() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                email TEXT,
                created_date TEXT NOT NULL,
                base_currency TEXT DEFAULT 'USD'
            )
        """;
        
        String createAccountsTable = """
            CREATE TABLE IF NOT EXISTS accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                balance DECIMAL(15,2) NOT NULL,
                currency TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (id)
            )
        """;
        
        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                account_id INTEGER NOT NULL,
                description TEXT NOT NULL,
                amount DECIMAL(15,2) NOT NULL,
                type TEXT NOT NULL,
                category TEXT NOT NULL,
                date TEXT NOT NULL,
                notes TEXT,
                FOREIGN KEY (user_id) REFERENCES users (id),
                FOREIGN KEY (account_id) REFERENCES accounts (id)
            )
        """;
        
        try (var stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createAccountsTable);
            stmt.execute(createTransactionsTable);
        }
    }

    private void cleanDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM accounts");
            stmt.execute("DELETE FROM users");
        } catch (SQLException e) {
            fail("Failed to clean database", e);
        }
    }

    @Test
    void testCreateUser_Success() {
        // Act
        boolean result = userRepository.createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY);
        
        // Assert
        assertTrue(result);
        
        // Verify user was actually created
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            pstmt.setString(1, TEST_USERNAME);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(TEST_USERNAME, rs.getString("username"));
                assertEquals(TEST_EMAIL, rs.getString("email"));
                assertEquals(TEST_CURRENCY.getCurrencyCode(), rs.getString("base_currency"));
                assertNotNull(rs.getString("password_hash"));
                assertNotNull(rs.getString("salt"));
                assertNotNull(rs.getString("created_date"));
            }
        } catch (SQLException e) {
            fail("Database error during verification", e);
        }
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        // Arrange - Create first user
        userRepository.createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY);
        
        // Act & Assert - Try to create user with same username
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userRepository.createUser(TEST_USERNAME, "differentpassword", "different@example.com", TEST_CURRENCY));
        
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void testCreateUser_DatabaseError() {
        // Arrange - Create a situation that causes SQLException
        // We'll use an invalid SQL statement to simulate database error
        UserRepository brokenRepository = new UserRepository();
        try {
            var field = UserRepository.class.getDeclaredField("connection");
            field.setAccessible(true);
            // Set to a closed connection to force error
            Connection closedConnection = DriverManager.getConnection("jdbc:sqlite:file:closeddb?mode=memory");
            closedConnection.close();
            field.set(brokenRepository, closedConnection);
        } catch (Exception e) {
            fail("Failed to set up test", e);
        }
        
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> brokenRepository.createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY));
    }

    @Test
    void testAuthenticateUser_Success() {
        // Arrange - Create a user first
        userRepository.createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY);
        
        // Act
        User user = userRepository.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
        
        // Assert
        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertEquals(TEST_CURRENCY, user.getBaseCurrency());
        assertTrue(user.getId() > 0);
        assertNotNull(user.getCreatedDate());
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        // Arrange - Create a user first
        userRepository.createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY);
        
        // Act
        User user = userRepository.authenticateUser(TEST_USERNAME, "wrongpassword");
        
        // Assert
        assertNull(user);
    }

    @Test
    void testAuthenticateUser_NonExistentUser() {
        // Act
        User user = userRepository.authenticateUser("nonexistent", TEST_PASSWORD);
        
        // Assert
        assertNull(user);
    }

    @Test
    void testAuthenticateUser_DatabaseError() {
        // Arrange - Create repository with closed connection
        UserRepository brokenRepository = new UserRepository();
        try {
            var field = UserRepository.class.getDeclaredField("connection");
            field.setAccessible(true);
            Connection closedConnection = DriverManager.getConnection("jdbc:sqlite:file:closeddb?mode=memory");
            closedConnection.close();
            field.set(brokenRepository, closedConnection);
        } catch (Exception e) {
            fail("Failed to set up test", e);
        }
        
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> brokenRepository.authenticateUser(TEST_USERNAME, TEST_PASSWORD));
    }

    @Test
    void testUserExists_True() {
        // Arrange - Create a user first
        userRepository.createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY);
        
        // Act
        boolean exists = userRepository.userExists(TEST_USERNAME);
        
        // Assert
        assertTrue(exists);
    }

    @Test
    void testUserExists_False() {
        // Act
        boolean exists = userRepository.userExists("nonexistentuser");
        
        // Assert
        assertFalse(exists);
    }

    @Test
    void testUserExists_DatabaseError() {
        // Arrange - Create repository with closed connection
        UserRepository brokenRepository = new UserRepository();
        try {
            var field = UserRepository.class.getDeclaredField("connection");
            field.setAccessible(true);
            Connection closedConnection = DriverManager.getConnection("jdbc:sqlite:file:closeddb?mode=memory");
            closedConnection.close();
            field.set(brokenRepository, closedConnection);
        } catch (Exception e) {
            fail("Failed to set up test", e);
        }
        
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> brokenRepository.userExists(TEST_USERNAME));
    }

    @Test
    void testUpdatePassword_Success() {
        // Arrange - Create a user first and get their ID
        userRepository.createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY);
        User user = userRepository.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(user);
        
        String newPassword = "newpassword123";
        
        // Act
        boolean result = userRepository.updatePassword(user.getId(), newPassword);
        
        // Assert
        assertTrue(result);
        
        // Verify password was actually changed
        User userAfterUpdate = userRepository.authenticateUser(TEST_USERNAME, newPassword);
        assertNotNull(userAfterUpdate);
        
        // Verify old password no longer works
        User userWithOldPassword = userRepository.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
        assertNull(userWithOldPassword);
    }

    @Test
    void testUpdatePassword_NonExistentUser() {
        // Act
        boolean result = userRepository.updatePassword(9999, "newpassword");
        
        // Assert - Should return false when no rows are updated
        assertFalse(result);
    }

    @Test
    void testUpdatePassword_DatabaseError() {
        // Arrange - Create repository with closed connection
        UserRepository brokenRepository = new UserRepository();
        try {
            var field = UserRepository.class.getDeclaredField("connection");
            field.setAccessible(true);
            Connection closedConnection = DriverManager.getConnection("jdbc:sqlite:file:closeddb?mode=memory");
            closedConnection.close();
            field.set(brokenRepository, closedConnection);
        } catch (Exception e) {
            fail("Failed to set up test", e);
        }
        
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> brokenRepository.updatePassword(1, "newpassword"));
    }

    @Test
    void testPasswordHashingIntegration() {
        // This test verifies that the password hashing works correctly with the repository
        String password = "testpassword";
        
        // Act - Create user
        userRepository.createUser("hashuser", password, "hash@test.com", TEST_CURRENCY);
        
        // Verify we can authenticate with the same password
        User user = userRepository.authenticateUser("hashuser", password);
        assertNotNull(user);
        
        // Verify wrong password fails
        User wrongUser = userRepository.authenticateUser("hashuser", "wrongpassword");
        assertNull(wrongUser);
    }

    @Test
    void testCreateUserWithNullValues() {
        // Test handling of null values (should throw SQLException wrapped in RuntimeException)
        assertThrows(RuntimeException.class, 
            () -> userRepository.createUser(null, TEST_PASSWORD, TEST_EMAIL, TEST_CURRENCY));
    }
    
    // Tests with stub connection for SQLException branches
    @Test
    void testAuthenticateUserSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        UserRepository repoWithStub = new UserRepository(stubConnection);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.authenticateUser("testuser", "password");
        });
        
        assertEquals("Failed to authenticate user", exception.getMessage());
    }
    
    @Test
    void testUserExistsSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        UserRepository repoWithStub = new UserRepository(stubConnection);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.userExists("testuser");
        });
        
        assertEquals("Failed to check user existence", exception.getMessage());
    }
    
    @Test
    void testUpdatePasswordSQLException() {
        StubConnectionForSQLException stubConnection = new StubConnectionForSQLException();
        UserRepository repoWithStub = new UserRepository(stubConnection);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repoWithStub.updatePassword(1, "newpassword");
        });
        
        assertEquals("Failed to update password", exception.getMessage());
    }
}