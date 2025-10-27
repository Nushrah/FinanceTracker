package financeTracker;

import java.math.BigDecimal;

public class FinancialMetrics {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netCashFlow;
    private BigDecimal savingsRate;
    private BigDecimal expenseToIncomeRatio;
    
    public FinancialMetrics() {
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.netCashFlow = BigDecimal.ZERO;
        this.savingsRate = BigDecimal.ZERO;
        this.expenseToIncomeRatio = BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    
    public BigDecimal getNetCashFlow() { return netCashFlow; }
    public void setNetCashFlow(BigDecimal netCashFlow) { this.netCashFlow = netCashFlow; }
    
    public BigDecimal getSavingsRate() { return savingsRate; }
    public void setSavingsRate(BigDecimal savingsRate) { this.savingsRate = savingsRate; }
    
    public BigDecimal getExpenseToIncomeRatio() { return expenseToIncomeRatio; }
    public void setExpenseToIncomeRatio(BigDecimal expenseToIncomeRatio) { this.expenseToIncomeRatio = expenseToIncomeRatio; }
    
    @Override
    public String toString() {
        return String.format("FinancialMetrics{income=%.2f, expenses=%.2f, netFlow=%.2f, savingsRate=%.2f%%, expenseRatio=%.2f%%}",
            totalIncome, totalExpenses, netCashFlow, savingsRate, expenseToIncomeRatio);
    }
}