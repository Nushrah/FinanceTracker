package financeTracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Currency;
import financeTracker.*;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_EMAIL = "test@example.com";
    private final Currency TEST_CURRENCY = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Test default constructor")
    void testDefaultConstructor() {
        assertNotNull(user, "Default constructor should create a non-null User object");
        assertNull(user.getUsername(), "Default username should be null");
        assertNull(user.getEmail(), "Default email should be null");
        assertNull(user.getBaseCurrency(), "Default base currency should be null");
        assertNull(user.getCreatedDate(), "Default created date should be null");
        assertEquals(0, user.getId(), "Default ID should be 0");
    }

    @Test
    @DisplayName("Test parameterized constructor")
    void testParameterizedConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        User paramUser = new User(TEST_USERNAME, TEST_EMAIL, TEST_CURRENCY);
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(paramUser, "Parameterized constructor should create a non-null User object");
        assertEquals(TEST_USERNAME, paramUser.getUsername(), "Username should match constructor parameter");
        assertEquals(TEST_EMAIL, paramUser.getEmail(), "Email should match constructor parameter");
        assertEquals(TEST_CURRENCY, paramUser.getBaseCurrency(), "Base currency should match constructor parameter");
        
        LocalDateTime createdDate = paramUser.getCreatedDate();
        assertNotNull(createdDate, "Created date should be set by constructor");
        assertTrue((createdDate.isEqual(beforeCreation) || createdDate.isAfter(beforeCreation)) && 
                  (createdDate.isEqual(afterCreation) || createdDate.isBefore(afterCreation)),
                  "Created date should be between before and after creation timestamps");
    }

    @Test
    @DisplayName("Test ID getter and setter")
    void testIdGetterAndSetter() {
        int testId = 123;
        
        user.setId(testId);
        assertEquals(testId, user.getId(), "ID getter should return the value set by setter");
    }

    @Test
    @DisplayName("Test username getter and setter")
    void testUsernameGetterAndSetter() {
        user.setUsername(TEST_USERNAME);
        assertEquals(TEST_USERNAME, user.getUsername(), "Username getter should return the value set by setter");
    }

    @Test
    @DisplayName("Test username with null value")
    void testUsernameWithNull() {
        user.setUsername(null);
        assertNull(user.getUsername(), "Username should allow null values");
    }

    @Test
    @DisplayName("Test email getter and setter")
    void testEmailGetterAndSetter() {
        user.setEmail(TEST_EMAIL);
        assertEquals(TEST_EMAIL, user.getEmail(), "Email getter should return the value set by setter");
    }

    @Test
    @DisplayName("Test email with null value")
    void testEmailWithNull() {
        user.setEmail(null);
        assertNull(user.getEmail(), "Email should allow null values");
    }

    @Test
    @DisplayName("Test created date getter and setter")
    void testCreatedDateGetterAndSetter() {
        LocalDateTime testDate = LocalDateTime.of(2023, 1, 1, 12, 0);
        
        user.setCreatedDate(testDate);
        assertEquals(testDate, user.getCreatedDate(), "Created date getter should return the value set by setter");
    }

    @Test
    @DisplayName("Test created date with null value")
    void testCreatedDateWithNull() {
        user.setCreatedDate(null);
        assertNull(user.getCreatedDate(), "Created date should allow null values");
    }

    @Test
    @DisplayName("Test base currency getter and setter")
    void testBaseCurrencyGetterAndSetter() {
        Currency eurCurrency = Currency.getInstance("EUR");
        
        user.setBaseCurrency(eurCurrency);
        assertEquals(eurCurrency, user.getBaseCurrency(), "Base currency getter should return the value set by setter");
    }

    @Test
    @DisplayName("Test base currency with null value")
    void testBaseCurrencyWithNull() {
        user.setBaseCurrency(null);
        assertNull(user.getBaseCurrency(), "Base currency should allow null values");
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        user.setId(1);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setBaseCurrency(TEST_CURRENCY);
        
        String result = user.toString();
        
        assertNotNull(result, "toString should not return null");
        assertTrue(result.contains("User{"), "toString should start with User{");
        assertTrue(result.contains("id=1"), "toString should contain ID");
        assertTrue(result.contains("username='testuser'"), "toString should contain username");
        assertTrue(result.contains("email='test@example.com'"), "toString should contain email");
        assertTrue(result.contains("currency=USD"), "toString should contain currency code");
        assertTrue(result.endsWith("}"), "toString should end with }");
    }

    @Test
    @DisplayName("Test multiple property changes")
    void testMultiplePropertyChanges() {
        // Initial setup
        user.setId(1);
        user.setUsername("initial");
        user.setEmail("initial@test.com");
        user.setBaseCurrency(Currency.getInstance("EUR"));
        
        // Verify initial values
        assertEquals(1, user.getId());
        assertEquals("initial", user.getUsername());
        assertEquals("initial@test.com", user.getEmail());
        assertEquals("EUR", user.getBaseCurrency().getCurrencyCode());
        
        // Change values
        user.setId(2);
        user.setUsername("updated");
        user.setEmail("updated@test.com");
        user.setBaseCurrency(Currency.getInstance("GBP"));
        
        // Verify updated values
        assertEquals(2, user.getId());
        assertEquals("updated", user.getUsername());
        assertEquals("updated@test.com", user.getEmail());
        assertEquals("GBP", user.getBaseCurrency().getCurrencyCode());
    }

    @Test
    @DisplayName("Test different currency codes")
    void testDifferentCurrencyCodes() {
        String[] currencyCodes = {"USD", "EUR", "GBP", "JPY", "CAD"};
        
        for (String currencyCode : currencyCodes) {
            Currency currency = Currency.getInstance(currencyCode);
            user.setBaseCurrency(currency);
            assertEquals(currency, user.getBaseCurrency(), 
                        "Should handle currency: " + currencyCode);
            assertEquals(currencyCode, user.getBaseCurrency().getCurrencyCode(), 
                        "Currency code should match: " + currencyCode);
        }
    }
}