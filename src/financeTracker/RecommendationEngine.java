package financeTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;


public class RecommendationEngine {
    private final Random random;
    
    public RecommendationEngine() {
        this.random = new Random();
    }
    
    public List<String> generateRecommendations(FinancialMetrics metrics) {
        List<String> recommendations = new ArrayList<>();
        
        // Savings Rate Recommendations
        if (metrics.getSavingsRate().compareTo(BigDecimal.valueOf(0.10)) < 0) {
            recommendations.addAll(getLowSavingsRateRecommendations());
        } else if (metrics.getSavingsRate().compareTo(BigDecimal.valueOf(0.20)) < 0) {
            recommendations.addAll(getModerateSavingsRateRecommendations());
        } else {
            recommendations.addAll(getHighSavingsRateRecommendations());
        }
        
        // Expense Ratio Recommendations
        if (metrics.getExpenseToIncomeRatio().compareTo(BigDecimal.valueOf(0.90)) > 0) {
            recommendations.addAll(getHighExpenseRatioRecommendations());
        } else if (metrics.getExpenseToIncomeRatio().compareTo(BigDecimal.valueOf(0.70)) > 0) {
            recommendations.addAll(getModerateExpenseRatioRecommendations());
        }
        
        // Net Cash Flow Recommendations
        if (metrics.getNetCashFlow().compareTo(BigDecimal.ZERO) < 0) {
            recommendations.addAll(getNegativeCashFlowRecommendations());
        }
        
        return recommendations;
    }
    
    public String getRandomRecommendation(FinancialMetrics metrics) {
        List<String> recommendations = generateRecommendations(metrics);
        if (recommendations.isEmpty()) {
            return "Your financial health looks good! Keep maintaining your current habits.";
        }
        return recommendations.get(random.nextInt(recommendations.size()));
    }
    
    private List<String> getLowSavingsRateRecommendations() {
        return List.of(
            "Savings ratio too low. Aim to save at least 20% of your income for financial security.",
            "Consider reducing discretionary spending to improve your savings rate.",
            "Your savings may not cover 3-6 months of expenses. Focus on building an emergency fund.",
            "Try implementing the 50/30/20 rule: 50% needs, 30% wants, 20% savings.",
            "Review recurring subscriptions and memberships that you may not be using frequently."
        );
    }
    
    private List<String> getModerateSavingsRateRecommendations() {
        return List.of(
            "Good start on savings! Consider increasing your savings rate to 20% or more.",
            "You're building a solid foundation. Look for opportunities to optimize fixed expenses.",
            "Consider setting up automatic transfers to savings on payday.",
            "Your savings rate is decent. Think about specific financial goals to work towards.",
            "Review your budget for categories where you can potentially save more."
        );
    }
    
    private List<String> getHighSavingsRateRecommendations() {
        return List.of(
            "Excellent savings rate! Consider investing surplus funds for long-term growth.",
            "Great job saving! You might want to explore retirement accounts or investment options.",
            "With your high savings rate, you're well positioned for major financial goals.",
            "Consider speaking with a financial advisor about investment strategies.",
            "Your strong savings habit will serve you well. Keep up the good work!"
        );
    }
    
    private List<String> getHighExpenseRatioRecommendations() {
        return List.of(
            "Your expenses are very high relative to income. Focus on essential spending.",
            "Consider tracking every expense for 30 days to identify spending patterns.",
            "Review your largest expense categories for potential reductions.",
            "High expense ratio may limit financial flexibility. Look for cost-cutting opportunities.",
            "Consider whether lifestyle inflation is affecting your financial goals."
        );
    }
    
    private List<String> getModerateExpenseRatioRecommendations() {
        return List.of(
            "Your expense ratio is reasonable, but there's room for optimization.",
            "Consider meal planning to reduce food expenses.",
            "Review utility bills for potential savings through conservation.",
            "Look for opportunities to refinance high-interest debt.",
            "Consider bulk purchasing for frequently used items to save money."
        );
    }
    
    private List<String> getNegativeCashFlowRecommendations() {
        return List.of(
            "You're spending more than you earn. Immediate action is needed.",
            "Create a strict budget focusing only on essential expenses.",
            "Consider temporary additional income sources to cover the deficit.",
            "Review and pause non-essential subscriptions and memberships.",
            "Negative cash flow is unsustainable. Prioritize debt reduction and expense cutting."
        );
    }
}