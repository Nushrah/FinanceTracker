package financeTracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class CurrencyExchange {
    private final Map<String, BigDecimal> exchangeRates;
    
    public CurrencyExchange() {
        this.exchangeRates = new HashMap<>();
        initializeExchangeRates();
    }
    
    private void initializeExchangeRates() {
        exchangeRates.put("HKD", BigDecimal.ONE);
        exchangeRates.put("USD", BigDecimal.valueOf(7.77));   // 1 USD = 7.77 HKD
        exchangeRates.put("EUR", BigDecimal.valueOf(9.01));   // 1 EUR = 9.01 HKD
        exchangeRates.put("CNY", BigDecimal.valueOf(1.09));   // 1 CNY = 1.09 HKD
        exchangeRates.put("SGD", BigDecimal.valueOf(5.97));   // 1 SGD = 5.97 HKD
    }
    
    public BigDecimal convertCurrency(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        String fromCode = fromCurrency.getCurrencyCode();
        String toCode = toCurrency.getCurrencyCode();
        
        BigDecimal fromRate = exchangeRates.get(fromCode);
        BigDecimal toRate = exchangeRates.get(toCode);
        
        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + 
                (fromRate == null ? fromCode : toCode));
        }
        
        BigDecimal amountInHKD = amount.multiply(fromRate);
        return amountInHKD.divide(toRate, 2, RoundingMode.HALF_UP);
    }
    
    public void updateExchangeRate(String currencyCode, BigDecimal rate) {
        if (currencyCode.equals("HKD")) {
            throw new IllegalArgumentException("Cannot update HKD rate as it is the base currency (1.0)");
        }
        exchangeRates.put(currencyCode, rate);
    }
    
    public Map<String, BigDecimal> getAvailableCurrencies() {
        return new HashMap<>(exchangeRates);
    }
    
    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        BigDecimal fromRate = exchangeRates.get(fromCurrency.getCurrencyCode());
        BigDecimal toRate = exchangeRates.get(toCurrency.getCurrencyCode());
        
        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        
        return toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
    }
    
}