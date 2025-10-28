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
        // Using HKD as the base currency (HKD = 1)
        exchangeRates.put("HKD", BigDecimal.ONE);
        exchangeRates.put("USD", BigDecimal.valueOf(0.128));  // 1 HKD = 0.128 USD
        exchangeRates.put("EUR", BigDecimal.valueOf(0.118));  // 1 HKD = 0.118 EUR
        exchangeRates.put("CNY", BigDecimal.valueOf(0.925));  // 1 HKD = 0.925 CNY
        exchangeRates.put("SGD", BigDecimal.valueOf(0.172));  // 1 HKD = 0.172 SGD
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
        
        // Convert to HKD first, then to target currency
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
    
}
