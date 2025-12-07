package financeTracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import financeTracker.*;



import java.time.LocalDateTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

class AuthServiceTest {
    
    private AuthService authService;
    private TestUserRepository testUserRepository;
    
    @BeforeEach
    void setUp() {
        testUserRepository = new TestUserRepository();
        authService = new AuthService();
        injectTestRepository(authService, testUserRepository);
    }
    
    // Test constructor
    @Test
    @DisplayName("Constructor should initialize with no logged in user")
    void testConstructor() {
        AuthService freshService = new AuthService();
        assertFalse(freshService.isLoggedIn());
        assertNull(freshService.getCurrentUser());
    }
    
    // Register tests
    @Test
    @DisplayName("Register should succeed with valid inputs")
    void testRegister_Success() {
        // Act
        boolean result = authService.register("newuser", "password123", "user@email.com", "USD");
        
        // Assert
        assertTrue(result);
        assertTrue(testUserRepository.userExists("newuser"));
    }
    
    @Test
    @DisplayName("Register should throw exception when username is empty")
    void testRegister_EmptyUsername() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register("", "password123", "user@email.com", "USD"));
        assertEquals("Username cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Register should throw exception when username is null")
    void testRegister_NullUsername() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register(null, "password123", "user@email.com", "USD"));
        assertEquals("Username cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Register should throw exception when password is too short")
    void testRegister_ShortPassword() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register("newuser", "123", "user@email.com", "USD"));
        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }
    
    @Test
    @DisplayName("Register should throw exception when password is null")
    void testRegister_NullPassword() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register("newuser", null, "user@email.com", "USD"));
        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }
    
    @Test
    @DisplayName("Register should throw exception when username already exists")
    void testRegister_UsernameExists() {
        // Arrange
        authService.register("existinguser", "password123", "user@email.com", "USD");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register("existinguser", "password123", "user2@email.com", "EUR"));
        assertEquals("Username already exists", exception.getMessage());
    }
    
    @Test
    @DisplayName("Register should throw exception for invalid currency code")
    void testRegister_InvalidCurrency() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register("newuser", "password123", "user@email.com", "INVALID"));
        assertEquals("Invalid currency code: INVALID", exception.getMessage());
    }
    
    @Test
    @DisplayName("Register should create user with correct currency")
    void testRegister_CurrencyCorrectlySet() {
        // Act
        authService.register("newuser", "password123", "user@email.com", "EUR");
        
        // Assert - verify user can login and has correct currency
        User user = authService.login("newuser", "password123");
        assertNotNull(user);
        assertEquals("EUR", user.getBaseCurrency().getCurrencyCode());
    }
    
    // Login tests
    @Test
    @DisplayName("Login should succeed with valid credentials")
    void testLogin_Success() {
        // Arrange
        authService.register("testuser", "password123", "test@email.com", "USD");
        
        // Act
        User result = authService.login("testuser", "password123");
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@email.com", result.getEmail());
        assertEquals("USD", result.getBaseCurrency().getCurrencyCode());
        assertTrue(authService.isLoggedIn());
        assertEquals(result, authService.getCurrentUser());
    }
    
    @Test
    @DisplayName("Login should return null with invalid credentials")
    void testLogin_InvalidCredentials() {
        // Arrange
        authService.register("testuser", "password123", "test@email.com", "USD");
        
        // Act
        User result = authService.login("testuser", "wrongpassword");
        
        // Assert
        assertNull(result);
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }
    
    @Test
    @DisplayName("Login should return null with null username")
    void testLogin_NullUsername() {
        // Act
        User result = authService.login(null, "password123");
        
        // Assert
        assertNull(result);
        assertFalse(authService.isLoggedIn());
    }
    
    @Test
    @DisplayName("Login should return null with null password")
    void testLogin_NullPassword() {
        // Act
        User result = authService.login("testuser", null);
        
        // Assert
        assertNull(result);
        assertFalse(authService.isLoggedIn());
    }
    
    @Test
    @DisplayName("Login should return null for non-existent user")
    void testLogin_NonExistentUser() {
        // Act
        User result = authService.login("nonexistent", "password123");
        
        // Assert
        assertNull(result);
        assertFalse(authService.isLoggedIn());
    }
    
    // Logout tests
    @Test
    @DisplayName("Logout should clear current user when logged in")
    void testLogout_WhenLoggedIn() {
        // Arrange - login first
        authService.register("testuser", "password123", "test@email.com", "USD");
        authService.login("testuser", "password123");
        assertTrue(authService.isLoggedIn());
        
        // Act
        authService.logout();
        
        // Assert
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }
    
    @Test
    @DisplayName("Logout should do nothing when not logged in")
    void testLogout_WhenNotLoggedIn() {
        // Arrange
        assertFalse(authService.isLoggedIn());
        
        // Act
        authService.logout();
        
        // Assert
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }
    
    // Change password tests
    @Test
    @DisplayName("Change password should succeed with valid current password")
    void testChangePassword_Success() {
        // Arrange - register and login
        authService.register("testuser", "oldpassword", "test@email.com", "USD");
        authService.login("testuser", "oldpassword");
        
        // Act
        boolean result = authService.changePassword("oldpassword", "newpassword");
        
        // Assert
        assertTrue(result);
        // Verify new password works and old password doesn't
        authService.logout();
        assertNotNull(authService.login("testuser", "newpassword"));
        authService.logout();
        assertNull(authService.login("testuser", "oldpassword"));
    }
    
    @Test
    @DisplayName("Change password should throw exception when no user logged in")
    void testChangePassword_NoUserLoggedIn() {
        // Arrange
        assertFalse(authService.isLoggedIn());
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> authService.changePassword("oldpassword", "newpassword"));
        assertEquals("No user logged in", exception.getMessage());
    }
    
    @Test
    @DisplayName("Change password should fail with wrong current password")
    void testChangePassword_WrongCurrentPassword() {
        // Arrange - register and login
        authService.register("testuser", "correctpassword", "test@email.com", "USD");
        authService.login("testuser", "correctpassword");
        
        // Act
        boolean result = authService.changePassword("wrongpassword", "newpassword");
        
        // Assert
        assertFalse(result);
        // Verify original password still works
        authService.logout();
        assertNotNull(authService.login("testuser", "correctpassword"));
    }
    
    @Test
    @DisplayName("Change password should maintain user session after success")
    void testChangePassword_MaintainsSession() {
        // Arrange - register and login
        authService.register("testuser", "oldpassword", "test@email.com", "USD");
        User originalUser = authService.login("testuser", "oldpassword");
        
        // Act
        boolean result = authService.changePassword("oldpassword", "newpassword");
        
        // Assert
        assertTrue(result);
        assertTrue(authService.isLoggedIn());
        assertEquals(originalUser, authService.getCurrentUser());
    }
    
    // Edge case tests
    @Test
    @DisplayName("Multiple registrations with different currencies")
    void testMultipleRegistrations_DifferentCurrencies() {
        // Act
        authService.register("user1", "password123", "user1@email.com", "USD");
        authService.register("user2", "password123", "user2@email.com", "EUR");
        authService.register("user3", "password123", "user3@email.com", "JPY");
        
        // Assert
        User user1 = authService.login("user1", "password123");
        User user2 = authService.login("user2", "password123");
        User user3 = authService.login("user3", "password123");
        
        assertEquals("USD", user1.getBaseCurrency().getCurrencyCode());
        assertEquals("EUR", user2.getBaseCurrency().getCurrencyCode());
        assertEquals("JPY", user3.getBaseCurrency().getCurrencyCode());
    }
    
    @Test
    @DisplayName("User created date should be set after registration")
    void testUserCreatedDate() {
        // Act
        authService.register("testuser", "password123", "test@email.com", "USD");
        User user = authService.login("testuser", "password123");
        
        // Assert
        assertNotNull(user.getCreatedDate());
        assertTrue(user.getCreatedDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(user.getCreatedDate().isAfter(LocalDateTime.now().minusSeconds(1)));
    }
    
    // Helper method to inject test repository using reflection
    private void injectTestRepository(AuthService authService, UserRepository repository) {
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("userRepository");
            field.setAccessible(true);
            field.set(authService, repository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject test repository", e);
        }
    }
    
    // Test implementation of UserRepository that properly handles passwords
    private static class TestUserRepository extends UserRepository {
        private Map<String, User> users = new HashMap<>();
        private Map<String, String> passwords = new HashMap<>(); // username -> password
        private int nextId = 1;
        
        @Override
        public boolean userExists(String username) {
            return users.containsKey(username);
        }
        
        @Override
        public boolean createUser(String username, String password, String email, Currency currency) {
            if (userExists(username)) {
                return false;
            }
            User user = new User(username, email, currency);
            user.setId(nextId++);
            users.put(username, user);
            passwords.put(username, password); // Store plain text for testing (in real app, hash this)
            return true;
        }
        
        @Override
        public User authenticateUser(String username, String password) {
            if (!userExists(username)) {
                return null;
            }
            // For testing, compare plain text passwords
            String storedPassword = passwords.get(username);
            if (storedPassword != null && storedPassword.equals(password)) {
                return users.get(username);
            }
            return null;
        }
        
        @Override
        public boolean updatePassword(int userId, String newPassword) {
            // Find user by ID
            for (User user : users.values()) {
                if (user.getId() == userId) {
                    String username = user.getUsername();
                    passwords.put(username, newPassword); // Update password
                    return true;
                }
            }
            return false;
        }
    }
}