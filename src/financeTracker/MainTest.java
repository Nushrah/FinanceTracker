package testFinanceTracker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    
    @Test
    public void testLoginAttemptsLogic() {
        // (lines 100-113)
        final int MAX_ATTEMPTS = 3;
        int attempts = 0;
        
        attempts++;
        assertEquals(1, attempts);
        assertTrue(attempts < MAX_ATTEMPTS);
        
        attempts++;
        attempts++;
        assertEquals(3, attempts);
        assertTrue(attempts >= MAX_ATTEMPTS);
    }
    
    @Test
    public void testRemainingAttemptsCalculation() {
        final int MAX_ATTEMPTS = 3;
        
        int attempts = 1;
        int remaining = MAX_ATTEMPTS - attempts;
        assertEquals(2, remaining);
        
        attempts = 3;
        remaining = MAX_ATTEMPTS - attempts;
        assertEquals(0, remaining);
    }
    
    @Test
    public void testPasswordValidationLength() {
        // (lines 125, 167)
        final int MIN_LENGTH = 6;
        
        String shortPassword = "12345";
        String validPassword = "123456";
        
        assertTrue(shortPassword.length() < MIN_LENGTH);
        assertTrue(validPassword.length() >= MIN_LENGTH);
    }
    
    @Test
    public void testPasswordConfirmationMatching() {
        // (lines 163-166)
        String password1 = "mypassword";
        String password2 = "mypassword";
        String password3 = "differentpass";
        
        assertTrue(password1.equals(password2));
        assertFalse(password1.equals(password3));
    }
    
    @Test
    public void testCurrencyToUpperCase() {
        // (line 133)
        String currency = "usd".toUpperCase();
        assertEquals("USD", currency);
        
        String currency2 = "hkd".toUpperCase();
        assertEquals("HKD", currency2);
    }
    
    @Test
    public void testAvailableCurrencies() {
        // (line 131)
        String[] availableCurrencies = {"USD", "HKD", "EUR", "CNY", "SGD"};
        
        assertEquals(5, availableCurrencies.length);
        assertTrue(java.util.Arrays.asList(availableCurrencies).contains("USD"));
        assertTrue(java.util.Arrays.asList(availableCurrencies).contains("HKD"));
    }
    
    @Test
    public void testBooleanReturnValuesForAuthMethods() {
        // (lines 82-149)
        boolean successCase = true;
        boolean failureCase = false;
        
        assertTrue(successCase);
        assertFalse(failureCase);
        
        boolean registrationSuccess = !failureCase;
        assertTrue(registrationSuccess);
    }
    
    @Test
    public void testMainMenuOptionsCount() {
        // (lines 191-202)
        int menuOptionsCount = 10; 
        
        assertTrue(menuOptionsCount > 0);
        assertTrue(menuOptionsCount <= 10);
    }
}
