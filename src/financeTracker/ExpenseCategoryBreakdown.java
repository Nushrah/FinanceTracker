package financeTracker;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseCategoryBreakdown {
    private final BigDecimal totalExpenses;
    private final Map<String, BigDecimal> categoryPercentages;

    public ExpenseCategoryBreakdown(BigDecimal totalExpenses, Map<String, BigDecimal> categoryPercentages) {
        this.totalExpenses = totalExpenses == null ? BigDecimal.ZERO : totalExpenses;
        this.categoryPercentages = new LinkedHashMap<>();

        if (categoryPercentages != null) {
            this.categoryPercentages.putAll(categoryPercentages);
        }
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public Map<String, BigDecimal> getCategoryPercentages() {
        return Collections.unmodifiableMap(categoryPercentages);
    }

    @Override
    public String toString() {
        if (categoryPercentages.isEmpty()) {
            return "No expenses for the selected period.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Expense breakdown (percent of total):\n");

        for (Map.Entry<String, BigDecimal> e : categoryPercentages.entrySet()) {
            sb.append(String.format("- %s: %.2f%%\n", e.getKey(), e.getValue()));
        }
        
        return sb.toString();
    }
}
