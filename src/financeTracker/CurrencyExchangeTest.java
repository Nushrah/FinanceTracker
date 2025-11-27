package testFinanceTracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для CurrencyExchange
 * 11 тестов с >90% coverage
 */
public class CurrencyExchangeTest {
    private CurrencyExchange exchange;
    
    @BeforeEach
    public void setUp() {
        exchange = new CurrencyExchange();
    }
    
    // Базовые тесты конвертации
    @Test
    public void testConvertSameCurrency() {
        Currency hkd = Currency.getInstance("HKD");
        BigDecimal amount = BigDecimal.valueOf(1000);
        
        BigDecimal result = exchange.convertCurrency(amount, hkd, hkd);
        
        assertEquals(amount, result);
    }
    
    @Test
    public void testConvertHKDToUSD() {
        Currency hkd = Currency.getInstance("HKD");
        Currency usd = Currency.getInstance("USD");
        BigDecimal amount = BigDecimal.valueOf(1000);
        
        BigDecimal result = exchange.convertCurrency(amount, hkd, usd);
        
        BigDecimal expected = BigDecimal.valueOf(128.70);
        assertEquals(0, expected.compareTo(result));
    }
    
    @Test
    public void testConvertUSDToHKD() {
        Currency usd = Currency.getInstance("USD");
        Currency hkd = Currency.getInstance("HKD");
        BigDecimal amount = BigDecimal.valueOf(100);
        
        BigDecimal result = exchange.convertCurrency(amount, usd, hkd);
        
        BigDecimal expected = BigDecimal.valueOf(777.00);
        assertEquals(0, expected.compareTo(result));
    }
    
    @Test
    public void testConvertUSDToEUR() {
        // Cross-currency conversion test
        Currency usd = Currency.getInstance("USD");
        Currency eur = Currency.getInstance("EUR");
        BigDecimal amount = BigDecimal.valueOf(100);
        
        BigDecimal result = exchange.convertCurrency(amount, usd, eur);
        
        BigDecimal expected = BigDecimal.valueOf(86.24);
        assertEquals(expected, result);
    }
    
    // Граничные случаи
    @Test
    public void testConvertWithZeroAmount() {
        Currency hkd = Currency.getInstance("HKD");
        Currency usd = Currency.getInstance("USD");
        BigDecimal amount = BigDecimal.ZERO;
        
        BigDecimal result = exchange.convertCurrency(amount, hkd, usd);
        
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
    
    @Test
    public void testConvertWithUnsupportedCurrency() {
        Currency hkd = Currency.getInstance("HKD");
        Currency jpy = Currency.getInstance("JPY");
        BigDecimal amount = BigDecimal.valueOf(1000);
        
        assertThrows(IllegalArgumentException.class, () -> {
            exchange.convertCurrency(amount, hkd, jpy);
        });
    }
    
    // Update и get методы
    @Test
    public void testUpdateExchangeRate() {
        Currency hkd = Currency.getInstance("HKD");
        Currency usd = Currency.getInstance("USD");
        
        BigDecimal newRate = BigDecimal.valueOf(8.00);
        exchange.updateExchangeRate("USD", newRate);
        
        BigDecimal amount = BigDecimal.valueOf(1000);
        BigDecimal result = exchange.convertCurrency(amount, hkd, usd);
        
        BigDecimal expected = BigDecimal.valueOf(125.00);
        assertEquals(0, expected.compareTo(result));
    }
    
    @Test
    public void testUpdateHKDRateThrowsException() {
        BigDecimal newRate = BigDecimal.valueOf(2.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            exchange.updateExchangeRate("HKD", newRate);
        });
    }
    
    @Test
    public void testGetAvailableCurrencies() {
        Map<String, BigDecimal> currencies = exchange.getAvailableCurrencies();
        
        assertNotNull(currencies);
        assertEquals(5, currencies.size());
        assertTrue(currencies.containsKey("HKD"));
        assertTrue(currencies.containsKey("USD"));
        
        // Проверяем правильность курсов
        assertEquals(BigDecimal.ONE, currencies.get("HKD"));
        assertEquals(BigDecimal.valueOf(7.77), currencies.get("USD"));
    }
    
    // Тесты для getExchangeRate (новый метод для coverage)
    @Test
    public void testGetExchangeRate() {
        Currency usd = Currency.getInstance("USD");
        Currency eur = Currency.getInstance("EUR");
        
        BigDecimal rate = exchange.getExchangeRate(usd, eur);
        
        assertNotNull(rate);
        assertTrue(rate.compareTo(BigDecimal.ZERO) > 0);
    }
    
    @Test
    public void testGetExchangeRateUnsupportedCurrency() {
        Currency usd = Currency.getInstance("USD");
        Currency jpy = Currency.getInstance("JPY");
        
        assertThrows(IllegalArgumentException.class, () -> {
            exchange.getExchangeRate(usd, jpy);
        });
    }
}
