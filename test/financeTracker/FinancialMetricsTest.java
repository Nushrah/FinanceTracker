package financeTracker;

import financeTracker.FinancialMetrics;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@DisplayName("FinancialMetrics Unit Tests")
class FinancialMetricsTest {
    
    private FinancialMetrics metrics;
    
    @BeforeEach
    void setUp() {
        metrics = new FinancialMetrics();
    }
    
    @Test
    @DisplayName("Constructor initializes all fields to zero")
    void testConstructorInitializesToZero() {
        assertEquals(BigDecimal.ZERO, metrics.getTotalIncome());
        assertEquals(BigDecimal.ZERO, metrics.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, metrics.getNetCashFlow());
        assertEquals(BigDecimal.ZERO, metrics.getSavingsRate());
        assertEquals(BigDecimal.ZERO, metrics.getExpenseToIncomeRatio());
    }
    
    @Test
    @DisplayName("Set and get total income")
    void testSetAndGetTotalIncome() {
        BigDecimal income = new BigDecimal("5000.50");
        metrics.setTotalIncome(income);
        assertEquals(income, metrics.getTotalIncome());
    }
    
    @Test
    @DisplayName("Set and get total expenses")
    void testSetAndGetTotalExpenses() {
        BigDecimal expenses = new BigDecimal("3000.75");
        metrics.setTotalExpenses(expenses);
        assertEquals(expenses, metrics.getTotalExpenses());
    }
    
    @Test
    @DisplayName("Set and get net cash flow")
    void testSetAndGetNetCashFlow() {
        BigDecimal netFlow = new BigDecimal("2000.00");
        metrics.setNetCashFlow(netFlow);
        assertEquals(netFlow, metrics.getNetCashFlow());
    }
    
    @Test
    @DisplayName("Set and get savings rate")
    void testSetAndGetSavingsRate() {
        BigDecimal savingsRate = new BigDecimal("40.00");
        metrics.setSavingsRate(savingsRate);
        assertEquals(savingsRate, metrics.getSavingsRate());
    }
    
    @Test
    @DisplayName("Set and get expense to income ratio")
    void testSetAndGetExpenseToIncomeRatio() {
        BigDecimal ratio = new BigDecimal("60.00");
        metrics.setExpenseToIncomeRatio(ratio);
        assertEquals(ratio, metrics.getExpenseToIncomeRatio());
    }
    
    @Test
    @DisplayName("toString includes all metric values")
    void testToStringFormat() {
        metrics.setTotalIncome(new BigDecimal("5000.00"));
        metrics.setTotalExpenses(new BigDecimal("3000.00"));
        metrics.setNetCashFlow(new BigDecimal("2000.00"));
        metrics.setSavingsRate(new BigDecimal("40.00"));
        metrics.setExpenseToIncomeRatio(new BigDecimal("0.60"));
        
        String result = metrics.toString();
        
        assertTrue(result.contains("5000,00"));
        assertTrue(result.contains("3000,00"));
        assertTrue(result.contains("2000,00"));
        assertTrue(result.contains("40,00"));
        assertTrue(result.contains("0,60"));
    }
    
    @Test
    @DisplayName("toString handles negative values")
    void testToStringWithNegativeValues() {
        metrics.setTotalIncome(new BigDecimal("1000.00"));
        metrics.setTotalExpenses(new BigDecimal("2000.00"));
        metrics.setNetCashFlow(new BigDecimal("-1000.00"));
        metrics.setSavingsRate(new BigDecimal("-100.00"));
        
        String result = metrics.toString();
        
        assertTrue(result.contains("-1000,00"));
        assertTrue(result.contains("-100,00"));
    }
    
    @Test
    @DisplayName("Multiple setters work independently")
    void testMultipleSetters() {
        BigDecimal income = new BigDecimal("5000.00");
        BigDecimal expenses = new BigDecimal("3000.00");
        BigDecimal netFlow = new BigDecimal("2000.00");
        
        metrics.setTotalIncome(income);
        metrics.setTotalExpenses(expenses);
        metrics.setNetCashFlow(netFlow);
        
        assertEquals(income, metrics.getTotalIncome());
        assertEquals(expenses, metrics.getTotalExpenses());
        assertEquals(netFlow, metrics.getNetCashFlow());
    }
    
    
}
