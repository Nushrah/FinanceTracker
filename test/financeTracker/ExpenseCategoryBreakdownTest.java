package financeTracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@DisplayName("ExpenseCategoryBreakdown Unit Tests")
class ExpenseCategoryBreakdownTest {
    
    private ExpenseCategoryBreakdown breakdown;
    
    @Test
    @DisplayName("Constructor with null total expenses initializes to ZERO")
    void testConstructorWithNullTotalExpenses() {
        breakdown = new ExpenseCategoryBreakdown(null, new HashMap<>());
        assertEquals(BigDecimal.ZERO, breakdown.getTotalExpenses());
    }
    
    @Test
    @DisplayName("Constructor stores valid total expenses")
    void testConstructorWithValidTotalExpenses() {
        BigDecimal total = new BigDecimal("1000.00");
        breakdown = new ExpenseCategoryBreakdown(total, new HashMap<>());
        assertEquals(0, total.compareTo(breakdown.getTotalExpenses()));
    }
    
    @Test
    @DisplayName("Constructor with null categories initializes empty map")
    void testConstructorWithNullCategories() {
        breakdown = new ExpenseCategoryBreakdown(BigDecimal.ZERO, null);
        assertTrue(breakdown.getCategoryPercentages().isEmpty());
    }
    
    @Test
    @DisplayName("Constructor stores category percentages")
    void testConstructorStoresCategoryPercentages() {
        Map<String, BigDecimal> categories = new HashMap<>();
        categories.put("Food", new BigDecimal("50.00"));
        categories.put("Transport", new BigDecimal("30.00"));
        
        breakdown = new ExpenseCategoryBreakdown(new BigDecimal("1000.00"), categories);
        
        assertEquals(2, breakdown.getCategoryPercentages().size());
        assertEquals(0, new BigDecimal("50.00").compareTo(breakdown.getCategoryPercentages().get("Food")));
        assertEquals(0, new BigDecimal("30.00").compareTo(breakdown.getCategoryPercentages().get("Transport")));
    }
    
    @Test
    @DisplayName("getCategoryPercentages returns unmodifiable map")
    void testGetCategoryPercentagesIsUnmodifiable() {
        Map<String, BigDecimal> categories = new HashMap<>();
        categories.put("Food", new BigDecimal("50.00"));
        breakdown = new ExpenseCategoryBreakdown(new BigDecimal("1000.00"), categories);
        
        Map<String, BigDecimal> retrieved = breakdown.getCategoryPercentages();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            retrieved.put("NewCategory", new BigDecimal("10.00"));
        });
    }
    
    @Test
    @DisplayName("toString shows no expenses message for empty breakdown")
    void testToStringEmptyBreakdown() {
        breakdown = new ExpenseCategoryBreakdown(BigDecimal.ZERO, new HashMap<>());
        assertEquals("No expenses for the selected period.", breakdown.toString());
    }
    
    @Test
    @DisplayName("toString formats single category")
    void testToStringSingleCategory() {
        Map<String, BigDecimal> categories = new HashMap<>();
        categories.put("Food", new BigDecimal("100.00"));
        breakdown = new ExpenseCategoryBreakdown(new BigDecimal("1000.00"), categories);
        
        String result = breakdown.toString();
        
        assertTrue(result.contains("- Food: 100,00%"));
    }
    
    @Test
    @DisplayName("toString formats multiple categories")
    void testToStringMultipleCategories() {
        Map<String, BigDecimal> categories = new HashMap<>();
        categories.put("Food", new BigDecimal("50.00"));
        categories.put("Transport", new BigDecimal("30.00"));
        categories.put("Entertainment", new BigDecimal("20.00"));
        
        breakdown = new ExpenseCategoryBreakdown(new BigDecimal("1000.00"), categories);
        
        String result = breakdown.toString();
        
        assertTrue(result.contains("- Food: 50,00%"));
        assertTrue(result.contains("- Transport: 30,00%"));
        assertTrue(result.contains("- Entertainment: 20,00%"));
    }
    
    @Test
    @DisplayName("toString handles decimal percentages with precision")
    void testToStringDecimalPrecision() {
        Map<String, BigDecimal> categories = new HashMap<>();
        categories.put("Groceries", new BigDecimal("33.3333"));
        breakdown = new ExpenseCategoryBreakdown(new BigDecimal("100.00"), categories);
        
        String result = breakdown.toString();
        
        assertTrue(result.contains("- Groceries: 33,33%"));
    }
    
//    @Test
//    @DisplayName("toString handles very small percentages")
//    void testToStringSmallPercentages() {
//        Map<String, BigDecimal> categories = new HashMap<>();
//        categories.put("Misc", new BigDecimal("0.01"));
//        breakdown = new ExpenseCategoryBreakdown(new BigDecimal("10000.00"), categories);
//        
//        String result = breakdown.toString();
//        
//        assertTrue(result.contains("Misc: 0.01%"));
//    }
}
