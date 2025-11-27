package testFinanceTracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecommendationEngineTest {
    private RecommendationEngine engine;
    private FinancialMetrics metrics;
    
    @BeforeEach
    public void setUp() {
        engine = new RecommendationEngine();
        metrics = new FinancialMetrics();
    }
    
    @Test
    public void testLowSavingsRateRecommendations() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.05));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(0.50));
        metrics.setNetCashFlow(BigDecimal.valueOf(100));
        
        List<String> recommendations = engine.generateRecommendations(metrics);
        
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(r -> 
            r.toLowerCase().contains("save") || r.toLowerCase().contains("savings")));
    }
    
    @Test
    public void testHighSavingsRateRecommendations() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.25));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(0.50));
        metrics.setNetCashFlow(BigDecimal.valueOf(500));
        
        List<String> recommendations = engine.generateRecommendations(metrics);
        
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(r -> 
            r.toLowerCase().contains("excellent") || r.toLowerCase().contains("invest")));
    }
    
    @Test
    public void testHighExpenseRatioRecommendations() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.05));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(0.95));
        metrics.setNetCashFlow(BigDecimal.valueOf(50));
        
        List<String> recommendations = engine.generateRecommendations(metrics);
        
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(r -> 
            r.toLowerCase().contains("expense") || r.toLowerCase().contains("spending")));
    }
    
    @Test
    public void testModerateExpenseRatioRecommendations() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.20));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(0.75));
        metrics.setNetCashFlow(BigDecimal.valueOf(150));
        
        List<String> recommendations = engine.generateRecommendations(metrics);
        
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(r -> 
            r.toLowerCase().contains("reasonable") || r.toLowerCase().contains("optimization")));
    }
    
    @Test
    public void testNegativeCashFlowRecommendations() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.0));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(1.2));
        metrics.setNetCashFlow(BigDecimal.valueOf(-500));
        
        List<String> recommendations = engine.generateRecommendations(metrics);
        
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(r -> 
            r.toLowerCase().contains("spending more") || r.toLowerCase().contains("negative")));
    }
    
    @Test
    public void testGetRandomRecommendationReturnsValidText() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.05));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(0.95));
        metrics.setNetCashFlow(BigDecimal.valueOf(-100));
        
        String recommendation = engine.getRandomRecommendation(metrics);
        
        assertNotNull(recommendation);
        assertFalse(recommendation.isEmpty());
        assertTrue(recommendation.length() > 10);
    }
    
    @Test
    public void testGetRandomRecommendationIsFromGeneratedList() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.05));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(0.80));
        metrics.setNetCashFlow(BigDecimal.valueOf(100));
        
        List<String> allRecommendations = engine.generateRecommendations(metrics);
        String randomRecommendation = engine.getRandomRecommendation(metrics);
        
        assertTrue(allRecommendations.contains(randomRecommendation));
    }
    
    @Test
    public void testMultipleRandomCallsReturnValidRecommendations() {
        metrics.setSavingsRate(BigDecimal.valueOf(0.08));
        metrics.setExpenseToIncomeRatio(BigDecimal.valueOf(0.85));
        metrics.setNetCashFlow(BigDecimal.valueOf(50));
        
        List<String> allRecommendations = engine.generateRecommendations(metrics);
        
        for (int i = 0; i < 5; i++) {
            String recommendation = engine.getRandomRecommendation(metrics);
            assertNotNull(recommendation);
            assertTrue(allRecommendations.contains(recommendation));
        }
    }
}
